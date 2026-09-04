"""Repository contract only; it does not implement Git or worktrees."""

from abc import ABC, abstractmethod
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class RepositorySnapshot:
    repository_path: str
    worktree_path: str
    branch: str | None
    is_detached: bool
    revision: str
    upstream_ref: str | None
    resolved_upstream_revision: str | None
    staged_paths: tuple[str, ...]
    modified_paths: tuple[str, ...]
    untracked_paths: tuple[str, ...]

    def __post_init__(self) -> None:
        if not self.repository_path or not self.worktree_path or not self.revision:
            raise ValueError("repository, worktree, and revision identity are required")
        if self.is_detached == (self.branch is not None):
            raise ValueError("snapshot must represent exactly one of attached branch or detached HEAD")
        if self.branch is not None and not self.branch:
            raise ValueError("an attached branch needs a non-empty name")
        if self.upstream_ref is None and self.resolved_upstream_revision is not None:
            raise ValueError("an unresolved upstream cannot have a revision")
        if set(self.untracked_paths) & (set(self.staged_paths) | set(self.modified_paths)):
            raise ValueError("untracked paths cannot be staged or tracked modifications")


class Repository(ABC):
    @abstractmethod
    def snapshot(self) -> RepositorySnapshot: ...

    @abstractmethod
    def read_text(self, path: str) -> str: ...
