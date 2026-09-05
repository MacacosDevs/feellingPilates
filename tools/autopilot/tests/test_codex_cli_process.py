import io
import json
import os
import signal
import threading
import time
import tracemalloc
import unittest
from pathlib import Path
import tempfile
from types import SimpleNamespace

from feelingpilates_autopilot.adapters.execution.codex_cli import CapabilityError, CodexCliAdapter, _BoundedCapture
from feelingpilates_autopilot.adapters.execution.command import CommandValidationError
from feelingpilates_autopilot.adapters.execution.result_parser import SessionEvidence
from feelingpilates_autopilot.domain.models import ExecutionId, RunId, WorkflowId
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, ExecutionStatus, SandboxMode


FAKE = str(Path(__file__).parent / "fixtures" / "fake_codex_cli.py")


class RecordingCapabilityAdapter(CodexCliAdapter):
    def __init__(self, *args, **kwargs):
        self.capability_outputs = {}
        super().__init__(*args, **kwargs)

    def _run_capability_probe(self, arguments):
        output = super()._run_capability_probe(arguments)
        self.capability_outputs[arguments] = output
        return output


class ProcessTests(unittest.TestCase):
    def setUp(self):
        self._old_behavior = os.environ.get("FAKE_CODEX_BEHAVIOR")
        self._old_delay = os.environ.get("FAKE_CODEX_DELAY_SECONDS")
        self._old_capability_pid_file = os.environ.get("FAKE_CODEX_CAPABILITY_PID_FILE")

    def tearDown(self):
        if self._old_behavior is None:
            os.environ.pop("FAKE_CODEX_BEHAVIOR", None)
        else:
            os.environ["FAKE_CODEX_BEHAVIOR"] = self._old_behavior
        if self._old_delay is None:
            os.environ.pop("FAKE_CODEX_DELAY_SECONDS", None)
        else:
            os.environ["FAKE_CODEX_DELAY_SECONDS"] = self._old_delay
        if self._old_capability_pid_file is None:
            os.environ.pop("FAKE_CODEX_CAPABILITY_PID_FILE", None)
        else:
            os.environ["FAKE_CODEX_CAPABILITY_PID_FILE"] = self._old_capability_pid_file

    def request(self, timeout=.15, instructions="x", execution_id=None):
        return ExecutionRequest(RunId("run-1"), instructions, WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(Path.cwd()), SandboxMode.READ_ONLY, timeout_seconds=timeout, execution_id=execution_id)

    def adapter(self, **changes):
        return CodexCliAdapter(FAKE, authorized_workspace_root=str(Path.cwd()), termination_grace_seconds=.05, **changes)

    def wait_until(self, predicate, seconds=.8):
        deadline = time.monotonic() + seconds
        while time.monotonic() < deadline:
            if predicate():
                return True
            time.sleep(.01)
        return bool(predicate())

    @staticmethod
    def alive(pid):
        try:
            os.kill(pid, 0)
        except ProcessLookupError:
            return False
        return True

    def assert_dead(self, pid):
        self.assertTrue(self.wait_until(lambda: not self.alive(pid)), f"process {pid} survived cleanup")

    @staticmethod
    def cleanup_group(pgid):
        try:
            os.killpg(pgid, signal.SIGKILL)
        except ProcessLookupError:
            pass

    @staticmethod
    def group_alive(pgid):
        try:
            os.killpg(pgid, 0)
        except ProcessLookupError:
            return False
        return True

    def test_timeout_is_physical_before_collection_and_session_is_incremental(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "timeout_session"
        adapter = self.adapter()
        handle = adapter.start(self.request(.15))
        turn = adapter._turns[str(handle.execution_id)]
        self.assertTrue(self.wait_until(lambda: turn.session_evidence.session_id() == "thread-1"))
        self.assertIsNone(turn.termination_cause)
        self.assertIsNone(turn.process.poll())
        self.assertTrue(self.wait_until(lambda: turn.reaped, .5))
        timeout = adapter.get_result(handle)
        self.assertTrue(timeout.timed_out)
        self.assertEqual(timeout.session.session_id, "thread-1")

    def test_stdout_bound_stops_accumulation_and_stderr_overflow_stays_auxiliary(self):
        adapter = self.adapter(stdout_limit_bytes=64)
        adapter._request_stop = lambda turn, cause: True
        turn = SimpleNamespace(session_evidence=SessionEvidence(), stdout_overflow=False, state_lock=threading.RLock())
        source = io.BytesIO(b"x" * 500000)
        tracemalloc.start()
        adapter._read_stream(source, _BoundedCapture(64), turn, True)
        _, peak = tracemalloc.get_traced_memory()
        tracemalloc.stop()
        self.assertTrue(turn.stdout_overflow)
        self.assertLess(peak, 128000)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "unterminated_large"
        adapter = self.adapter(stdout_limit_bytes=64)
        overflow = adapter.get_result(adapter.start(self.request()))
        self.assertEqual(overflow.status, ExecutionStatus.FAILED)
        self.assertTrue(overflow.stdout_truncated)
        self.assertTrue(overflow.output_limit_exceeded)
        self.assertLessEqual(len(overflow.raw_stdout or ""), 64)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "stderr_large"
        adapter = self.adapter(stderr_limit_bytes=64)
        stderr = adapter.get_result(adapter.start(self.request(2)))
        self.assertEqual(stderr.status, ExecutionStatus.SUCCEEDED)
        self.assertTrue(stderr.stderr_truncated)
        self.assertFalse(stderr.output_limit_exceeded)
        self.assertLessEqual(len(stderr.raw_stderr or ""), 64)

    def test_capability_probe_bounds_large_help_and_hanging_probe(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "capability_large"
        tracemalloc.start()
        with self.assertRaises(CapabilityError):
            self.adapter(capability_capture_limit_bytes=64)
        _, peak = tracemalloc.get_traced_memory()
        tracemalloc.stop()
        self.assertLess(peak, 1_000_000)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "capability_hang"
        started = time.monotonic()
        with self.assertRaises(CapabilityError):
            self.adapter(capability_timeout_seconds=.1)
        self.assertLess(time.monotonic() - started, 1)

    def test_capability_leader_exit_stubborn_descendant_is_cleaned_before_return(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "capability_leader_exit_stubborn"
        with tempfile.TemporaryDirectory() as directory:
            pid_file = Path(directory) / "capability-pids"
            os.environ["FAKE_CODEX_CAPABILITY_PID_FILE"] = str(pid_file)
            leader_pgid = None
            try:
                adapter = RecordingCapabilityAdapter(FAKE, authorized_workspace_root=str(Path.cwd()), termination_grace_seconds=.05)
                leader_pgid, child_pid = map(int, pid_file.read_text().split())
                self.assertIn("capability_leader_exit=natural", adapter.capability_outputs[("exec", "--help")])
                self.assert_dead(child_pid)
                self.assertTrue(self.wait_until(lambda: not self.group_alive(leader_pgid)))
            finally:
                if leader_pgid is not None:
                    self.cleanup_group(leader_pgid)

    def test_dual_streams_and_structured_stdout_truncation_do_not_deadlock(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "dual_large"
        adapter = self.adapter(stdout_limit_bytes=64, stderr_limit_bytes=64)
        started = time.monotonic()
        result = adapter.get_result(adapter.start(self.request(2)))
        self.assertLess(time.monotonic() - started, 3)
        self.assertEqual(result.status, ExecutionStatus.FAILED)
        self.assertTrue(result.stdout_truncated)
        self.assertTrue(result.output_limit_exceeded)

    def test_blocked_stdin_timeout_cancellation_and_brokenpipe_are_truthful(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "blocked_stdin"
        adapter = self.adapter()
        timed = adapter.get_result(adapter.start(self.request(.15, "x" * 2_000_000)))
        self.assertEqual(timed.status, ExecutionStatus.FAILED)
        self.assertTrue(timed.timed_out)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "cancel"
        adapter = self.adapter()
        handle = adapter.start(self.request(2, "x" * 2_000_000))
        adapter.interrupt(handle)
        cancelled = adapter.get_result(handle)
        self.assertEqual(cancelled.status, ExecutionStatus.INTERRUPTED)
        self.assertTrue(cancelled.cancelled)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "closed_stdin"
        adapter = self.adapter()
        broken = adapter.get_result(adapter.start(self.request(2, "x" * 10_000_000)))
        self.assertEqual(broken.status, ExecutionStatus.FAILED)
        self.assertFalse(broken.cancelled)
        self.assertFalse(broken.timed_out)
        self.assertEqual(broken.failure.message, "managed stdin delivery failed")
        os.environ["FAKE_CODEX_BEHAVIOR"] = "closed_stdin_nonzero"
        adapter = self.adapter()
        nonzero = adapter.get_result(adapter.start(self.request(2, "x" * 10_000_000)))
        self.assertEqual(nonzero.exit_code, 7)
        self.assertFalse(nonzero.cancelled)
        self.assertFalse(nonzero.timed_out)
        self.assertEqual(nonzero.failure.message, "Codex CLI process did not exit successfully")
        os.environ["FAKE_CODEX_BEHAVIOR"] = "closed_stdin_timeout"
        adapter = self.adapter()
        stdin_timeout = adapter.get_result(adapter.start(self.request(.15, "x" * 10_000_000)))
        self.assertTrue(stdin_timeout.timed_out)

    def test_immediate_collection_waits_for_lifecycle_not_a_shorter_timeout(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "slow_valid"
        os.environ["FAKE_CODEX_DELAY_SECONDS"] = "2.2"
        adapter = self.adapter()
        started = time.monotonic()
        result = adapter.get_result(adapter.start(self.request(3)))
        self.assertGreater(time.monotonic() - started, 2)
        self.assertEqual(result.status, ExecutionStatus.SUCCEEDED)
        self.assertFalse(result.timed_out)

    def test_duplicate_id_and_cancel_after_exit_preserve_terminal_truth(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "blocked_stdin"
        adapter = self.adapter()
        first = adapter.start(self.request(2, "x" * 2_000_000, ExecutionId("same")))
        with self.assertRaises(CommandValidationError):
            adapter.start(self.request(2, "x", ExecutionId("same")))
        adapter.interrupt(first)
        self.assertEqual(adapter.get_result(first).status, ExecutionStatus.INTERRUPTED)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "valid"
        completed = adapter.start(self.request(2))
        turn = adapter._turns[str(completed.execution_id)]
        self.assertTrue(self.wait_until(lambda: turn.process.poll() is not None))
        adapter.interrupt(completed)
        self.assertEqual(adapter.get_result(completed).status, ExecutionStatus.SUCCEEDED)

    def test_leader_first_timeout_and_cancel_all_force_kill_stubborn_descendants(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "leader_exit_stubborn"
        leader = self.adapter()
        leader_handle = leader.start(self.request(2))
        leader_turn = leader._turns[str(leader_handle.execution_id)]
        try:
            self.assertTrue(self.wait_until(lambda: "\n" in leader_turn.stdout.text()))
            child_pid = json.loads(leader_turn.stdout.text().splitlines()[0])["child_pid"]
            self.assertTrue(self.wait_until(lambda: leader_turn.reaped))
            self.assert_dead(child_pid)
            self.assertEqual(leader.get_result(leader_handle).status, ExecutionStatus.FAILED)
        finally:
            self.cleanup_group(leader_turn.process.pid)
        for trigger in ("timeout", "cancel"):
            with self.subTest(trigger=trigger):
                os.environ["FAKE_CODEX_BEHAVIOR"] = "stubborn_child"
                adapter = self.adapter()
                handle = adapter.start(self.request(.15 if trigger == "timeout" else 2))
                turn = adapter._turns[str(handle.execution_id)]
                try:
                    self.assertTrue(self.wait_until(lambda: "\n" in turn.stdout.text()))
                    child_pid = json.loads(turn.stdout.text().splitlines()[0])["child_pid"]
                    if trigger == "cancel":
                        adapter.interrupt(handle)
                    result = adapter.get_result(handle)
                    self.assertEqual(result.status, ExecutionStatus.FAILED if trigger == "timeout" else ExecutionStatus.INTERRUPTED)
                    self.assert_dead(child_pid)
                    self.assertTrue(turn.reaped)
                finally:
                    self.cleanup_group(turn.process.pid)

    def test_signal_and_normal_descendant_cleanup_are_non_successes(self):
        os.environ["FAKE_CODEX_BEHAVIOR"] = "signal"
        adapter = self.adapter()
        signalled = adapter.get_result(adapter.start(self.request()))
        self.assertEqual(signalled.status, ExecutionStatus.FAILED)
        self.assertEqual(signalled.termination_signal, 15)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "child"
        child_adapter = self.adapter()
        child_result = child_adapter.get_result(child_adapter.start(self.request()))
        child_pid = int((child_result.raw_stdout or "").splitlines()[0])
        self.assert_dead(child_pid)
