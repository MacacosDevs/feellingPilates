"""Deterministic, raw-byte checksummed SQLite migration runner."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone
from hashlib import sha256
from pathlib import Path
import re
import sqlite3


class MigrationError(RuntimeError):
    """The database schema cannot safely be used."""


def configure_connection(connection: sqlite3.Connection, busy_timeout_ms: int = 5_000) -> None:
    """Establish and verify the mandatory R3 semantics for one SQLite connection."""
    if busy_timeout_ms < 0:
        raise ValueError("busy_timeout_ms must be non-negative")
    connection.execute("PRAGMA foreign_keys = ON")
    connection.execute(f"PRAGMA busy_timeout = {busy_timeout_ms}")
    journal_mode = str(connection.execute("PRAGMA journal_mode = WAL").fetchone()[0]).lower()
    connection.execute("PRAGMA synchronous = FULL")
    verify_connection(connection, busy_timeout_ms, journal_mode)


def verify_connection(
    connection: sqlite3.Connection, busy_timeout_ms: int, journal_mode: str | None = None
) -> None:
    """Fail closed unless the effective connection contract is present."""
    effective_mode = journal_mode or str(connection.execute("PRAGMA journal_mode").fetchone()[0]).lower()
    foreign_keys = int(connection.execute("PRAGMA foreign_keys").fetchone()[0])
    busy_timeout = int(connection.execute("PRAGMA busy_timeout").fetchone()[0])
    synchronous = int(connection.execute("PRAGMA synchronous").fetchone()[0])
    if foreign_keys != 1 or busy_timeout != busy_timeout_ms or synchronous != 2 or effective_mode != "wal":
        raise MigrationError("mandatory SQLite connection contract could not be established")


@dataclass(frozen=True, slots=True)
class Migration:
    migration_id: str
    raw_bytes: bytes
    checksum: str

    @classmethod
    def from_path(cls, path: Path) -> "Migration":
        match = re.fullmatch(r"(\d+_[A-Za-z0-9_-]+)\.sql", path.name)
        if match is None:
            raise MigrationError(f"invalid migration filename: {path.name}")
        raw_bytes = path.read_bytes()
        return cls(match.group(1), raw_bytes, sha256(raw_bytes).hexdigest())

    def sql(self) -> str:
        try:
            return self.raw_bytes.decode("utf-8")
        except UnicodeDecodeError as error:
            raise MigrationError(f"migration {self.migration_id} is not valid UTF-8") from error


def load_migrations(migrations_directory: Path) -> tuple[Migration, ...]:
    migrations = tuple(Migration.from_path(path) for path in sorted(migrations_directory.glob("*.sql")))
    identifiers = tuple(migration.migration_id for migration in migrations)
    if not migrations or len(set(identifiers)) != len(identifiers):
        raise MigrationError("migration identifiers must be present and unique")
    return migrations


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def apply_migrations(
    connection: sqlite3.Connection, migrations_directory: Path, *, busy_timeout_ms: int = 5_000
) -> None:
    """Apply known migrations in order, rejecting drift and future database state."""
    configure_connection(connection, busy_timeout_ms)
    migrations = load_migrations(migrations_directory)
    connection.execute("BEGIN IMMEDIATE")
    try:
        connection.execute(
            "CREATE TABLE IF NOT EXISTS schema_migrations "
            "(migration_id TEXT PRIMARY KEY NOT NULL, checksum TEXT NOT NULL, applied_at TEXT NOT NULL)"
        )
        connection.commit()
    except Exception:
        connection.rollback()
        raise

    applied_rows = connection.execute(
        "SELECT migration_id, checksum FROM schema_migrations ORDER BY migration_id"
    ).fetchall()
    known = {migration.migration_id: migration for migration in migrations}
    for migration_id, checksum in applied_rows:
        known_migration = known.get(migration_id)
        if known_migration is None:
            raise MigrationError(f"unknown future schema migration: {migration_id}")
        if checksum != known_migration.checksum:
            raise MigrationError(f"migration checksum drift: {migration_id}")

    applied_ids = {migration_id for migration_id, _ in applied_rows}
    expected_prefix = tuple(migration.migration_id for migration in migrations[: len(applied_ids)])
    if tuple(migration_id for migration_id, _ in applied_rows) != expected_prefix:
        raise MigrationError("applied migrations are not a known ordered prefix")
    for migration in migrations:
        if migration.migration_id in applied_ids:
            continue
        # executescript owns the transaction script. The applied marker is deliberately
        # inside that same transaction, so a SQL error leaves neither schema nor marker.
        marker = (
            "INSERT INTO schema_migrations (migration_id, checksum, applied_at) VALUES "
            f"({migration.migration_id!r}, {migration.checksum!r}, {_utc_now()!r});"
        )
        try:
            connection.executescript(f"BEGIN IMMEDIATE;\n{migration.sql()}\n{marker}\nCOMMIT;")
        except sqlite3.Error as error:
            connection.rollback()
            raise MigrationError(f"migration failed: {migration.migration_id}") from error
