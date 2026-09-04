from datetime import datetime, timezone
from pathlib import Path
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.sqlite_store import DurableStateError, SQLiteStateStore
from feelingpilates_autopilot.domain.models import Checkpoint, CheckpointId, HumanDecision, HumanDecisionId, LeaseResolution, Run, RunId, Workflow, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState


class SQLiteRecoveryTests(unittest.TestCase):
    def test_reopen_reconstructs_no_lease_checkpoint_and_wal_safe_online_backup(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            path = root / "state.db"
            store = SQLiteStateStore(path)
            workflow = Workflow(WorkflowId("workflow"), ())
            run = Run(RunId("run"), workflow.workflow_id, OperationalState.RECOVERING)
            store.save_workflow(workflow); store.save_run(run)
            store.save_checkpoint(Checkpoint(CheckpointId("checkpoint"), run.run_id, OperationalState.RECOVERING, datetime(2026, 9, 3, tzinfo=timezone.utc)))
            self.assertEqual(store.connection_settings()["journal_mode"], "wal")
            store.backup_to(root / "backup.db")
            store.close()
            reopened = SQLiteStateStore(path)
            context = reopened.load_recovery_context(run.run_id)
            self.assertEqual(context.lease_resolution, LeaseResolution.NONE)
            self.assertIsNone(context.lease)
            self.assertEqual(context.latest_checkpoint.checkpoint_id, CheckpointId("checkpoint"))
            reopened.integrity_check()
            reopened.close()
            backup = SQLiteStateStore(root / "backup.db")
            self.assertEqual(backup.load_run(run.run_id), run)
            backup.integrity_check(); backup.close()

    def test_unresolved_decision_reopens_then_resolves_once_and_reopens_resolved(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.db"
            store = SQLiteStateStore(path)
            workflow = Workflow(WorkflowId("workflow"), ())
            run = Run(RunId("run"), workflow.workflow_id, OperationalState.HUMAN_DECISION_REQUIRED)
            decision_id = HumanDecisionId("decision")
            store.save_workflow(workflow); store.save_run(run)
            store.save_human_decision(HumanDecision(decision_id, run.run_id, "approve", "human authority required"))
            store.close()
            reopened = SQLiteStateStore(path)
            self.assertTrue(reopened.load_human_decision(decision_id).unresolved)
            resolved = reopened.resolve_human_decision(decision_id, "approved by operator")
            self.assertFalse(resolved.unresolved)
            self.assertEqual(resolved.resolution, "approved by operator")
            with self.assertRaises(DurableStateError):
                reopened.resolve_human_decision(HumanDecisionId("missing"), "no")
            with self.assertRaises(DurableStateError):
                reopened.resolve_human_decision(decision_id, "conflicting response")
            reopened.close()
            final = SQLiteStateStore(path)
            loaded = final.load_human_decision(decision_id)
            self.assertFalse(loaded.unresolved)
            self.assertEqual(loaded.resolution, "approved by operator")
            final.close()
