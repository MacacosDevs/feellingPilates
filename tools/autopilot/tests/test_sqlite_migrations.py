from pathlib import Path
import sqlite3
import tempfile
import unittest

from feelingpilates_autopilot.adapters.state.migrations import Migration, MigrationError, apply_migrations
from feelingpilates_autopilot.adapters.state.sqlite_store import SQLiteStateStore


class SQLiteMigrationTests(unittest.TestCase):
    def test_raw_bytes_checksum_and_idempotent_public_migration_connection(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            migrations = root / "migrations"
            migrations.mkdir()
            migration = migrations / "001_test.sql"
            original = b"CREATE TABLE sample (id INTEGER);\n"
            migration.write_bytes(original)
            first = Migration.from_path(migration).checksum
            self.assertEqual(first, Migration.from_path(migration).checksum)
            migration.write_bytes(b"CREATE TABLE sample (id INTEGER); \n")
            self.assertNotEqual(first, Migration.from_path(migration).checksum)
            migration.write_bytes(b"CREATE TABLE sample (id INTEGER);\r\n")
            self.assertNotEqual(first, Migration.from_path(migration).checksum)
            migration.write_bytes(original)
            connection = sqlite3.connect(root / "state.db", isolation_level=None)
            apply_migrations(connection, migrations, busy_timeout_ms=321)
            apply_migrations(connection, migrations, busy_timeout_ms=321)
            self.assertEqual(connection.execute("PRAGMA foreign_keys").fetchone()[0], 1)
            self.assertEqual(connection.execute("PRAGMA busy_timeout").fetchone()[0], 321)
            self.assertEqual(connection.execute("PRAGMA synchronous").fetchone()[0], 2)
            self.assertEqual(connection.execute("PRAGMA journal_mode").fetchone()[0].lower(), "wal")
            self.assertEqual(connection.execute("SELECT COUNT(*) FROM schema_migrations").fetchone()[0], 1)
            connection.close()

    def test_checksum_drift_and_partial_failure_fail_closed_without_marker_or_schema(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            migrations = root / "migrations"
            migrations.mkdir()
            migration = migrations / "001_test.sql"
            migration.write_text("CREATE TABLE sample (id INTEGER);\n")
            connection = sqlite3.connect(root / "state.db", isolation_level=None)
            apply_migrations(connection, migrations)
            migration.write_text("CREATE TABLE sample (id TEXT);\n")
            with self.assertRaises(MigrationError):
                apply_migrations(connection, migrations)
            connection.close()

            failed = root / "failed"
            failed.mkdir()
            (failed / "001_partial.sql").write_text("CREATE TABLE partial (id INTEGER);\nINVALID SQL;\n")
            failed_connection = sqlite3.connect(root / "failed.db", isolation_level=None)
            with self.assertRaises(MigrationError):
                apply_migrations(failed_connection, failed)
            self.assertEqual(
                failed_connection.execute("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'partial'").fetchone()[0],
                0,
            )
            self.assertEqual(failed_connection.execute("SELECT COUNT(*) FROM schema_migrations").fetchone()[0], 0)
            failed_connection.close()

    def test_reopened_store_reestablishes_every_connection_pragma(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.db"
            first = SQLiteStateStore(path, busy_timeout_ms=987)
            first.close()
            reopened = SQLiteStateStore(path, busy_timeout_ms=987)
            self.assertEqual(
                reopened.connection_settings(),
                {"foreign_keys": 1, "busy_timeout": 987, "synchronous": 2, "journal_mode": "wal"},
            )
            reopened.close()
