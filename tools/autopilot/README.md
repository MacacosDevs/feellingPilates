# FeelingPilates Autopilot

R5 materializes an `openai-codex==0.147.0` Python SDK `AgentExecutor` as the
bounded **PRIMARY** adapter candidate. R4's Codex CLI adapter remains a
separately selectable **FALLBACK / DIAGNOSTIC** capability. There is no
automatic fallback routing, workflow engine, Git integration, or product
integration here.

## Runtime contract

The tested R1 runtime baseline is recorded in
`config/runtime-contract.json`.  It is evidence, not an instruction to install
or invoke an SDK in R2.  In particular, `auto_publish` is `false`.

## Development checks

The project-local dependency declaration pins the supported SDK. Normal tests
inject a deterministic fake SDK and never require a login, network call, or
provider turn. Run the suite from this directory:

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
without private adapter memory. The SDK adapter applies the same request
authority checks to new and resumed turns, validates the typed thread ID, and
persists it only as operational session evidence. It consumes the SDK's public
streaming surface with bounded event/result evidence. Each provider turn is
collected in a terminable child process: cancellation and timeout first request
SDK interruption and resource cleanup, then escalate through bounded process
termination when collection is non-cooperative. A terminal observation is
published only after that child is no longer alive, and an accepted cancellation
cannot be promoted to success. If bounded escalation cannot confirm containment,
the adapter reports a non-terminal `UNKNOWN` observation and authorizes no
workflow progression; it may terminalize later only after death is verified.
Adapter-owned parent observers and watchdogs stop after that bounded report;
later `get_result()` calls perform fresh bounded liveness checks without a
persistent background monitor.
New SDK thread identity is passed incrementally as validated operational
evidence, never as workflow authority. R5 remains an implementation candidate,
not an accepted or published runtime.
