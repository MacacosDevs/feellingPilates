package com.feelingpilates.transicion.programacion.detector;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceAtomType.RESERVA;
import static com.feelingpilates.transicion.programacion.detector.DetectorVocabulary.SourceSystem.LEGACY;

public record ReservationSourceSnapshot(
        UUID reservationId,
        DetectorVocabulary.ReservationState state,
        LocalDate date,
        UUID salonId,
        UUID instructorId,
        UUID activityId,
        ReservedSubinterval reservedSubinterval,
        String snapshotIdentity,
        String sourceFingerprint,
        Map<String, String> additionalObservableFields,
        Optional<HistoricalProgrammingTargetSnapshot> historicalProgrammingTarget,
        EvidenceProvenance provenance) implements SourceSnapshot {

    public ReservationSourceSnapshot {
        reservationId = required(reservationId, "reservationId");
        state = required(state, "state");
        date = required(date, "date");
        salonId = required(salonId, "salonId");
        instructorId = required(instructorId, "instructorId");
        activityId = required(activityId, "activityId");
        reservedSubinterval = required(reservedSubinterval, "reservedSubinterval");
        snapshotIdentity = requiredText(snapshotIdentity, "snapshotIdentity");
        sourceFingerprint = requiredText(sourceFingerprint, "sourceFingerprint");
        additionalObservableFields = Map.copyOf(required(
                additionalObservableFields, "additionalObservableFields"));
        historicalProgrammingTarget = required(historicalProgrammingTarget, "historicalProgrammingTarget");
        provenance = required(provenance, "provenance");
    }

    public ReservationSourceSnapshot(
            UUID reservationId,
            DetectorVocabulary.ReservationState state,
            LocalDate date,
            UUID salonId,
            UUID instructorId,
            UUID activityId,
            ReservedSubinterval reservedSubinterval,
            String snapshotIdentity,
            String sourceFingerprint,
            Map<String, String> additionalObservableFields,
            EvidenceProvenance provenance) {
        this(reservationId, state, date, salonId, instructorId, activityId, reservedSubinterval,
                snapshotIdentity, sourceFingerprint, additionalObservableFields, Optional.empty(), provenance);
    }

    @Override
    public DetectorVocabulary.SourceSystem sourceSystem() {
        return LEGACY;
    }

    @Override
    public DetectorVocabulary.SourceAtomType sourceAtomType() {
        return RESERVA;
    }

    @Override
    public String sourceIdentity() {
        return reservationId.toString();
    }

    @Override
    public Map<String, String> observableFields() {
        Map<String, String> fields = new LinkedHashMap<>(additionalObservableFields);
        fields.put("state", state.name());
        fields.put("date", date.toString());
        fields.put("salonId", salonId.toString());
        fields.put("instructorId", instructorId.toString());
        fields.put("activityId", activityId.toString());
        return Map.copyOf(fields);
    }
}
