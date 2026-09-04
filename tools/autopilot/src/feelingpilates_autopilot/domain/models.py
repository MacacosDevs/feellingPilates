"""Immutable language-neutral values shared by future adapters and application code."""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from enum import StrEnum
from typing import TYPE_CHECKING, Mapping, NewType

from .states import OperationalState, WorkPhaseKind

if TYPE_CHECKING:
    from .failures import FailureRecord

WorkflowId = NewType("WorkflowId", str)
RunId = NewType("RunId", str)
PhaseId = NewType("PhaseId", str)
AttemptId = NewType("AttemptId", str)
TransitionId = NewType("TransitionId", str)
CheckpointId = NewType("CheckpointId", str)
ArtifactId = NewType("ArtifactId", str)
SessionId = NewType("SessionId", str)
UsageId = NewType("UsageId", str)
FailureId = NewType("FailureId", str)
LeaseId = NewType("LeaseId", str)
HumanDecisionId = NewType("HumanDecisionId", str)
ExecutionId = NewType("ExecutionId", str)


class UsageProvenance(StrEnum):
    OBSERVED = "OBSERVED"
    UNAVAILABLE = "UNAVAILABLE"
    DERIVED = "DERIVED"
    ESTIMATED = "ESTIMATED"


class TokenClass(StrEnum):
    INPUT = "input_tokens"
    CACHED_INPUT = "cached_input_tokens"
    OUTPUT = "output_tokens"
    REASONING_OUTPUT = "reasoning_output_tokens"
    TOTAL = "total_tokens"


class AgentExecutionStatus(StrEnum):
    """Semantic outcome reported by an agent, separate from gate evaluation."""

    SUCCEEDED = "SUCCEEDED"
    FAILED = "FAILED"
    INTERRUPTED = "INTERRUPTED"
    UNKNOWN = "UNKNOWN"


class GateResult(StrEnum):
    """Canonical gate applicability/lifecycle vocabulary from GATES.md."""

    NOT_APPLICABLE = "NOT_APPLICABLE"
    PENDING = "PENDING"
    PASS = "PASS"
    FAIL = "FAIL"
    BLOCKED = "BLOCKED"
    STOP = "STOP"


class TestOutcome(StrEnum):
    PASS = "PASS"
    FAIL = "FAIL"
    BLOCKED = "BLOCKED"
    NOT_RUN = "NOT_RUN"


@dataclass(frozen=True, slots=True)
class TokenMeasurement:
    """One exact token class; null is unavailable rather than zero."""

    value: int | None
    provenance: UsageProvenance
    direct_evidence: str | None = None

    def __post_init__(self) -> None:
        if self.provenance is UsageProvenance.OBSERVED:
            if self.value is None or self.value < 0 or not self.direct_evidence:
                raise ValueError("OBSERVED token values need non-negative value and direct evidence")
        elif self.provenance is UsageProvenance.UNAVAILABLE:
            if self.value is not None or self.direct_evidence is not None:
                raise ValueError("UNAVAILABLE token values must remain null without evidence")
        else:
            raise ValueError("R2 observed token fields cannot hold derived or estimated values")

    @classmethod
    def unavailable(cls) -> "TokenMeasurement":
        return cls(value=None, provenance=UsageProvenance.UNAVAILABLE)


@dataclass(frozen=True, slots=True)
class UsageRecord:
    """Normalized observed telemetry without inferred component values."""

    adapter: str
    measurements: Mapping[TokenClass, TokenMeasurement]

    def __post_init__(self) -> None:
        if not self.adapter:
            raise ValueError("adapter is required")
        missing = set(TokenClass) - set(self.measurements)
        if missing:
            raise ValueError("every token class must be explicit, including unavailable classes")

    @classmethod
    def from_direct_evidence(
        cls, adapter: str, observed: Mapping[TokenClass, int], evidence: Mapping[TokenClass, str]
    ) -> "UsageRecord":
        unexpected_evidence = set(evidence) - set(observed)
        if unexpected_evidence:
            raise ValueError("direct evidence cannot describe an unobserved token value")
        if set(observed) != set(evidence):
            raise ValueError("every observed token value requires direct evidence for that exact class")
        measurements = {
            token_class: TokenMeasurement(
                value=observed[token_class],
                provenance=UsageProvenance.OBSERVED,
                direct_evidence=evidence[token_class],
            )
            if token_class in observed
            else TokenMeasurement.unavailable()
            for token_class in TokenClass
        }
        return cls(adapter=adapter, measurements=measurements)


@dataclass(frozen=True, slots=True)
class Findings:
    """Severity-classified findings required by the structured result protocol."""

    p0: tuple[str, ...]
    p1: tuple[str, ...]
    p2: tuple[str, ...]

    def __post_init__(self) -> None:
        for finding in (*self.p0, *self.p1, *self.p2):
            if not finding:
                raise ValueError("findings must be non-empty strings")


@dataclass(frozen=True, slots=True)
class TestEvidence:
    name: str
    outcome: TestOutcome
    evidence: str

    def __post_init__(self) -> None:
        if not self.name or not self.evidence:
            raise ValueError("test evidence needs a name and evidence reference")


