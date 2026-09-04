import json
from pathlib import Path
import unittest


class RuntimeContractTests(unittest.TestCase):
    def test_r1_runtime_baselines_are_preserved(self) -> None:
        path = Path(__file__).parents[1] / "config" / "runtime-contract.json"
        contract = json.loads(path.read_text())
        self.assertEqual(contract["runtime"]["python"], "3.14-compatible")
        self.assertEqual(contract["primary_sdk"], {"package": "openai-codex", "tested_version": "0.147.0"})
        self.assertEqual(contract["fallback_cli"], {"package": "codex-cli", "tested_version": "0.150.1"})
        self.assertEqual(contract["state_backend"]["package"], "sqlite3")
        self.assertEqual(contract["state_backend"]["source"], "stdlib")
        self.assertEqual(contract["state_backend"]["tested_sqlite_baseline"], "3.53.3")
        self.assertFalse(contract["auto_publish"])

    def test_telemetry_contract_forbids_fabrication(self) -> None:
        path = Path(__file__).parents[1] / "config" / "runtime-contract.json"
        telemetry = json.loads(path.read_text())["telemetry"]
        self.assertIsNone(telemetry["unavailable_token_classes"])
        self.assertEqual(telemetry["synthetic_zero_for_unavailable"], "forbidden")
        self.assertEqual(telemetry["fabricated_usage"], "forbidden")
        self.assertEqual(telemetry["heuristic_decomposition_as_observed"], "forbidden")
        self.assertTrue(telemetry["observed_value_requires_direct_evidence"])
        self.assertTrue(telemetry["derived_or_estimated_usage_must_be_explicitly_labeled"])
