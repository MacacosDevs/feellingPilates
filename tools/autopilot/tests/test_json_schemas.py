import json
from pathlib import Path
import unittest


SCHEMAS = Path(__file__).parents[1] / "schemas"


def validate_agent_result_instance(instance: object, schema: dict[str, object]) -> bool:
    """Validate the deliberate R2 subset without adding a JSON Schema runtime."""
    if not isinstance(instance, dict):
        return False
    properties = schema["properties"]
    required = set(schema["required"])
    if set(instance) - set(properties) or not required <= set(instance):
        return False
    if instance["schema_version"] != properties["schema_version"]["const"]:
        return False
    for name in ("workflow_id", "run_id", "role", "gate", "summary", "recommendation"):
        if not isinstance(instance[name], str) or not instance[name].strip():
            return False
    if instance["status"] not in properties["status"]["enum"]:
        return False
    if instance["gate_result"] not in properties["gate_result"]["enum"]:
        return False
    findings = instance["findings"]
    finding_schema = properties["findings"]
    if not isinstance(findings, dict) or set(findings) != set(finding_schema["required"]):
        return False
    if any(not isinstance(values, list) or any(not isinstance(item, str) for item in values) for values in findings.values()):
        return False
    evidence_schema = properties["test_evidence"]["items"]
    if not isinstance(instance["test_evidence"], list):
        return False
    for evidence in instance["test_evidence"]:
        if not isinstance(evidence, dict) or set(evidence) != set(evidence_schema["required"]):
            return False
        if not isinstance(evidence["name"], str) or not evidence["name"]:
            return False
        if evidence["outcome"] not in evidence_schema["properties"]["outcome"]["enum"]:
            return False
        if not isinstance(evidence["evidence"], str) or not evidence["evidence"]:
            return False
    if not isinstance(instance["changed_paths"], list) or any(
        not isinstance(path, str) or not path for path in instance["changed_paths"]
    ):
        return False
    if not isinstance(instance["requires_human_decision"], bool) or not isinstance(instance["p1_correctable"], bool):
        return False
    if instance["p1_correctable"]:
        return isinstance(instance["correction_artifact"], str) and bool(instance["correction_artifact"])
    return instance["correction_artifact"] is None


class JsonSchemaTests(unittest.TestCase):
    def test_all_initial_schemas_are_json_objects_with_r2_versions(self) -> None:
        expected = {"agent-result.schema.json", "checkpoint.schema.json", "usage-record.schema.json", "workflow.schema.json"}
        self.assertEqual({path.name for path in SCHEMAS.glob("*.json")}, expected)
        for path in SCHEMAS.glob("*.json"):
            schema = json.loads(path.read_text())
            self.assertEqual(schema["$schema"], "https://json-schema.org/draft/2020-12/schema")
            self.assertEqual(schema["properties"]["schema_version"]["const"], "r2")

    def test_usage_schema_preserves_null_and_observed_zero_semantics(self) -> None:
        schema = json.loads((SCHEMAS / "usage-record.schema.json").read_text())
        token = schema["$defs"]["token_measurement"]
        self.assertEqual(token["properties"]["value"]["type"], ["integer", "null"])
        self.assertEqual(token["properties"]["provenance"]["enum"], ["OBSERVED", "UNAVAILABLE"])
        observed_rule, unavailable_rule = token["allOf"]
        self.assertEqual(observed_rule["then"]["properties"]["value"]["minimum"], 0)
        self.assertEqual(unavailable_rule["then"]["properties"]["value"]["type"], "null")

    def test_agent_result_has_the_full_gate_protocol_and_reuses_usage_schema(self) -> None:
        schema = json.loads((SCHEMAS / "agent-result.schema.json").read_text())
        required = {
            "workflow_id",
            "role",
            "status",
            "gate",
            "gate_result",
            "findings",
            "test_evidence",
            "changed_paths",
            "recommendation",
            "requires_human_decision",
            "p1_correctable",
            "correction_artifact",
        }
        self.assertTrue(required <= set(schema["required"]))
        self.assertEqual(set(schema["properties"]["findings"]["required"]), {"p0", "p1", "p2"})
        self.assertEqual(
            schema["properties"]["gate_result"]["enum"],
            ["NOT_APPLICABLE", "PENDING", "PASS", "FAIL", "BLOCKED", "STOP"],
        )
        self.assertEqual(schema["properties"]["summary"]["minLength"], 1)
        self.assertEqual(schema["properties"]["summary"]["pattern"], ".*\\S.*")
        self.assertFalse(schema["additionalProperties"])
        self.assertEqual(
            schema["properties"]["usage_record"]["anyOf"][0]["$ref"],
            "usage-record.schema.json",
        )
        correction_rule = schema["allOf"][0]
        self.assertEqual(correction_rule["if"]["properties"]["p1_correctable"]["const"], True)
        self.assertEqual(
            correction_rule["then"]["properties"]["correction_artifact"]["type"],
            "string",
        )

    def test_protocol_compliant_agent_result_instance_is_accepted(self) -> None:
        schema = json.loads((SCHEMAS / "agent-result.schema.json").read_text())
        instance = {
            "schema_version": "r2",
            "workflow_id": "workflow-1",
            "run_id": "run-1",
            "role": "AUDITOR",
            "status": "SUCCEEDED",
            "gate": "implementation",
            "gate_result": "PASS",
            "summary": "Independent audit found no blocking issue.",
            "findings": {"p0": [], "p1": [], "p2": []},
            "test_evidence": [{"name": "unit", "outcome": "PASS", "evidence": "unittest output"}],
            "changed_paths": [],
            "recommendation": "Proceed to the next applicable gate.",
            "requires_human_decision": False,
            "p1_correctable": False,
            "correction_artifact": None,
        }
        self.assertTrue(validate_agent_result_instance(instance, schema))

    def test_agent_result_instance_rejects_required_protocol_violations(self) -> None:
        schema = json.loads((SCHEMAS / "agent-result.schema.json").read_text())
        valid = {
            "schema_version": "r2", "workflow_id": "workflow-1", "run_id": "run-1", "role": "AUDITOR",
            "status": "SUCCEEDED", "gate": "implementation", "gate_result": "PASS", "summary": "Pass.",
            "findings": {"p0": [], "p1": [], "p2": []}, "test_evidence": [], "changed_paths": [],
            "recommendation": "Proceed.", "requires_human_decision": False,
            "p1_correctable": False, "correction_artifact": None,
        }
        missing_workflow = valid.copy()
        del missing_workflow["workflow_id"]
        missing_gate_result = valid.copy()
        del missing_gate_result["gate_result"]
        invalid_gate_result = valid | {"gate_result": "SKIPPED"}
        empty_summary = valid | {"summary": ""}
        whitespace_summary = valid | {"summary": "   "}
        unknown_field = valid | {"unexpected": "not contracted"}
        for invalid in (missing_workflow, missing_gate_result, invalid_gate_result, empty_summary, whitespace_summary, unknown_field):
            self.assertFalse(validate_agent_result_instance(invalid, schema))

    def test_checkpoint_schema_matches_the_domain_checkpoint_contract(self) -> None:
        schema = json.loads((SCHEMAS / "checkpoint.schema.json").read_text())
        self.assertEqual(
            set(schema["required"]),
            {"schema_version", "checkpoint_id", "run_id", "operational_state", "created_at"},
        )
        self.assertEqual(schema["properties"]["resume_data"]["additionalProperties"], {"type": "string"})
