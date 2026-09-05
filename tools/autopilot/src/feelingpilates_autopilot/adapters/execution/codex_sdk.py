"""Bounded Python SDK primary ``AgentExecutor`` adapter for openai-codex 0.147.0.

Provider collection runs in a child process. A Python thread cannot safely be
killed, but the process is a real terminal boundary for an uncooperative SDK.
Only a small primitive payload crosses back to the parent.
"""
from __future__ import annotations

import importlib.metadata
import inspect
import json
import multiprocessing
import sysconfig
import threading
import uuid
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from ...domain.failures import FailureCategory, FailureRecord
from ...domain.models import Checkpoint, ExecutionId, FailureId, SessionId, SessionReference, TokenClass
from ...ports.agent_executor import AgentExecutor, ExecutionHandle, ExecutionObservation, ExecutionRequest, ExecutionStatus, ExecutorCapabilities, SandboxMode
from .result_parser import ResultProtocolError, parse_structured_result, validate_protocol_session_id
from .sdk_events import SdkEventEvidence, usage_from_values


class SdkCapabilityError(RuntimeError):
    """The installed provider surface differs from the accepted R5 profile."""


@dataclass(slots=True)
class _RunningTurn:
    request: ExecutionRequest
    session: SessionReference | None
    process: object | None = None
    result_connection: object | None = None
    control_connection: object | None = None
    worker: threading.Thread | None = None
    watchdog: threading.Thread | None = None
    # An observation can be available before containment has been proven.  Keep
    # that reportability signal distinct from the terminal-completion signal.
    observation_available: threading.Event = field(default_factory=threading.Event)
    completed: threading.Event = field(default_factory=threading.Event)
    # Stops adapter-owned parent monitoring without claiming that provider
    # execution itself is terminal.
    worker_shutdown: threading.Event = field(default_factory=threading.Event)
    terminal_lock: threading.Lock = field(default_factory=threading.Lock)
    state_lock: threading.RLock = field(default_factory=threading.RLock)
    termination_cause: str | None = None
    finalized: ExecutionObservation | None = None
    observation: ExecutionObservation | None = None
    observed_session_ids: set[str] = field(default_factory=set)
    session_failure: bool = False


def _safe_close(value: object | None) -> None:
    close = getattr(value, "close", None)
    if callable(close):
        try:
            close()
        except Exception:
            pass


def _sandbox_value(sdk: object, mode: SandboxMode) -> object:
    if sdk is None:
        return "read-only" if mode is SandboxMode.READ_ONLY else "workspace-write"
    sandbox = getattr(sdk, "Sandbox")
    return sandbox.read_only if mode is SandboxMode.READ_ONLY else sandbox.workspace_write


def _child_send(connection: object, payload: dict[str, object]) -> None:
    """IPC is deliberately limited to JSON-like primitives and bounded strings."""
    try:
        connection.send(payload)  # type: ignore[attr-defined]
    except Exception:
        pass


