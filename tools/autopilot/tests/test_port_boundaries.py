import inspect
from pathlib import Path
import unittest

from feelingpilates_autopilot.domain.failures import FailureCategory, FailureRecord
from feelingpilates_autopilot.domain.models import (
    AgentExecutionStatus,
    AgentResult,
    ExecutionId,
    FailureId,
    Findings,
    GateResult,
    Lease,
    LeaseId,
    LeaseResolution,
    OperationalState,
    Run,
    RunId,
    RunRecoveryContext,
    TestOutcome,
    Workflow,
    WorkflowId,
)
from feelingpilates_autopilot.ports.agent_executor import (
    AgentExecutor,
    ExecutionObservation,
    ExecutionStatus,
)
from feelingpilates_autopilot.ports.clock import Clock
from feelingpilates_autopilot.ports.repository import Repository, RepositorySnapshot
from feelingpilates_autopilot.ports.state_store import StateStore


class PortBoundaryTests(unittest.TestCase):
    def test_ports_are_abstract_contracts(self) -> None:
        for port in (AgentExecutor, StateStore, Repository, Clock):
            self.assertTrue(inspect.isabstract(port))

    def test_agent_executor_supports_the_four_contract_operations(self) -> None:
        self.assertTrue({"capabilities", "start", "resume", "interrupt"} <= set(AgentExecutor.__abstractmethods__))

    def test_agent_executor_exposes_a_normalized_terminal_result(self) -> None:
        self.assertIn("get_result", AgentExecutor.__abstractmethods__)
        self.assertEqual(
            ExecutionObservation.__annotations__["status"],
            ExecutionStatus,
        )
        self.assertIn("agent_result", ExecutionObservation.__annotations__)
        self.assertIn("failure", ExecutionObservation.__annotations__)
        self.assertIn("raw_output", ExecutionObservation.__annotations__)

    def test_executor_public_contract_uses_internal_not_provider_native_types(self) -> None:
        signature = inspect.signature(AgentExecutor.get_result)
        annotations = " ".join(str(value) for value in (signature.return_annotation, *ExecutionObservation.__annotations__.values())).lower()
        for provider_native_name in ("openai", "codex", "subprocess", "threadresult", "process"):
            self.assertNotIn(provider_native_name, annotations)

    def test_successful_executor_observation_carries_structured_agent_result(self) -> None:
        run_id = RunId("run-1")
        result = AgentResult(
            workflow_id=WorkflowId("workflow-1"),
            run_id=run_id,
            role="EXECUTOR",
            status=AgentExecutionStatus.SUCCEEDED,
            gate="scope",
            gate_result=GateResult.PASS,
            summary="The authorized change was materialized.",
            findings=Findings((), (), ()),
            test_evidence=(),
            changed_paths=("tools/autopilot/src/example.py",),
            recommendation="Submit to independent audit.",
            requires_human_decision=False,
            p1_correctable=False,
            correction_artifact=None,
        )
        observation = ExecutionObservation(
            execution_id=ExecutionId("execution-1"),
            run_id=run_id,
            session=None,
            status=ExecutionStatus.SUCCEEDED,
            agent_result=result,
            raw_output="auxiliary log text",
        )
        self.assertIs(observation.agent_result, result)
        self.assertEqual(observation.raw_output, "auxiliary log text")

    def test_failure_and_nonterminal_executor_observations_have_distinct_contracts(self) -> None:
        failure = FailureRecord(
            failure_id=FailureId("failure-1"),
            category=FailureCategory.NETWORK_UNAVAILABLE,
            message="Network unavailable before a valid result.",
        )
        failed = ExecutionObservation(
            execution_id=ExecutionId("execution-1"),
            run_id=RunId("run-1"),
            session=None,
            status=ExecutionStatus.FAILED,
            failure=failure,
        )
        pending = ExecutionObservation(
            execution_id=ExecutionId("execution-1"),
            run_id=RunId("run-1"),
            session=None,
            status=ExecutionStatus.RUNNING,
        )
        self.assertIs(failed.failure, failure)
        self.assertIsNone(pending.agent_result)
        with self.assertRaises(ValueError):
            ExecutionObservation(
                execution_id=ExecutionId("execution-1"),
                run_id=RunId("run-1"),
                session=None,
                status=ExecutionStatus.SUCCEEDED,
            )

    def test_state_store_can_reconstruct_recovery_state(self) -> None:
        required_reads = {
            "load_workflow",
            "load_run",
            "list_attempts",
            "list_transitions",
            "load_checkpoint",
            "latest_checkpoint",
            "list_artifacts",
            "load_session",
            "load_usage",
            "list_usage",
            "list_failures",
            "load_lease",
            "load_recovery_context",
            "load_human_decision",
            "list_human_decisions",
        }
        self.assertTrue(required_reads <= set(StateStore.__abstractmethods__))

    def test_recovery_context_discovers_lease_by_run_without_silent_selection(self) -> None:
        workflow = Workflow(workflow_id=WorkflowId("workflow-1"), phases=())
        run = Run(run_id=RunId("run-1"), workflow_id=workflow.workflow_id, state=OperationalState.RECOVERING)
        one_lease = RunRecoveryContext(
            workflow=workflow,
            run=run,
            latest_checkpoint=None,
            session=None,
            lease_resolution=LeaseResolution.ONE_RELEVANT,
            lease=Lease(lease_id=LeaseId("lease-1"), run_id=run.run_id, holder="worker-1"),
            failures=(),
            transitions=(),
            human_decisions=(),
        )
        ambiguous = RunRecoveryContext(
            workflow=workflow,
            run=run,
            latest_checkpoint=None,
            session=None,
            lease_resolution=LeaseResolution.AMBIGUOUS,
            lease=None,
            failures=(),
            transitions=(),
            human_decisions=(),
        )
        self.assertEqual(one_lease.lease.lease_id, LeaseId("lease-1"))
        self.assertIsNone(ambiguous.lease)
        with self.assertRaises(ValueError):
            RunRecoveryContext(
                workflow=workflow,
                run=run,
                latest_checkpoint=None,
                session=None,
                lease_resolution=LeaseResolution.AMBIGUOUS,
                lease=Lease(lease_id=LeaseId("lease-2"), run_id=run.run_id, holder="worker-2"),
                failures=(),
                transitions=(),
                human_decisions=(),
            )

    def test_repository_snapshot_represents_identity_detached_head_and_path_categories(self) -> None:
        self.assertEqual(
            set(RepositorySnapshot.__annotations__),
            {
                "repository_path",
                "worktree_path",
                "branch",
                "is_detached",
                "revision",
                "upstream_ref",
                "resolved_upstream_revision",
                "staged_paths",
                "modified_paths",
                "untracked_paths",
            },
        )
        attached = RepositorySnapshot(
            repository_path="/repos/feelingpilates/.git",
            worktree_path="/repos/feelingpilates",
            branch="orquestacion/autopilot-r1",
            is_detached=False,
            revision="5e0a3bd",
            upstream_ref="origin/orquestacion/autopilot-r1",
            resolved_upstream_revision="5e0a3bd",
            staged_paths=("staged.py",),
            modified_paths=("modified.py",),
            untracked_paths=("new.py",),
        )
        detached = RepositorySnapshot(
            repository_path="/repos/feelingpilates/.git",
            worktree_path="/repos/feelingpilates",
            branch=None,
            is_detached=True,
            revision="5e0a3bd",
            upstream_ref=None,
            resolved_upstream_revision=None,
            staged_paths=(),
            modified_paths=(),
            untracked_paths=(),
        )
        self.assertNotEqual(attached.untracked_paths, attached.modified_paths)
        self.assertTrue(detached.is_detached)
        self.assertIsNone(detached.branch)
        with self.assertRaises(ValueError):
            RepositorySnapshot(
                repository_path="/repos/feelingpilates/.git",
                worktree_path="/repos/feelingpilates",
                branch=None,
                is_detached=False,
                revision="5e0a3bd",
                upstream_ref=None,
                resolved_upstream_revision=None,
                staged_paths=(),
                modified_paths=(),
                untracked_paths=(),
            )

    def test_no_forbidden_adapter_dependency_is_imported(self) -> None:
        package = Path(__file__).parents[1] / "src" / "feelingpilates_autopilot"
        source = "\n".join(path.read_text() for path in package.joinpath("domain").rglob("*.py"))
        source += "\n" + "\n".join(path.read_text() for path in package.joinpath("ports").rglob("*.py"))
        source = source.lower()
        for forbidden in ("openai_codex", "subprocess", "sqlite3", "gitpython", "launchd", "spring"):
            self.assertNotIn(forbidden, source)
