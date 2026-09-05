"""Bounded, no-shell Codex CLI fallback/diagnostic AgentExecutor adapter."""

from __future__ import annotations

import os
import signal
import subprocess
import threading
import time
import uuid
from dataclasses import dataclass, field
from pathlib import Path

from ...domain.failures import FailureCategory, FailureRecord
from ...domain.models import Checkpoint, ExecutionId, FailureId, SessionId, SessionReference
from ...ports.agent_executor import (
    AgentExecutor, ExecutionHandle, ExecutionObservation, ExecutionRequest, ExecutionStatus, ExecutorCapabilities,
)
from .command import CommandValidationError, build_command, resolve_executable, validate_session_id
from .result_parser import ResultProtocolError, SessionEvidence, parse_terminal


class CapabilityError(RuntimeError):
    pass


class _BoundedCapture:
    def __init__(self, limit: int) -> None:
        self.limit, self._data, self.seen, self.truncated = limit, bytearray(), 0, False
        self._lock = threading.Lock()

    def append(self, chunk: bytes) -> bool:
        with self._lock:
            self.seen += len(chunk)
            available = max(0, self.limit - len(self._data))
            if available:
                self._data.extend(chunk[:available])
            if len(chunk) > available:
                self.truncated = True
            return self.truncated

    def text(self) -> str:
        with self._lock:
            return bytes(self._data).decode("utf-8", errors="replace")


@dataclass(slots=True)
class _RunningTurn:
    request: ExecutionRequest
    process: subprocess.Popen[bytes]
    stdout: _BoundedCapture
    stderr: _BoundedCapture
    started_at: float
    stdin: object | None = None
    session_evidence: SessionEvidence = field(default_factory=SessionEvidence)
    termination_cause: str | None = None
    stdout_overflow: bool = False
    stdin_failure: bool = False
    finalized: ExecutionObservation | None = None
    readers: list[threading.Thread] = field(default_factory=list)
    writer: threading.Thread | None = None
    watchdog: threading.Thread | None = None
    state_lock: threading.RLock = field(default_factory=threading.RLock)
    termination_lock: threading.Lock = field(default_factory=threading.Lock)
    reap_lock: threading.Lock = field(default_factory=threading.Lock)
    reaped: bool = False
    completed: threading.Event = field(default_factory=threading.Event)


