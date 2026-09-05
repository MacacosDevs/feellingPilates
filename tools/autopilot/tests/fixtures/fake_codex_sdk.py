"""Deterministic stand-in for the public 0.147.0 streaming path."""
from __future__ import annotations

import json
import multiprocessing
import threading
import time
from dataclasses import dataclass
from types import SimpleNamespace


@dataclass
class FakeUsageLast:
    input_tokens: int | None = None
    cached_input_tokens: int | None = None
    output_tokens: int | None = None
    reasoning_output_tokens: int | None = None
    total_tokens: int | None = None

@dataclass
class FakeUsage:
    last: FakeUsageLast

@dataclass
class FakeEvent:
    method: str
    payload: object | None = None

@dataclass
class FakeThreadPayload:
    id: str

@dataclass
class FakeItem:
    text: str | None
    phase: str | None = "final_answer"

@dataclass
class FakeItemPayload:
    item: FakeItem

@dataclass
class FakeUsagePayload:
    token_usage: FakeUsage

@dataclass
class FakeCompletedTurn:
    status: str = "completed"
    error: object | None = None

@dataclass
class FakeCompletedPayload:
    turn: FakeCompletedTurn


class CapabilitySandbox:
    read_only = "read-only"
    workspace_write = "workspace-write"


class CapabilityCodex:
    def close(self) -> None: pass
    def thread_start(self, *, cwd=None, model=None, sandbox=None): pass
    def thread_resume(self, _id, *, cwd=None, model=None, sandbox=None): pass


class CapabilityThread:
    id: str
    def turn(self, input, *, effort=None, output_schema=None): pass


class CapabilityTurnHandle:
    def stream(self): pass
    def interrupt(self): pass


class CapabilityTurnResult:
    __dataclass_fields__ = {name: object() for name in ("status", "error", "final_response", "usage")}


class CapabilityNotification:
    method: str
    payload: object


class CapabilityThreadStartedNotification:
    thread: object


class CapabilityItemCompletedNotification:
    item: object


class CapabilityThreadItem:
    root: object


class CapabilityAgentMessageThreadItem:
    text: str
    phase: object


class CapabilityTurnCompletedNotification:
    turn: object


class CapabilityTurn:
    status: object
    error: object


class CapabilityThreadTokenUsageUpdatedNotification:
    token_usage: object


class CapabilityThreadTokenUsage:
    last: object


class CapabilityTokenUsageBreakdown:
    input_tokens: int
    cached_input_tokens: int
    output_tokens: int
    reasoning_output_tokens: int
    total_tokens: int


def capability_sdk_module() -> SimpleNamespace:
    """Fake the public module layout consumed by the R5 capability probe."""
    generated = SimpleNamespace(
        ThreadStartedNotification=CapabilityThreadStartedNotification,
        ItemCompletedNotification=CapabilityItemCompletedNotification,
        ThreadItem=CapabilityThreadItem,
        AgentMessageThreadItem=CapabilityAgentMessageThreadItem,
        TurnCompletedNotification=CapabilityTurnCompletedNotification,
        Turn=CapabilityTurn,
        ThreadTokenUsageUpdatedNotification=CapabilityThreadTokenUsageUpdatedNotification,
        ThreadTokenUsage=CapabilityThreadTokenUsage,
        TokenUsageBreakdown=CapabilityTokenUsageBreakdown,
    )
    return SimpleNamespace(
        Codex=CapabilityCodex,
        Sandbox=CapabilitySandbox,
        Thread=CapabilityThread,
        TurnHandle=CapabilityTurnHandle,
        TurnResult=CapabilityTurnResult,
        models=SimpleNamespace(Notification=CapabilityNotification),
        generated=SimpleNamespace(v2_all=generated),
    )

def payload(**changes: object) -> dict[str, object]:
    value: dict[str, object] = {"schema_version": "r2", "workflow_id": "workflow-1", "run_id": "run-1", "role": "EXECUTOR", "status": "SUCCEEDED", "gate": "implementation", "gate_result": "PASS", "summary": "done", "findings": {"p0": [], "p1": [], "p2": []}, "test_evidence": [], "changed_paths": [], "recommendation": "audit", "requires_human_decision": False, "p1_correctable": False, "correction_artifact": None}
    value.update(changes)
    return value

