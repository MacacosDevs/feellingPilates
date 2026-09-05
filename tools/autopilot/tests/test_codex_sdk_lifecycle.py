from __future__ import annotations

from pathlib import Path
import threading
import time
import unittest
from unittest.mock import patch

from feelingpilates_autopilot.adapters.execution.codex_sdk import CodexSdkAdapter
from feelingpilates_autopilot.domain.models import RunId, WorkflowId
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, ExecutionStatus, SandboxMode
from tests.fixtures.fake_codex_sdk import FakeCodex


class TestCodexSdkLifecycle(unittest.TestCase):
    def make(self, behavior: str, timeout: float = 1):
        fake = FakeCodex(behavior)
        adapter = CodexSdkAdapter(codex_factory=lambda: fake, authorized_workspace_root=str(Path.cwd()), cleanup_grace_seconds=.05, verify_capabilities=False)
        request = ExecutionRequest(RunId("run-1"), "offline", WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(Path.cwd()), SandboxMode.READ_ONLY, timeout_seconds=timeout)
        return fake, adapter, adapter.start(request)

    @staticmethod
    def wait_for_session(adapter: CodexSdkAdapter, handle) -> None:
        turn = adapter._turns[str(handle.execution_id)]
        deadline = time.monotonic() + .5
        while turn.session is None and time.monotonic() < deadline:
            time.sleep(.005)
        if turn.session is None:
            raise AssertionError("parent did not receive incremental session evidence")

    def test_timeout_interrupts_without_waiting_for_collection(self) -> None:
        fake, adapter, handle = self.make("hang", .03)
        time.sleep(.08)
        result = adapter.get_result(handle)
        self.assertEqual(result.status, ExecutionStatus.FAILED)
        self.assertTrue(result.timed_out)
        self.assertFalse(result.cancelled)
        self.assertGreaterEqual(fake.interrupts, 1)

    def test_explicit_cancel_double_cancel_and_post_completion_are_truthful(self) -> None:
        fake, adapter, handle = self.make("hang")
        self.assertTrue(fake.stream_entered.wait(.5))
        adapter.interrupt(handle); adapter.interrupt(handle)
        result = adapter.get_result(handle)
        self.assertEqual(result.status, ExecutionStatus.INTERRUPTED)
        self.assertTrue(result.cancelled)
        self.assertFalse(result.timed_out)
        completed_fake, completed_adapter, completed_handle = self.make("valid")
        done = completed_adapter.get_result(completed_handle)
        completed_adapter.interrupt(completed_handle)
        self.assertEqual(completed_adapter.get_result(completed_handle).status, ExecutionStatus.SUCCEEDED)
        self.assertGreaterEqual(completed_fake.interrupts, 1)  # child cleanup interrupts completed SDK turns too
        self.assertGreaterEqual(fake.interrupts, 1)

    def test_cancel_timeout_race_and_workers_finish(self) -> None:
        fake, adapter, handle = self.make("cancel_race", .04)
        timer = threading.Timer(.01, lambda: adapter.interrupt(handle))
        timer.start()
        result = adapter.get_result(handle)
        timer.join()
        self.assertIn(result.status, {ExecutionStatus.INTERRUPTED, ExecutionStatus.FAILED})
        self.assertTrue(result.cancelled or result.timed_out)
        turn = adapter._turns[str(handle.execution_id)]
        self.assertTrue(turn.completed.is_set())
        self.assertFalse(turn.worker.is_alive())

    def test_large_event_stream_is_bounded_during_collection(self) -> None:
        fake, adapter, handle = self.make("unknown_events")
        result = adapter.get_result(handle)
        self.assertEqual(result.status, ExecutionStatus.FAILED)
        self.assertFalse(adapter._turns[str(handle.execution_id)].process.is_alive())

    def test_noncooperative_collection_is_unblocked_and_cleaned_before_terminal_cancel(self) -> None:
        fake, adapter, handle = self.make("noncooperative")
        self.assertTrue(fake.stream_entered.wait(.5))
        self.wait_for_session(adapter, handle)
        adapter.interrupt(handle)
        result = adapter.get_result(handle)
        turn = adapter._turns[str(handle.execution_id)]
        self.assertEqual(result.status, ExecutionStatus.INTERRUPTED)
        self.assertEqual(result.session.session_id, "thread-1")
        self.assertTrue(turn.completed.is_set())
        self.assertFalse(turn.worker.is_alive())
        self.assertGreaterEqual(fake.closes, 1)

    def test_noncooperative_timeout_is_bounded_and_leaves_no_execution_unit(self) -> None:
        fake, adapter, handle = self.make("noncooperative", .08)
        self.assertTrue(fake.stream_entered.wait(.5))
        self.wait_for_session(adapter, handle)
        started = time.monotonic()
        result = adapter.get_result(handle)
        self.assertLess(time.monotonic() - started, .5)
        turn = adapter._turns[str(handle.execution_id)]
        self.assertTrue(result.timed_out)
        self.assertNotEqual(result.status, ExecutionStatus.SUCCEEDED)
        self.assertEqual(result.session.session_id, "thread-1")
        self.assertFalse(turn.process.is_alive())
        self.assertFalse(turn.worker.is_alive())

    def test_failed_containment_reports_nonterminal_until_execution_is_dead(self) -> None:
        fake, adapter, handle = self.make("noncooperative")
        self.assertTrue(fake.stream_entered.wait(.5))
        turn = adapter._turns[str(handle.execution_id)]
        # The managed unit remains alive while bounded joins expire and both
        # supported hard-stop mechanisms fail.  Restore real methods before
        # the explicit cleanup below.
        with patch.object(turn.process, "terminate", side_effect=OSError), patch.object(turn.process, "kill", side_effect=OSError):
            started = time.monotonic()
            adapter.interrupt(handle)
            result = adapter.get_result(handle)
        self.assertLess(time.monotonic() - started, .5)
        self.assertEqual(result.status, ExecutionStatus.UNKNOWN)
        self.assertIsNone(result.agent_result)
        self.assertIsNone(result.failure)
        self.assertTrue(result.cancelled)
        self.assertTrue(turn.process.is_alive())
        self.assertFalse(turn.completed.is_set())
        self.assertFalse(turn.worker.is_alive())
        self.assertFalse(turn.watchdog.is_alive())
        self.assertIn("could not establish termination", result.raw_stderr)
        started = time.monotonic()
        repeated = adapter.get_result(handle)
        self.assertLess(time.monotonic() - started, .1)
        self.assertEqual(repeated.status, ExecutionStatus.UNKNOWN)
        self.assertTrue(turn.process.is_alive())
        self.assertFalse(turn.worker.is_alive())
        self.assertFalse(turn.watchdog.is_alive())
        turn.process.kill(); turn.process.join(.2)
        terminal = adapter.get_result(handle)
        self.assertTrue(turn.completed.is_set())
        self.assertEqual(terminal.status, ExecutionStatus.INTERRUPTED)
        self.assertFalse(turn.worker.is_alive())
        self.assertFalse(turn.watchdog.is_alive())

    def test_termination_before_session_observation_keeps_session_empty(self) -> None:
        fake, adapter, handle = self.make("before_session")
        self.assertTrue(fake.thread_start_entered.wait(.5))
        adapter.interrupt(handle)
        result = adapter.get_result(handle)
        self.assertEqual(result.status, ExecutionStatus.INTERRUPTED)
        self.assertIsNone(result.session)
