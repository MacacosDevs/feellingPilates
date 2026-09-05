import os
from datetime import datetime, timezone
from pathlib import Path
import unittest

from feelingpilates_autopilot.adapters.execution.codex_cli import CodexCliAdapter
from feelingpilates_autopilot.domain.models import AttemptId, Checkpoint, CheckpointId, ExecutionId, RunId, SessionId, SessionReference, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, ExecutionStatus, SandboxMode


FAKE = str(Path(__file__).parent / "fixtures" / "fake_codex_cli.py")


class AdapterTests(unittest.TestCase):
    def request(self, **changes):
        values = dict(run_id=RunId("run-1"), instructions="offline", workflow_id=WorkflowId("workflow-1"), role="EXECUTOR", gate="implementation", working_directory=str(Path.cwd()), sandbox=SandboxMode.READ_ONLY, timeout_seconds=1)
        values.update(changes)
        return ExecutionRequest(**values)

    def execute_behavior(self, behavior):
        old = os.environ.get("FAKE_CODEX_BEHAVIOR")
        os.environ["FAKE_CODEX_BEHAVIOR"] = behavior
        try:
            adapter = CodexCliAdapter(FAKE)
            handle = adapter.start(self.request())
            return adapter, handle, adapter.get_result(handle)
        finally:
            if old is None: os.environ.pop("FAKE_CODEX_BEHAVIOR", None)
            else: os.environ["FAKE_CODEX_BEHAVIOR"] = old

    def test_valid_result_zero_and_one_session(self):
        _, _, zero = self.execute_behavior("valid")
        self.assertEqual(zero.status, ExecutionStatus.SUCCEEDED)
        self.assertIsNone(zero.session)
        _, _, one = self.execute_behavior("session")
        self.assertEqual(one.status, ExecutionStatus.SUCCEEDED)
        self.assertEqual(one.session.session_id, "thread-1")

    def test_realistic_cli_help_fixture_supports_config_without_advertising_reasoning_key(self):
        adapter = CodexCliAdapter(FAKE)
        adapter.probe_capabilities()

    def test_failure_preserves_valid_session_but_conflict_fails_closed(self):
        _, _, failed = self.execute_behavior("nonzero_session")
        self.assertEqual(failed.status, ExecutionStatus.FAILED)
        self.assertEqual(failed.session.session_id, "thread-1")
        _, _, conflict = self.execute_behavior("conflict_session")
        self.assertEqual(conflict.status, ExecutionStatus.FAILED)
        self.assertIsNone(conflict.session)
        _, _, stderr_only = self.execute_behavior("stderr_result")
        self.assertEqual(stderr_only.status, ExecutionStatus.FAILED)
        _, _, invalid_outcome = self.execute_behavior("bad_outcome")
        self.assertEqual(invalid_outcome.status, ExecutionStatus.FAILED)

    def test_resume_and_bad_result_are_bounded_at_adapter_boundary(self):
        adapter, handle, completed = self.execute_behavior("session")
        checkpoint = Checkpoint(CheckpointId("checkpoint-1"), RunId("run-1"), OperationalState.PAUSED, datetime.now(timezone.utc), workflow_id=WorkflowId("workflow-1"), session_id=SessionId("thread-1"))
        os.environ["FAKE_CODEX_BEHAVIOR"] = "resume"
        persisted = handle.__class__(handle.execution_id, handle.run_id, completed.session, handle.resume_context)
        resumed = adapter.resume(persisted, checkpoint)
        self.assertEqual(adapter.get_result(resumed).status, ExecutionStatus.SUCCEEDED)
        _, _, bad = self.execute_behavior("wrong_identity")
        self.assertEqual(bad.status, ExecutionStatus.FAILED)

    def test_cross_adapter_resume_uses_explicit_context_and_rejects_cross_run_session(self):
        adapter_a, handle, completed = self.execute_behavior("session")
        checkpoint = Checkpoint(CheckpointId("checkpoint-1"), RunId("run-1"), OperationalState.PAUSED, datetime.now(timezone.utc), workflow_id=WorkflowId("workflow-1"))
        persisted = handle.__class__(handle.execution_id, handle.run_id, completed.session, handle.resume_context)
        os.environ["FAKE_CODEX_BEHAVIOR"] = "resume"
        adapter_b = CodexCliAdapter(FAKE)
        resumed = adapter_b.resume(persisted, checkpoint)
        self.assertEqual(adapter_b.get_result(resumed).status, ExecutionStatus.SUCCEEDED)
        wrong = SessionReference(SessionId("thread-1"), RunId("other-run"), "thread-1", "codex-cli", "EXECUTOR")
        rejected = adapter_b.resume(handle.__class__(ExecutionId("other"), RunId("run-1"), wrong, handle.resume_context), checkpoint)
        self.assertEqual(adapter_b.get_result(rejected).status, ExecutionStatus.FAILED)

    def test_resume_rejects_full_provenance_contradictions_before_launch(self):
        _, handle, completed = self.execute_behavior("session")
        checkpoint = Checkpoint(CheckpointId("checkpoint-1"), RunId("run-1"), OperationalState.PAUSED, datetime.now(timezone.utc), workflow_id=WorkflowId("workflow-1"))
        adapter = CodexCliAdapter(FAKE)
        cases = (
            ("workflow", completed.session, Checkpoint(CheckpointId("wrong-workflow"), RunId("run-1"), OperationalState.PAUSED, datetime.now(timezone.utc), workflow_id=WorkflowId("other"))),
            ("adapter", SessionReference(SessionId("thread-1"), RunId("run-1"), "thread-1", "other-adapter", "EXECUTOR"), checkpoint),
            ("role", SessionReference(SessionId("thread-1"), RunId("run-1"), "thread-1", "codex-cli", "AUDITOR"), checkpoint),
            ("attempt", SessionReference(SessionId("thread-1"), RunId("run-1"), "thread-1", "codex-cli", "EXECUTOR", AttemptId("other-attempt")), checkpoint),
            ("opaque", SessionReference(SessionId("thread-1"), RunId("run-1"), "other-thread", "codex-cli", "EXECUTOR"), checkpoint),
            ("malformed", SessionReference(SessionId(" thread-1 "), RunId("run-1"), " thread-1 ", "codex-cli", "EXECUTOR"), checkpoint),
            ("checkpoint-session", completed.session, Checkpoint(CheckpointId("wrong-session"), RunId("run-1"), OperationalState.PAUSED, datetime.now(timezone.utc), workflow_id=WorkflowId("workflow-1"), session_id=SessionId("thread-2"))),
        )
        for label, session, candidate_checkpoint in cases:
            with self.subTest(label=label):
                candidate = handle.__class__(ExecutionId(f"bad-{label}"), RunId("run-1"), session, handle.resume_context)
                rejected = adapter.resume(candidate, candidate_checkpoint)
                self.assertEqual(adapter.get_result(rejected).status, ExecutionStatus.FAILED)
                self.assertFalse(hasattr(adapter._turns[str(rejected.execution_id)], "process"))
