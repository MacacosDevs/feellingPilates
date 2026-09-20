package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import java.util.ArrayList;
import java.util.List;

import static com.feelingpilates.programacion.dominio.ReferenciaOcurrencia.Tipo.AJUSTE;
import static com.feelingpilates.programacion.dominio.ReferenciaOcurrencia.Tipo.SERIE_ASIGNACION;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.CandidateType.ADDITION_OCCURRENCE;

public final class F2DAuthorityGuard {

    public F2DCompatibilityResult evaluate(ProgrammingCandidateSnapshot candidate) {
        DetectorValidation.required(candidate, "candidate");
        ReferenciaOcurrencia.Tipo expected = candidate.candidateType() == ADDITION_OCCURRENCE
                ? AJUSTE
                : SERIE_ASIGNACION;
        if (candidate.reference().tipo() == expected) {
            return F2DCompatibilityResult.compatible();
        }
        return F2DCompatibilityResult.conflict(List.of(
                "candidate type " + candidate.candidateType()
                        + " requires reference type " + expected
                        + " but received " + candidate.reference().tipo()));
    }

    public F2DCompatibilityResult combine(List<F2DCompatibilityResult> results) {
        List<F2DCompatibilityResult> immutable = List.copyOf(
                DetectorValidation.required(results, "results"));
        List<String> conflicts = new ArrayList<>();
        immutable.stream()
                .filter(F2DCompatibilityResult::blocking)
                .forEach(result -> conflicts.addAll(result.conflictDetails()));
        return conflicts.isEmpty()
                ? F2DCompatibilityResult.compatible()
                : F2DCompatibilityResult.conflict(List.copyOf(conflicts));
    }
}
