package com.feelingpilates.transicion.programacion.adapter.jpa.policy;

import java.util.Optional;
import java.util.regex.Pattern;

public final class F2eSqlPolicyViolationException extends RuntimeException {

    private static final Pattern HUELLA = Pattern.compile("[0-9a-f]{64}");

    private final Reason reason;
    private final Optional<String> catalogStatementId;

    public F2eSqlPolicyViolationException(Reason reason, String catalogStatementId) {
        super("F2E SQL policy violation: " + java.util.Objects.requireNonNull(reason, "reason").name());
        if (catalogStatementId != null && !HUELLA.matcher(catalogStatementId).matches()) {
            throw new IllegalArgumentException("catalogStatementId must be a lower-case SHA-256 digest");
        }
        this.reason = reason;
        this.catalogStatementId = Optional.ofNullable(catalogStatementId);
    }

    public Reason reason() { return reason; }
    public Optional<String> catalogStatementId() { return catalogStatementId; }

    public enum Reason {
        NORMALIZATION_REJECTED,
        CATALOG_MISS,
        STATEMENT_CLASS_DENIED,
        DENYLIST_VIOLATION
    }
}
