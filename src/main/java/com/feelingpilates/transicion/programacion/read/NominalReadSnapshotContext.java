package com.feelingpilates.transicion.programacion.read;

import java.time.LocalDate;
import java.time.ZoneId;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Evidence carrier only. Value equality or construction grants no owner authority. */
public record NominalReadSnapshotContext(
        String runIdentity, String attemptIdentity, LocalDate fecha, ZoneId businessZone,
        String ruleCatalogVersion, String sourceName, String schemaFingerprint,
        String databaseName, String schemaName, String principal, String physicalResourceIdentity,
        ProjectionCatalogVersion projectionCatalogVersion, String readerInvocationIdentity,
        SnapshotClaim snapshotClaim, String snapshotEvidenceId, String statementCaptureCommitment) {
    public enum ProjectionCatalogVersion { R3_NOMINAL_V1 }
    public enum SnapshotClaim { R3_INTERNAL_RR_TEST }
    public NominalReadSnapshotContext {
        try {
            for (String s : new String[]{runIdentity, attemptIdentity, ruleCatalogVersion, sourceName,
                    schemaFingerprint, databaseName, schemaName, principal, physicalResourceIdentity,
                    readerInvocationIdentity, snapshotEvidenceId, statementCaptureCommitment}) {
                if (s == null || s.isBlank() || s.indexOf('\0') >= 0) throw new IllegalArgumentException();
                ReadSnapshotIdentifiers.secuenciaTextos(s);
            }
            if (fecha == null || businessZone == null
                    || projectionCatalogVersion != ProjectionCatalogVersion.R3_NOMINAL_V1
                    || snapshotClaim != SnapshotClaim.R3_INTERNAL_RR_TEST
                    || !schemaFingerprint.matches("sha256:[0-9a-f]{64}")
                    || !snapshotEvidenceId.matches("[0-9a-f]{64}")
                    || !statementCaptureCommitment.matches("[0-9a-f]{64}")) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new NominalReadFailure(NominalReadFailure.Category.INVALID_INPUT, fecha, java.util.List.of(), e);
        }
    }
    public short dayOfWeek() { return (short) (fecha.getDayOfWeek().getValue() % 7); }
    public String scopeCanonical() { return "R3_NOMINAL_ON_DATE_V1/" + fecha + "/" + dayOfWeek(); }
    public String executionProvenanceId() {
        return hash("F2E-R3-EXECUTION-V1", runIdentity, attemptIdentity, sourceName, schemaFingerprint,
                ruleCatalogVersion, businessZone.getId(), scopeCanonical(), readerInvocationIdentity,
                databaseName, schemaName, principal, physicalResourceIdentity,
                projectionCatalogVersion.name(), snapshotClaim.name(), snapshotEvidenceId, statementCaptureCommitment);
    }
    public static String hash(String... fields) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(ReadSnapshotIdentifiers.secuenciaTextos(fields))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
