"""Validated argv construction for the Codex CLI fallback adapter.

This module deliberately returns an argv tuple and stdin data.  It never
constructs a shell command string and exposes no generic option pass-through.
"""

from __future__ import annotations

import os
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from ...ports.agent_executor import ExecutionRequest, SandboxMode


class CommandValidationError(ValueError):
    """A request cannot be represented safely by the supported CLI profile."""


@dataclass(frozen=True, slots=True)
class CliCommand:
    argv: tuple[str, ...]
    stdin: str


_SANDBOX_FLAGS = {
    SandboxMode.READ_ONLY: "read-only",
    SandboxMode.WORKSPACE_WRITE: "workspace-write",
}

# The supported Codex CLI profile accepts these only through its bounded
# configuration override mechanism.  This is intentionally not a generic
# configuration pass-through.
_REASONING_EFFORTS = frozenset(("low", "medium", "high", "xhigh"))


def resolve_executable(configured_executable: str | None) -> str:
    """Resolve trusted infrastructure configuration to an executable file."""
    candidate = configured_executable
    if candidate is None:
        candidate = shutil.which("codex")
    if not candidate:
        raise CommandValidationError("Codex CLI executable is unavailable")
    path = Path(candidate)
    if not path.is_absolute():
        resolved = shutil.which(candidate)
        if not resolved:
            raise CommandValidationError("configured Codex CLI executable is unavailable")
        path = Path(resolved)
    try:
        path = path.resolve(strict=True)
    except OSError as exc:
        raise CommandValidationError("configured Codex CLI executable is unavailable") from exc
    if not path.is_file() or not os.access(path, os.X_OK):
        raise CommandValidationError("configured Codex CLI target is not executable")
    return str(path)


def validate_session_id(value: object) -> str:
    """Accept Codex UUID/thread-name syntax without allowing argv fragments."""
    if not isinstance(value, str) or not value or value != value.strip() or len(value) > 128:
        raise CommandValidationError("resume requires one non-blank session identity")
    if not value[0].isalnum() or any(not (char.isalnum() or char in "._:-") for char in value):
        raise CommandValidationError("resume session identity has unsupported syntax")
    return value


def _require_request(
    request: ExecutionRequest, allowed_models: Iterable[str], authorized_workspace_root: str
) -> None:
    if not request.workflow_id or not request.role or not request.gate:
        raise CommandValidationError("workflow, role, and gate identity are required")
    if not isinstance(request.instructions, str) or not request.instructions.strip():
        raise CommandValidationError("instructions are required")
    if not request.working_directory:
        raise CommandValidationError("an explicit working directory is required")
    cwd = Path(request.working_directory)
    root = Path(authorized_workspace_root)
    if not cwd.is_absolute() or not root.is_absolute():
        raise CommandValidationError("working directory and authorized workspace root must be absolute")
    try:
        cwd = cwd.resolve(strict=True)
        root = root.resolve(strict=True)
    except OSError as exc:
        raise CommandValidationError("working directory and authorized workspace root must exist") from exc
    if not cwd.is_dir() or not root.is_dir():
        raise CommandValidationError("working directory must be an existing absolute directory")
    try:
        cwd.relative_to(root)
    except ValueError as exc:
        raise CommandValidationError("working directory is outside the authorized workspace root") from exc
    if request.sandbox not in _SANDBOX_FLAGS:
        raise CommandValidationError("sandbox mode is unsupported")
    if request.timeout_seconds is None or request.timeout_seconds <= 0:
        raise CommandValidationError("a positive per-turn timeout is required")
    if request.reasoning_effort is not None and request.reasoning_effort not in _REASONING_EFFORTS:
        raise CommandValidationError("reasoning effort is unsupported by the bounded CLI profile")
    if request.model is not None:
        if not isinstance(request.model, str) or not request.model or request.model not in set(allowed_models):
            raise CommandValidationError("model is not allowed by adapter configuration")


def build_command(
    request: ExecutionRequest,
    executable: str,
    schema_path: str,
    *,
    resume_session_id: str | None = None,
    allowed_models: Iterable[str] = (),
    authorized_workspace_root: str,
) -> CliCommand:
    """Build the sole supported new-turn or resume command for CLI 0.150.1."""
    _require_request(request, allowed_models, authorized_workspace_root)
    schema = Path(schema_path)
    if not schema.is_absolute() or not schema.is_file():
        raise CommandValidationError("agent-result schema is unavailable")
    argv = [executable, "exec"]
    if request.reasoning_effort is not None:
        argv.extend(("-c", f"model_reasoning_effort={request.reasoning_effort}"))
    if request.model is not None:
        argv.extend(("--model", request.model))
    argv.extend(("--sandbox", _SANDBOX_FLAGS[request.sandbox], "--output-schema", str(schema), "--json"))
    if resume_session_id is None:
        argv.append("-")
    else:
        argv.extend(("resume", validate_session_id(resume_session_id), "-"))
    return CliCommand(argv=tuple(argv), stdin=request.instructions)
