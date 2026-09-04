"""Durable-state abstraction; R2 supplies no database implementation."""

from abc import ABC, abstractmethod
from typing import Mapping

from ..domain.failures import FailureRecord
from ..domain.models import (
    Artifact,
    Attempt,
    Checkpoint,
    CheckpointId,
    HumanDecision,
    HumanDecisionId,
    Lease,
    LeaseId,
    RunRecoveryContext,
    Run,
    RunId,
    SessionReference,
    StateTransition,
    UsageId,
    UsageRecord,
    Workflow,
    WorkflowId,
)


class StateStore(ABC):
    @abstractmethod
    def save_workflow(self, workflow: Workflow) -> None: ...

    @abstractmethod
    def save_run(self, run: Run) -> None: ...

    @abstractmethod
    def save_attempt(self, attempt: Attempt) -> None: ...

    @abstractmethod
    def save_transition(self, transition: StateTransition) -> None: ...

    @abstractmethod
    def save_checkpoint(self, checkpoint: Checkpoint) -> None: ...

    @abstractmethod
    def save_artifact(self, artifact: Artifact) -> None: ...

    @abstractmethod
    def save_session(self, session: SessionReference) -> None: ...

    @abstractmethod
    def save_usage(self, usage_id: UsageId, run: Run, usage: UsageRecord) -> None: ...

    @abstractmethod
    def save_failure(self, run: Run, failure: FailureRecord) -> None: ...

    @abstractmethod
    def acquire_lease(self, lease: Lease) -> bool: ...

    @abstractmethod
    def release_lease(self, lease: Lease) -> None: ...

    @abstractmethod
    def save_human_decision(self, decision: HumanDecision) -> None: ...

    @abstractmethod
    def resolve_human_decision(self, decision_id: HumanDecisionId, resolution: str) -> HumanDecision: ...

    @abstractmethod
    def load_workflow(self, workflow_id: WorkflowId) -> Workflow | None: ...

    @abstractmethod
    def load_run(self, run_id: RunId) -> Run | None: ...

    @abstractmethod
    def list_attempts(self, run_id: RunId) -> tuple[Attempt, ...]: ...

    @abstractmethod
    def list_transitions(self, run_id: RunId) -> tuple[StateTransition, ...]: ...

    @abstractmethod
    def load_checkpoint(self, checkpoint_id: CheckpointId) -> Checkpoint | None: ...

    @abstractmethod
    def latest_checkpoint(self, run_id: RunId) -> Checkpoint | None: ...

    @abstractmethod
    def list_artifacts(self, run_id: RunId) -> tuple[Artifact, ...]: ...

    @abstractmethod
    def load_session(self, run_id: RunId) -> SessionReference | None: ...

    @abstractmethod
    def load_usage(self, usage_id: UsageId) -> UsageRecord | None: ...

    @abstractmethod
    def list_usage(self, run_id: RunId) -> tuple[tuple[UsageId, UsageRecord], ...]: ...

    @abstractmethod
    def list_failures(self, run_id: RunId) -> tuple[FailureRecord, ...]: ...

    @abstractmethod
    def load_lease(self, lease_id: LeaseId) -> Lease | None: ...

    @abstractmethod
    def load_recovery_context(self, run_id: RunId) -> RunRecoveryContext | None:
        """Load one fail-closed durable recovery view from only a run identifier."""

    @abstractmethod
    def load_human_decision(self, decision_id: HumanDecisionId) -> HumanDecision | None: ...

    @abstractmethod
    def list_human_decisions(self, run_id: RunId) -> tuple[HumanDecision, ...]: ...

    @abstractmethod
    def update_run(self, run: Run, expected_version: int) -> Run: ...

    @abstractmethod
    def save_transition_checkpoint(
        self,
        transition: StateTransition,
        checkpoint: Checkpoint,
        expected_version: int,
        *,
        protected_resource_key: str | None = None,
        fencing_token: int | None = None,
        idempotency_key: str | None = None,
        operation_kind: str = "transition_checkpoint",
        canonical_operation_identity: str | None = None,
        payload: Mapping[str, object] | None = None,
    ) -> bool: ...

    @abstractmethod
    def renew_lease(self, lease: Lease, expected_fencing_token: int) -> Lease: ...

    @abstractmethod
    def inspect_lease(self, protected_resource_key: str) -> Lease | None: ...

    @abstractmethod
    def integrity_check(self) -> None: ...

    @abstractmethod
    def backup_to(self, destination_path: str) -> None: ...
