from dataclasses import replace
from datetime import datetime, timedelta, timezone
from pathlib import Path
import sqlite3
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.sqlite_store import FencingConflict, LeaseConflict, SQLiteStateStore
from feelingpilates_autopilot.domain.models import Checkpoint, CheckpointId, Lease, LeaseId, LeaseResolution, Run, RunId, StateTransition, TransitionId, Workflow, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState
from feelingpilates_autopilot.ports.clock import Clock


class FakeClock(Clock):
    def __init__(self) -> None:
        self.value = datetime(2026, 9, 3, tzinfo=timezone.utc)

    def now(self) -> datetime:
        return self.value

    def monotonic_seconds(self) -> float:
        return 0.0

    def advance(self, duration: timedelta) -> None:
        self.value += duration


class SQLiteLeaseTests(unittest.TestCase):
    def _store_with_run(self, directory: str, clock: FakeClock) -> tuple[SQLiteStateStore, Run]:
        store = SQLiteStateStore(Path(directory) / "state.db", clock=clock, lease_duration=timedelta(seconds=10))
        workflow = Workflow(WorkflowId("workflow"), ())
        run = Run(RunId("run"), workflow.workflow_id, OperationalState.PENDING)
        store.save_workflow(workflow); store.save_run(run)
        return store, run

    @staticmethod
    def _protected_operation(
        run: Run,
        clock: FakeClock,
        suffix: str,
        protected_resource_key: str,
        fencing_token: int,
    ) -> tuple[StateTransition, Checkpoint]:
        return (
            StateTransition(
                TransitionId(f"transition-{suffix}"),
                run.run_id,
                OperationalState.PENDING,
                OperationalState.RUNNING,
                protected_resource_key=protected_resource_key,
                fencing_token=fencing_token,
            ),
            Checkpoint(
                CheckpointId(f"checkpoint-{suffix}"),
                run.run_id,
                OperationalState.RUNNING,
                clock.now(),
            ),
        )

    def _assert_no_semantic_effect(
        self,
        store: SQLiteStateStore,
        run: Run,
        checkpoint_id: CheckpointId,
        idempotency_key: str,
    ) -> None:
        self.assertEqual(store.load_run(run.run_id), run)
        self.assertEqual(store.list_transitions(run.run_id), ())
        self.assertIsNone(store.load_checkpoint(checkpoint_id))
        self.assertEqual(
            store._connection.execute(
                "SELECT COUNT(*) FROM idempotency_records WHERE idempotency_key = ?",
                (idempotency_key,),
            ).fetchone()[0],
            0,
        )

    def _assert_durable_lease_authorization_was_reached(
        self, trace: list[str]
    ) -> None:
        normalized = [" ".join(statement.upper().split()) for statement in trace]
        begin = next(
            index for index, statement in enumerate(normalized)
            if statement.startswith("BEGIN IMMEDIATE")
        )
        lease_lookup = next(
            index for index, statement in enumerate(normalized)
            if "FROM LEASES" in statement and "PROTECTED_RESOURCE_KEY" in statement
        )
        self.assertLess(begin, lease_lookup)

    def test_key_is_required_and_acquire_inspect_expire_reacquire_are_per_resource_monotonic(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            store, run = self._store_with_run(directory, clock)
            with self.assertRaises(LeaseConflict):
                store.acquire_durable_lease(Lease(LeaseId("missing"), run.run_id, "A"))
            first = store.acquire_durable_lease(Lease(LeaseId("one"), run.run_id, "A", "workspace"))
            self.assertEqual(store.inspect_lease("workspace").lease_id, first.lease_id)
            self.assertEqual(store.load_recovery_context(run.run_id).lease_resolution, LeaseResolution.ONE_RELEVANT)
            self.assertIsNone(store.acquire_durable_lease(Lease(LeaseId("blocked"), run.run_id, "B", "workspace")))
            clock.advance(timedelta(seconds=10))
            second = store.acquire_durable_lease(Lease(LeaseId("two"), run.run_id, "B", "workspace"))
            self.assertGreater(second.fencing_token, first.fencing_token)
            other = store.acquire_durable_lease(Lease(LeaseId("other"), run.run_id, "C", "other-workspace"))
            self.assertEqual(other.fencing_token, 1)
            self.assertEqual(store.load_recovery_context(run.run_id).lease_resolution, LeaseResolution.AMBIGUOUS)
            store.close()

    def test_renewal_binds_full_identity_and_release_reopen_has_no_active_lease(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            path = Path(directory) / "state.db"
            store = SQLiteStateStore(path, clock=clock, lease_duration=timedelta(seconds=10))
            workflow = Workflow(WorkflowId("workflow"), ())
            run = Run(RunId("run"), workflow.workflow_id, OperationalState.PENDING)
            other_run = Run(RunId("other"), workflow.workflow_id, OperationalState.PENDING)
            store.save_workflow(workflow); store.save_run(run); store.save_run(other_run)
            lease = store.acquire_durable_lease(Lease(LeaseId("lease"), run.run_id, "owner", "workspace"))
            for forged in (
                replace(lease, run_id=other_run.run_id),
                replace(lease, protected_resource_key="other"),
                replace(lease, holder="other-owner"),
            ):
                with self.assertRaises(LeaseConflict):
                    store.renew_lease(forged, lease.fencing_token)
            clock.advance(timedelta(seconds=1))
            renewed = store.renew_lease(lease, lease.fencing_token)
            self.assertEqual(renewed.fencing_token, lease.fencing_token)
            self.assertGreater(renewed.expires_at, lease.expires_at)
            store.release_lease(renewed)
            store.close()
            reopened = SQLiteStateStore(path, clock=clock)
            context = reopened.load_recovery_context(run.run_id)
            self.assertEqual(context.lease_resolution, LeaseResolution.NONE)
            self.assertIsNone(context.lease)
            reopened.close()

    def test_stale_fencing_rejects_real_mutation_and_acquire_failure_rolls_back(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            store, run = self._store_with_run(directory, clock)
            first = store.acquire_durable_lease(Lease(LeaseId("one"), run.run_id, "A", "workspace"))
            clock.advance(timedelta(seconds=10))
            second = store.acquire_durable_lease(Lease(LeaseId("two"), run.run_id, "B", "workspace"))
            stale_transition, stale_checkpoint = self._protected_operation(
                run, clock, "stale", "workspace", first.fencing_token
            )
            with self.assertRaisesRegex(FencingConflict, "stale fencing token"):
                store.save_transition_checkpoint(
                    stale_transition,
                    stale_checkpoint,
                    0,
                    protected_resource_key="workspace",
                    fencing_token=first.fencing_token,
                    idempotency_key="stale-key",
                    canonical_operation_identity="stale",
                    payload={},
                )
            self._assert_no_semantic_effect(store, run, stale_checkpoint.checkpoint_id, "stale-key")
            self.assertGreater(second.fencing_token, first.fencing_token)
            clock.advance(timedelta(seconds=10))
            with self.assertRaises(sqlite3.IntegrityError):
                store.acquire_durable_lease(Lease(LeaseId("two"), run.run_id, "C", "workspace"))
            self.assertIsNone(store.load_lease(LeaseId("two")).released_at)
            store.close()

    def test_wrong_resource_rejects_through_durable_authorization_without_semantic_effect(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            store, run = self._store_with_run(directory, clock)
            lease = store.acquire_durable_lease(
                Lease(LeaseId("lease"), run.run_id, "owner", "resource-a")
            )
            wrong_transition, wrong_checkpoint = self._protected_operation(
                run, clock, "wrong-resource", "resource-b", lease.fencing_token
            )
            trace: list[str] = []
            store._connection.set_trace_callback(trace.append)
            try:
                with self.assertRaisesRegex(FencingConflict, "no active lease for protected resource"):
                    store.save_transition_checkpoint(
                        wrong_transition,
                        wrong_checkpoint,
                        0,
                        protected_resource_key="resource-b",
                        fencing_token=lease.fencing_token,
                        idempotency_key="wrong-resource-key",
                        canonical_operation_identity="wrong-resource",
                        payload={"resource": "resource-b"},
                    )
            finally:
                store._connection.set_trace_callback(None)
            self._assert_durable_lease_authorization_was_reached(trace)
            self._assert_no_semantic_effect(
                store, run, wrong_checkpoint.checkpoint_id, "wrong-resource-key"
            )
            self.assertEqual(store.load_lease(lease.lease_id), lease)
            store.close()

    def test_current_lease_authorizes_positive_protected_mutation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            store, run = self._store_with_run(directory, clock)
            lease = store.acquire_durable_lease(
                Lease(LeaseId("lease"), run.run_id, "owner", "workspace")
            )
            transition, checkpoint = self._protected_operation(
                run, clock, "authorized", "workspace", lease.fencing_token
            )

            self.assertTrue(
                store.save_transition_checkpoint(
                    transition,
                    checkpoint,
                    0,
                    protected_resource_key="workspace",
                    fencing_token=lease.fencing_token,
                    idempotency_key="authorized-key",
                    canonical_operation_identity="authorized",
                    payload={"authorized": True},
                )
            )

            durable_run = store.load_run(run.run_id)
            durable_transition = store.list_transitions(run.run_id)[0]
            durable_checkpoint = store.load_checkpoint(checkpoint.checkpoint_id)
            self.assertEqual(
                (durable_run.state, durable_run.state_version),
                (OperationalState.RUNNING, 1),
            )
            self.assertEqual(
                (durable_transition.protected_resource_key, durable_transition.fencing_token),
                ("workspace", lease.fencing_token),
            )
            self.assertEqual(durable_checkpoint.state_version, 1)
            self.assertEqual(
                store._connection.execute(
                    "SELECT COUNT(*) FROM idempotency_records WHERE idempotency_key = ?",
                    ("authorized-key",),
                ).fetchone()[0],
                1,
            )
            store.close()

    def test_cross_run_lease_rejection_is_atomic_and_does_not_consume_idempotency(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            clock = FakeClock()
            store, lease_run = self._store_with_run(directory, clock)
            mutation_run = Run(
                RunId("other-run"), lease_run.workflow_id, OperationalState.PENDING
            )
            store.save_run(mutation_run)
            lease = store.acquire_durable_lease(
                Lease(LeaseId("lease"), lease_run.run_id, "owner", "workspace")
            )
            rejected_transition, rejected_checkpoint = self._protected_operation(
                mutation_run, clock, "cross-run", "workspace", lease.fencing_token
            )

            with self.assertRaisesRegex(FencingConflict, "different run"):
                store.save_transition_checkpoint(
                    rejected_transition,
                    rejected_checkpoint,
                    0,
                    protected_resource_key="workspace",
                    fencing_token=lease.fencing_token,
                    idempotency_key="cross-run-key",
                    canonical_operation_identity="cross-run-operation",
                    payload={"attempt": 1},
                )

            self._assert_no_semantic_effect(
                store, mutation_run, rejected_checkpoint.checkpoint_id, "cross-run-key"
            )
            self.assertEqual(store.load_lease(lease.lease_id), lease)

            authorized_transition, authorized_checkpoint = self._protected_operation(
                lease_run, clock, "authorized-retry", "workspace", lease.fencing_token
            )
            self.assertTrue(
                store.save_transition_checkpoint(
                    authorized_transition,
                    authorized_checkpoint,
                    0,
                    protected_resource_key="workspace",
                    fencing_token=lease.fencing_token,
                    idempotency_key="cross-run-key",
                    canonical_operation_identity="cross-run-operation",
                    payload={"attempt": 1},
                )
            )
            self.assertEqual(store.load_run(lease_run.run_id).state_version, 1)
            store.close()
