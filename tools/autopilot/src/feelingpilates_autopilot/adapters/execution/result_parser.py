"""Strict parser for the verified Codex JSONL event profile."""

from __future__ import annotations

import json
from dataclasses import dataclass
from typing import Any

from ...domain.models import (
    AgentExecutionStatus, AgentResult, Findings, GateResult, RunId, TestEvidence,
    TestOutcome, TokenClass, UsageRecord, WorkflowId,
)
from ...ports.agent_executor import ExecutionRequest


class ResultProtocolError(ValueError):
    pass


def validate_protocol_session_id(value: object) -> str:
    """Validate, rather than normalize, the session identity emitted by JSONL."""
    if not isinstance(value, str) or not value or value != value.strip() or len(value) > 128:
        raise ResultProtocolError("recognized session event is malformed")
    if not value[0].isalnum() or any(not (char.isalnum() or char in "._:-") for char in value):
        raise ResultProtocolError("recognized session event is malformed")
    return value


@dataclass(slots=True)
class SessionEvidence:
    """Incrementally observed state; malformed stdout protocol bytes fail closed."""
    sessions: set[str]
    error: ResultProtocolError | None = None

    def __init__(self) -> None:
        self.sessions = set()
        self.error = None

    def observe_line(self, line: bytes) -> None:
        if self.error is not None or not line.strip():
            return
        try:
            event = json.loads(line.decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError):
            self.error = ResultProtocolError("malformed JSONL event")
            return
        if not isinstance(event, dict):
            self.error = ResultProtocolError("CLI event must be an object")
            return
        if event.get("type") == "thread.started":
            try:
                self.sessions.add(validate_protocol_session_id(event.get("thread_id")))
            except ResultProtocolError as exc:
                self.error = exc

    def session_id(self) -> str | None:
        if self.error is not None or len(self.sessions) != 1:
            return None
        return next(iter(self.sessions))


@dataclass(frozen=True, slots=True)
class ParsedTerminal:
    result: AgentResult
    session_id: str | None
    usage: UsageRecord | None


