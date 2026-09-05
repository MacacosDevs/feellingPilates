"""Concrete infrastructure adapters; importing the package performs no I/O."""

from .execution import CodexCliAdapter

__all__ = ["CodexCliAdapter"]
