import unittest


class ImportTests(unittest.TestCase):
    def test_package_imports_without_adapters(self) -> None:
        import feelingpilates_autopilot
        import feelingpilates_autopilot.domain
        import feelingpilates_autopilot.ports

        self.assertEqual(feelingpilates_autopilot.OperationalState.PENDING, "PENDING")
