CREATE TABLE workflows (
    workflow_id TEXT PRIMARY KEY NOT NULL
);

CREATE TABLE phases (
    phase_id TEXT PRIMARY KEY NOT NULL,
    workflow_id TEXT NOT NULL REFERENCES workflows(workflow_id) ON DELETE RESTRICT,
    kind TEXT NOT NULL,
    UNIQUE (workflow_id, phase_id)
);

CREATE TABLE runs (
    run_id TEXT PRIMARY KEY NOT NULL,
    workflow_id TEXT NOT NULL REFERENCES workflows(workflow_id) ON DELETE RESTRICT,
    state TEXT NOT NULL,
    state_version INTEGER NOT NULL CHECK (state_version >= 0),
    UNIQUE (workflow_id, run_id)
);

CREATE TABLE attempts (
    attempt_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    workflow_id TEXT NOT NULL,
    phase_id TEXT NOT NULL REFERENCES phases(phase_id) ON DELETE RESTRICT,
    ordinal INTEGER NOT NULL CHECK (ordinal >= 0),
    UNIQUE (run_id, phase_id, ordinal),
    UNIQUE (run_id, attempt_id),
    FOREIGN KEY (workflow_id, run_id) REFERENCES runs(workflow_id, run_id) ON DELETE RESTRICT,
    FOREIGN KEY (workflow_id, phase_id) REFERENCES phases(workflow_id, phase_id) ON DELETE RESTRICT
);

CREATE TABLE transitions (
    transition_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    workflow_id TEXT NOT NULL,
    phase_id TEXT,
    previous_state TEXT NOT NULL,
    current_state TEXT NOT NULL,
    event TEXT,
    actor TEXT,
    created_at TEXT NOT NULL,
    gate_reference TEXT,
    evidence_reference TEXT,
    idempotency_key TEXT,
    state_version INTEGER,
    protected_resource_key TEXT,
    fencing_token INTEGER,
    idempotency_operation_kind TEXT,
    canonical_operation_identity TEXT,
    payload_fingerprint TEXT,
    UNIQUE (run_id, transition_id),
    FOREIGN KEY (workflow_id, run_id) REFERENCES runs(workflow_id, run_id) ON DELETE RESTRICT,
    FOREIGN KEY (workflow_id, phase_id) REFERENCES phases(workflow_id, phase_id) ON DELETE RESTRICT
);

CREATE TABLE checkpoints (
    checkpoint_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    workflow_id TEXT NOT NULL,
    phase_id TEXT,
    operational_state TEXT NOT NULL,
    created_at TEXT NOT NULL,
    resume_data_json TEXT NOT NULL,
    state_version INTEGER,
    base_reference TEXT,
    last_safe_transition_id TEXT,
    session_id TEXT,
    artifact_references_json TEXT NOT NULL,
    evidence_references_json TEXT NOT NULL,
    idempotency_key TEXT,
    idempotency_operation_kind TEXT,
    canonical_operation_identity TEXT,
    payload_fingerprint TEXT,
    UNIQUE (run_id, checkpoint_id),
    FOREIGN KEY (workflow_id, run_id) REFERENCES runs(workflow_id, run_id) ON DELETE RESTRICT,
    FOREIGN KEY (workflow_id, phase_id) REFERENCES phases(workflow_id, phase_id) ON DELETE RESTRICT,
    FOREIGN KEY (run_id, last_safe_transition_id) REFERENCES transitions(run_id, transition_id) ON DELETE RESTRICT,
    FOREIGN KEY (run_id, session_id) REFERENCES agent_sessions(run_id, session_id) ON DELETE RESTRICT
);
CREATE INDEX checkpoints_by_run_created ON checkpoints(run_id, created_at DESC, checkpoint_id DESC);

CREATE TABLE artifacts (
    artifact_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    reference TEXT NOT NULL
);

CREATE TABLE agent_sessions (
    session_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    opaque_reference TEXT NOT NULL,
    adapter TEXT,
    role TEXT,
    attempt_id TEXT,
    status TEXT,
    UNIQUE (run_id, session_id),
    FOREIGN KEY (run_id, attempt_id) REFERENCES attempts(run_id, attempt_id) ON DELETE RESTRICT
);
CREATE INDEX agent_sessions_by_run ON agent_sessions(run_id, session_id DESC);

CREATE TABLE usage_records (
    usage_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    adapter TEXT NOT NULL,
    measurements_json TEXT NOT NULL
);

CREATE TABLE failures (
    failure_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    category TEXT NOT NULL,
    message TEXT NOT NULL,
    retryable INTEGER NOT NULL CHECK (retryable IN (0, 1)),
    evidence_reference TEXT,
    recorded_at TEXT NOT NULL
);

CREATE TABLE human_decisions (
    decision_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    decision TEXT NOT NULL,
    reason TEXT,
    unresolved INTEGER NOT NULL CHECK (unresolved IN (0, 1)),
    resolution TEXT,
    requested_at TEXT,
    resolved_at TEXT,
    CHECK (
        (unresolved = 1 AND resolution IS NULL AND resolved_at IS NULL)
        OR (unresolved = 0 AND resolution IS NOT NULL AND resolved_at IS NOT NULL)
    )
);

CREATE TABLE leases (
    lease_id TEXT PRIMARY KEY NOT NULL,
    run_id TEXT NOT NULL REFERENCES runs(run_id) ON DELETE RESTRICT,
    holder TEXT NOT NULL,
    protected_resource_key TEXT NOT NULL,
    workspace_reference TEXT,
    purpose TEXT,
    issued_at TEXT NOT NULL,
    expires_at TEXT NOT NULL,
    fencing_token INTEGER NOT NULL CHECK (fencing_token > 0),
    version INTEGER NOT NULL CHECK (version >= 0),
    released_at TEXT
);
CREATE INDEX leases_by_run_active ON leases(run_id, released_at, expires_at);
CREATE UNIQUE INDEX one_active_lease_per_resource ON leases(protected_resource_key) WHERE released_at IS NULL;

CREATE TABLE idempotency_records (
    idempotency_key TEXT PRIMARY KEY NOT NULL,
    operation_kind TEXT NOT NULL,
    canonical_operation_identity TEXT NOT NULL,
    payload_fingerprint TEXT NOT NULL,
    applied_at TEXT NOT NULL,
    run_id TEXT NOT NULL,
    transition_id TEXT NOT NULL,
    checkpoint_id TEXT NOT NULL,
    resulting_state_version INTEGER NOT NULL CHECK (resulting_state_version >= 0),
    FOREIGN KEY (run_id, transition_id) REFERENCES transitions(run_id, transition_id) ON DELETE RESTRICT,
    FOREIGN KEY (run_id, checkpoint_id) REFERENCES checkpoints(run_id, checkpoint_id) ON DELETE RESTRICT
);