class FakeTurnHandle:
    def __init__(self, codex: "FakeCodex", thread_id: str) -> None:
        self.codex, self.thread_id, self.interrupted = codex, thread_id, threading.Event()

    def interrupt(self) -> None:
        self.interrupted.set(); self.codex.interrupts += 1

    def stream(self):
        behavior = self.codex.behavior
        if behavior == "exception": raise RuntimeError("provider exception with SECRET_SENTINEL")
        if behavior == "noncooperative":
            self.codex.stream_entered.set()
            while True: time.sleep(.005)
        if behavior in {"hang", "cancel_race"}:
            self.codex.stream_entered.set()
            while not (self.interrupted.is_set() or self.codex.closed.is_set()): time.sleep(.005)
            raise RuntimeError("interrupted")
        if behavior == "slow": time.sleep(.08)
        if behavior == "missing":
            yield FakeEvent("turn/completed", FakeCompletedPayload(FakeCompletedTurn())); return
        if behavior == "unknown_events":
            for _ in range(1000): yield FakeEvent("unknown/event")
        if behavior == "conflicting_event_session": yield FakeEvent("thread/started", FakeThreadPayload("other-thread"))
        value = payload()
        if behavior == "wrong_identity": value["run_id"] = "other"
        if behavior == "wrong_workflow": value["workflow_id"] = "other-workflow"
        if behavior == "bad_gate": value["gate_result"] = "INVALID"
        if behavior == "unknown_field": value["unknown"] = True
        if behavior == "blank_summary": value["summary"] = " "
        if behavior == "bad_nested": value["test_evidence"] = [{"name": "x", "outcome": "NO", "evidence": "y"}]
        if behavior == "session_conflict": value["session_reference"] = "other-thread"
        text = "{" if behavior == "malformed" else json.dumps(value)
        if behavior == "oversized": text = "x" * 4096
        yield FakeEvent("item/completed", FakeItemPayload(FakeItem(text)))
        if behavior == "malformed_then_valid": yield FakeEvent("item/completed", FakeItemPayload(FakeItem(json.dumps(payload()))))
        if behavior == "two_finals": yield FakeEvent("item/completed", FakeItemPayload(FakeItem(json.dumps(payload()))))
        if behavior == "usage": yield FakeEvent("thread/tokenUsage/updated", FakeUsagePayload(FakeUsage(FakeUsageLast(input_tokens=3, output_tokens=0, total_tokens=3))))
        if behavior == "partial_usage": yield FakeEvent("thread/tokenUsage/updated", FakeUsagePayload(FakeUsage(FakeUsageLast(total_tokens=7))))
        yield FakeEvent("turn/completed", FakeCompletedPayload(FakeCompletedTurn()))

class FakeThread:
    def __init__(self, codex: "FakeCodex", thread_id: str) -> None: self.codex, self.id = codex, thread_id
    def turn(self, _input: str, **_options: object) -> FakeTurnHandle:
        self.codex.turns += 1; return FakeTurnHandle(self.codex, self.id)

class FakeCodex:
    def __init__(self, behavior: str = "valid", store: set[str] | None = None) -> None:
        self.behavior, self.store = behavior, store if store is not None else set()
        self._starts = multiprocessing.Value("i", 0)
        self._resumes = multiprocessing.Value("i", 0)
        self._turns = multiprocessing.Value("i", 0)
        self._interrupts = multiprocessing.Value("i", 0)
        self._closes = multiprocessing.Value("i", 0)
        self.stream_entered = multiprocessing.Event()
        self.thread_start_entered = multiprocessing.Event()
        self.closed = threading.Event()
    @property
    def starts(self) -> int: return self._starts.value
    @starts.setter
    def starts(self, value: int) -> None: self._starts.value = value
    @property
    def resumes(self) -> int: return self._resumes.value
    @resumes.setter
    def resumes(self, value: int) -> None: self._resumes.value = value
    @property
    def turns(self) -> int: return self._turns.value
    @turns.setter
    def turns(self, value: int) -> None: self._turns.value = value
    @property
    def interrupts(self) -> int: return self._interrupts.value
    @interrupts.setter
    def interrupts(self, value: int) -> None: self._interrupts.value = value
    @property
    def closes(self) -> int: return self._closes.value
    @closes.setter
    def closes(self, value: int) -> None: self._closes.value = value
    def close(self) -> None: self.closes += 1; self.closed.set()
    def thread_start(self, **_options: object) -> FakeThread:
        if self.behavior == "before_session":
            self.thread_start_entered.set()
            while True: time.sleep(.005)
        self.closed.clear(); self.starts += 1; thread_id = "thread-1" if self.behavior != "bad_session" else " "; self.store.add(thread_id); return FakeThread(self, thread_id)
    def thread_resume(self, thread_id: str, **_options: object) -> FakeThread:
        self.closed.clear(); self.resumes += 1
        return FakeThread(self, "other-thread" if self.behavior == "resume_mismatch" else thread_id)