def _child_execute(factory: Callable[[], object] | None, sdk: object, request: ExecutionRequest, expected_session_id: str | None, schema: dict[str, object], result_limit: int, event_limit: int, result_connection: object, control_connection: object) -> None:
    """Run one SDK turn in the hard-containment child process.

    Provider objects, exception reprs, credentials, and environment values never
    cross IPC. The control thread requests supported cleanup; the parent process
    is the hard backstop when the SDK ignores it.
    """
    client: object | None = None
    active: dict[str, object | None] = {"turn": None, "stream": None}
    listener_done = threading.Event()

    def request_cooperative_stop() -> None:
        interrupt = getattr(active["turn"], "interrupt", None)
        if callable(interrupt):
            try:
                interrupt()
            except Exception:
                pass
        _safe_close(active["stream"])
        _safe_close(client)

    def listen_for_stop() -> None:
        while not listener_done.is_set():
            try:
                if control_connection.poll(.02):  # type: ignore[attr-defined]
                    control_connection.recv()  # type: ignore[attr-defined]
                    request_cooperative_stop()
                    return
            except (EOFError, OSError):
                return
            except Exception:
                return

    listener: threading.Thread | None = None
    evidence = SdkEventEvidence(event_limit)
    session_id: str | None = None
    session_known = False
    try:
        client = factory() if factory is not None else getattr(sdk, "Codex")()
        listener = threading.Thread(target=listen_for_stop, name="codex-sdk-child-control")
        listener.start()
        thread_options = {"cwd": request.working_directory, "model": request.model, "sandbox": _sandbox_value(sdk, request.sandbox)}
        if expected_session_id is None:
            thread = client.thread_start(**thread_options)  # type: ignore[attr-defined]
        else:
            thread = client.thread_resume(expected_session_id, **thread_options)  # type: ignore[attr-defined]
        session_id = validate_protocol_session_id(getattr(thread, "id", None))
        session_known = True
        if expected_session_id is not None and session_id != expected_session_id:
            raise ResultProtocolError("resumed SDK thread identity conflicts with caller session")
        _child_send(result_connection, {"kind": "session_observed", "session_id": session_id})
        turn_options = dict(thread_options)
        if request.reasoning_effort is not None:
            turn_options["effort"] = request.reasoning_effort
        turn_options["output_schema"] = schema
        sdk_turn = thread.turn(request.instructions, **turn_options)  # type: ignore[attr-defined]
        active["turn"] = sdk_turn
        stream_factory = getattr(sdk_turn, "stream", None)
        if not callable(stream_factory):
            raise ResultProtocolError("SDK turn does not expose the required stream surface")
        stream = stream_factory()
        if not hasattr(stream, "__iter__"):
            raise ResultProtocolError("SDK turn stream is malformed")
        active["stream"] = stream
        candidates, final_response, overflow, usage, completed = 0, None, False, None, None
        try:
            for event in stream:
                previous_session_ids = set(evidence.session_ids)
                evidence.observe(event)
                if evidence.error is not None:
                    request_cooperative_stop()
                    raise evidence.error
                for observed_session_id in evidence.session_ids - previous_session_ids:
                    _child_send(result_connection, {"kind": "session_observed", "session_id": observed_session_id})
                method, payload = getattr(event, "method", None), getattr(event, "payload", None)
                if method == "item/completed":
                    item = getattr(payload, "item", None); item = getattr(item, "root", item)
                    text = getattr(item, "text", None)
                    phase = getattr(getattr(item, "phase", None), "value", getattr(item, "phase", None))
                    if phase == "final_answer":
                        candidates += 1
                        if not isinstance(text, str):
                            raise ResultProtocolError("SDK terminal structured result is malformed")
                        if len(text.encode("utf-8")) > result_limit:
                            overflow = True
                        elif candidates == 1:
                            final_response = text
                elif method == "thread/tokenUsage/updated":
                    usage = getattr(payload, "token_usage", None)
                elif method == "turn/completed":
                    completed = getattr(payload, "turn", None)
        finally:
            _safe_close(stream); active["stream"] = None
        if overflow:
            raise ResultProtocolError("SDK structured result exceeds its bound during collection")
        if candidates != 1:
            raise ResultProtocolError("exactly one SDK terminal structured result is required")
        if completed is None:
            raise ResultProtocolError("SDK turn completed event is missing")
        evidence.reconciled_session(session_id)
        values: dict[str, int] = {}
        last = getattr(usage, "last", None) if usage is not None else None
        if usage is not None and last is None:
            raise ResultProtocolError("SDK usage is malformed")
        for token_class in TokenClass:
            value = getattr(last, token_class.value, None) if last is not None else None
            if value is not None:
                if type(value) is not int or value < 0:
                    raise ResultProtocolError("SDK usage token value is invalid")
                values[token_class.value] = value
        status = getattr(getattr(completed, "status", None), "value", getattr(completed, "status", None))
        _child_send(result_connection, {"kind": "collected", "session_id": session_id, "status": status, "has_error": getattr(completed, "error", None) is not None, "final_response": final_response, "usage": values})
    except ResultProtocolError as exc:
        _child_send(result_connection, {"kind": "failure", "category": "session" if evidence.session_failure or not session_known else "result", "session_id": session_id, "message": str(exc)[:512]})
    except Exception:
        _child_send(result_connection, {"kind": "failure", "category": "session" if not session_known else "transport", "session_id": session_id, "message": "SDK operation failed"})
    finally:
        request_cooperative_stop(); listener_done.set()
        if listener is not None:
            listener.join(.1)
        _safe_close(result_connection); _safe_close(control_connection)


