from __future__ import annotations

from pathlib import Path
import unittest

from feelingpilates_autopilot.adapters.execution.codex_sdk import CodexSdkAdapter
from feelingpilates_autopilot.domain.failures import FailureCategory
from feelingpilates_autopilot.domain.models import RunId, TokenClass, WorkflowId
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, ExecutionStatus, SandboxMode
from tests.fixtures.fake_codex_sdk import FakeCodex


class TestCodexSdkResults(unittest.TestCase):
    def execute(self, behavior: str):
        fake = FakeCodex(behavior)
        adapter = CodexSdkAdapter(codex_factory=lambda: fake, authorized_workspace_root=str(Path.cwd()), verify_capabilities=False)
        request = ExecutionRequest(RunId("run-1"), "offline", WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(Path.cwd()), SandboxMode.READ_ONLY, timeout_seconds=1)
        return adapter.get_result(adapter.start(request))

    def test_valid_strict_result_and_usage_truthfulness(self) -> None:
        valid = self.execute("valid")
        self.assertEqual(valid.status, ExecutionStatus.SUCCEEDED)
        self.assertIsNone(valid.usage_record)
        present = self.execute("usage")
        self.assertEqual(present.usage_record.measurements[TokenClass.INPUT].value, 3)
        self.assertEqual(present.usage_record.measurements[TokenClass.OUTPUT].value, 0)
        self.assertIsNone(present.usage_record.measurements[TokenClass.CACHED_INPUT].value)
        partial = self.execute("partial_usage")
        self.assertEqual(partial.usage_record.measurements[TokenClass.TOTAL].value, 7)
        self.assertIsNone(partial.usage_record.measurements[TokenClass.INPUT].value)

    def test_missing_malformed_and_invalid_semantics_fail_closed(self) -> None:
        for behavior in ("missing", "malformed", "wrong_identity", "wrong_workflow", "bad_gate", "unknown_field", "blank_summary", "bad_nested", "session_conflict"):
            with self.subTest(behavior=behavior):
                observed = self.execute(behavior)
                self.assertEqual(observed.status, ExecutionStatus.FAILED)
                self.assertEqual(observed.failure.category, FailureCategory.RESULT_CONTRACT_FAILURE)

    def test_unknown_provider_events_remain_auxiliary_not_domain_authority(self) -> None:
        observed = self.execute("unknown_events")
        self.assertEqual(observed.status, ExecutionStatus.FAILED)
        self.assertEqual(observed.failure.category, FailureCategory.RESULT_CONTRACT_FAILURE)
        self.assertIsNone(observed.agent_result)

    def test_stream_terminal_cardinality_and_bound_are_fail_closed_during_collection(self) -> None:
        for behavior in ("missing", "malformed_then_valid", "two_finals"):
            with self.subTest(behavior=behavior):
                observed = self.execute(behavior)
                self.assertEqual(observed.failure.category, FailureCategory.RESULT_CONTRACT_FAILURE)
        fake = FakeCodex("oversized")
        adapter = CodexSdkAdapter(codex_factory=lambda: fake, authorized_workspace_root=str(Path.cwd()), result_limit_bytes=128, verify_capabilities=False)
        request = ExecutionRequest(RunId("run-1"), "offline", WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(Path.cwd()), SandboxMode.READ_ONLY, timeout_seconds=1)
        observed = adapter.get_result(adapter.start(request))
        self.assertEqual(observed.failure.category, FailureCategory.RESULT_CONTRACT_FAILURE)

    def test_typed_and_event_session_contradiction_is_sdk_session_failure_without_session(self) -> None:
        observed = self.execute("conflicting_event_session")
        self.assertEqual(observed.failure.category, FailureCategory.SDK_SESSION_FAILURE)
        self.assertIsNone(observed.session)
