package com.feelingpilates.transicion.programacion.read;

import com.feelingpilates.transicion.programacion.detector.ProgrammingCandidateSnapshot;
import com.feelingpilates.transicion.programacion.detector.DetectorVocabulary;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import java.util.List;
import java.util.HashSet;

public record NominalProgrammingReadSet(List<ProgrammingCandidateSnapshot> candidates,
                                         List<NominalBackingSnapshot> backing) {
    public NominalProgrammingReadSet {
        try {
            candidates = List.copyOf(candidates); backing = List.copyOf(backing);
            if (candidates.size() != backing.size()) throw new IllegalArgumentException();
            var seen = new HashSet<ReferenciaOcurrencia>();
            for (int i = 0; i < candidates.size(); i++) {
                var c = candidates.get(i); var b = backing.get(i);
                if (!seen.add(c.reference()) || !c.reference().equals(b.reference())
                        || c.candidateType() != DetectorVocabulary.CandidateType.NOMINAL_OCCURRENCE
                        || !c.salonId().equals(b.salonId()) || !c.instructorId().equals(b.instructorId())
                        || !c.activityId().equals(b.activityId()) || !c.start().equals(b.assignmentStart())
                        || !c.end().equals(b.assignmentEnd())
                        || !c.provenance().recordIds().equals(List.of(b.assignmentId().toString(),b.blockId().toString()))) throw new IllegalArgumentException();
            }
        } catch (RuntimeException e) {
            throw new NominalReadFailure(NominalReadFailure.Category.CARDINALITY_OR_BACKING_MISMATCH,
                    null, List.of(), e);
        }
    }
}
