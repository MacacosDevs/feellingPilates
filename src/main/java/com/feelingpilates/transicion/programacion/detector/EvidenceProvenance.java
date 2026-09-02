package com.feelingpilates.transicion.programacion.detector;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.required;
import static com.feelingpilates.transicion.programacion.detector.DetectorValidation.requiredText;

public record EvidenceProvenance(
        String sourceName,
        String schemaFingerprint,
        List<String> recordIds,
        String ruleId,
        String ruleVersion,
        String businessTimeContext,
        Map<String, String> normalizedFields) {

    public EvidenceProvenance {
        sourceName = requiredText(sourceName, "sourceName");
        schemaFingerprint = requiredText(schemaFingerprint, "schemaFingerprint");
        recordIds = List.copyOf(required(recordIds, "recordIds"));
        if (recordIds.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.REQUIRED_VALUE_MISSING,
                    "recordIds cannot contain blank values");
        }
        ruleId = requiredText(ruleId, "ruleId");
        ruleVersion = requiredText(ruleVersion, "ruleVersion");
        businessTimeContext = requiredText(businessTimeContext, "businessTimeContext");
        normalizedFields = Map.copyOf(required(normalizedFields, "normalizedFields"));
        if (normalizedFields.entrySet().stream().anyMatch(entry ->
                entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null)) {
            throw new DetectorInputInvalidException(
                    DetectorVocabulary.InputErrorCode.REQUIRED_VALUE_MISSING,
                    "normalizedFields must contain non-blank keys and non-null values");
        }
    }

    public String semanticHash() {
        return SemanticHash.sha256(sourceName + '|' + schemaFingerprint + '|'
                + String.join(",", recordIds.stream().sorted().toList()) + '|'
                + ruleId + '|' + ruleVersion + '|' + businessTimeContext + '|'
                + new TreeMap<>(normalizedFields));
    }
}
