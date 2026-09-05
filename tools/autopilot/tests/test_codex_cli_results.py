import json
import unittest

from feelingpilates_autopilot.adapters.execution.result_parser import ResultProtocolError, parse_terminal
from feelingpilates_autopilot.domain.models import RunId, WorkflowId
from feelingpilates_autopilot.ports.agent_executor import ExecutionRequest, SandboxMode


class ResultsTests(unittest.TestCase):
    def request(self):
        return ExecutionRequest(RunId("run-1"), "x", WorkflowId("workflow-1"), "EXECUTOR", "implementation", "/tmp", SandboxMode.READ_ONLY, timeout_seconds=1)

    def event(self, payload):
        return json.dumps({"type": "item.completed", "item": {"type": "agent_message", "text": json.dumps(payload)}})

    def payload(self):
        return {"schema_version":"r2","workflow_id":"workflow-1","run_id":"run-1","role":"EXECUTOR","status":"SUCCEEDED","gate":"implementation","gate_result":"PASS","summary":"ok","findings":{"p0":[],"p1":[],"p2":[]},"test_evidence":[],"changed_paths":[],"recommendation":"audit","requires_human_decision":False,"p1_correctable":False,"correction_artifact":None}

    def test_unknown_missing_malformed_and_conflicting_results_fail_closed(self):
        for payload in (self.payload() | {"unknown": True}, self.payload() | {"summary": " "}):
            with self.assertRaises(ResultProtocolError): parse_terminal(self.event(payload), self.request())
        with self.assertRaises(ResultProtocolError): parse_terminal('{', self.request())
        with self.assertRaises(ResultProtocolError): parse_terminal(self.event(self.payload()) + '\n' + self.event(self.payload()), self.request())

    def test_session_and_runtime_usage_are_strict(self):
        payload = self.payload() | {"session_reference": "thread-1"}
        stdout = json.dumps({"type":"thread.started","thread_id":"thread-1"}) + '\n' + self.event(payload)
        self.assertEqual(parse_terminal(stdout, self.request()).session_id, "thread-1")
        with self.assertRaises(ResultProtocolError):
            parse_terminal(json.dumps({"type":"thread.started","thread_id":" "}) + '\n' + self.event(self.payload()), self.request())
        with self.assertRaises(ResultProtocolError):
            parse_terminal(self.event(self.payload() | {"usage_record":{"schema_version":"r2","adapter":"x","measurements":{}}}), self.request())

    def test_machine_channel_nested_evidence_enums_and_usage_are_strict(self):
        with self.assertRaises(ResultProtocolError): parse_terminal("human prose", self.request())
        whitespace = json.dumps({"type":"thread.started","thread_id":" thread-1 "}) + "\n" + self.event(self.payload())
        with self.assertRaises(ResultProtocolError): parse_terminal(whitespace, self.request())
        for evidence in ({"name": 1, "outcome": "PASS", "evidence": "x"}, {"name": "x", "outcome": "NOPE", "evidence": "x"}, {"name": "x", "outcome": "PASS", "evidence": []}):
            with self.assertRaises(ResultProtocolError): parse_terminal(self.event(self.payload() | {"test_evidence":[evidence]}), self.request())
        base = {"schema_version":"r2","adapter":"other","measurements":{}}
        with self.assertRaises(ResultProtocolError): parse_terminal(self.event(self.payload() | {"usage_record":base}), self.request())

    def test_complete_embedded_usage_validation_matrix(self):
        measurements = {key: {"value": None, "provenance": "UNAVAILABLE", "direct_evidence": None} for key in ("input_tokens", "cached_input_tokens", "output_tokens", "reasoning_output_tokens", "total_tokens")}
        usage = {"schema_version":"r2", "adapter":"codex-cli", "measurements":measurements}
        runtime = json.dumps({"type":"turn.completed", "usage":{}})
        self.assertIsNotNone(parse_terminal(runtime + "\n" + self.event(self.payload() | {"usage_record":usage}), self.request()).usage)
        cases = []
        cases.extend(([], "not-an-object", 7))
        cases.append({"schema_version":"r2", "adapter":"codex-cli", "measurements": []})
        cases.append({"schema_version":"r2", "adapter":"codex-cli", "measurements": measurements | {"unknown": {}}})
        bad_scalar = json.loads(json.dumps(usage)); bad_scalar["measurements"]["input_tokens"] = {"value": "1", "provenance":"OBSERVED", "direct_evidence":"x"}; cases.append(bad_scalar)
        bad_negative = json.loads(json.dumps(usage)); bad_negative["measurements"]["input_tokens"] = {"value": -1, "provenance":"OBSERVED", "direct_evidence":"x"}; cases.append(bad_negative)
        bad_bool = json.loads(json.dumps(usage)); bad_bool["measurements"]["input_tokens"] = {"value": True, "provenance":"OBSERVED", "direct_evidence":"x"}; cases.append(bad_bool)
        bad_null = json.loads(json.dumps(usage)); bad_null["measurements"]["input_tokens"] = {"value": None, "provenance":"OBSERVED", "direct_evidence":"x"}; cases.append(bad_null)
        bad_provenance = json.loads(json.dumps(usage)); bad_provenance["measurements"]["input_tokens"]["provenance"] = "DERIVED"; cases.append(bad_provenance)
        bad_partial = json.loads(json.dumps(usage)); del bad_partial["measurements"]["input_tokens"]["direct_evidence"]; cases.append(bad_partial)
        bad_nested_unknown = json.loads(json.dumps(usage)); bad_nested_unknown["measurements"]["input_tokens"]["unknown"] = True; cases.append(bad_nested_unknown)
        bad_source = json.loads(json.dumps(usage)); bad_source["adapter"] = "other"; cases.append(bad_source)
        for invalid in cases:
            with self.assertRaises(ResultProtocolError): parse_terminal(runtime + "\n" + self.event(self.payload() | {"usage_record":invalid}), self.request())
