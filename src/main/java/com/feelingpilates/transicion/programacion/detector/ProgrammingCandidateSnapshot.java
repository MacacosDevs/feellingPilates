package com.feelingpilates.transicion.programacion.detector;

import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;

public record ProgrammingCandidateSnapshot(
        ReferenciaOcurrencia reference,
        DetectorVocabulary.CandidateType candidateType,
        String snapshotIdentity,
        String candidateFingerprint,
        UUID salonId,
        UUID instructorId,
        UUID activityId,
        LocalTime start,
        LocalTime end,
        Map<String, String> observableFields,
        EvidenceProvenance provenance) {

    public ProgrammingCandidateSnapshot {
        reference = required(reference, "reference");
        required(reference.tipo(), "reference.tipo");
        required(reference.id(), "reference.id");
        required(reference.fecha(), "reference.fecha");
        candidateType = required(candidateType, "candidateType");
        snapshotIdentity = requiredText(snapshotIdentity, "snapshotIdentity");
        candidateFingerprint = requiredText(candidateFingerprint, "candidateFingerprint");
        salonId = required(salonId, "salonId");
        instructorId = required(instructorId, "instructorId");
        activityId = required(activityId, "activityId");
        start = required(start, "start");
        end = required(end, "end");
        if (!end.isAfter(start)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.INVALID_INTERVAL,
                    "candidate interval must be positive and half-open");
        }
        observableFields = Map.copyOf(required(observableFields, "observableFields"));
        provenance = required(provenance, "provenance");
    }
}
