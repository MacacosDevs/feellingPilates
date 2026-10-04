package com.feelingpilates.transicion.programacion.detector;

import java.util.Map;

public sealed interface SourceSnapshot permits GenericSourceSnapshot, ReservationSourceSnapshot {

    DetectorVocabulary.SourceSystem sourceSystem();

    DetectorVocabulary.SourceAtomType sourceAtomType();

    String sourceIdentity();

    String snapshotIdentity();

    String sourceFingerprint();

    Map<String, String> observableFields();

    EvidenceProvenance provenance();
}
