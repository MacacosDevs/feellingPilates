import json
from pathlib import Path
import tempfile
import unittest

from feelingpilates_autopilot.adapters.execution.command import CommandValidationError, build_command, validate_session_id
from feelingpilates_autopilot.domain.models import RunId, WorkflowId
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, SandboxMode


class CommandTests(unittest.TestCase):
    def request(self, **changes):
        with tempfile.TemporaryDirectory() as directory:
            values = dict(run_id=RunId("run-1"), instructions="hello; --danger", workflow_id=WorkflowId("workflow-1"), role="EXECUTOR", gate="implementation", working_directory=directory, sandbox=SandboxMode.READ_ONLY, timeout_seconds=1)
            values.update(changes)
            yield ExecutionRequest(**values)

    def test_argv_is_a_data_sequence_without_prompt_interpolation(self):
        schema = Path(__file__).parents[1] / "schemas" / "agent-result.schema.json"
        for request in self.request():
            command = build_command(request, "/bin/echo", str(schema), authorized_workspace_root=request.working_directory)
        self.assertEqual(command.argv[:2], ("/bin/echo", "exec"))
        self.assertEqual(command.argv[-1], "-")
        self.assertNotIn("hello; --danger", command.argv)
        self.assertEqual(command.stdin, "hello; --danger")

    def test_sandbox_and_resume_are_explicit_and_unknown_values_fail_closed(self):
        schema = Path(__file__).parents[1] / "schemas" / "agent-result.schema.json"
        for request in self.request(sandbox=SandboxMode.WORKSPACE_WRITE):
            command = build_command(request, "/bin/echo", str(schema), resume_session_id="thread-1", authorized_workspace_root=request.working_directory)
        self.assertIn("workspace-write", command.argv)
        self.assertIn("resume", command.argv)
        with self.assertRaises(CommandValidationError):
            validate_session_id("thread-1 --sandbox danger-full-access")

    def test_reasoning_mapping_unknown_values_and_models_fail_closed(self):
        schema = Path(__file__).parents[1] / "schemas" / "agent-result.schema.json"
        for request in self.request(reasoning_effort="high"):
            command = build_command(request, "/bin/echo", str(schema), authorized_workspace_root=request.working_directory)
        self.assertEqual(command.argv[2:4], ("-c", "model_reasoning_effort=high"))
        for request in self.request(reasoning_effort="invented"):
            with self.assertRaises(CommandValidationError):
                build_command(request, "/bin/echo", str(schema), authorized_workspace_root=request.working_directory)
        for request in self.request(model="not-allowed"):
            with self.assertRaises(CommandValidationError):
                build_command(request, "/bin/echo", str(schema), allowed_models=("allowed",), authorized_workspace_root=request.working_directory)

    def test_reasoning_effort_is_bounded_and_prompt_config_text_stays_data(self):
        schema = Path(__file__).parents[1] / "schemas" / "agent-result.schema.json"
        prompt = '-c arbitrary=value\nmodel_reasoning_effort="ultra"'
        for effort in ("low", "medium", "high", "xhigh"):
            for request in self.request(reasoning_effort=effort, instructions=prompt):
                command = build_command(request, "/bin/echo", str(schema), authorized_workspace_root=request.working_directory)
            self.assertEqual(command.argv[2:4], ("-c", f"model_reasoning_effort={effort}"))
            self.assertNotIn(prompt, command.argv)
            self.assertEqual(command.stdin, prompt)
        for request in self.request(instructions=prompt):
            command = build_command(request, "/bin/echo", str(schema), authorized_workspace_root=request.working_directory)
        self.assertNotIn("-c", command.argv)

    def test_workspace_boundary_is_symlink_aware(self):
        schema = Path(__file__).parents[1] / "schemas" / "agent-result.schema.json"
        with tempfile.TemporaryDirectory() as base:
            root = Path(base) / "root"; root.mkdir()
            outside = Path(base) / "outside"; outside.mkdir()
            child = root / "child"; child.mkdir()
            for cwd in (root, str(child)):
                request = ExecutionRequest(RunId("run-1"), "x", WorkflowId("workflow-1"), "EXECUTOR", "implementation", cwd, SandboxMode.WORKSPACE_WRITE, timeout_seconds=1)
                build_command(request, "/bin/echo", str(schema), authorized_workspace_root=str(root))
            outside_request = ExecutionRequest(RunId("run-1"), "x", WorkflowId("workflow-1"), "EXECUTOR", "implementation", outside, SandboxMode.WORKSPACE_WRITE, timeout_seconds=1)
            with self.assertRaises(CommandValidationError): build_command(outside_request, "/bin/echo", str(schema), authorized_workspace_root=str(root))
            for invalid in (Path(base), root / "missing", root / "file"):
                if invalid.name == "file": invalid.write_text("x")
                invalid_request = ExecutionRequest(RunId("run-1"), "x", WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(invalid), SandboxMode.READ_ONLY, timeout_seconds=1)
                with self.assertRaises(CommandValidationError): build_command(invalid_request, "/bin/echo", str(schema), authorized_workspace_root=str(root))
            link = root / "escape"; link.symlink_to(outside, target_is_directory=True)
            escaped = ExecutionRequest(RunId("run-1"), "x", WorkflowId("workflow-1"), "EXECUTOR", "implementation", str(link), SandboxMode.READ_ONLY, timeout_seconds=1)
            with self.assertRaises(CommandValidationError): build_command(escaped, "/bin/echo", str(schema), authorized_workspace_root=str(root))

    def test_r4_runtime_contract_is_materialized_without_claiming_routing_or_publication(self):
        contract = json.loads((Path(__file__).parents[1] / "config" / "runtime-contract.json").read_text())
        self.assertEqual(contract["r4_cli_adapter"]["role"], "IMPLEMENTED_CANDIDATE_FALLBACK_DIAGNOSTIC")
        self.assertEqual(contract["r4_cli_adapter"]["structured_protocol"], "IMPLEMENTED_CANDIDATE")
        self.assertEqual(contract["r4_cli_adapter"]["session_resume"], "IMPLEMENTED_CANDIDATE")
        self.assertEqual(contract["r4_cli_adapter"]["timeout_cancellation_process_cleanup"], "IMPLEMENTED_CANDIDATE")
        self.assertEqual(contract["r4_cli_adapter"]["reasoning_effort"]["allowed_values"], ["low", "medium", "high", "xhigh"])
        self.assertEqual(contract["architecture"]["python_sdk"], "PRIMARY_ARCHITECTURE")
        self.assertEqual(contract["architecture"]["automatic_fallback_routing"], "NOT_IMPLEMENTED")
        self.assertEqual(contract["architecture"]["workflow_engine"], "NOT_IMPLEMENTED")
        self.assertEqual(contract["architecture"]["publisher"], "NOT_IMPLEMENTED")
        self.assertFalse(contract["auto_publish"])