@dataclass(frozen=True, slots=True)
class AgentResult:
    """The R2 semantic result protocol, independent from execution transport."""

    workflow_id: WorkflowId
    run_id: RunId
    role: str
    status: AgentExecutionStatus
    gate: str
    gate_result: GateResult
    summary: str
    findings: Findings
    test_evidence: tuple[TestEvidence, ...]
    changed_paths: tuple[str, ...]
    recommendation: str
    requires_human_decision: bool
    p1_correctable: bool
    correction_artifact: str | None
    session_reference: str | None = None
    usage_record: UsageRecord | None = None
    artifacts: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        if not self.workflow_id or not self.run_id or not self.role or not self.gate:
            raise ValueError("workflow, run, role, and gate identity are required")
        if not isinstance(self.status, AgentExecutionStatus):
            raise ValueError("status must use the canonical agent execution vocabulary")
        if not isinstance(self.gate_result, GateResult):
            raise ValueError("gate_result must use the canonical gate lifecycle vocabulary")
        if not isinstance(self.findings, Findings):
            raise ValueError("findings must preserve P0/P1/P2 classification")
        if any(not isinstance(item, TestEvidence) for item in self.test_evidence):
            raise ValueError("test evidence must use the structured contract")
        if not self.summary.strip():
            raise ValueError("summary must be non-empty")
        if not self.recommendation.strip():
            raise ValueError("recommendation must be non-empty")
        if any(not path for path in self.changed_paths):
            raise ValueError("changed paths must be non-empty strings")
        if any(not artifact for artifact in self.artifacts):
            raise ValueError("artifact references must be non-empty strings")
        if self.p1_correctable and not self.correction_artifact:
            raise ValueError("a correctable P1 requires a correction artifact")
        if not self.p1_correctable and self.correction_artifact is not None:
            raise ValueError("a correction artifact is only valid for a correctable P1")
        if self.usage_record is not None and not isinstance(self.usage_record, UsageRecord):
            raise ValueError("usage_record must be a provenance-checked UsageRecord")


@dataclass(frozen=True, slots=True)
class WorkflowPhase:
    phase_id: PhaseId
    kind: WorkPhaseKind


@dataclass(frozen=True, slots=True)
class Workflow:
    workflow_id: WorkflowId
    phases: tuple[WorkflowPhase, ...]


@dataclass(frozen=True, slots=True)
class Run:
    run_id: RunId
    workflow_id: WorkflowId
    state: OperationalState


@dataclass(frozen=True, slots=True)
class Attempt:
    attempt_id: AttemptId
    run_id: RunId
    phase_id: PhaseId
    ordinal: int


@dataclass(frozen=True, slots=True)
class StateTransition:
    transition_id: TransitionId
    run_id: RunId
    previous: OperationalState
    current: OperationalState


@dataclass(frozen=True, slots=True)
class Checkpoint:
    checkpoint_id: CheckpointId
    run_id: RunId
    operational_state: OperationalState
    created_at: datetime
    resume_data: Mapping[str, str] = field(default_factory=dict)


@dataclass(frozen=True, slots=True)
class Artifact:
    artifact_id: ArtifactId
    run_id: RunId
    reference: str


@dataclass(frozen=True, slots=True)
class SessionReference:
    session_id: SessionId
    run_id: RunId
    opaque_reference: str


@dataclass(frozen=True, slots=True)
class Lease:
    lease_id: LeaseId
    run_id: RunId
    holder: str


class LeaseResolution(StrEnum):
    """Fail-closed lease discovery state for recovery by run identity."""

    NONE = "NONE"
    ONE_RELEVANT = "ONE_RELEVANT"
    AMBIGUOUS = "AMBIGUOUS"


@dataclass(frozen=True, slots=True)
class RunRecoveryContext:
    """Durable recovery view obtained solely from a run identifier."""

    workflow: Workflow
    run: Run
    latest_checkpoint: Checkpoint | None
    session: SessionReference | None
    lease_resolution: LeaseResolution
    lease: Lease | None
    failures: tuple[FailureRecord, ...]
    transitions: tuple[StateTransition, ...]
    human_decisions: tuple[HumanDecision, ...]

    def __post_init__(self) -> None:
        if self.workflow.workflow_id != self.run.workflow_id:
            raise ValueError("recovery workflow must belong to the recovered run")
        if self.latest_checkpoint is not None and self.latest_checkpoint.run_id != self.run.run_id:
            raise ValueError("recovery checkpoint must belong to the recovered run")
        if self.session is not None and self.session.run_id != self.run.run_id:
            raise ValueError("recovery session must belong to the recovered run")
        if self.lease_resolution is LeaseResolution.ONE_RELEVANT:
            if self.lease is None or self.lease.run_id != self.run.run_id:
                raise ValueError("one relevant lease requires the run's lease")
        elif self.lease is not None:
            raise ValueError("only an unambiguous relevant lease may be returned")


@dataclass(frozen=True, slots=True)
class HumanDecision:
    decision_id: HumanDecisionId
    run_id: RunId
    decision: str
