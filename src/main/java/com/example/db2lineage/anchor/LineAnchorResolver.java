package com.example.db2lineage.anchor;

import com.example.db2lineage.source.SqlSourceFile;
import com.example.db2lineage.source.StatementRegion;

import java.util.Optional;

/**
 * Resolves final line_no and line_content from original SQL text.
 *
 * Intentionally keeps anchoring independent from parser node positions.
 */
public final class LineAnchorResolver {

    public Optional<StatementRegion> locateStatementRegion(SqlSourceFile sourceFile, String statementText) {
        if (statementText == null || statementText.isBlank()) {
            return Optional.empty();
        }

        for (int i = 0; i < sourceFile.lines().size(); i++) {
            if (sourceFile.lines().get(i).contains(statementText.trim())) {
                int lineNo = i + 1;
                return Optional.of(new StatementRegion(lineNo, lineNo, sourceFile.lineAt(lineNo)));
            }
        }
        return Optional.empty();
    }
}
