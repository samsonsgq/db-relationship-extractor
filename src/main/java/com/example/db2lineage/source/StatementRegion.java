package com.example.db2lineage.source;

import java.util.Objects;

/**
 * Tracks a statement's coarse source region for downstream line anchoring.
 */
public record StatementRegion(int startLine, int endLine, String statementText) {

    public StatementRegion {
        if (startLine < 1 || endLine < startLine) {
            throw new IllegalArgumentException("Invalid statement region");
        }
        Objects.requireNonNull(statementText, "statementText");
    }
}
