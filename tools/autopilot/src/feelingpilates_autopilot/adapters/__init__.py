"""Concrete infrastructure adapters; importing the package performs no I/O."""

from .execution import CodexCliAdapter
from .execution import CodexSdkAdapter

__all__ = ["CodexCliAdapter", "CodexSdkAdapter"]
