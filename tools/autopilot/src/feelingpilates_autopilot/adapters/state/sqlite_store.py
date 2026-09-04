"""Concrete stdlib SQLite durable StateStore for the R3 recovery foundation."""

from __future__ import annotations

from contextlib import contextmanager
from datetime import datetime, timedelta, timezone
from hashlib import sha256
import json
from pathlib import Path
import sqlite3
from typing import Iterator, Mapping

from ...domain.failures import FailureCategory, FailureRecord
from ...domain.models import (
    Artifact, Attempt, AttemptId, Checkpoint, CheckpointId, HumanDecision,
    HumanDecisionId, Lease, LeaseId, LeaseResolution, PhaseId, Run,
    RunId, RunRecoveryContext, SessionId, SessionReference, StateTransition,
    TokenClass, TokenMeasurement, TransitionId, UsageId, UsageProvenance,
    UsageRecord, Workflow, WorkflowId, WorkflowPhase,
)
from ...domain.states import OperationalState, WorkPhaseKind
from ...ports.clock import Clock
from ...ports.state_store import StateStore
from .migrations import apply_migrations, configure_connection, verify_connection


class DurableStateError(RuntimeError):
    """A durable-state invariant prevented a safe operation."""


class ConcurrencyConflict(DurableStateError):
    pass


class FencingConflict(DurableStateError):
    pass


class IdempotencyConflict(DurableStateError):
    pass


class LeaseConflict(DurableStateError):
    pass


class _SystemClock(Clock):
    def now(self) -> datetime:
        return datetime.now(timezone.utc)

    def monotonic_seconds(self) -> float:
        # This is intentionally not used for any durable time decision.
        import time
        return time.monotonic()


def _utc(value: datetime) -> datetime:
    if value.tzinfo is None or value.utcoffset() is None:
        raise ValueError("durable timestamps must be timezone-aware UTC instants")
    return value.astimezone(timezone.utc)


def _timestamp(value: datetime) -> str:
    return _utc(value).isoformat(timespec="microseconds").replace("+00:00", "Z")


def _from_timestamp(value: str) -> datetime:
    parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    return _utc(parsed)