class CodexSdkAdapter(AgentExecutor):
    adapter_name = "openai-codex"
    package_version = "0.147.0"
    _efforts = frozenset(("low", "medium", "high", "xhigh"))

    def __init__(self, *, codex_factory: Callable[[], object] | None = None, sdk_module: object | None = None, allowed_models: tuple[str, ...] = (), authorized_workspace_root: str | None = None, schema_path: str | None = None, instruction_limit_bytes: int = 65_536, result_limit_bytes: int = 262_144, event_limit: int = 256, cleanup_grace_seconds: float = .2, verify_capabilities: bool = True) -> None:
        if min(instruction_limit_bytes, result_limit_bytes, event_limit, cleanup_grace_seconds) <= 0:
            raise ValueError("SDK bounds and cleanup grace must be positive")
        self._sdk, self._factory = sdk_module, codex_factory
        self._allowed_models = tuple(allowed_models)
        self._authorized_root = Path(authorized_workspace_root or Path.cwd()).resolve()
        self._schema_path = Path(schema_path).resolve() if schema_path else self._resolve_schema_path()
        self._instruction_limit, self._result_limit, self._event_limit, self._grace = instruction_limit_bytes, result_limit_bytes, event_limit, cleanup_grace_seconds
        self._turns: dict[str, _RunningTurn] = {}
        self._lock = threading.RLock()
        if verify_capabilities and codex_factory is None:
            self.probe_capabilities()

    @staticmethod
    def _resolve_schema_path() -> Path:
        source = Path(__file__).parents[4] / "schemas" / "agent-result.schema.json"
        installed = Path(sysconfig.get_path("data")) / "feelingpilates_autopilot" / "schemas" / "agent-result.schema.json"
        for candidate in (installed, source):
            if candidate.is_file():
                return candidate.resolve()
        return installed

    def capabilities(self) -> ExecutorCapabilities:
        return ExecutorCapabilities(supports_resume=True, supports_interrupt=True, emits_usage=True)

    def probe_capabilities(self) -> None:
        """Verify exactly the public surface consumed by the R5 implementation."""
        try:
            if importlib.metadata.version("openai-codex") != self.package_version:
                raise SdkCapabilityError("openai-codex version does not match the R5 capability profile")
            if self._sdk is None:
                import openai_codex  # type: ignore[import-not-found]
                self._sdk = openai_codex
            sdk = self._sdk
            codex, sandbox = getattr(sdk, "Codex"), getattr(sdk, "Sandbox")
            thread, turn, result = getattr(sdk, "Thread"), getattr(sdk, "TurnHandle"), getattr(sdk, "TurnResult")
            required = ((codex, "thread_start"), (codex, "thread_resume"), (codex, "close"), (thread, "turn"), (turn, "stream"), (turn, "interrupt"))
            if any(not callable(getattr(owner, name, None)) for owner, name in required):
                raise SdkCapabilityError("openai-codex lacks an R5-required callable surface")
            for owner, name, parameters in ((codex, "thread_start", {"cwd", "model", "sandbox"}), (codex, "thread_resume", {"cwd", "model", "sandbox"}), (thread, "turn", {"input", "effort", "output_schema"})):
                if not parameters <= set(inspect.signature(getattr(owner, name)).parameters):
                    raise SdkCapabilityError(f"openai-codex {name} signature lacks the R5 surface")
            if not self._valid_sandbox_values(getattr(sandbox, "read_only"), getattr(sandbox, "workspace_write")):
                raise SdkCapabilityError("openai-codex sandbox values do not match the R5 authority mapping")
            if not self._identity_surface(thread):
                raise SdkCapabilityError("openai-codex lacks the required Thread.id identity surface")
            if not {"status", "error", "final_response", "usage"} <= set(getattr(result, "__dataclass_fields__", {})):
                raise SdkCapabilityError("openai-codex lacks the R5 turn result surface")
            models, generated = self._event_modules(sdk)
            if not self._event_surface(models, generated):
                raise SdkCapabilityError("openai-codex event surface is incomplete")
            if not self._usage_surface(generated):
                raise SdkCapabilityError("openai-codex usage surface is incomplete")
        except (ImportError, importlib.metadata.PackageNotFoundError, AttributeError, TypeError, ValueError) as exc:
            if isinstance(exc, SdkCapabilityError):
                raise
            raise SdkCapabilityError("openai-codex R5 capability profile is unavailable") from exc

    @staticmethod
    def _valid_sandbox_values(read_only: object, workspace_write: object) -> bool:
        def normalized(value: object) -> str:
            return str(getattr(value, "value", value)).strip().lower().replace("_", "-")
        return normalized(read_only) == "read-only" and normalized(workspace_write) == "workspace-write" and read_only != workspace_write

    @staticmethod
    def _identity_surface(thread: object) -> bool:
        return "id" in getattr(thread, "__dataclass_fields__", {}) or "id" in getattr(thread, "__annotations__", {}) or hasattr(thread, "id")

    @staticmethod
    def _event_modules(sdk: object) -> tuple[object | None, object | None]:
        """Resolve the pinned SDK's public event modules, never fictitious root exports."""
        models = getattr(sdk, "models", None)
        generated = getattr(getattr(sdk, "generated", None), "v2_all", None)
        if getattr(sdk, "__name__", None) == "openai_codex":
            if models is None:
                models = importlib.import_module("openai_codex.models")
            if generated is None:
                generated = importlib.import_module("openai_codex.generated.v2_all")
        return models, generated

    @classmethod
    def _event_surface(cls, models: object | None, generated: object | None) -> bool:
        if not cls._has_fields(getattr(models, "Notification", None), {"method", "payload"}):
            return False
        required = (
            ("ThreadStartedNotification", {"thread"}),
            ("ItemCompletedNotification", {"item"}),
            ("ThreadItem", {"root"}),
            ("AgentMessageThreadItem", {"text", "phase"}),
            ("TurnCompletedNotification", {"turn"}),
            ("Turn", {"status", "error"}),
        )
        return all(cls._has_fields(getattr(generated, name, None), fields) for name, fields in required)

    @classmethod
    def _usage_surface(cls, generated: object | None) -> bool:
        if not cls._has_fields(getattr(generated, "ThreadTokenUsageUpdatedNotification", None), {"token_usage"}):
            return False
        if not cls._has_fields(getattr(generated, "ThreadTokenUsage", None), {"last"}):
            return False
        return cls._has_fields(getattr(generated, "TokenUsageBreakdown", None), {token_class.value for token_class in TokenClass})

    @staticmethod
    def _has_fields(value: object | None, expected: set[str]) -> bool:
        if value is None:
            return False
        fields = set(getattr(value, "__dataclass_fields__", {}))
        fields.update(getattr(value, "__annotations__", {}))
        fields.update(getattr(value, "model_fields", {}))
        return expected <= fields

    def start(self, request: ExecutionRequest) -> ExecutionHandle:
        execution_id = request.execution_id or ExecutionId(str(uuid.uuid4()))
        handle = ExecutionHandle(execution_id, request.run_id, None, request)
        try:
            self._validate_request(request)
            return self._launch(handle, request, None)
        except Exception as exc:
            return self._rejected(handle, FailureCategory.SDK_SESSION_FAILURE, self._safe_message(exc, "SDK session creation failed"))

    def resume(self, handle: ExecutionHandle, checkpoint: Checkpoint) -> ExecutionHandle:
        request = handle.resume_context
        try:
            self._validate_request(request)
            error = self._validate_resume(handle, request, checkpoint)
            if error is not None:
                raise ValueError(error)
            assert request is not None and handle.session is not None
            resumed = ExecutionHandle(ExecutionId(str(uuid.uuid4())), handle.run_id, handle.session, request)
            return self._launch(resumed, request, str(handle.session.session_id))
        except Exception as exc:
            return self._rejected(handle, FailureCategory.SDK_SESSION_FAILURE, self._safe_message(exc, "SDK session resume failed"))

    def interrupt(self, handle: ExecutionHandle) -> None:
        with self._lock:
            turn = self._turns.get(str(handle.execution_id))
        if turn is not None:
            self._stop(handle, turn, "cancel")

    def get_result(self, handle: ExecutionHandle) -> ExecutionObservation:
        with self._lock:
            turn = self._turns.get(str(handle.execution_id))
        if turn is None:
            return self._failure_observation(handle, None, FailureCategory.RESULT_CONTRACT_FAILURE, "unknown execution handle")
        # The watchdog/interrupt path always makes a bounded observation
        # available.  It deliberately need not be terminal when process
        # containment has not been established.
        turn.observation_available.wait()
        # Unresolved containment retains the process reference, not a permanent
        # monitor.  Each later caller interaction performs its own fresh,
        # bounded liveness check and terminalizes only after verified death.
        if turn.finalized is None and self._termination_cause(turn) is not None:
            process = turn.process
            if process is not None:
                getattr(process, "join")(0)
                if not getattr(process, "is_alive")():
                    self._finalize_requested_termination(handle, turn)
        self._join_monitors(turn, self._grace * 4)
        with turn.state_lock:
            if turn.finalized is not None:
                result = turn.finalized
            else:
                result = turn.observation
        if result is not None:
            if turn.finalized is not None and turn.worker is not None and turn.worker is not threading.current_thread():
                turn.worker.join(self._grace * 4)
            return result
        # A child exit is terminal evidence, so this fallback remains a terminal
        # transport failure.  It cannot run for a live child.
        if not getattr(turn.process, "is_alive")():
            self._finalize_failure(handle, turn, FailureCategory.SDK_TRANSPORT_FAILURE, "SDK turn ended without a terminal result")
            with turn.state_lock:
                assert turn.finalized is not None
                return turn.finalized
        return self._containment_unresolved_observation(handle, turn)

    def _launch(self, handle: ExecutionHandle, request: ExecutionRequest, expected_session_id: str | None) -> ExecutionHandle:
        if "fork" not in multiprocessing.get_all_start_methods():
            raise SdkCapabilityError("R5 requires a fork-capable process containment boundary")
        schema = json.loads(self._schema_path.read_text(encoding="utf-8"))
        if not isinstance(schema, dict):
            raise ResultProtocolError("canonical SDK output schema is malformed")
        context = multiprocessing.get_context("fork")
        result_parent, result_child = context.Pipe(duplex=False)
        control_child, control_parent = context.Pipe(duplex=False)
        turn = _RunningTurn(request, handle.session, result_connection=result_parent, control_connection=control_parent)
        process = context.Process(target=_child_execute, args=(self._factory, self._sdk, request, expected_session_id, schema, self._result_limit, self._event_limit, result_child, control_child), name=f"codex-sdk-{handle.execution_id}")
        turn.process = process
        with self._lock:
            if str(handle.execution_id) in self._turns:
                raise ValueError("execution identity is already active")
            self._turns[str(handle.execution_id)] = turn
        try:
            process.start()
        except Exception:
            with self._lock:
                self._turns.pop(str(handle.execution_id), None)
            raise
        _safe_close(result_child); _safe_close(control_child)
        turn.worker = threading.Thread(target=self._await_child, args=(handle, turn), name=f"codex-sdk-parent-{handle.execution_id}")
        turn.watchdog = threading.Thread(target=self._watch_timeout, args=(handle, turn), name=f"codex-sdk-timeout-{handle.execution_id}")
        turn.worker.start(); turn.watchdog.start()
        return handle

    def _await_child(self, handle: ExecutionHandle, turn: _RunningTurn) -> None:
        try:
            while True:
                if turn.worker_shutdown.is_set():
                    return
                if getattr(turn.result_connection, "poll")(.02):
                    try:
                        payload = getattr(turn.result_connection, "recv")()
                    except EOFError:
                        # A closed payload channel is not proof that the
                        # managed process died (for example, its writer can be
                        # closed during an interrupted child unwind).  Keep the
                        # bounded watcher alive until liveness is known.
                        if getattr(turn.process, "is_alive")():
                            getattr(turn.process, "join")(.02)
                            continue
                        self._finalize_child_exit(handle, turn)
                    else:
                        if self._observe_incremental_payload(handle, turn, payload):
                            continue
                        self._finalize_child_payload(handle, turn, payload)
                    return
                if not getattr(turn.process, "is_alive")():
                    self._finalize_child_exit(handle, turn)
                    return
        finally:
            _safe_close(turn.result_connection)

    def _finalize_child_payload(self, handle: ExecutionHandle, turn: _RunningTurn, payload: object) -> None:
        if not self._ensure_child_stopped(turn):
            self._report_containment_unresolved(handle, turn)
            return
        if self._finalize_requested_termination(handle, turn):
            return
        if turn.session_failure:
            self._finalize_failure(handle, turn, FailureCategory.SDK_SESSION_FAILURE, "SDK session evidence is contradictory or malformed")
            return
        if not isinstance(payload, dict) or not isinstance(payload.get("kind"), str):
            self._finalize_failure(handle, turn, FailureCategory.SDK_TRANSPORT_FAILURE, "SDK child payload is malformed"); return
        if payload["kind"] == "failure":
            category = FailureCategory.SDK_SESSION_FAILURE if payload.get("category") == "session" else FailureCategory.RESULT_CONTRACT_FAILURE if payload.get("category") == "result" else FailureCategory.SDK_TRANSPORT_FAILURE
            session = None if category is FailureCategory.SDK_SESSION_FAILURE else self._session_from_payload(handle, turn, payload)
            self._finalize_failure(handle, turn, category, payload.get("message") if isinstance(payload.get("message"), str) else "SDK operation failed", session=session); return
        if payload["kind"] != "collected" or not isinstance(payload.get("session_id"), str) or not isinstance(payload.get("final_response"), str) or not isinstance(payload.get("usage"), dict):
            self._finalize_failure(handle, turn, FailureCategory.SDK_TRANSPORT_FAILURE, "SDK child payload is malformed"); return
        try:
            session = self._session_from_payload(handle, turn, payload)
            if session is None:
                raise ResultProtocolError("SDK thread identity is missing")
            if payload.get("status") not in {"completed", "COMPLETED", "succeeded", "SUCCEEDED"} or payload.get("has_error") is not False:
                raise ResultProtocolError("SDK turn did not complete successfully")
            usage = usage_from_values(payload["usage"])
            parsed = parse_structured_result(payload["final_response"], turn.request, adapter=self.adapter_name, runtime_usage=usage)
            if parsed.session_reference is not None and parsed.session_reference != session.session_id:
                raise ResultProtocolError("agent result session reference conflicts with SDK thread identity")
            self._commit(handle, turn, ExecutionObservation(handle.execution_id, handle.run_id, session, ExecutionStatus.SUCCEEDED, agent_result=parsed, usage_record=usage))
        except ResultProtocolError as exc:
            self._finalize_failure(handle, turn, FailureCategory.RESULT_CONTRACT_FAILURE, str(exc))
        except Exception:
            self._finalize_failure(handle, turn, FailureCategory.SDK_TRANSPORT_FAILURE, "SDK child payload is malformed")

    def _observe_incremental_payload(self, handle: ExecutionHandle, turn: _RunningTurn, payload: object) -> bool:
        """Accept only the small, typed operational event emitted by the child.

        It is intentionally separate from terminal collection payloads: the
        parent owns provenance construction and validates the primitive before
        retaining it as operational session evidence.
        """
        if not isinstance(payload, dict) or payload.get("kind") != "session_observed":
            return False
        if set(payload) != {"kind", "session_id"} or not isinstance(payload.get("session_id"), str):
            self._invalidate_session(turn)
            return True
        try:
            self._observe_session_id(handle, turn, payload["session_id"])
        except ResultProtocolError:
            # Retain no selected identity after malformed or contradictory
            # evidence; terminal collection will emit SDK_SESSION_FAILURE.
            pass
        return True

    def _observe_session_id(self, handle: ExecutionHandle, turn: _RunningTurn, value: str) -> SessionReference | None:
        try:
            session_id = validate_protocol_session_id(value)
        except ResultProtocolError:
            self._invalidate_session(turn)
            raise
        with turn.state_lock:
            if turn.session_failure:
                raise ResultProtocolError("SDK session evidence is contradictory or malformed")
            if turn.observed_session_ids and session_id not in turn.observed_session_ids:
                self._invalidate_session(turn)
                raise ResultProtocolError("SDK session identity changed during the turn")
            turn.observed_session_ids.add(session_id)
            if turn.session is None:
                turn.session = SessionReference(SessionId(session_id), handle.run_id, session_id, self.adapter_name, turn.request.role, turn.request.attempt_id)
            return turn.session

    @staticmethod
    def _invalidate_session(turn: _RunningTurn) -> None:
        with turn.state_lock:
            turn.session_failure = True
            turn.observed_session_ids.clear()
            turn.session = None

    def _finalize_child_exit(self, handle: ExecutionHandle, turn: _RunningTurn) -> None:
        if self._finalize_requested_termination(handle, turn):
            return
        category = FailureCategory.SDK_SESSION_FAILURE if turn.session_failure else FailureCategory.SDK_TRANSPORT_FAILURE
        message = "SDK session evidence is contradictory or malformed" if turn.session_failure else "SDK child exited without result payload"
        self._finalize_failure(handle, turn, category, message)

    def _finalize_requested_termination(self, handle: ExecutionHandle, turn: _RunningTurn) -> bool:
        cause = self._termination_cause(turn)
        if cause is None:
            return False
        if getattr(turn.process, "is_alive")():
            return False
        self._commit(handle, turn, self._stop_observation(handle, turn, cause))
        return True

    def _containment_unresolved_observation(self, handle: ExecutionHandle, turn: _RunningTurn) -> ExecutionObservation:
        cause = self._termination_cause(turn)
        message = "SDK child containment cleanup could not establish termination"
        return ExecutionObservation(handle.execution_id, handle.run_id, turn.session, ExecutionStatus.UNKNOWN, raw_stderr=message, timed_out=cause == "timeout", cancelled=cause == "cancel")

    def _report_containment_unresolved(self, handle: ExecutionHandle, turn: _RunningTurn) -> None:
        # UNKNOWN is the R2 legal non-terminal state: it intentionally carries
        # neither AgentResult nor FailureRecord, so callers cannot mistake this
        # bounded report for a semantic completion.
        observation = self._containment_unresolved_observation(handle, turn)
        with turn.terminal_lock:
            if turn.finalized is not None:
                return
            with turn.state_lock:
                turn.observation = observation
            turn.observation_available.set()
            # Reportability is not terminality, and neither requires permanent
            # monitoring.  Preserve the process reference for caller-driven
            # rechecks while stopping both parent monitoring units.
            turn.worker_shutdown.set()

    def _watch_timeout(self, handle: ExecutionHandle, turn: _RunningTurn) -> None:
        if not turn.worker_shutdown.wait(turn.request.timeout_seconds):
            self._stop(handle, turn, "timeout")

    def _stop(self, handle: ExecutionHandle, turn: _RunningTurn, cause: str) -> bool:
        with turn.terminal_lock:
            if turn.finalized is not None or turn.termination_cause is not None:
                return False
            turn.termination_cause = cause
        try:
            getattr(turn.control_connection, "send")("stop")
        except Exception:
            pass
        stopped = self._ensure_child_stopped(turn)
        if stopped:
            self._commit(handle, turn, self._stop_observation(handle, turn, cause))
        else:
            self._report_containment_unresolved(handle, turn)
        # Once the process is dead, allow the bounded observer a few poll
        # intervals to reap its IPC state before exposing normal hard-stop
        # completion. Unresolved containment stops all monitors promptly.
        self._join_monitors(turn, self._grace * (4 if stopped else 1))
        return True

    @staticmethod
    def _join_monitors(turn: _RunningTurn, timeout: float) -> None:
        current = threading.current_thread()
        for monitor in (turn.worker, turn.watchdog):
            if monitor is not None and monitor is not current:
                monitor.join(timeout)

    def _ensure_child_stopped(self, turn: _RunningTurn) -> bool:
        process = turn.process
        if process is None:
            return True
        getattr(process, "join")(self._grace)
        if getattr(process, "is_alive")():
            try:
                getattr(process, "terminate")()
            except Exception:
                pass
            getattr(process, "join")(self._grace)
        if getattr(process, "is_alive")() and callable(getattr(process, "kill", None)):
            try:
                getattr(process, "kill")()
            except Exception:
                pass
            getattr(process, "join")(self._grace)
        return not getattr(process, "is_alive")()

    @staticmethod
    def _termination_cause(turn: _RunningTurn) -> str | None:
        with turn.terminal_lock:
            return turn.termination_cause

    def _stop_observation(self, handle: ExecutionHandle, turn: _RunningTurn, cause: str) -> ExecutionObservation:
        return self._failure_observation(handle, turn.session, FailureCategory.SDK_TRANSPORT_FAILURE, "SDK execution cancelled" if cause == "cancel" else "SDK execution timed out", status=ExecutionStatus.INTERRUPTED if cause == "cancel" else ExecutionStatus.FAILED, cancelled=cause == "cancel", timed_out=cause == "timeout")

    def _session_from_payload(self, handle: ExecutionHandle, turn: _RunningTurn, payload: dict[object, object]) -> SessionReference | None:
        value = payload.get("session_id")
        if not isinstance(value, str):
            return turn.session
        return self._observe_session_id(handle, turn, value)

    def _finalize_failure(self, handle: ExecutionHandle, turn: _RunningTurn, category: FailureCategory, message: str, *, session: SessionReference | None = None) -> None:
        if self._termination_cause(turn) is None:
            selected = None if category is FailureCategory.SDK_SESSION_FAILURE else turn.session if session is None else session
            self._commit(handle, turn, self._failure_observation(handle, selected, category, message))

    def _commit(self, handle: ExecutionHandle, turn: _RunningTurn, observation: ExecutionObservation) -> None:
        with turn.terminal_lock:
            if turn.finalized is not None:
                return
            if turn.termination_cause is not None and observation.status is ExecutionStatus.SUCCEEDED:
                return
            turn.finalized = observation
            with turn.state_lock:
                turn.observation = observation
            turn.completed.set(); turn.observation_available.set(); turn.worker_shutdown.set()
        _safe_close(turn.control_connection)

    def _rejected(self, handle: ExecutionHandle, category: FailureCategory, message: str) -> ExecutionHandle:
        turn = _RunningTurn(handle.resume_context or ExecutionRequest(handle.run_id, ""), handle.session)
        turn.finalized = self._failure_observation(handle, handle.session, category, message)
        turn.observation = turn.finalized; turn.completed.set(); turn.observation_available.set()
        with self._lock:
            self._turns[str(handle.execution_id)] = turn
        return handle

    def _validate_request(self, request: ExecutionRequest | None) -> None:
        if not isinstance(request, ExecutionRequest): raise ValueError("a complete execution request is required")
        if not request.workflow_id or not request.role or not request.gate: raise ValueError("workflow, role, and gate identity are required")
        if not isinstance(request.instructions, str) or not request.instructions.strip() or len(request.instructions.encode("utf-8")) > self._instruction_limit: raise ValueError("instructions are missing or exceed the SDK bound")
        if request.timeout_seconds is None or type(request.timeout_seconds) not in (int, float) or request.timeout_seconds <= 0: raise ValueError("a positive per-turn timeout is required")
        if type(request.sandbox) is not SandboxMode: raise ValueError("sandbox mode is unsupported")
        if request.reasoning_effort is not None and request.reasoning_effort not in self._efforts: raise ValueError("reasoning effort is unsupported by the R5 profile")
        if request.model is not None and (not request.model or request.model not in self._allowed_models): raise ValueError("model is not allowed by adapter configuration")
        if not request.working_directory: raise ValueError("an explicit working directory is required")
        cwd = Path(request.working_directory)
        if not cwd.is_absolute() or not cwd.resolve().is_dir(): raise ValueError("working directory must be an existing absolute directory")
        try:
            cwd.resolve().relative_to(self._authorized_root)
        except ValueError as exc:
            raise ValueError("working directory is outside the authorized workspace root") from exc

    def _validate_resume(self, handle: ExecutionHandle, request: ExecutionRequest | None, checkpoint: Checkpoint) -> str | None:
        if request is None or handle.session is None or checkpoint.run_id != handle.run_id: return "resume requires persisted context and a session bound to this run"
        session = handle.session
        if request.run_id != handle.run_id or session.run_id != handle.run_id: return "resume context and session must match the represented run"
        if request.workflow_id != checkpoint.workflow_id: return "resume context workflow does not match checkpoint"
        if session.adapter != self.adapter_name or session.role != request.role or session.attempt_id != request.attempt_id: return "resume session provenance does not match request"
        try:
            identity = validate_protocol_session_id(session.session_id)
        except ResultProtocolError:
            return "resume session identity is malformed"
        if session.opaque_reference != identity: return "resume session opaque reference is inconsistent"
        if checkpoint.session_id is not None and checkpoint.session_id != session.session_id: return "resume checkpoint session does not match session reference"
        return None

    @staticmethod
    def _safe_message(exc: Exception, fallback: str) -> str:
        return str(exc) if isinstance(exc, (SdkCapabilityError, ResultProtocolError, ValueError)) else fallback

    @staticmethod
    def _failure_observation(handle: ExecutionHandle, session: SessionReference | None, category: FailureCategory, message: str, *, status: ExecutionStatus = ExecutionStatus.FAILED, timed_out: bool = False, cancelled: bool = False) -> ExecutionObservation:
        return ExecutionObservation(handle.execution_id, handle.run_id, session, status, failure=FailureRecord(FailureId(str(uuid.uuid4())), category, message), timed_out=timed_out, cancelled=cancelled)
