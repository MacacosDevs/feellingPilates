import unittest
from datetime import datetime, timezone

from feelingpilates_autopilot.domain.failures import FailureCategory
from feelingpilates_autopilot.domain.models import (
    AgentExecutionStatus,
    AgentResult,
    Checkpoint,
    CheckpointId,
    Findings,
    GateResult,
    RunId,
    TestEvidence,
    TokenClass,
    TokenMeasurement,
    TestOutcome,
    UsageProvenance,
    UsageRecord,
    WorkflowId,
)
from feelingpilates_autopilot.domain.states import OperationalState, WorkPhaseKind


class DomainContractTests(unittest.TestCase):
    def test_operational_states_and_phase_kind_are_separate(self) -> None:
        self.assertEqual(len(OperationalState), 12)
        self.assertIn(OperationalState.WAITING_FOR_QUOTA, OperationalState)
        self.assertNotIn("RUNNING", {kind.value for kind in WorkPhaseKind})

    def test_failure_taxonomy_contains_required_categories(self) -> None:
        self.assertEqual(
            {"SDK_TRANSPORT_FAILURE", "SDK_SESSION_FAILURE", "CLI_PROCESS_FAILURE", "NETWORK_UNAVAILABLE", "QUOTA_EXHAUSTED", "RESULT_CONTRACT_FAILURE", "PROCESS_CRASH", "UNCERTAIN_WRITE", "GIT_BASELINE_DRIFT", "TEST_FAILURE", "SECURITY_STOP"},
            {category.value for category in FailureCategory},
        )

    def test_aggregate_only_evidence_leaves_components_unavailable(self) -> None:
        usage = UsageRecord.from_direct_evidence(
            "aggregate-adapter",
            {TokenClass.TOTAL: 1000},
            {TokenClass.TOTAL: "adapter.usage.total_tokens"},
        )
        self.assertEqual(usage.measurements[TokenClass.TOTAL].value, 1000)
        self.assertIsNone(usage.measurements[TokenClass.INPUT].value)
        self.assertEqual(usage.measurements[TokenClass.INPUT].provenance, UsageProvenance.UNAVAILABLE)

    def test_directly_observed_zero_is_valid(self) -> None:
        usage = UsageRecord.from_direct_evidence(
            "adapter", {TokenClass.OUTPUT: 0}, {TokenClass.OUTPUT: "adapter.usage.output_tokens"}
        )
        self.assertEqual(usage.measurements[TokenClass.OUTPUT].value, 0)
        self.assertEqual(usage.measurements[TokenClass.OUTPUT].provenance, UsageProvenance.OBSERVED)

    def test_missing_class_does_not_become_zero(self) -> None:
        usage = UsageRecord.from_direct_evidence("adapter", {}, {})
        self.assertIsNone(usage.measurements[TokenClass.OUTPUT].value)

    def test_heuristic_decomposition_without_exact_evidence_is_rejected(self) -> None:
        with self.assertRaises(ValueError):
            UsageRecord.from_direct_evidence(
                "adapter",
                {TokenClass.TOTAL: 1000, TokenClass.INPUT: 600, TokenClass.OUTPUT: 400},
                {TokenClass.TOTAL: "adapter.usage.total_tokens"},
            )

    def test_derived_or_estimated_values_cannot_replace_observed_fields_in_r2(self) -> None:
        with self.assertRaises(ValueError):
            TokenMeasurement(value=10, provenance=UsageProvenance.DERIVED)

    def test_checkpoint_uses_the_schema_names_and_default_resume_data(self) -> None:
        checkpoint = Checkpoint(
            checkpoint_id=CheckpointId("checkpoint-1"),
            run_id=RunId("run-1"),
            operational_state=OperationalState.PAUSED,
            created_at=datetime(2026, 9, 3, tzinfo=timezone.utc),
        )
        self.assertEqual(checkpoint.operational_state, OperationalState.PAUSED)
        self.assertEqual(checkpoint.resume_data, {})

    def test_agent_result_separates_agent_outcome_from_gate_result(self) -> None:
        result = AgentResult(
            workflow_id=WorkflowId("workflow-1"),
            run_id=RunId("run-1"),
            role="AUDITOR",
            status=AgentExecutionStatus.SUCCEEDED,
            gate="implementation",
            gate_result=GateResult.NOT_APPLICABLE,
            summary="No implementation gate applies to this documentation-only profile.",
            findings=Findings((), (), ()),
            test_evidence=(TestEvidence("scope", TestOutcome.PASS, "scope snapshot"),),
            changed_paths=(),
            recommendation="Continue with the applicable documentation gate.",
            requires_human_decision=False,
            p1_correctable=False,
            correction_artifact=None,
        )
        self.assertEqual(result.status, AgentExecutionStatus.SUCCEEDED)
        self.assertEqual(result.gate_result, GateResult.NOT_APPLICABLE)

    def test_agent_result_rejects_empty_summary_and_unprovenanced_usage(self) -> None:
        kwargs = {
            "workflow_id": WorkflowId("workflow-1"),
            "run_id": RunId("run-1"),
            "role": "AUDITOR",
            "status": AgentExecutionStatus.SUCCEEDED,
            "gate": "implementation",
            "gate_result": GateResult.PASS,
            "findings": Findings((), (), ()),
            "test_evidence": (),
            "changed_paths": (),
            "recommendation": "Proceed.",
            "requires_human_decision": False,
            "p1_correctable": False,
            "correction_artifact": None,
        }
        with self.assertRaises(ValueError):
            AgentResult(summary="", **kwargs)
        with self.assertRaises(ValueError):
            AgentResult(summary="Pass.", usage_record=object(), **kwargs)

    def test_agent_result_rejects_noncanonical_gate_value(self) -> None:
        with self.assertRaises(ValueError):
            AgentResult(
                workflow_id=WorkflowId("workflow-1"),
                run_id=RunId("run-1"),
                role="AUDITOR",
                status=AgentExecutionStatus.SUCCEEDED,
                gate="implementation",
                gate_result="SKIPPED",  # type: ignore[arg-type]
                summary="Invalid lifecycle test.",
                findings=Findings((), (), ()),
                test_evidence=(),
                changed_paths=(),
                recommendation="Stop.",
                requires_human_decision=False,
                p1_correctable=False,
                correction_artifact=None,
            )
