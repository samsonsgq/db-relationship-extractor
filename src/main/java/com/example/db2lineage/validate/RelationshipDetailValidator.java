package com.example.db2lineage.validate;

import com.example.db2lineage.model.RelationshipDetailRow;

import java.util.List;

/**
 * Lightweight starter validator for structural checks.
 */
public final class RelationshipDetailValidator {

    private RelationshipDetailValidator() {
    }

    public static void validate(List<RelationshipDetailRow> rows) {
        if (rows == null) {
            throw new IllegalArgumentException("rows must not be null");
        }
        // TODO: add contract-level checks (duplicate rows, sequence consistency, line fidelity, etc.).
    }
}
