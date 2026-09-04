"""Stable, adapter-neutral failure vocabulary."""

from dataclasses import dataclass
from enum import StrEnum

from .models import FailureId


class FailureCategory(StrEnum):
    SDK_TRANSPORT_FAILURE = "SDK_TRANSPORT_FAILURE"
    SDK_SESSION_FAILURE = "SDK_SESSION_FAILURE"
    CLI_PROCESS_FAILURE = "CLI_PROCESS_FAILURE"
    NETWORK_UNAVAILABLE = "NETWORK_UNAVAILABLE"
    QUOTA_EXHAUSTED = "QUOTA_EXHAUSTED"
    RESULT_CONTRACT_FAILURE = "RESULT_CONTRACT_FAILURE"
    PROCESS_CRASH = "PROCESS_CRASH"
    UNCERTAIN_WRITE = "UNCERTAIN_WRITE"
    GIT_BASELINE_DRIFT = "GIT_BASELINE_DRIFT"
    TEST_FAILURE = "TEST_FAILURE"
    SECURITY_STOP = "SECURITY_STOP"


@dataclass(frozen=True, slots=True)
class FailureRecord:
    failure_id: FailureId
    category: FailureCategory
    message: str
    retryable: bool = False
    evidence_reference: str | None = None
