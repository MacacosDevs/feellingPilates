from __future__ import annotations

from datetime import datetime, timezone
from dataclasses import replace
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

from feelingpilates_autopilot.adapters.execution.codex_sdk import CodexSdkAdapter, SdkCapabilityError
from feelingpilates_autopilot.domain.failures import FailureCategory
from feelingpilates_autopilot.domain.models import Checkpoint, CheckpointId, RunId, SessionId, SessionReference, WorkflowId
from feelingpilates_autopilot.domain.states import OperationalState
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, ExecutionStatus, SandboxMode
from tests.fixtures.fake_codex_sdk import FakeCodex, capability_sdk_module


class TestCodexSdkAdapter(unittest.TestCase):
    def setUp(self) -> None:
        self.fake = FakeCodex()
        self.adapter = CodexSdkAdapter(codex_factory=lambda: self.fake, authorized_workspace_root=str(Path.cwd()), verify_capabilities=False)

    def request(self, **changes: object) -> ExecutionRequest:
        values: dict[str, object] = dict(run_id=RunId("run-1"), instructions="offline", workflow_id=WorkflowId("workflow-1"), role="EXECUTOR", gate="implementation", working_directory=str(Path.cwd()), sandbox=SandboxMode.READ_ONLY, timeout_seconds=1)
        values.update(changes)
        return ExecutionRequest(**values)  # type: ignore[arg-type]

    def checkpoint(self, **changes: object) -> Checkpoint:
        values: dict[str, object] = dict(checkpoint_id=CheckpointId("checkpoint-1"), run_id=RunId("run-1"), operational_state=OperationalState.PAUSED, created_at=datetime.now(timezone.utc), workflow_id=WorkflowId("workflow-1"))
        values.update(changes)
        return Checkpoint(**values)  # type: ignore[arg-type]

    def test_successful_new_turn_is_primary_and_extracts_required_session(self) -> None:
        handle = self.adapter.start(self.request())
        observation = self.adapter.get_result(handle)
        self.assertEqual(observation.status, ExecutionStatus.SUCCEEDED)
        self.assertEqual(observation.session.session_id, "thread-1")
        self.assertEqual(self.fake.starts, 1)
        self.assertEqual(self.fake.turns, 1)
        self.assertTrue(self.adapter.capabilities().supports_resume)

    def test_sdk_failure_preserves_observed_session_without_cli_fallback(self) -> None:
        self.fake.behavior = "exception"
        handle = self.adapter.start(self.request())
        observation = self.adapter.get_result(handle)
        self.assertEqual(observation.status, ExecutionStatus.FAILED)
        self.assertEqual(observation.failure.category, FailureCategory.SDK_TRANSPORT_FAILURE)
        self.assertEqual(observation.session.session_id, "thread-1")
        self.assertEqual(self.fake.starts, 1)
        self.assertEqual(self.fake.turns, 1)

    def test_same_process_and_cross_adapter_resume_use_persisted_reference(self) -> None:
        completed = self.adapter.get_result(self.adapter.start(self.request()))
        original_handle = self.adapter.start(self.request())
        original_result = self.adapter.get_result(original_handle)
        resumed = self.adapter.resume(replace(original_handle, session=original_result.session), self.checkpoint())
        # A persisted operational session can be continued through the boundary.
        self.assertEqual(self.adapter.get_result(resumed).status, ExecutionStatus.SUCCEEDED)
        persisted = self.adapter.start(self.request())
        original = self.adapter.get_result(persisted)
        second_fake = FakeCodex(store=self.fake.store)
        second = CodexSdkAdapter(codex_factory=lambda: second_fake, authorized_workspace_root=str(Path.cwd()), verify_capabilities=False)
        resumed_cross = second.resume(persisted.__class__(persisted.execution_id, persisted.run_id, original.session, persisted.resume_context), self.checkpoint())
        self.assertEqual(second.get_result(resumed_cross).status, ExecutionStatus.SUCCEEDED)
        self.assertEqual(second_fake.resumes, 1)
        self.assertIsNotNone(completed.session)

    def test_resume_provenance_conflicts_fail_closed_before_provider_execution(self) -> None:
        handle = self.adapter.start(self.request())
        session = self.adapter.get_result(handle).session
        bad = SessionReference(SessionId("thread-1"), RunId("run-1"), "thread-1", "codex-cli", "EXECUTOR")
        rejected = self.adapter.resume(handle.__class__(handle.execution_id, handle.run_id, bad, handle.resume_context), self.checkpoint())
        result = self.adapter.get_result(rejected)
        self.assertEqual(result.status, ExecutionStatus.FAILED)
        self.assertEqual(result.failure.category, FailureCategory.SDK_SESSION_FAILURE)
        self.assertEqual(self.fake.resumes, 0)
        self.assertIsNotNone(session)

    def test_resume_reuses_full_request_authority_validation_before_sdk_execution(self) -> None:
        original = self.adapter.start(self.request())
        session = self.adapter.get_result(original).session
        for name, change in (
            ("outside-root", {"working_directory": str(Path.cwd().parent)}),
            ("missing-sandbox", {"sandbox": None}),
            ("unsupported-sandbox", {"sandbox": "FULL_ACCESS"}),
            ("unapproved-model", {"model": "not-allowed"}),
            ("bad-effort", {"reasoning_effort": "unsafe"}),
        ):
            with self.subTest(name=name):
                self.fake.resumes = 0
                context = replace(original.resume_context, **change)
                rejected = self.adapter.resume(replace(original, session=session, resume_context=context), self.checkpoint())
                observed = self.adapter.get_result(rejected)
                self.assertEqual(observed.failure.category, FailureCategory.SDK_SESSION_FAILURE)
                self.assertEqual(self.fake.resumes, 0)

    def test_bad_session_and_sdk_result_identity_fail_closed(self) -> None:
        self.fake.behavior = "bad_session"
        self.assertEqual(self.adapter.get_result(self.adapter.start(self.request())).failure.category, FailureCategory.SDK_SESSION_FAILURE)
        self.fake.behavior = "wrong_identity"
        self.assertEqual(self.adapter.get_result(self.adapter.start(self.request())).failure.category, FailureCategory.RESULT_CONTRACT_FAILURE)

    def test_secret_like_environment_and_provider_error_text_are_not_serialized(self) -> None:
        old = os.environ.get("R5_SECRET_SENTINEL")
        os.environ["R5_SECRET_SENTINEL"] = "SECRET_SENTINEL"
        try:
            self.fake.behavior = "exception"
            observation = self.adapter.get_result(self.adapter.start(self.request()))
        finally:
            if old is None:
                os.environ.pop("R5_SECRET_SENTINEL", None)
            else:
                os.environ["R5_SECRET_SENTINEL"] = old
        self.assertNotIn("SECRET_SENTINEL", repr(observation))

    def test_isolated_installed_artifact_resolves_the_canonical_schema(self) -> None:
        project = Path(__file__).parents[1]
        generated_names = {"build", "dist", "venv", "__pycache__"}

        def live_generated_artifacts() -> set[Path]:
            return {
                path.relative_to(project)
                for path in project.rglob("*")
                if path.name in generated_names
                or path.name.endswith(".egg-info")
                or path.suffix == ".pyc"
            }

        before = live_generated_artifacts()
        with tempfile.TemporaryDirectory(prefix="r5-installed-") as temporary:
            temporary_root = Path(temporary)
            source = temporary_root / "source"
            source.mkdir()
            shutil.copy2(project / "pyproject.toml", source / "pyproject.toml")
            shutil.copy2(project / "README.md", source / "README.md")
            shutil.copytree(project / "schemas", source / "schemas")
            (source / "src").mkdir()
            shutil.copytree(
                project / "src" / "feelingpilates_autopilot",
                source / "src" / "feelingpilates_autopilot",
                ignore=shutil.ignore_patterns("__pycache__", "*.pyc", "*.egg-info"),
            )
            venv = temporary_root / "venv"
            subprocess.run([sys.executable, "-m", "venv", str(venv)], check=True, capture_output=True, text=True)
            python = venv / "bin" / "python"
            subprocess.run([str(python), "-m", "pip", "install", "--no-deps", str(source)], check=True, capture_output=True, text=True)
            check = "import inspect,sys; from pathlib import Path; from feelingpilates_autopilot.adapters.execution.codex_sdk import CodexSdkAdapter; source=Path(sys.argv[1]).resolve(); module=Path(inspect.getfile(CodexSdkAdapter)).resolve(); assert not module.is_relative_to(source); assert 'site-packages' in module.parts; p=CodexSdkAdapter(codex_factory=lambda: object(), verify_capabilities=False)._schema_path; assert p.is_file(); print(p)"
            result = subprocess.run([str(python), "-I", "-c", check, str(source)], check=True, capture_output=True, text=True, cwd=temporary_root)
            self.assertIn("feelingpilates_autopilot/schemas/agent-result.schema.json", result.stdout)
        self.assertEqual(live_generated_artifacts(), before)

    def test_capability_probe_checks_the_required_streaming_surface(self) -> None:
        sdk = self._capable_sdk()
        with patch("importlib.metadata.version", return_value="0.147.0"):
            CodexSdkAdapter(sdk_module=sdk, verify_capabilities=True).probe_capabilities()
            broken = self._capable_sdk(turn_handle=type("TurnHandle", (), {"interrupt": lambda self: None}))
            with self.assertRaises(SdkCapabilityError):
                CodexSdkAdapter(sdk_module=broken, verify_capabilities=True)

    def test_capability_probe_rejects_each_materially_incomplete_surface(self) -> None:
        complete = self._capable_sdk()
        failures = {
            "sandbox": self._capable_sdk(sandbox=type("Sandbox", (), {"read_only": "unsafe", "workspace_write": "also-unsafe"})),
            "thread-id": self._capable_sdk(thread=type("Thread", (), {"turn": complete.Thread.turn})),
            "stream": self._capable_sdk(turn_handle=type("TurnHandle", (), {"interrupt": complete.TurnHandle.interrupt})),
            "interrupt": self._capable_sdk(turn_handle=type("TurnHandle", (), {"stream": complete.TurnHandle.stream})),
            "schema": self._capable_sdk(thread=type("Thread", (), {"id": "x", "turn": lambda self, input, *, effort=None: None})),
            "events": self._capable_sdk(event=False),
            "usage": self._capable_sdk(usage=False),
        }
        with patch("importlib.metadata.version", return_value="0.147.0"):
            for name, sdk in failures.items():
                with self.subTest(name=name), self.assertRaises(SdkCapabilityError):
                    CodexSdkAdapter(sdk_module=sdk, verify_capabilities=True)

    @staticmethod
    def _capable_sdk(*, sandbox=None, thread=None, turn_handle=None, event: bool = True, usage: bool = True):
        sdk = capability_sdk_module()
        sdk.Sandbox = sdk.Sandbox if sandbox is None else sandbox
        sdk.Thread = sdk.Thread if thread is None else thread
        sdk.TurnHandle = sdk.TurnHandle if turn_handle is None else turn_handle
        if not event:
            sdk.models.Notification = None
        if not usage:
            sdk.generated.v2_all.ThreadTokenUsageUpdatedNotification = None
        return sdk
