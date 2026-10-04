package com.feelingpilates.transicion.programacion.r4.read;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotIdentifiers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Evidence carrier only. Value equality or construction grants no owner authority. */
public record AdjustmentReadSnapshotContext(
        String runIdentity, String attemptIdentity, LocalDate fecha, ZoneId businessZone,
        String ruleCatalogVersion, String sourceName, String schemaFingerprint,
        String databaseName, String schemaName, String principal, String physicalResourceIdentity,
        ProjectionCatalogVersion projectionCatalogVersion, String readerInvocationIdentity,
        SnapshotClaim snapshotClaim, String snapshotEvidenceId, String statementCaptureCommitment) {
    public enum ProjectionCatalogVersion { R4_ADJUSTMENT_V1 }
    public enum SnapshotClaim { R4_INTERNAL_RR_TEST }
    public AdjustmentReadSnapshotContext {
        try {
            for (String s : new String[]{runIdentity, attemptIdentity, ruleCatalogVersion, sourceName,
                    schemaFingerprint, databaseName, schemaName, principal, physicalResourceIdentity,
                    readerInvocationIdentity, snapshotEvidenceId, statementCaptureCommitment}) {
                if (s == null || s.isBlank() || s.indexOf('\0') >= 0) throw new IllegalArgumentException();
                ReadSnapshotIdentifiers.secuenciaTextos(s);
            }
            if (fecha == null || businessZone == null
                    || projectionCatalogVersion != ProjectionCatalogVersion.R4_ADJUSTMENT_V1
                    || snapshotClaim != SnapshotClaim.R4_INTERNAL_RR_TEST
                    || !schemaFingerprint.matches("sha256:[0-9a-f]{64}")
                    || !snapshotEvidenceId.matches("[0-9a-f]{64}")
                    || !statementCaptureCommitment.matches("[0-9a-f]{64}")) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.INVALID_INPUT, fecha, java.util.List.of(), e);
        }
    }
    public String scopeCanonical() { return "R4_ACTIVE_ADJUSTMENTS_ON_DATE_V1/" + fecha; }
    public String executionProvenanceId() {
        return hash("F2E-R4-EXECUTION-V1", runIdentity, attemptIdentity, scopeCanonical(), businessZone.getId(),
                ruleCatalogVersion, sourceName, schemaFingerprint, databaseName, schemaName, principal,
                physicalResourceIdentity, projectionCatalogVersion.name(), readerInvocationIdentity,
                snapshotClaim.name(), snapshotEvidenceId, statementCaptureCommitment);
    }
    public static String hash(String... fields) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(ReadSnapshotIdentifiers.secuenciaTextos(fields))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
