"""Bounded execution adapters; importing this package performs no I/O."""

from .codex_cli import CodexCliAdapter

__all__ = ["CodexCliAdapter"]