def _as_object(value: object, label: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise ResultProtocolError(f"{label} must be an object")
    return value


def _strict_usage(value: object, *, adapter: str) -> UsageRecord:
    record = _as_object(value, "usage_record")
    if set(record) != {"schema_version", "adapter", "measurements"} or record["schema_version"] != "r2":
        raise ResultProtocolError("usage_record has an unsupported shape")
    if record["adapter"] != adapter:
        raise ResultProtocolError("usage_record adapter does not match CLI provenance")
    measurements = _as_object(record["measurements"], "usage measurements")
    expected = {item.value for item in TokenClass}
    if set(measurements) != expected:
        raise ResultProtocolError("usage_record measurements are incomplete or unknown")
    observed: dict[TokenClass, int] = {}
    evidence: dict[TokenClass, str] = {}
    for token_class in TokenClass:
        measurement = _as_object(measurements[token_class.value], token_class.value)
        if set(measurement) != {"value", "provenance", "direct_evidence"}:
            raise ResultProtocolError("usage measurement has unknown fields")
        provenance, amount, direct = measurement["provenance"], measurement["value"], measurement["direct_evidence"]
        if provenance == "OBSERVED":
            if type(amount) is not int or amount < 0 or not isinstance(direct, str) or not direct:
                raise ResultProtocolError("observed usage measurement is invalid")
            observed[token_class] = amount
            evidence[token_class] = direct
        elif provenance == "UNAVAILABLE":
            if amount is not None or direct is not None:
                raise ResultProtocolError("unavailable usage measurement is invalid")
        else:
            raise ResultProtocolError("usage provenance is invalid")
    try:
        return UsageRecord.from_direct_evidence(adapter, observed, evidence)
    except (TypeError, ValueError, KeyError) as exc:
        raise ResultProtocolError("usage_record violates the domain contract") from exc


def _runtime_usage(value: object) -> UsageRecord:
    usage = _as_object(value, "turn usage")
    observed: dict[TokenClass, int] = {}
    evidence: dict[TokenClass, str] = {}
    for token_class in TokenClass:
        if token_class.value in usage:
            amount = usage[token_class.value]
            if type(amount) is not int or amount < 0:
                raise ResultProtocolError("turn usage token value is invalid")
            observed[token_class] = amount
            evidence[token_class] = f"cli.turn.completed.usage.{token_class.value}"
    try:
        return UsageRecord.from_direct_evidence("codex-cli", observed, evidence)
    except (TypeError, ValueError, KeyError) as exc:
        raise ResultProtocolError("turn usage violates the domain contract") from exc


def parse_agent_result(
    value: object, request: ExecutionRequest, runtime_usage: UsageRecord | None, *, adapter: str
) -> AgentResult:
    """Strictly translate one complete structured result from a trusted transport.

    Transport adapters supply only their explicit provenance and directly
    observed current-turn usage.  This deliberately shares R4's semantic
    contract without allowing provider event shapes into the domain parser.
    """
    result = _as_object(value, "agent result")
    allowed = {"schema_version", "workflow_id", "run_id", "role", "status", "gate", "gate_result", "summary", "session_reference", "usage_record", "findings", "test_evidence", "changed_paths", "recommendation", "requires_human_decision", "p1_correctable", "correction_artifact", "artifacts"}
    required = allowed - {"session_reference", "usage_record", "artifacts"}
    if set(result) - allowed or not required <= set(result):
        raise ResultProtocolError("agent result has missing or unknown fields")
    if result.get("schema_version") != "r2":
        raise ResultProtocolError("agent result schema version is invalid")
    if result.get("workflow_id") != request.workflow_id or result.get("run_id") != request.run_id:
        raise ResultProtocolError("agent result identity does not match request")
    if result.get("role") != request.role or result.get("gate") != request.gate:
        raise ResultProtocolError("agent result role or gate does not match request")
    try:
        status, gate_result = AgentExecutionStatus(result["status"]), GateResult(result["gate_result"])
    except (KeyError, TypeError, ValueError) as exc:
        raise ResultProtocolError("agent result status or gate result is invalid") from exc
    if not isinstance(result["summary"], str) or not result["summary"].strip() or not isinstance(result["recommendation"], str) or not result["recommendation"].strip():
        raise ResultProtocolError("agent result summary or recommendation is invalid")
    findings = _as_object(result["findings"], "findings")
    if set(findings) != {"p0", "p1", "p2"} or any(not isinstance(findings[key], list) or any(not isinstance(item, str) or not item for item in findings[key]) for key in findings):
        raise ResultProtocolError("findings are invalid")
    if not isinstance(result["test_evidence"], list):
        raise ResultProtocolError("test evidence is invalid")
    test_evidence: list[TestEvidence] = []
    for item in result["test_evidence"]:
        item = _as_object(item, "test evidence")
        if set(item) != {"name", "outcome", "evidence"}:
            raise ResultProtocolError("test evidence has unknown fields")
        if not isinstance(item["name"], str) or not isinstance(item["evidence"], str):
            raise ResultProtocolError("test evidence values must be strings")
        try:
            test_evidence.append(TestEvidence(item["name"], TestOutcome(item["outcome"]), item["evidence"]))
        except (TypeError, ValueError, KeyError) as exc:
            raise ResultProtocolError("test evidence violates the domain contract") from exc
    for name in ("changed_paths", "artifacts"):
        if name in result and (not isinstance(result[name], list) or any(not isinstance(item, str) or not item for item in result[name])):
            raise ResultProtocolError(f"{name} is invalid")
    if type(result["requires_human_decision"]) is not bool or type(result["p1_correctable"]) is not bool:
        raise ResultProtocolError("agent result boolean field is invalid")
    correction = result["correction_artifact"]
    if (result["p1_correctable"] and (not isinstance(correction, str) or not correction)) or (not result["p1_correctable"] and correction is not None):
        raise ResultProtocolError("correction artifact is invalid")
    embedded = None
    if "usage_record" in result and result["usage_record"] is not None:
        embedded = _strict_usage(result["usage_record"], adapter=adapter)
        if runtime_usage is None or embedded.measurements != runtime_usage.measurements:
            raise ResultProtocolError("embedded usage_record contradicts runtime usage")
    session = result.get("session_reference")
    if session is not None:
        try:
            session = validate_protocol_session_id(session)
        except ResultProtocolError as exc:
            raise ResultProtocolError("agent result session reference is invalid") from exc
    try:
        return AgentResult(workflow_id=WorkflowId(result["workflow_id"]), run_id=RunId(result["run_id"]), role=result["role"], status=status, gate=result["gate"], gate_result=gate_result, summary=result["summary"], findings=Findings(tuple(findings["p0"]), tuple(findings["p1"]), tuple(findings["p2"])), test_evidence=tuple(test_evidence), changed_paths=tuple(result["changed_paths"]), recommendation=result["recommendation"], requires_human_decision=result["requires_human_decision"], p1_correctable=result["p1_correctable"], correction_artifact=correction, session_reference=session, usage_record=embedded, artifacts=tuple(result.get("artifacts", ())))
    except (TypeError, ValueError, KeyError) as exc:
        raise ResultProtocolError("agent result violates the domain contract") from exc


def parse_terminal(stdout: str, request: ExecutionRequest) -> ParsedTerminal:
    """Consume only valid JSONL events from the stdout machine protocol channel."""
    evidence, candidate_texts, runtime_usage = SessionEvidence(), [], None
    for line in stdout.splitlines():
        evidence.observe_line(line.encode("utf-8"))
        if evidence.error is not None:
            raise evidence.error
        if not line.strip():
            continue
        event = json.loads(line)
        assert isinstance(event, dict)
        if event.get("type") == "item.completed":
            item = _as_object(event.get("item"), "item.completed item")
            if item.get("type") == "agent_message":
                if not isinstance(item.get("text"), str):
                    raise ResultProtocolError("terminal agent message is malformed")
                candidate_texts.append(item["text"])
        elif event.get("type") == "turn.completed" and "usage" in event:
            if runtime_usage is not None:
                raise ResultProtocolError("multiple terminal usage events are ambiguous")
            runtime_usage = _runtime_usage(event["usage"])
    if len(evidence.sessions) > 1:
        raise ResultProtocolError("conflicting session events")
    if len(candidate_texts) != 1:
        raise ResultProtocolError("exactly one terminal structured result is required")
    try:
        candidate = json.loads(candidate_texts[0])
    except (TypeError, json.JSONDecodeError) as exc:
        raise ResultProtocolError("terminal structured result is malformed") from exc
    result = parse_agent_result(candidate, request, runtime_usage, adapter="codex-cli")
    session_id = evidence.session_id()
    if result.session_reference != session_id and (result.session_reference is not None or session_id is not None):
        raise ResultProtocolError("agent result session reference conflicts with event evidence")
    return ParsedTerminal(result=result, session_id=session_id, usage=runtime_usage)


def parse_structured_result(
    text: object, request: ExecutionRequest, *, adapter: str, runtime_usage: UsageRecord | None
) -> AgentResult:
    """Decode exactly one bounded SDK final-response object; never repair JSON."""
    if not isinstance(text, str):
        raise ResultProtocolError("terminal structured result is missing")
    try:
        candidate = json.loads(text)
    except json.JSONDecodeError as exc:
        raise ResultProtocolError("terminal structured result is malformed") from exc
    return parse_agent_result(candidate, request, runtime_usage, adapter=adapter)


def extract_session(stdout: str) -> str | None:
    evidence = SessionEvidence()
    for line in stdout.splitlines():
        evidence.observe_line(line.encode("utf-8"))
        if evidence.error is not None:
            return None
    return evidence.session_id()
