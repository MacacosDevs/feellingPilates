package com.feelingpilates.transicion.programacion.comparison.v2;

/** Operational failure of the whole supplied unit; never a semantic missing result. */
public final class ComparisonFailureV2 extends RuntimeException {
    private final String stage;
    public ComparisonFailureV2(String stage, String message, Throwable cause) {
        super(message, cause); this.stage = stage;
    }
    public String stage() { return stage; }
    static void require(boolean valid, String stage, String rule) {
        if (!valid) throw new ComparisonFailureV2(stage, rule, null);
    }
}
