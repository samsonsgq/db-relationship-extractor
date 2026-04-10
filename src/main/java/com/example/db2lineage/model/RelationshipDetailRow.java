package com.example.db2lineage.model;

import java.util.Objects;

/**
 * Canonical row model for relationship_detail.tsv.
 *
 * Note: intentionally excludes removed fields
 * (persistent_target_objects, intermediate_target_objects).
 */
public record RelationshipDetailRow(
        String sourceObjectType,
        String sourceObject,
        String sourceField,
        TargetObjectType targetObjectType,
        String targetObject,
        String targetField,
        RelationshipType relationship,
        int lineNo,
        int lineRelationSeq,
        String lineContent,
        ExtractionConfidence confidence
) {

    public RelationshipDetailRow {
        sourceObjectType = safe(sourceObjectType);
        sourceObject = safe(sourceObject);
        sourceField = safe(sourceField);
        targetObject = safe(targetObject);
        targetField = safe(targetField);
        lineContent = safe(lineContent);

        Objects.requireNonNull(targetObjectType, "targetObjectType");
        Objects.requireNonNull(relationship, "relationship");
        Objects.requireNonNull(confidence, "confidence");

        if (lineNo < 1) {
            throw new IllegalArgumentException("lineNo must be >= 1");
        }
        if (lineRelationSeq < 0) {
            throw new IllegalArgumentException("lineRelationSeq must be >= 0");
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
