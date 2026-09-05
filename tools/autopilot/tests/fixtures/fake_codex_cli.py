#!/usr/bin/env python3
"""Deterministic, offline stand-in for the narrow Codex CLI R4 profile."""

from __future__ import annotations

import json
import os
import signal
import subprocess
import sys
import threading
import time


def emit(value: object) -> None:
    print(json.dumps(value), flush=True)


def result() -> dict[str, object]:
    return {
        "schema_version": "r2", "workflow_id": "workflow-1", "run_id": "run-1", "role": "EXECUTOR",
        "status": "SUCCEEDED", "gate": "implementation", "gate_result": "PASS", "summary": "Completed safely.",
        "findings": {"p0": [], "p1": [], "p2": []}, "test_evidence": [], "changed_paths": [],
        "recommendation": "Submit to independent audit.", "requires_human_decision": False,
        "p1_correctable": False, "correction_artifact": None,
    }


def main(argv: list[str]) -> int:
    if argv == ["--version"]:
        if os.environ.get("FAKE_CODEX_BEHAVIOR") == "capability_hang":
            while True:
                time.sleep(0.05)
        print("codex-cli 0.150.1")
        return 0
    if argv[-1:] == ["--help"]:
        if os.environ.get("FAKE_CODEX_BEHAVIOR") == "capability_hang":
            while True:
                time.sleep(0.05)
        if os.environ.get("FAKE_CODEX_BEHAVIOR") == "capability_large":
            sys.stdout.write("x" * 500000)
            sys.stdout.flush()
            return 0
        if os.environ.get("FAKE_CODEX_BEHAVIOR") == "capability_leader_exit_stubborn" and "resume" not in argv:
            child = subprocess.Popen([sys.executable, "-c", "import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(60)"])
            pid_file = os.environ.get("FAKE_CODEX_CAPABILITY_PID_FILE")
            if pid_file:
                with open(pid_file, "w", encoding="utf-8") as output:
                    output.write(f"{os.getpid()} {child.pid}\n")
            print(f"--sandbox read-only workspace-write --output-schema --json --model -c --config capability_leader_exit=natural child_pid={child.pid}")
            return 0
        if "resume" in argv:
            print("--json --output-schema --model -c --config")
        else:
            print("--sandbox read-only workspace-write --output-schema --json --model -c --config")
        return 0
    behavior = os.environ.get("FAKE_CODEX_BEHAVIOR", "valid")
    if "resume" in argv and behavior == "resume":
        behavior = "valid"
    if behavior == "stderr":
        print("diagnostic only", file=sys.stderr, flush=True)
    if behavior == "stderr_result":
        print(json.dumps({"type": "item.completed", "item": {"type": "agent_message", "text": json.dumps(result())}}), file=sys.stderr, flush=True)
        return 0
    if behavior in {"session", "nonzero_session", "timeout_session", "cancel_session", "duplicate_session"}:
        emit({"type": "thread.started", "thread_id": "thread-1"})
    if behavior == "duplicate_session":
        emit({"type": "thread.started", "thread_id": "thread-1"})
    if behavior in {"conflict_session", "conflict_failure"}:
        emit({"type": "thread.started", "thread_id": "thread-1"})
        emit({"type": "thread.started", "thread_id": "thread-2"})
    if behavior == "malformed_session":
        emit({"type": "thread.started", "thread_id": " "})
    if behavior == "whitespace_session":
        emit({"type": "thread.started", "thread_id": " thread-1 "})
    if behavior == "nonjson":
        print("not JSONL", flush=True)
    if behavior == "missing":
        return 0
    if behavior == "malformed":
        emit({"type": "item.completed", "item": {"type": "agent_message", "text": "{"}})
        return 0
    if behavior == "signal":
        os.kill(os.getpid(), signal.SIGTERM)
    if behavior in {"timeout", "timeout_session", "cancel", "cancel_session", "blocked_stdin"}:
        print("partial stderr", file=sys.stderr, flush=True)
        while True:
            time.sleep(0.05)
    if behavior == "closed_stdin":
        os.close(sys.stdin.fileno())
        time.sleep(0.05)
        return 0
    if behavior == "closed_stdin_nonzero":
        os.close(sys.stdin.fileno())
        return 7
    if behavior == "closed_stdin_timeout":
        os.close(sys.stdin.fileno())
        while True:
            time.sleep(0.05)
    if behavior == "slow_valid":
        time.sleep(float(os.environ.get("FAKE_CODEX_DELAY_SECONDS", "2.2")))
    if behavior == "child":
        child = subprocess.Popen([sys.executable, "-c", "import time; time.sleep(60)"])
        print(child.pid, flush=True)
        while True:
            time.sleep(0.05)
    if behavior == "stubborn_child":
        child = subprocess.Popen([sys.executable, "-c", "import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(60)"])
        emit({"type": "diagnostic", "child_pid": child.pid})
        signal.signal(signal.SIGTERM, lambda *_: sys.exit(0))
        while True:
            time.sleep(0.05)
    if behavior == "leader_exit_stubborn":
        child = subprocess.Popen([sys.executable, "-c", "import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(60)"])
        emit({"type": "diagnostic", "child_pid": child.pid})
        return 0
    if behavior == "large":
        print("x" * 20000, flush=True)
        return 0
    if behavior == "unterminated_large":
        sys.stdout.write("x" * 500000)
        sys.stdout.flush()
        return 0
    if behavior == "stderr_large":
        sys.stderr.write("diagnostic" * 50000)
        sys.stderr.flush()
    if behavior == "dual_large":
        def write_stdout() -> None:
            sys.stdout.write("x" * 200000)
            sys.stdout.flush()

        def write_stderr() -> None:
            sys.stderr.write("diagnostic" * 20000)
            sys.stderr.flush()

        first, second = threading.Thread(target=write_stdout), threading.Thread(target=write_stderr)
        first.start(); second.start()
        first.join(); second.join()
        return 0
    if behavior in {"nonzero", "nonzero_session", "conflict_failure"}:
        return 9
    payload = result()
    if behavior == "wrong_identity":
        payload["run_id"] = "wrong"
    if behavior == "unknown":
        payload["unexpected"] = True
    if behavior == "usage":
        emit({"type": "turn.completed", "usage": {"input_tokens": 3, "output_tokens": 0}})
    if behavior == "bad_usage":
        payload["usage_record"] = {"schema_version": "r2", "adapter": "codex-cli", "measurements": {}}
    if behavior == "bad_evidence":
        payload["test_evidence"] = [{"name": 3, "outcome": "INVALID", "evidence": []}]
    if behavior == "bad_outcome":
        payload["test_evidence"] = [{"name": "unit", "outcome": "INVALID", "evidence": "x"}]
    if behavior == "usage_mismatch":
        payload["usage_record"] = {"schema_version": "r2", "adapter": "other", "measurements": {}}
    if behavior in {"session", "duplicate_session"}:
        payload["session_reference"] = "thread-1"
    emit({"type": "item.completed", "item": {"type": "agent_message", "text": json.dumps(payload)}})
    return 0


raise SystemExit(main(sys.argv[1:]))