class CodexCliAdapter(AgentExecutor):
    """CLI 0.150.1 implementation candidate; it owns no workflow or durable state."""

    adapter_name = "codex-cli"

    def __init__(
        self, executable: str | None = None, *, schema_path: str | None = None,
        allowed_models: tuple[str, ...] = (), authorized_workspace_root: str | None = None,
        stdout_limit_bytes: int = 1_000_000, stderr_limit_bytes: int = 256_000,
        termination_grace_seconds: float = 0.2, capability_capture_limit_bytes: int = 128_000,
        capability_timeout_seconds: float = 5, verify_capabilities: bool = True,
    ) -> None:
        if min(stdout_limit_bytes, stderr_limit_bytes, termination_grace_seconds, capability_capture_limit_bytes, capability_timeout_seconds) <= 0:
            raise ValueError("capture bounds and termination grace must be positive")
        self._executable = resolve_executable(executable)
        self._schema_path = str(Path(schema_path or Path(__file__).parents[4] / "schemas" / "agent-result.schema.json").resolve())
        self._authorized_root = str(Path(authorized_workspace_root or Path.cwd()).resolve())
        self._allowed_models, self._stdout_limit, self._stderr_limit, self._grace = tuple(allowed_models), stdout_limit_bytes, stderr_limit_bytes, termination_grace_seconds
        self._capability_limit, self._capability_timeout = capability_capture_limit_bytes, capability_timeout_seconds
        self._turns: dict[str, _RunningTurn] = {}
        self._reserved: set[str] = set()
        self._lock = threading.RLock()
        if verify_capabilities:
            self.probe_capabilities()

    def capabilities(self) -> ExecutorCapabilities:
        return ExecutorCapabilities(supports_resume=True, supports_interrupt=True, emits_usage=True)

    def probe_capabilities(self) -> None:
        def run(*arguments: str) -> str:
            return self._run_capability_probe(arguments)
        version, execution_help, resume_help = run("--version"), run("exec", "--help"), run("exec", "resume", "--help")
        if "codex-cli 0.150.1" not in version:
            raise CapabilityError("Codex CLI version does not match the supported capability profile")
        # `-c/--config` is a CLI capability.  The bounded
        # `model_reasoning_effort` key is repository capability authority, not
        # a string which CLI 0.150.1 advertises in `exec --help`.
        if any(value not in execution_help for value in ("--sandbox", "read-only", "workspace-write", "--output-schema", "--json", "--model", "-c")):
            raise CapabilityError("Codex CLI lacks required new-turn capabilities")
        if "--json" not in resume_help or "--output-schema" not in resume_help:
            raise CapabilityError("Codex CLI lacks required resume capabilities")

    def _run_capability_probe(self, arguments: tuple[str, ...]) -> str:
        try:
            process = subprocess.Popen(
                (self._executable, *arguments), stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                shell=False, start_new_session=True,
            )
        except OSError as exc:
            raise CapabilityError("Codex CLI capability probe could not run") from exc
        assert process.stdout is not None and process.stderr is not None
        stdout, stderr, stdout_overflow = _BoundedCapture(self._capability_limit), _BoundedCapture(self._capability_limit), threading.Event()

        def drain(source: object, capture: _BoundedCapture, fatal_overflow: bool) -> None:
            try:
                while chunk := source.read(4096):  # type: ignore[attr-defined]
                    if capture.append(chunk) and fatal_overflow and not stdout_overflow.is_set():
                        stdout_overflow.set()
                        self._terminate_group(process.pid)
            finally:
                try: source.close()  # type: ignore[attr-defined]
                except OSError: pass

        readers = (
            threading.Thread(target=drain, args=(process.stdout, stdout, True), daemon=True),
            threading.Thread(target=drain, args=(process.stderr, stderr, False), daemon=True),
        )
        for reader in readers:
            reader.start()
        timed_out = False
        cleanup_failed = False
        try:
            process.wait(timeout=self._capability_timeout)
        except subprocess.TimeoutExpired:
            timed_out = True
        finally:
            # The isolated process group, rather than its leader PID, is the
            # probe lifecycle unit.  A normally exited leader can leave a
            # descendant holding the pipe descriptors open.
            if self._group_alive(process.pid):
                cleanup_failed = not self._terminate_group(process.pid)
            if process.poll() is None:
                process.wait()
            for reader in readers:
                reader.join()
        if cleanup_failed:
            raise CapabilityError("Codex CLI capability probe process group cleanup failed")
        if timed_out:
            raise CapabilityError("Codex CLI capability probe timed out")
        if stdout_overflow.is_set() or stdout.truncated:
            raise CapabilityError("Codex CLI capability probe output exceeded validation bound")
        if process.returncode:
            raise CapabilityError("Codex CLI capability probe failed")
        return stdout.text()

    def start(self, request: ExecutionRequest) -> ExecutionHandle:
        execution_id = request.execution_id or ExecutionId(str(uuid.uuid4()))
        key = str(execution_id)
        handle = ExecutionHandle(execution_id, request.run_id, None, request)
        with self._lock:
            if key in self._turns or key in self._reserved:
                raise CommandValidationError("execution identity is already active")
            self._reserved.add(key)
        return self._start_turn(handle, request, None)

    def resume(self, handle: ExecutionHandle, checkpoint: Checkpoint) -> ExecutionHandle:
        request = handle.resume_context
        if request is None or handle.session is None or checkpoint.run_id != handle.run_id:
            return self._rejected(handle, "resume requires explicit persisted context and a session bound to this run")
        if request.run_id != handle.run_id or handle.session.run_id != handle.run_id:
            return self._rejected(handle, "resume session and context must match the represented run")
        if request.workflow_id is not None and checkpoint.workflow_id is not None and request.workflow_id != checkpoint.workflow_id:
            return self._rejected(handle, "resume context workflow does not match checkpoint")
        try:
            session_id = validate_session_id(handle.session.session_id)
        except CommandValidationError as exc:
            return self._rejected(handle, str(exc))
        if checkpoint.session_id is not None:
            try:
                checkpoint_session_id = validate_session_id(checkpoint.session_id)
            except CommandValidationError as exc:
                return self._rejected(handle, str(exc))
            if checkpoint_session_id != session_id:
                return self._rejected(handle, "resume checkpoint session does not match session reference")
        if handle.session.adapter != self.adapter_name:
            return self._rejected(handle, "resume session adapter does not match Codex CLI")
        if handle.session.role != request.role:
            return self._rejected(handle, "resume session role does not match request")
        if handle.session.opaque_reference != session_id:
            return self._rejected(handle, "resume session opaque reference is inconsistent")
        if handle.session.attempt_id != request.attempt_id:
            return self._rejected(handle, "resume session attempt does not match request")
        resumed = ExecutionHandle(ExecutionId(str(uuid.uuid4())), handle.run_id, handle.session, request)
        with self._lock:
            self._reserved.add(str(resumed.execution_id))
        return self._start_turn(resumed, request, session_id)

    def _rejected(self, handle: ExecutionHandle, message: str) -> ExecutionHandle:
        observation = ExecutionObservation(execution_id=handle.execution_id, run_id=handle.run_id, session=handle.session, status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.RESULT_CONTRACT_FAILURE, message))
        turn = _RunningTurn.__new__(_RunningTurn)
        turn.finalized = observation
        with self._lock:
            self._reserved.discard(str(handle.execution_id))
            self._turns[str(handle.execution_id)] = turn
        return handle

    def _start_turn(self, handle: ExecutionHandle, request: ExecutionRequest, resume_session: str | None) -> ExecutionHandle:
        key = str(handle.execution_id)
        try:
            command = build_command(request, self._executable, self._schema_path, resume_session_id=resume_session, allowed_models=self._allowed_models, authorized_workspace_root=self._authorized_root)
            process = subprocess.Popen(command.argv, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=request.working_directory, shell=False, start_new_session=True)
        except (CommandValidationError, OSError) as exc:
            return self._rejected(handle, str(exc))
        assert process.stdin is not None and process.stdout is not None and process.stderr is not None
        turn = _RunningTurn(request, process, _BoundedCapture(self._stdout_limit), _BoundedCapture(self._stderr_limit), time.monotonic(), process.stdin)
        # Registration happens before any potentially blocked stdin delivery.
        with self._lock:
            self._reserved.discard(key)
            self._turns[key] = turn
        for source, capture, protocol in ((process.stdout, turn.stdout, True), (process.stderr, turn.stderr, False)):
            reader = threading.Thread(target=self._read_stream, args=(source, capture, turn, protocol), daemon=True)
            reader.start()
            turn.readers.append(reader)
        turn.writer = threading.Thread(target=self._write_stdin, args=(process.stdin, command.stdin, turn), daemon=True)
        turn.writer.start()
        turn.watchdog = threading.Thread(target=self._watch_timeout, args=(turn,), daemon=True)
        turn.watchdog.start()
        return handle

    def _read_stream(self, source: object, capture: _BoundedCapture, turn: _RunningTurn, protocol: bool) -> None:
        reader, line = source, bytearray()
        try:
            while True:
                # One-byte protocol reads make a flushed session line observable before EOF,
                # while preserving a finite line/capture limit.
                chunk = reader.read(1) if protocol and not turn.stdout_overflow else reader.read(4096)  # type: ignore[attr-defined]
                if not chunk:
                    if protocol and line and not turn.stdout_overflow:
                        turn.session_evidence.observe_line(bytes(line))
                    return
                if protocol:
                    overflowed = capture.append(chunk)
                    if overflowed:
                        self._mark_stdout_overflow(turn)
                        continue
                    line.extend(chunk)
                    if chunk == b"\n":
                        turn.session_evidence.observe_line(bytes(line[:-1]))
                        line.clear()
                else:
                    # Stderr is bounded auxiliary evidence.  Its truncation is
                    # not a structured-result failure and must not stop a valid
                    # stdout result from completing.
                    capture.append(chunk)
        finally:
            try: reader.close()  # type: ignore[attr-defined]
            except OSError: pass

    def _mark_stdout_overflow(self, turn: _RunningTurn) -> None:
        with turn.state_lock:
            if turn.stdout_overflow:
                return
            turn.stdout_overflow = True
            turn.session_evidence.error = ResultProtocolError("JSONL line exceeds bound")
        self._request_stop(turn, "overflow")

    def _write_stdin(self, stream: object, stdin: str, turn: _RunningTurn) -> None:
        try:
            stream.write(stdin.encode("utf-8"))  # type: ignore[attr-defined]
        except (OSError, ValueError):
            # A child-side input close is not caller cancellation or an
            # immutable terminal cause.  Finalization reconciles physical exit
            # status before treating incomplete input as primary evidence.
            with turn.state_lock:
                turn.stdin_failure = True
        finally:
            try: stream.close()  # type: ignore[attr-defined]
            except (OSError, ValueError): pass

    def _watch_timeout(self, turn: _RunningTurn) -> None:
        try:
            deadline = turn.started_at + (turn.request.timeout_seconds or 0)
            while time.monotonic() < deadline:
                if turn.process.poll() is not None:
                    self._ensure_group_gone(turn)
                    self._reap(turn)
                    return
                time.sleep(min(.01, max(0.001, deadline - time.monotonic())))
            self._request_stop(turn, "timeout")
            self._reap(turn)
        finally:
            turn.completed.set()

    def interrupt(self, handle: ExecutionHandle) -> None:
        with self._lock:
            turn = self._turns.get(str(handle.execution_id))
        if turn is not None and getattr(turn, "finalized", None) is None:
            self._request_stop(turn, "cancel")

    def _request_stop(self, turn: _RunningTurn, cause: str) -> bool:
        with turn.termination_lock:
            if not self._group_alive(turn.process.pid):
                return False
            current = turn.termination_cause
            priorities = {"overflow": 10, "stdin_failure": 20, "timeout": 30, "cancel": 40}
            if current is not None and priorities[cause] <= priorities[current]:
                return False
            turn.termination_cause = cause
            self._terminate_group(turn.process.pid)
            return True

    def _ensure_group_gone(self, turn: _RunningTurn) -> None:
        with turn.termination_lock:
            if self._group_alive(turn.process.pid):
                self._terminate_group(turn.process.pid)

    def _terminate_group(self, pgid: int) -> bool:
        try: os.killpg(pgid, signal.SIGTERM)
        except (ProcessLookupError, PermissionError): pass
        deadline = time.monotonic() + self._grace
        while self._group_alive(pgid) and time.monotonic() < deadline:
            time.sleep(.01)
        if self._group_alive(pgid):
            try: os.killpg(pgid, signal.SIGKILL)
            except (ProcessLookupError, PermissionError): pass
            deadline = time.monotonic() + self._grace
            while self._group_alive(pgid) and time.monotonic() < deadline:
                time.sleep(.01)
        return not self._group_alive(pgid)

    @staticmethod
    def _group_alive(pgid: int) -> bool:
        try: os.killpg(pgid, 0)
        except ProcessLookupError: return False
        except PermissionError: return True
        return True

    def get_result(self, handle: ExecutionHandle) -> ExecutionObservation:
        with self._lock:
            turn = self._turns.get(str(handle.execution_id))
        if turn is None:
            return self._failed(handle, FailureCategory.RESULT_CONTRACT_FAILURE, "unknown execution handle")
        if turn.finalized is not None:
            return turn.finalized
        turn.completed.wait()
        with turn.state_lock:
            if turn.finalized is None:
                turn.finalized = self._finalize(handle, turn)
            return turn.finalized

    def _reap(self, turn: _RunningTurn) -> None:
        with turn.reap_lock:
            if not turn.reaped:
                turn.process.wait()
                turn.reaped = True
        for reader in turn.readers: reader.join(timeout=1)
        if turn.writer is not None: turn.writer.join(timeout=1)

    def _finalize(self, handle: ExecutionHandle, turn: _RunningTurn) -> ExecutionObservation:
        stdout, stderr, code = turn.stdout.text(), turn.stderr.text(), turn.process.returncode
        signal_number = -code if code is not None and code < 0 else None
        session = self._session_reference(turn.session_evidence.session_id(), handle, turn.request)
        common = dict(execution_id=handle.execution_id, run_id=handle.run_id, session=session, raw_output=stdout, raw_stdout=stdout, raw_stderr=stderr, stdout_truncated=turn.stdout.truncated, stderr_truncated=turn.stderr.truncated, output_limit_exceeded=turn.stdout_overflow, exit_code=code, termination_signal=signal_number, timed_out=turn.termination_cause == "timeout", cancelled=turn.termination_cause == "cancel")
        if turn.termination_cause == "cancel":
            return ExecutionObservation(status=ExecutionStatus.INTERRUPTED, failure=self._failure(FailureCategory.CLI_PROCESS_FAILURE, "execution cancelled"), **common)
        if turn.termination_cause == "timeout":
            return ExecutionObservation(status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.CLI_PROCESS_FAILURE, "execution timed out"), **common)
        if turn.stdout_overflow:
            return ExecutionObservation(status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.RESULT_CONTRACT_FAILURE, "structured stdout exceeded validation bound"), **common)
        if code != 0 or signal_number is not None:
            return ExecutionObservation(status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.CLI_PROCESS_FAILURE, "Codex CLI process did not exit successfully"), **common)
        if turn.stdin_failure:
            return ExecutionObservation(status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.CLI_PROCESS_FAILURE, "managed stdin delivery failed"), **common)
        try:
            parsed = parse_terminal(stdout, turn.request)
            common["session"] = self._session_reference(parsed.session_id, handle, turn.request)
            return ExecutionObservation(status=ExecutionStatus.SUCCEEDED, agent_result=parsed.result, usage_record=parsed.usage, **common)
        except ResultProtocolError as exc:
            return ExecutionObservation(status=ExecutionStatus.FAILED, failure=self._failure(FailureCategory.RESULT_CONTRACT_FAILURE, str(exc)), **common)

    def _session_reference(self, session_id: str | None, handle: ExecutionHandle, request: ExecutionRequest) -> SessionReference | None:
        if session_id is None:
            return None
        try: validated = validate_session_id(session_id)
        except CommandValidationError: return None
        return SessionReference(session_id=SessionId(validated), run_id=handle.run_id, opaque_reference=validated, adapter=self.adapter_name, role=request.role, attempt_id=request.attempt_id)

    def _failed(self, handle: ExecutionHandle, category: FailureCategory, message: str) -> ExecutionObservation:
        return ExecutionObservation(execution_id=handle.execution_id, run_id=handle.run_id, session=handle.session, status=ExecutionStatus.FAILED, failure=self._failure(category, message))

    @staticmethod
    def _failure(category: FailureCategory, message: str) -> FailureRecord:
        return FailureRecord(failure_id=FailureId(str(uuid.uuid4())), category=category, message=message)
