package com.feelingpilates.transicion.programacion.comparison.v2;

import java.util.Optional;
import java.time.LocalDate;
import com.feelingpilates.transicion.programacion.detector.SourceSnapshot;

/** Operational failure of the whole supplied unit; never a semantic missing result. */
public final class ComparisonFailureV2 extends RuntimeException {
    public record SafeContext(Optional<String> runIdentity, Optional<String> invocationIdentity,
            Optional<LocalDate> date, Optional<String> sourceIdentity) { }
    private final String stage;
    private final SafeContext context;
    public ComparisonFailureV2(String stage, String message, Throwable cause) {
        this(stage,message,cause,new SafeContext(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()));
    }
    private ComparisonFailureV2(String stage,String message,Throwable cause,SafeContext context) {
        super(message,cause);this.stage=stage;this.context=context;
    }
    ComparisonFailureV2 withContext(ComparisonInputV2 input,SourceSnapshot source) {
        if(input==null)return this;
        return new ComparisonFailureV2(stage,getMessage(),getCause(),new SafeContext(Optional.of(input.runIdentity()),
                Optional.of(input.invocationIdentity()),Optional.of(input.date()),source==null?context.sourceIdentity():Optional.of(source.sourceIdentity())));
    }
    public String stage() { return stage; }
    public SafeContext context() { return context; }
    static void require(boolean valid, String stage, String rule) {
        if (!valid) throw new ComparisonFailureV2(stage, rule, null);
    }
}