def canonical_json_bytes(payload: object) -> bytes:
    """Encode only the deliberately small, deterministic JSON value domain."""
    def validate(value: object) -> None:
        if value is None or isinstance(value, (bool, str, int)):
            return
        if isinstance(value, (list, tuple)):
            for item in value:
                validate(item)
            return
        if isinstance(value, Mapping):
            if any(not isinstance(key, str) for key in value):
                raise ValueError("canonical JSON object keys must be strings")
            for item in value.values():
                validate(item)
            return
        raise ValueError("payload is outside the canonical JSON domain")

    validate(payload)
    return json.dumps(payload, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode("utf-8")


def payload_fingerprint(payload: object) -> str:
    return sha256(canonical_json_bytes(payload)).hexdigest()


class SQLiteStateStore(StateStore):
    """Explicit-path, transaction-bound StateStore backed only by ``sqlite3``."""

    def __init__(
        self,
        database_path: str | Path,
        *,
        clock: Clock | None = None,
        busy_timeout_ms: int = 5_000,
        lease_duration: timedelta = timedelta(minutes=5),
        migrations_directory: str | Path | None = None,
    ) -> None:
        self._path = Path(database_path).expanduser().resolve()
        self._assert_external_database_path(self._path)
        if busy_timeout_ms < 0:
            raise ValueError("busy_timeout_ms must be non-negative")
        if lease_duration <= timedelta(0):
            raise ValueError("lease_duration must be positive")
        self._clock = clock or _SystemClock()
        self._busy_timeout_ms = busy_timeout_ms
        self._lease_duration = lease_duration
        self._migrations_directory = (
            Path(migrations_directory)
            if migrations_directory is not None
            else Path(__file__).resolve().parents[4] / "migrations"
        )
        self._path.parent.mkdir(parents=True, exist_ok=True)
        self._connection = self._open_connection()
        try:
            apply_migrations(
                self._connection,
                self._migrations_directory,
                busy_timeout_ms=self._busy_timeout_ms,
            )
        except Exception:
            self._connection.close()
            raise

    @staticmethod
    def _assert_external_database_path(path: Path) -> None:
        for parent in (path.parent, *path.parents):
            if (parent / ".git").exists():
                raise DurableStateError("runtime SQLite databases must be outside the Git checkout")

    def _open_connection(self) -> sqlite3.Connection:
        connection = sqlite3.connect(self._path, isolation_level=None)
        connection.row_factory = sqlite3.Row
        configure_connection(connection, self._busy_timeout_ms)
        return connection

    def _verify_connection(self, connection: sqlite3.Connection) -> None:
        try:
            verify_connection(connection, self._busy_timeout_ms)
        except Exception as error:
            raise DurableStateError("mandatory SQLite connection contract could not be established") from error

    def connection_settings(self) -> Mapping[str, object]:
        self._verify_connection(self._connection)
        return {
            "foreign_keys": int(self._connection.execute("PRAGMA foreign_keys").fetchone()[0]),
            "busy_timeout": int(self._connection.execute("PRAGMA busy_timeout").fetchone()[0]),
            "synchronous": int(self._connection.execute("PRAGMA synchronous").fetchone()[0]),
            "journal_mode": str(self._connection.execute("PRAGMA journal_mode").fetchone()[0]).lower(),
        }

    def close(self) -> None:
        self._connection.close()

    def __enter__(self) -> "SQLiteStateStore":
        return self

    def __exit__(self, *_: object) -> None:
        self.close()

    @contextmanager
    def _write_transaction(self) -> Iterator[sqlite3.Connection]:
        self._verify_connection(self._connection)
        self._connection.execute("BEGIN IMMEDIATE")
        try:
            yield self._connection
            self._connection.execute("COMMIT")
        except Exception:
            self._connection.execute("ROLLBACK")
            raise

    def _now(self) -> datetime:
        return _utc(self._clock.now())

    @staticmethod
    def _one(row: sqlite3.Row | None, factory):
        return None if row is None else factory(row)

    def save_workflow(self, workflow: Workflow) -> None:
        with self._write_transaction() as connection:
            existing = connection.execute("SELECT workflow_id FROM workflows WHERE workflow_id = ?", (str(workflow.workflow_id),)).fetchone()
            if existing is not None:
                loaded = self.load_workflow(workflow.workflow_id)
                if loaded != workflow:
                    raise DurableStateError("workflow identity is immutable")
                return
            connection.execute("INSERT INTO workflows(workflow_id) VALUES (?)", (str(workflow.workflow_id),))
            for phase in workflow.phases:
                connection.execute("INSERT INTO phases(phase_id, workflow_id, kind) VALUES (?, ?, ?)", (str(phase.phase_id), str(workflow.workflow_id), phase.kind.value))

    def save_run(self, run: Run) -> None:
        with self._write_transaction() as connection:
            connection.execute(
                "INSERT INTO runs(run_id, workflow_id, state, state_version) VALUES (?, ?, ?, ?)",
                (str(run.run_id), str(run.workflow_id), run.state.value, run.state_version),
            )

    def update_run(self, run: Run, expected_version: int) -> Run:
        with self._write_transaction() as connection:
            result = connection.execute(
                "UPDATE runs SET state = ?, state_version = state_version + 1 WHERE run_id = ? AND state_version = ?",
                (run.state.value, str(run.run_id), expected_version),
            )
            if result.rowcount != 1:
                raise ConcurrencyConflict("stale run state_version")
        return Run(run.run_id, run.workflow_id, run.state, expected_version + 1)

    def save_attempt(self, attempt: Attempt) -> None:
        with self._write_transaction() as connection:
            workflow_id = self._workflow_id_for_run(connection, attempt.run_id)
            connection.execute(
                "INSERT INTO attempts(attempt_id, run_id, workflow_id, phase_id, ordinal) VALUES (?, ?, ?, ?, ?)",
                (str(attempt.attempt_id), str(attempt.run_id), workflow_id, str(attempt.phase_id), attempt.ordinal),
            )

    def save_transition(self, transition: StateTransition) -> None:
        with self._write_transaction() as connection:
            self._insert_transition(connection, transition)

    def _insert_transition(self, connection: sqlite3.Connection, transition: StateTransition) -> None:
        workflow_id = self._workflow_id_for_run(connection, transition.run_id)
        if transition.workflow_id is not None and str(transition.workflow_id) != workflow_id:
            raise DurableStateError("transition workflow does not match its run")
        created_at = _timestamp(transition.created_at or self._now())
        connection.execute(
            "INSERT INTO transitions(transition_id, run_id, workflow_id, phase_id, previous_state, current_state, event, actor, created_at, gate_reference, evidence_reference, idempotency_key, state_version, protected_resource_key, fencing_token, idempotency_operation_kind, canonical_operation_identity, payload_fingerprint) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (str(transition.transition_id), str(transition.run_id), workflow_id, self._optional(transition.phase_id), transition.previous.value, transition.current.value, transition.event, transition.actor, created_at, transition.gate_reference, transition.evidence_reference, transition.idempotency_key, transition.state_version, transition.protected_resource_key, transition.fencing_token, transition.idempotency_operation_kind, transition.canonical_operation_identity, transition.payload_fingerprint),
        )

    def save_checkpoint(self, checkpoint: Checkpoint) -> None:
        with self._write_transaction() as connection:
            self._insert_checkpoint(connection, checkpoint)

    def _insert_checkpoint(self, connection: sqlite3.Connection, checkpoint: Checkpoint) -> None:
        workflow_id = self._workflow_id_for_run(connection, checkpoint.run_id)
        if checkpoint.workflow_id is not None and str(checkpoint.workflow_id) != workflow_id:
            raise DurableStateError("checkpoint workflow does not match its run")
        connection.execute(
            "INSERT INTO checkpoints(checkpoint_id, run_id, workflow_id, phase_id, operational_state, created_at, resume_data_json, state_version, base_reference, last_safe_transition_id, session_id, artifact_references_json, evidence_references_json, idempotency_key, idempotency_operation_kind, canonical_operation_identity, payload_fingerprint) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (str(checkpoint.checkpoint_id), str(checkpoint.run_id), workflow_id, self._optional(checkpoint.phase_id), checkpoint.operational_state.value, _timestamp(checkpoint.created_at), json.dumps(dict(checkpoint.resume_data), sort_keys=True, separators=(",", ":")), checkpoint.state_version, checkpoint.base_reference, self._optional(checkpoint.last_safe_transition_id), self._optional(checkpoint.session_id), json.dumps(checkpoint.artifact_references), json.dumps(checkpoint.evidence_references), checkpoint.idempotency_key, checkpoint.idempotency_operation_kind, checkpoint.canonical_operation_identity, checkpoint.payload_fingerprint),
        )

    def save_artifact(self, artifact: Artifact) -> None:
        self._insert("INSERT INTO artifacts(artifact_id, run_id, reference) VALUES (?, ?, ?)", (str(artifact.artifact_id), str(artifact.run_id), artifact.reference))

    def save_session(self, session: SessionReference) -> None:
        self._insert(
            "INSERT INTO agent_sessions(session_id, run_id, opaque_reference, adapter, role, attempt_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (str(session.session_id), str(session.run_id), session.opaque_reference, session.adapter, session.role, self._optional(session.attempt_id), session.status),
        )

    def save_usage(self, usage_id: UsageId, run: Run, usage: UsageRecord) -> None:
        measurements = {token.value: {"value": measurement.value, "provenance": measurement.provenance.value, "direct_evidence": measurement.direct_evidence} for token, measurement in usage.measurements.items()}
        self._insert("INSERT INTO usage_records VALUES (?, ?, ?, ?)", (str(usage_id), str(run.run_id), usage.adapter, json.dumps(measurements, sort_keys=True, separators=(",", ":"))))

    def save_failure(self, run: Run, failure: FailureRecord) -> None:
        self._insert("INSERT INTO failures VALUES (?, ?, ?, ?, ?, ?, ?)", (str(failure.failure_id), str(run.run_id), failure.category.value, failure.message, int(failure.retryable), failure.evidence_reference, _timestamp(self._now())))

    def save_human_decision(self, decision: HumanDecision) -> None:
        if not decision.unresolved:
            raise DurableStateError("human decisions must be created unresolved and resolved explicitly")
        requested_at = decision.requested_at or self._now()
        self._insert("INSERT INTO human_decisions VALUES (?, ?, ?, ?, ?, ?, ?, ?)", (str(decision.decision_id), str(decision.run_id), decision.decision, decision.reason, int(decision.unresolved), decision.resolution, _timestamp(requested_at), _timestamp(decision.resolved_at) if decision.resolved_at else None))

    def resolve_human_decision(self, decision_id: HumanDecisionId, resolution: str) -> HumanDecision:
        if not resolution.strip():
            raise ValueError("human decision resolution must be non-empty")
        with self._write_transaction() as connection:
            resolved_at = self._now()
            result = connection.execute(
                "UPDATE human_decisions SET unresolved = 0, resolution = ?, resolved_at = ? WHERE decision_id = ? AND unresolved = 1 AND resolution IS NULL AND resolved_at IS NULL",
                (resolution, _timestamp(resolved_at), str(decision_id)),
            )
            if result.rowcount != 1:
                raise DurableStateError("human decision is missing or already resolved")
            row = connection.execute("SELECT * FROM human_decisions WHERE decision_id = ?", (str(decision_id),)).fetchone()
        return self._decision_from_row(row)

    def acquire_lease(self, lease: Lease) -> bool:
        return self.acquire_durable_lease(lease) is not None

    def acquire_durable_lease(self, lease: Lease, duration: timedelta | None = None) -> Lease | None:
        duration = self._lease_duration if duration is None else duration
        if duration <= timedelta(0):
            raise ValueError("lease duration must be positive")
        if not lease.protected_resource_key:
            raise LeaseConflict("protected_resource_key is required")
        key = lease.protected_resource_key
        with self._write_transaction() as connection:
            now = self._now()
            active = connection.execute("SELECT * FROM leases WHERE protected_resource_key = ? AND released_at IS NULL", (key,)).fetchone()
            if active is not None:
                if _from_timestamp(active["expires_at"]) > now:
                    return None
                connection.execute("UPDATE leases SET released_at = ? WHERE lease_id = ?", (_timestamp(now), active["lease_id"]))
            token = int(connection.execute("SELECT COALESCE(MAX(fencing_token), 0) FROM leases WHERE protected_resource_key = ?", (key,)).fetchone()[0]) + 1
            expires_at = lease.expires_at or now + duration
            if _utc(expires_at) <= now:
                raise ValueError("new lease expiration must be in the future")
            connection.execute(
                "INSERT INTO leases VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (str(lease.lease_id), str(lease.run_id), lease.holder, key, lease.workspace_reference, lease.purpose, _timestamp(now), _timestamp(expires_at), token, 0, None),
            )
        return Lease(lease.lease_id, lease.run_id, lease.holder, key, lease.workspace_reference, lease.purpose, now, _utc(expires_at), token, 0, None)

    def inspect_lease(self, protected_resource_key: str) -> Lease | None:
        row = self._connection.execute("SELECT * FROM leases WHERE protected_resource_key = ? AND released_at IS NULL", (protected_resource_key,)).fetchone()
        if row is None or _from_timestamp(row["expires_at"]) <= self._now():
            return None
        return self._lease_from_row(row)

    def renew_lease(self, lease: Lease, expected_fencing_token: int) -> Lease:
        if not lease.protected_resource_key or lease.fencing_token is None:
            raise LeaseConflict("renewal requires protected resource and fencing token")
        with self._write_transaction() as connection:
            now = self._now()
            row = connection.execute("SELECT * FROM leases WHERE lease_id = ?", (str(lease.lease_id),)).fetchone()
            if (
                row is None
                or row["released_at"] is not None
                or row["run_id"] != str(lease.run_id)
                or row["protected_resource_key"] != lease.protected_resource_key
                or row["holder"] != lease.holder
                or row["fencing_token"] != expected_fencing_token
                or lease.fencing_token != expected_fencing_token
                or _from_timestamp(row["expires_at"]) <= now
            ):
                raise LeaseConflict("lease is not eligible for renewal")
            expires_at = now + self._lease_duration
            connection.execute("UPDATE leases SET expires_at = ?, version = version + 1 WHERE lease_id = ?", (_timestamp(expires_at), str(lease.lease_id)))
            row = connection.execute("SELECT * FROM leases WHERE lease_id = ?", (str(lease.lease_id),)).fetchone()
        return self._lease_from_row(row)

    def release_lease(self, lease: Lease) -> None:
        if not lease.protected_resource_key or lease.fencing_token is None:
            raise LeaseConflict("release requires protected resource and fencing token")
        with self._write_transaction() as connection:
            now = self._now()
            row = connection.execute("SELECT * FROM leases WHERE lease_id = ?", (str(lease.lease_id),)).fetchone()
            if (
                row is None
                or row["released_at"] is not None
                or row["run_id"] != str(lease.run_id)
                or row["protected_resource_key"] != lease.protected_resource_key
                or row["holder"] != lease.holder
            ):
                raise LeaseConflict("lease is not currently owned by this holder")
            if row["fencing_token"] != lease.fencing_token:
                raise FencingConflict("stale fencing token")
            connection.execute("UPDATE leases SET released_at = ?, version = version + 1 WHERE lease_id = ?", (_timestamp(now), str(lease.lease_id)))

    def save_transition_checkpoint(self, transition: StateTransition, checkpoint: Checkpoint, expected_version: int, *, protected_resource_key: str | None = None, fencing_token: int | None = None, idempotency_key: str | None = None, operation_kind: str = "transition_checkpoint", canonical_operation_identity: str | None = None, payload: Mapping[str, object] | None = None) -> bool:
        if transition.run_id != checkpoint.run_id:
            raise ValueError("transition and checkpoint must belong to the same run")
        if checkpoint.operational_state != transition.current:
            raise DurableStateError("checkpoint state must equal the transition's resulting run state")
        resulting_version = expected_version + 1
        if transition.state_version is not None and transition.state_version != resulting_version:
            raise DurableStateError("transition state_version must equal the resulting version")
        if checkpoint.state_version is not None and checkpoint.state_version != resulting_version:
            raise DurableStateError("checkpoint state_version must equal the resulting version")
        claimed_fencing = transition.protected_resource_key is not None or transition.fencing_token is not None
        supplied_fencing = protected_resource_key is not None or fencing_token is not None
        if claimed_fencing != supplied_fencing:
            raise FencingConflict("fencing evidence must be supplied and validated together")
        if supplied_fencing:
            if not protected_resource_key or fencing_token is None:
                raise FencingConflict("protected resource and fencing token are both required")
            if (
                transition.protected_resource_key != protected_resource_key
                or transition.fencing_token != fencing_token
            ):
                raise FencingConflict("transition fencing evidence does not match authorization")
        elif transition.protected_resource_key is not None or transition.fencing_token is not None:
            raise FencingConflict("non-fenced transitions cannot carry fencing evidence")
        if transition.idempotency_key not in (None, idempotency_key):
            raise IdempotencyConflict("transition idempotency key does not match operation")
        if checkpoint.idempotency_key not in (None, idempotency_key):
            raise IdempotencyConflict("checkpoint idempotency key does not match operation")
        if idempotency_key is None and any((
            transition.idempotency_operation_kind,
            transition.canonical_operation_identity,
            transition.payload_fingerprint,
            checkpoint.idempotency_operation_kind,
            checkpoint.canonical_operation_identity,
            checkpoint.payload_fingerprint,
        )):
            raise IdempotencyConflict("idempotency evidence requires an idempotency key")
        with self._write_transaction() as connection:
            current = connection.execute(
                "SELECT state, state_version FROM runs WHERE run_id = ?",
                (str(transition.run_id),),
            ).fetchone()
            if current is None:
                raise ConcurrencyConflict("transition references a missing durable run")
            if supplied_fencing:
                lease_row = connection.execute(
                    "SELECT * FROM leases WHERE protected_resource_key = ? "
                    "ORDER BY fencing_token DESC, lease_id DESC LIMIT 1",
                    (protected_resource_key,),
                ).fetchone()
                if lease_row is None or lease_row["released_at"] is not None:
                    raise FencingConflict("no active lease for protected resource")
                if lease_row["protected_resource_key"] != protected_resource_key:
                    raise FencingConflict("durable lease protected resource does not match mutation")
                if lease_row["run_id"] != str(transition.run_id):
                    raise FencingConflict("durable lease belongs to a different run")
                if lease_row["fencing_token"] != fencing_token:
                    raise FencingConflict("stale fencing token does not authorize mutation")
                if _from_timestamp(lease_row["expires_at"]) <= self._now():
                    raise FencingConflict("expired lease does not authorize mutation")
            identity = canonical_operation_identity or str(transition.transition_id)
            fingerprint = payload_fingerprint(payload if payload is not None else {})
            if idempotency_key is not None:
                for supplied, expected in (
                    (transition.idempotency_operation_kind, operation_kind),
                    (transition.canonical_operation_identity, identity),
                    (transition.payload_fingerprint, fingerprint),
                    (checkpoint.idempotency_operation_kind, operation_kind),
                    (checkpoint.canonical_operation_identity, identity),
                    (checkpoint.payload_fingerprint, fingerprint),
                ):
                    if supplied not in (None, expected):
                        raise IdempotencyConflict("supplied idempotency evidence does not match operation")
            if idempotency_key is not None:
                if not self._check_idempotent_replay(
                    connection, idempotency_key, operation_kind, identity, fingerprint,
                    transition, checkpoint, resulting_version,
                ):
                    return False
            if current["state"] != transition.previous.value:
                raise ConcurrencyConflict("transition previous state does not match durable run state")
            if current["state_version"] != expected_version:
                raise ConcurrencyConflict("stale run state_version")
            result = connection.execute("UPDATE runs SET state = ?, state_version = state_version + 1 WHERE run_id = ? AND state_version = ?", (transition.current.value, str(transition.run_id), expected_version))
            if result.rowcount != 1:
                raise ConcurrencyConflict("stale run state_version")
            durable_transition = StateTransition(transition.transition_id, transition.run_id, transition.previous, transition.current, transition.event, transition.workflow_id, transition.phase_id, transition.actor, transition.created_at, transition.gate_reference, transition.evidence_reference, idempotency_key, resulting_version, protected_resource_key, fencing_token, operation_kind if idempotency_key else None, identity if idempotency_key else None, fingerprint if idempotency_key else None)
            self._insert_transition(connection, durable_transition)
            stateful_checkpoint = Checkpoint(checkpoint.checkpoint_id, checkpoint.run_id, checkpoint.operational_state, checkpoint.created_at, checkpoint.resume_data, checkpoint.workflow_id, checkpoint.phase_id, resulting_version, checkpoint.base_reference, checkpoint.last_safe_transition_id, checkpoint.session_id, checkpoint.artifact_references, checkpoint.evidence_references, idempotency_key, operation_kind if idempotency_key else None, identity if idempotency_key else None, fingerprint if idempotency_key else None)
            self._insert_checkpoint(connection, stateful_checkpoint)
            if idempotency_key is not None:
                connection.execute(
                    "INSERT INTO idempotency_records(idempotency_key, operation_kind, canonical_operation_identity, payload_fingerprint, applied_at, run_id, transition_id, checkpoint_id, resulting_state_version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (idempotency_key, operation_kind, identity, fingerprint, _timestamp(self._now()), str(transition.run_id), str(transition.transition_id), str(checkpoint.checkpoint_id), resulting_version),
                )
            return True

    def _check_idempotent_replay(
        self,
        connection: sqlite3.Connection,
        key: str,
        kind: str,
        identity: str,
        fingerprint: str,
        transition: StateTransition,
        checkpoint: Checkpoint,
        resulting_version: int,
    ) -> bool:
        row = connection.execute("SELECT * FROM idempotency_records WHERE idempotency_key = ?", (key,)).fetchone()
        if row is None:
            return True
        if (
            row["operation_kind"], row["canonical_operation_identity"], row["payload_fingerprint"]
        ) != (kind, identity, fingerprint):
            raise IdempotencyConflict("idempotency key was reused for a different operation")
        if (
            row["run_id"], row["transition_id"], row["checkpoint_id"], row["resulting_state_version"]
        ) != (str(transition.run_id), str(transition.transition_id), str(checkpoint.checkpoint_id), resulting_version):
            raise DurableStateError("idempotency record does not describe this semantic effect")
        durable_transition = connection.execute(
            "SELECT previous_state, current_state, idempotency_key, state_version, idempotency_operation_kind, canonical_operation_identity, payload_fingerprint FROM transitions WHERE run_id = ? AND transition_id = ?",
            (str(transition.run_id), str(transition.transition_id)),
        ).fetchone()
        durable_checkpoint = connection.execute(
            "SELECT operational_state, state_version, idempotency_key, idempotency_operation_kind, canonical_operation_identity, payload_fingerprint FROM checkpoints WHERE run_id = ? AND checkpoint_id = ?",
            (str(checkpoint.run_id), str(checkpoint.checkpoint_id)),
        ).fetchone()
        expected_evidence = (key, kind, identity, fingerprint)
        if (
            durable_transition is None
            or tuple(durable_transition) != (transition.previous.value, transition.current.value, key, resulting_version, kind, identity, fingerprint)
            or durable_checkpoint is None
            or tuple(durable_checkpoint) != (transition.current.value, resulting_version, *expected_evidence)
        ):
            raise DurableStateError("idempotency record has no complete durable semantic effect")
        return False

    def load_workflow(self, workflow_id: WorkflowId) -> Workflow | None:
        row = self._connection.execute("SELECT workflow_id FROM workflows WHERE workflow_id = ?", (str(workflow_id),)).fetchone()
        if row is None:
            return None
        phases = self._connection.execute("SELECT phase_id, kind FROM phases WHERE workflow_id = ? ORDER BY phase_id", (str(workflow_id),)).fetchall()
        return Workflow(WorkflowId(row["workflow_id"]), tuple(WorkflowPhase(PhaseId(phase["phase_id"]), WorkPhaseKind(phase["kind"])) for phase in phases))

    def load_run(self, run_id: RunId) -> Run | None:
        return self._one(self._connection.execute("SELECT * FROM runs WHERE run_id = ?", (str(run_id),)).fetchone(), self._run_from_row)

    def list_attempts(self, run_id: RunId) -> tuple[Attempt, ...]:
        return tuple(Attempt(AttemptId(row["attempt_id"]), RunId(row["run_id"]), PhaseId(row["phase_id"]), row["ordinal"]) for row in self._connection.execute("SELECT * FROM attempts WHERE run_id = ? ORDER BY ordinal, attempt_id", (str(run_id),)))

    def list_transitions(self, run_id: RunId) -> tuple[StateTransition, ...]:
        return tuple(self._transition_from_row(row) for row in self._connection.execute("SELECT * FROM transitions WHERE run_id = ? ORDER BY created_at, transition_id", (str(run_id),)))

    def load_checkpoint(self, checkpoint_id: CheckpointId) -> Checkpoint | None:
        return self._one(self._connection.execute("SELECT * FROM checkpoints WHERE checkpoint_id = ?", (str(checkpoint_id),)).fetchone(), self._checkpoint_from_row)

    def latest_checkpoint(self, run_id: RunId) -> Checkpoint | None:
        return self._one(self._connection.execute("SELECT * FROM checkpoints WHERE run_id = ? ORDER BY created_at DESC, checkpoint_id DESC LIMIT 1", (str(run_id),)).fetchone(), self._checkpoint_from_row)

    def list_artifacts(self, run_id: RunId) -> tuple[Artifact, ...]:
        return tuple(Artifact(row["artifact_id"], row["run_id"], row["reference"]) for row in self._connection.execute("SELECT * FROM artifacts WHERE run_id = ? ORDER BY artifact_id", (str(run_id),)))

    def load_session(self, run_id: RunId) -> SessionReference | None:
        return self._one(self._connection.execute("SELECT * FROM agent_sessions WHERE run_id = ? ORDER BY session_id DESC LIMIT 1", (str(run_id),)).fetchone(), self._session_from_row)

    def load_usage(self, usage_id: UsageId) -> UsageRecord | None:
        row = self._connection.execute("SELECT * FROM usage_records WHERE usage_id = ?", (str(usage_id),)).fetchone()
        return None if row is None else self._usage_from_row(row)

    def list_usage(self, run_id: RunId) -> tuple[tuple[UsageId, UsageRecord], ...]:
        return tuple((UsageId(row["usage_id"]), self._usage_from_row(row)) for row in self._connection.execute("SELECT * FROM usage_records WHERE run_id = ? ORDER BY usage_id", (str(run_id),)))

    def list_failures(self, run_id: RunId) -> tuple[FailureRecord, ...]:
        return tuple(FailureRecord(row["failure_id"], FailureCategory(row["category"]), row["message"], bool(row["retryable"]), row["evidence_reference"]) for row in self._connection.execute("SELECT * FROM failures WHERE run_id = ? ORDER BY recorded_at, failure_id", (str(run_id),)))

    def load_lease(self, lease_id: LeaseId) -> Lease | None:
        return self._one(self._connection.execute("SELECT * FROM leases WHERE lease_id = ?", (str(lease_id),)).fetchone(), self._lease_from_row)

    def load_recovery_context(self, run_id: RunId) -> RunRecoveryContext | None:
        run = self.load_run(run_id)
        if run is None:
            return None
        workflow = self.load_workflow(run.workflow_id)
        if workflow is None:
            raise DurableStateError("run references a missing workflow")
        active = tuple(self._lease_from_row(row) for row in self._connection.execute("SELECT * FROM leases WHERE run_id = ? AND released_at IS NULL", (str(run_id),)) if _from_timestamp(row["expires_at"]) > self._now())
        resolution = LeaseResolution.NONE if not active else LeaseResolution.ONE_RELEVANT if len(active) == 1 else LeaseResolution.AMBIGUOUS
        return RunRecoveryContext(workflow, run, self.latest_checkpoint(run_id), self.load_session(run_id), resolution, active[0] if resolution is LeaseResolution.ONE_RELEVANT else None, self.list_failures(run_id), self.list_transitions(run_id), self.list_human_decisions(run_id))

    def load_human_decision(self, decision_id: HumanDecisionId) -> HumanDecision | None:
        return self._one(self._connection.execute("SELECT * FROM human_decisions WHERE decision_id = ?", (str(decision_id),)).fetchone(), self._decision_from_row)

    def list_human_decisions(self, run_id: RunId) -> tuple[HumanDecision, ...]:
        return tuple(self._decision_from_row(row) for row in self._connection.execute("SELECT * FROM human_decisions WHERE run_id = ? ORDER BY requested_at, decision_id", (str(run_id),)))

    def integrity_check(self) -> None:
        results = tuple(row[0] for row in self._connection.execute("PRAGMA integrity_check"))
        if results != ("ok",):
            raise DurableStateError(f"SQLite integrity check failed: {results!r}")

    def backup_to(self, destination_path: str) -> None:
        destination = Path(destination_path).expanduser().resolve()
        self._assert_external_database_path(destination)
        if destination == self._path:
            raise ValueError("backup destination must differ from source database")
        destination.parent.mkdir(parents=True, exist_ok=True)
        with sqlite3.connect(destination, isolation_level=None) as target:
            configure_connection(target, self._busy_timeout_ms)
            self._connection.backup(target)
            self._verify_connection(target)

    def _insert(self, statement: str, values: tuple[object, ...]) -> None:
        with self._write_transaction() as connection:
            connection.execute(statement, values)

    @staticmethod
    def _workflow_id_for_run(connection: sqlite3.Connection, run_id: RunId) -> str:
        row = connection.execute("SELECT workflow_id FROM runs WHERE run_id = ?", (str(run_id),)).fetchone()
        if row is None:
            raise DurableStateError("durable operation references a missing run")
        return str(row["workflow_id"])

    @staticmethod
    def _optional(value: object | None) -> object | None:
        return None if value is None else str(value)

    @staticmethod
    def _run_from_row(row: sqlite3.Row) -> Run:
        return Run(RunId(row["run_id"]), WorkflowId(row["workflow_id"]), OperationalState(row["state"]), row["state_version"])

    @staticmethod
    def _transition_from_row(row: sqlite3.Row) -> StateTransition:
        return StateTransition(TransitionId(row["transition_id"]), RunId(row["run_id"]), OperationalState(row["previous_state"]), OperationalState(row["current_state"]), row["event"], WorkflowId(row["workflow_id"]), PhaseId(row["phase_id"]) if row["phase_id"] else None, row["actor"], _from_timestamp(row["created_at"]), row["gate_reference"], row["evidence_reference"], row["idempotency_key"], row["state_version"], row["protected_resource_key"], row["fencing_token"], row["idempotency_operation_kind"], row["canonical_operation_identity"], row["payload_fingerprint"])

    @staticmethod
    def _checkpoint_from_row(row: sqlite3.Row) -> Checkpoint:
        return Checkpoint(CheckpointId(row["checkpoint_id"]), RunId(row["run_id"]), OperationalState(row["operational_state"]), _from_timestamp(row["created_at"]), json.loads(row["resume_data_json"]), WorkflowId(row["workflow_id"]), PhaseId(row["phase_id"]) if row["phase_id"] else None, row["state_version"], row["base_reference"], TransitionId(row["last_safe_transition_id"]) if row["last_safe_transition_id"] else None, SessionId(row["session_id"]) if row["session_id"] else None, tuple(json.loads(row["artifact_references_json"])), tuple(json.loads(row["evidence_references_json"])), row["idempotency_key"], row["idempotency_operation_kind"], row["canonical_operation_identity"], row["payload_fingerprint"])

    @staticmethod
    def _session_from_row(row: sqlite3.Row) -> SessionReference:
        return SessionReference(SessionId(row["session_id"]), RunId(row["run_id"]), row["opaque_reference"], row["adapter"], row["role"], AttemptId(row["attempt_id"]) if row["attempt_id"] else None, row["status"])

    @staticmethod
    def _lease_from_row(row: sqlite3.Row) -> Lease:
        return Lease(LeaseId(row["lease_id"]), RunId(row["run_id"]), row["holder"], row["protected_resource_key"], row["workspace_reference"], row["purpose"], _from_timestamp(row["issued_at"]), _from_timestamp(row["expires_at"]), row["fencing_token"], row["version"], _from_timestamp(row["released_at"]) if row["released_at"] else None)

    @staticmethod
    def _usage_from_row(row: sqlite3.Row) -> UsageRecord:
        raw = json.loads(row["measurements_json"])
        measurements = {TokenClass(name): TokenMeasurement(value=value["value"], provenance=UsageProvenance(value["provenance"]), direct_evidence=value["direct_evidence"]) for name, value in raw.items()}
        return UsageRecord(row["adapter"], measurements)

    @staticmethod
    def _decision_from_row(row: sqlite3.Row) -> HumanDecision:
        return HumanDecision(HumanDecisionId(row["decision_id"]), RunId(row["run_id"]), row["decision"], row["reason"], bool(row["unresolved"]), row["resolution"], _from_timestamp(row["requested_at"]) if row["requested_at"] else None, _from_timestamp(row["resolved_at"]) if row["resolved_at"] else None)
