"""Execution boundary shared conceptually by future SDK and CLI adapters."""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import StrEnum

from ..domain.failures import FailureRecord
from ..domain.models import (
    AttemptId,
    AgentResult,
    Artifact,
    Checkpoint,
    ExecutionId,
    RunId,
    SessionReference,
    UsageRecord,
    WorkflowId,
)


@dataclass(frozen=True, slots=True)
class ExecutorCapabilities:
    supports_resume: bool
    supports_interrupt: bool
    emits_usage: bool


class SandboxMode(StrEnum):
    """The only filesystem authorities an execution request may express."""

    READ_ONLY = "READ_ONLY"
    WORKSPACE_WRITE = "WORKSPACE_WRITE"


@dataclass(frozen=True, slots=True)
class ExecutionRequest:
    run_id: RunId
    instructions: str
    workflow_id: WorkflowId | None = None
    role: str | None = None
    gate: str | None = None
    working_directory: str | None = None
    sandbox: SandboxMode | None = None
    model: str | None = None
    reasoning_effort: str | None = None
    timeout_seconds: float | None = None
    execution_id: ExecutionId | None = None
    attempt_id: AttemptId | None = None


@dataclass(frozen=True, slots=True)
class ExecutionHandle:
    execution_id: ExecutionId
    run_id: RunId
    session: SessionReference | None
    # This is caller-persistable operational context, not adapter-private state.
    # It permits a freshly constructed adapter to construct the same bounded
    # resume argv after an orchestrator restart.
    resume_context: ExecutionRequest | None = None


class ExecutionStatus(StrEnum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
    INTERRUPTED = "INTERRUPTED"
    UNKNOWN = "UNKNOWN"


@dataclass(frozen=True, slots=True)
class ExecutionObservation:
    """Transport-neutral execution state with semantic terminal results."""

    execution_id: ExecutionId
    run_id: RunId
    session: SessionReference | None
    status: ExecutionStatus
    agent_result: AgentResult | None = None
    failure: FailureRecord | None = None
    usage_record: UsageRecord | None = None
    artifacts: tuple[Artifact, ...] = ()
    raw_output: str | None = None
    raw_stdout: str | None = None
    raw_stderr: str | None = None
    stdout_truncated: bool = False
    stderr_truncated: bool = False
    output_limit_exceeded: bool = False
    exit_code: int | None = None
    termination_signal: int | None = None
    timed_out: bool = False
    cancelled: bool = False

    def __post_init__(self) -> None:
        if self.status is ExecutionStatus.SUCCEEDED:
            if self.agent_result is None or self.failure is not None:
                raise ValueError("a successful terminal observation requires AgentResult only")
        elif self.status in (ExecutionStatus.FAILED, ExecutionStatus.INTERRUPTED):
            if self.failure is None or self.agent_result is not None:
                raise ValueError("a failed or interrupted terminal observation requires normalized FailureRecord only")
        elif self.agent_result is not None or self.failure is not None:
            raise ValueError("non-terminal observations cannot claim a semantic terminal result")
        if self.agent_result is not None and self.agent_result.run_id != self.run_id:
            raise ValueError("AgentResult must belong to the observed run")
        if self.usage_record is not None and not isinstance(self.usage_record, UsageRecord):
            raise ValueError("usage_record must be a provenance-checked UsageRecord")


# Kept as a compatibility name for the previously published R2 port surface.
ExecutionResult = ExecutionObservation


class AgentExecutor(ABC):
    @abstractmethod
    def capabilities(self) -> ExecutorCapabilities: ...

    @abstractmethod
    def start(self, request: ExecutionRequest) -> ExecutionHandle: ...

    @abstractmethod
    def resume(self, handle: ExecutionHandle, checkpoint: Checkpoint) -> ExecutionHandle: ...

    @abstractmethod
    def interrupt(self, handle: ExecutionHandle) -> None: ...

    @abstractmethod
    def get_result(self, handle: ExecutionHandle) -> ExecutionObservation:
        """Observe pending, running, or terminal execution without provider-native types."""
