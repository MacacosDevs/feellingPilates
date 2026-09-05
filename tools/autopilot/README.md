# FeelingPilates Autopilot

R4 materializes the Codex CLI `AgentExecutor` as a bounded **FALLBACK /
DIAGNOSTIC** adapter. Python SDK remains the selected primary architecture.
There is no automatic fallback routing, workflow engine, Git integration, or
product integration here.

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
direct evidence exists for that exact class. An unavailable class is `null`;
zero means directly observed zero and never means unknown. The CLI adapter uses
explicit argv, no shell, an explicit working directory, bounded separate stdout
and stderr capture, strict JSONL/schema validation, and process-group cleanup.
Both sandbox modes are bounded to a trusted configured workspace root after
symlink-aware resolution. The only supported reasoning-effort mechanism is the
allowlisted `-c model_reasoning_effort=value` argv pair; no generic config or
argument escape hatch exists. A handle carries caller-persistable request
context, enabling a new adapter instance to resume a validated same-run session
without private adapter memory.
