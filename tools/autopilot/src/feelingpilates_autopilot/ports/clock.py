"""Clock contract for deterministic future application services."""

from abc import ABC, abstractmethod
from datetime import datetime


class Clock(ABC):
    @abstractmethod
    def now(self) -> datetime: ...

    @abstractmethod
    def monotonic_seconds(self) -> float: ...
