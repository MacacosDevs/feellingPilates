from datetime import datetime, timezone
from pathlib import Path
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.sqlite_store import DurableStateError, IdempotencyConflict, SQLiteStateStore, payload_fingerprint
from feelingpilates_autopilot.domain.models import Checkpoint, CheckpointId, Run, RunId, StateTransition, TransitionId, Workflow, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState


class SQLiteIdempotencyTests(unittest.TestCase):
    def _store_with_run(self, directory: str) -> tuple[SQLiteStateStore, Run]:
        store = SQLiteStateStore(Path(directory) / "state.db")
        workflow = Workflow(WorkflowId("workflow"), ())
        run = Run(RunId("run"), workflow.workflow_id, OperationalState.PENDING)
        store.save_workflow(workflow); store.save_run(run)
        return store, run

    @staticmethod
    def _operation(run: Run, suffix: str = "one") -> tuple[StateTransition, Checkpoint]:
        return (
            StateTransition(TransitionId(f"transition-{suffix}"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING),
            Checkpoint(CheckpointId(f"checkpoint-{suffix}"), run.run_id, OperationalState.RUNNING, datetime(2026, 9, 3, tzinfo=timezone.utc)),
        )

    def test_canonical_payload_replay_and_all_conflicts(self) -> None:
        self.assertEqual(payload_fingerprint({"a": 1, "b": [2]}), payload_fingerprint({"b": [2], "a": 1}))
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.db"
            store, run = self._store_with_run(directory)
            transition, checkpoint = self._operation(run)
            self.assertTrue(store.save_transition_checkpoint(transition, checkpoint, 0, idempotency_key="key", operation_kind="operation", canonical_operation_identity="identity", payload={"a": 1, "b": 2}))
            self.assertFalse(store.save_transition_checkpoint(transition, checkpoint, 0, idempotency_key="key", operation_kind="operation", canonical_operation_identity="identity", payload={"b": 2, "a": 1}))
            self.assertEqual(store.load_run(run.run_id).state_version, 1)
            self.assertEqual(len(store.list_transitions(run.run_id)), 1)
            for kind, identity, payload in (
                ("different-kind", "identity", {"a": 1, "b": 2}),
                ("operation", "different-identity", {"a": 1, "b": 2}),
                ("operation", "identity", {"a": 2, "b": 2}),
            ):
                with self.assertRaises(IdempotencyConflict):
                    store.save_transition_checkpoint(transition, checkpoint, 0, idempotency_key="key", operation_kind=kind, canonical_operation_identity=identity, payload=payload)
            store.close()
            reopened = SQLiteStateStore(path)
            self.assertFalse(reopened.save_transition_checkpoint(transition, checkpoint, 0, idempotency_key="key", operation_kind="operation", canonical_operation_identity="identity", payload={"a": 1, "b": 2}))
            self.assertEqual(reopened.load_run(run.run_id).state_version, 1)
            reopened.close()

    def test_public_preconsumption_is_unavailable_and_inconsistent_completion_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            store, run = self._store_with_run(directory)
            self.assertFalse(hasattr(store, "consume_idempotency"))
            first_transition, first_checkpoint = self._operation(run, "first")
            store.save_transition_checkpoint(first_transition, first_checkpoint, 0, idempotency_key="first-key", canonical_operation_identity="first", payload={})
            fingerprint = payload_fingerprint({})
            store._connection.execute(
                "INSERT INTO idempotency_records(idempotency_key, operation_kind, canonical_operation_identity, payload_fingerprint, applied_at, run_id, transition_id, checkpoint_id, resulting_state_version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ("orphan", "transition_checkpoint", "second", fingerprint, "2026-09-03T00:00:00Z", str(run.run_id), str(first_transition.transition_id), str(first_checkpoint.checkpoint_id), 1),
            )
            second_transition = StateTransition(TransitionId("transition-second"), run.run_id, OperationalState.RUNNING, OperationalState.PAUSED)
            second_checkpoint = Checkpoint(CheckpointId("checkpoint-second"), run.run_id, OperationalState.PAUSED, datetime(2026, 9, 3, tzinfo=timezone.utc))
            with self.assertRaises(DurableStateError):
                store.save_transition_checkpoint(second_transition, second_checkpoint, 1, idempotency_key="orphan", canonical_operation_identity="second", payload={})
            self.assertIsNone(store.load_checkpoint(second_checkpoint.checkpoint_id))
            self.assertEqual(store.load_run(run.run_id).state_version, 1)
            store.close()
