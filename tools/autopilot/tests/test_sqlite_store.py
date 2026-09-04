from datetime import datetime, timezone
from pathlib import Path
import sqlite3
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.sqlite_store import DurableStateError, SQLiteStateStore
from feelingpilates_autopilot.domain.failures import FailureCategory, FailureRecord
from feelingpilates_autopilot.domain.models import (
    Attempt, AttemptId, Artifact, ArtifactId, Checkpoint, CheckpointId, FailureId,
    HumanDecision, HumanDecisionId, PhaseId, Run, RunId, SessionId, SessionReference,
    StateTransition, TokenClass, UsageId, UsageRecord, Workflow, WorkflowId, WorkflowPhase,
)
from feelingpilates_autopilot.domain.states import OperationalState, WorkPhaseKind


class SQLiteStoreTests(unittest.TestCase):
    def test_durable_operational_records_and_truthful_usage_survive_reopen(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.db"
            store = SQLiteStateStore(path)
            workflow = Workflow(WorkflowId("workflow"), ())
            run = Run(RunId("run"), workflow.workflow_id, OperationalState.PENDING)
            store.save_workflow(workflow)
            store.save_run(run)
            store.save_checkpoint(Checkpoint(CheckpointId("checkpoint"), run.run_id, OperationalState.PENDING, datetime(2026, 9, 3, tzinfo=timezone.utc), {"resume": "safe"}))
            store.save_artifact(Artifact(ArtifactId("artifact"), run.run_id, "evidence://1"))
            store.save_session(SessionReference(SessionId("session"), run.run_id, "opaque", "sdk", "EXECUTOR"))
            usage = UsageRecord.from_direct_evidence("adapter", {TokenClass.TOTAL: 0}, {TokenClass.TOTAL: "exact"})
            store.save_usage(UsageId("usage"), run, usage)
            failure = FailureRecord(FailureId("failure"), FailureCategory.PROCESS_CRASH, "crash", True)
            store.save_failure(run, failure)
            store.close()

            reopened = SQLiteStateStore(path)
            self.assertEqual(reopened.latest_checkpoint(run.run_id).operational_state, OperationalState.PENDING)
            self.assertEqual(reopened.load_session(run.run_id).opaque_reference, "opaque")
            self.assertEqual(reopened.list_failures(run.run_id), (failure,))
            loaded_usage = reopened.load_usage(UsageId("usage"))
            self.assertIsNone(loaded_usage.measurements[TokenClass.INPUT].value)
            self.assertEqual(loaded_usage.measurements[TokenClass.TOTAL].value, 0)
            self.assertEqual(reopened.list_artifacts(run.run_id)[0].reference, "evidence://1")
            reopened.close()

    def test_database_rejects_cross_aggregate_attempt_transition_checkpoint_and_session(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            store = SQLiteStateStore(Path(directory) / "state.db")
            first = Workflow(WorkflowId("first"), (WorkflowPhase(PhaseId("p1"), WorkPhaseKind.IMPLEMENTATION),))
            second = Workflow(WorkflowId("second"), (WorkflowPhase(PhaseId("p2"), WorkPhaseKind.AUDIT),))
            first_run = Run(RunId("run-1"), first.workflow_id, OperationalState.PENDING)
            second_run = Run(RunId("run-2"), second.workflow_id, OperationalState.PENDING)
            store.save_workflow(first); store.save_workflow(second)
            store.save_run(first_run); store.save_run(second_run)
            with self.assertRaises(sqlite3.IntegrityError):
                store.save_attempt(Attempt(AttemptId("cross-attempt"), first_run.run_id, PhaseId("p2"), 0))
            with self.assertRaises(DurableStateError):
                store.save_transition(StateTransition("cross-transition", first_run.run_id, OperationalState.PENDING, OperationalState.RUNNING, workflow_id=second.workflow_id))
            with self.assertRaises(DurableStateError):
                store.save_checkpoint(Checkpoint(CheckpointId("cross-checkpoint"), first_run.run_id, OperationalState.PENDING, datetime(2026, 9, 3, tzinfo=timezone.utc), workflow_id=second.workflow_id))
            store.save_attempt(Attempt(AttemptId("attempt-2"), second_run.run_id, PhaseId("p2"), 0))
            with self.assertRaises(sqlite3.IntegrityError):
                store.save_session(SessionReference(SessionId("cross-session"), first_run.run_id, "opaque", attempt_id=AttemptId("attempt-2")))
            store.close()
