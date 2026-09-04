"""Domain events only describe facts; they do not perform transitions."""

from dataclasses import dataclass
from datetime import datetime

from .states import OperationalState


@dataclass(frozen=True, slots=True)
class DomainEvent:
    event_id: str
    occurred_at: datetime


@dataclass(frozen=True, slots=True)
class OperationalStateChanged(DomainEvent):
    run_id: str
    previous: OperationalState
    current: OperationalState
    reason: str | None = None
