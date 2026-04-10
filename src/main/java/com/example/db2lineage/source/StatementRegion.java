package com.example.db2lineage.source;

import java.util.Objects;

/**
 * Tracks a statement's coarse source region for downstream parsing and line anchoring.
 */
public record StatementRegion(
        String sourceObjectName,
        String sourceObjectType,
        int startLine,
        int endLine,
        String statementText,
        String statementType
) {

    public StatementRegion {
        Objects.requireNonNull(sourceObjectName, "sourceObjectName");
        Objects.requireNonNull(sourceObjectType, "sourceObjectType");
        if (startLine < 1 || endLine < startLine) {
            throw new IllegalArgumentException("Invalid statement region");
        }
        Objects.requireNonNull(statementText, "statementText");
    }
}
