"""Narrow translation of the pinned SDK's notifications into safe evidence."""

from __future__ import annotations

from dataclasses import dataclass, field

from ...domain.models import TokenClass, UsageRecord
from .result_parser import ResultProtocolError, validate_protocol_session_id


def usage_from_sdk(value: object) -> UsageRecord | None:
    """Map only the SDK's current-turn ``usage.last`` counters.

    The SDK's cumulative ``usage.total`` and context window are deliberately
    ignored: neither is direct evidence for this requested turn.
    """
    if value is None:
        return None
    last = getattr(value, "last", None)
    if last is None:
        raise ResultProtocolError("SDK usage is malformed")
    observed: dict[TokenClass, int] = {}
    evidence: dict[TokenClass, str] = {}
    for token_class in TokenClass:
        amount = getattr(last, token_class.value, None)
        if amount is None:
            continue
        if type(amount) is not int or amount < 0:
            raise ResultProtocolError("SDK usage token value is invalid")
        observed[token_class] = amount
        evidence[token_class] = f"openai-codex.turn.usage.last.{token_class.value}"
    try:
        return UsageRecord.from_direct_evidence("openai-codex", observed, evidence)
    except ValueError as exc:
        raise ResultProtocolError("SDK usage violates the domain contract") from exc


def usage_from_values(value: object) -> UsageRecord | None:
    """Rebuild direct current-turn usage from the child process's primitive IPC.

    The child extracts only named integer counters.  This prevents provider
    object graphs, authentication internals, and arbitrary repr output from
    crossing the process boundary.
    """
    if not isinstance(value, dict):
        raise ResultProtocolError("SDK usage is malformed")
    if not value:
        return None
    observed: dict[TokenClass, int] = {}
    evidence: dict[TokenClass, str] = {}
    for name, amount in value.items():
        try:
            token_class = TokenClass(name)
        except ValueError as exc:
            raise ResultProtocolError("SDK usage is malformed") from exc
        if type(amount) is not int or amount < 0:
            raise ResultProtocolError("SDK usage token value is invalid")
        observed[token_class] = amount
        evidence[token_class] = f"openai-codex.turn.usage.last.{token_class.value}"
    try:
        return UsageRecord.from_direct_evidence("openai-codex", observed, evidence)
    except ValueError as exc:
        raise ResultProtocolError("SDK usage violates the domain contract") from exc


@dataclass(slots=True)
class SdkEventEvidence:
    """Bounded operational evidence; arbitrary payloads never escape here."""

    limit: int
    count: int = 0
    session_ids: set[str] = field(default_factory=set)
    diagnostics: list[str] = field(default_factory=list)
    error: ResultProtocolError | None = None
    session_failure: bool = False

    def observe(self, event: object) -> None:
        if self.error is not None:
            return
        self.count += 1
        if self.count > self.limit:
            self.error = ResultProtocolError("SDK event limit exceeded")
            return
        method = getattr(event, "method", None)
        payload = getattr(event, "payload", None)
        if not isinstance(method, str):
            self.error = ResultProtocolError("SDK event is malformed")
            return
        if method in {"thread/started", "thread/updated"}:
            thread = getattr(payload, "thread", payload)
            try:
                self.session_ids.add(validate_protocol_session_id(getattr(thread, "id", None)))
            except ResultProtocolError as exc:
                self.session_failure = True
                self.error = exc
        elif method in {"turn/started", "turn/completed", "thread/tokenUsage/updated", "item/completed"}:
            # These typed event kinds are consumed by the SDK operation. Their
            # semantic result is still taken only from the typed final result.
            return
        else:
            # Keep only a bounded type counter, never repr() or provider data.
            marker = f"unknown-sdk-event:{method[:64]}"
            if marker not in self.diagnostics and len(self.diagnostics) < 16:
                self.diagnostics.append(marker)

    def reconciled_session(self, expected: str) -> str:
        if self.error is not None:
            raise self.error
        if any(value != expected for value in self.session_ids):
            self.session_failure = True
            raise ResultProtocolError("SDK event session conflicts with typed thread identity")
        return expected
