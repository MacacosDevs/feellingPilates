package com.feelingpilates.transicion.programacion.r5.composition;

import java.time.*;
import java.util.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.GenericSourceSnapshot;
import com.feelingpilates.transicion.programacion.read.NominalBackingSnapshot;
import com.feelingpilates.transicion.programacion.r4.read.AdjustmentBackingSnapshot;

/** Whole-unit rejection with immutable original evidence; never a partial result. */
public final class EffectiveCompositionFailure extends RuntimeException {
    public enum Category {
        COMPOSITION_INPUT_INVALID, INPUT_EVIDENCE_INCOMPLETE, INPUT_ENVELOPE_INVALID, TARGET_NOMINAL_MISSING,
        AMBIGUOUS_OR_CONTRADICTORY_INPUT, READ_SET_INVARIANT_VIOLATION, EFFECTIVE_SET_CONFLICT, CANONICALIZATION_FAILURE
    }
    private final Category category;
    private final LocalDate date;
    private final List<ReferenciaOcurrencia> references;
    private final List<UUID> ids;
    private final String invariant;
    private final EffectiveCompositionInput evidence;
    public EffectiveCompositionFailure(Category category, LocalDate date, List<ReferenciaOcurrencia> references,
            List<UUID> ids, String invariant, EffectiveCompositionInput evidence, Throwable cause) {
        super(category+": "+invariant, cause);
        this.category=Objects.requireNonNull(category); this.date=date;
        this.references=references.stream().sorted(EffectiveCompositionCanonicalizer.REFERENCE_ORDER).toList();
        this.ids=ids.stream().sorted(EffectiveCompositionCanonicalizer.UUID_ORDER).toList();
        this.invariant=Objects.requireNonNull(invariant); this.evidence=evidence;
    }
    public Category category() { return category; }
    public LocalDate date() { return date; }
    public List<ReferenciaOcurrencia> references() { return references; }
    public List<UUID> ids() { return ids; }
    public String invariant() { return invariant; }
    public EffectiveCompositionInput evidence() { return evidence; }
}
