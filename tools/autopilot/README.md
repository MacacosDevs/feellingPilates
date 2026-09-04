# FeelingPilates Autopilot

R2 establishes the Python package boundary and language-neutral contracts for
the future Autopilot runtime.  It contains no execution adapter, workflow
engine, persistence implementation, Git integration, or product integration.

## Runtime contract

The tested R1 runtime baseline is recorded in
`config/runtime-contract.json`.  It is evidence, not an instruction to install
or invoke an SDK in R2.  In particular, `auto_publish` is `false`.

## Development checks

The package deliberately has no production dependencies.  Run its stdlib-only
contract suite from this directory:

```text
PYTHONPATH=src python -m unittest discover -s tests -v
```

Telemetry is intentionally conservative: a token class is populated only when
direct evidence exists for that exact class.  An unavailable class is `null`;
zero means directly observed zero and never means unknown.
