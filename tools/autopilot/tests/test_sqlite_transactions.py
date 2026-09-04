from datetime import datetime, timezone
from pathlib import Path
import sqlite3
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.sqlite_store import ConcurrencyConflict, DurableStateError, FencingConflict, SQLiteStateStore
from feelingpilates_autopilot.domain.models import Checkpoint, CheckpointId, Run, RunId, StateTransition, TransitionId, Workflow, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState


class SQLiteTransactionTests(unittest.TestCase):
    def _store_with_run(self, directory: str) -> tuple[SQLiteStateStore, Run]:
        store = SQLiteStateStore(Path(directory) / "state.db")
        workflow = Workflow(WorkflowId("workflow"), ())
        run = Run(RunId("run"), workflow.workflow_id, OperationalState.PENDING)
        store.save_workflow(workflow); store.save_run(run)
        return store, run

    def _assert_late_run_mutation_rolled_back(self, trace: list[str]) -> None:
        normalized = [" ".join(statement.upper().split()) for statement in trace]
        begin = next(
            index for index, statement in enumerate(normalized)
            if statement.startswith("BEGIN IMMEDIATE")
        )
        run_update = next(
            index for index, statement in enumerate(normalized)
            if statement.startswith("UPDATE RUNS SET STATE")
        )
        rollback = next(
            index for index, statement in enumerate(normalized)
            if statement.startswith("ROLLBACK")
        )
        self.assertLess(begin, run_update)
        self.assertLess(run_update, rollback)

    def test_transition_checkpoint_preserves_one_resulting_state_version_and_idempotency_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            store, run = self._store_with_run(directory)
            transition = StateTransition(TransitionId("transition"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING)
            checkpoint = Checkpoint(CheckpointId("checkpoint"), run.run_id, OperationalState.RUNNING, datetime(2026, 9, 3, tzinfo=timezone.utc))
            self.assertTrue(store.save_transition_checkpoint(transition, checkpoint, 0, idempotency_key="key", canonical_operation_identity="transition", payload={"a": 1}))
            durable_run = store.load_run(run.run_id)
            durable_transition = store.list_transitions(run.run_id)[0]
            durable_checkpoint = store.latest_checkpoint(run.run_id)
            self.assertEqual((durable_run.state, durable_run.state_version), (OperationalState.RUNNING, 1))
            self.assertEqual((durable_transition.state_version, durable_checkpoint.state_version), (1, 1))
            self.assertEqual(durable_transition.idempotency_key, "key")
            self.assertEqual(durable_checkpoint.idempotency_key, "key")
            self.assertEqual(durable_transition.canonical_operation_identity, "transition")
            store.close()

    def test_inconsistent_checkpoint_and_stale_version_fail_without_semantic_change(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            store, run = self._store_with_run(directory)
            transition = StateTransition(TransitionId("bad"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING)
            with self.assertRaises(DurableStateError):
                store.save_transition_checkpoint(transition, Checkpoint(CheckpointId("bad-cp"), run.run_id, OperationalState.PAUSED, datetime(2026, 9, 3, tzinfo=timezone.utc)), 0)
            with self.assertRaisesRegex(FencingConflict, "no active lease for protected resource"):
                store.save_transition_checkpoint(
                    StateTransition(TransitionId("fabricated-fence"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING, protected_resource_key="not-leased", fencing_token=1),
                    Checkpoint(CheckpointId("fabricated-fence-cp"), run.run_id, OperationalState.RUNNING, datetime(2026, 9, 3, tzinfo=timezone.utc)),
                    0,
                    protected_resource_key="not-leased",
                    fencing_token=1,
                    idempotency_key="fabricated-fence-key",
                    canonical_operation_identity="fabricated-fence",
                    payload={},
                )
            self.assertEqual(store.load_run(run.run_id), run)
            self.assertEqual(store.list_transitions(run.run_id), ())
            self.assertIsNone(store.latest_checkpoint(run.run_id))
            self.assertEqual(
                store._connection.execute(
                    "SELECT COUNT(*) FROM idempotency_records WHERE idempotency_key = ?",
                    ("fabricated-fence-key",),
                ).fetchone()[0],
                0,
            )
            good = StateTransition(TransitionId("good"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING)
            store.save_transition_checkpoint(good, Checkpoint(CheckpointId("good-cp"), run.run_id, OperationalState.RUNNING, datetime(2026, 9, 3, tzinfo=timezone.utc)), 0)
            with self.assertRaises(ConcurrencyConflict):
                store.save_transition_checkpoint(StateTransition(TransitionId("stale"), run.run_id, OperationalState.RUNNING, OperationalState.PAUSED), Checkpoint(CheckpointId("stale-cp"), run.run_id, OperationalState.PAUSED, datetime(2026, 9, 3, tzinfo=timezone.utc)), 0)
            self.assertEqual(store.load_run(run.run_id).state_version, 1)
            self.assertIsNone(store.load_checkpoint(CheckpointId("stale-cp")))
            store.close()

    def test_failure_after_run_update_rolls_back_run_transition_checkpoint_and_idempotency(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            store, run = self._store_with_run(directory)
            duplicate = StateTransition(TransitionId("duplicate"), run.run_id, OperationalState.PENDING, OperationalState.RUNNING)
            store.save_transition(duplicate)
            trace: list[str] = []
            store._connection.set_trace_callback(trace.append)
            try:
                with self.assertRaises(sqlite3.IntegrityError):
                    store.save_transition_checkpoint(
                        duplicate,
                        Checkpoint(CheckpointId("would-rollback"), run.run_id, OperationalState.RUNNING, datetime(2026, 9, 3, tzinfo=timezone.utc)),
                        0,
                        idempotency_key="rollback-key",
                        canonical_operation_identity="duplicate",
                        payload={},
                    )
            finally:
                store._connection.set_trace_callback(None)
            self._assert_late_run_mutation_rolled_back(trace)
            durable_run = store.load_run(run.run_id)
            self.assertEqual(durable_run.state, run.state)
            self.assertEqual(durable_run.state_version, run.state_version)
            self.assertIsNone(store.load_checkpoint(CheckpointId("would-rollback")))
            self.assertEqual(store._connection.execute("SELECT COUNT(*) FROM idempotency_records WHERE idempotency_key = 'rollback-key'").fetchone()[0], 0)
            self.assertEqual(
                tuple(transition.transition_id for transition in store.list_transitions(run.run_id)),
                (TransitionId("duplicate"),),
            )
            store.close()
