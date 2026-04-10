package com.example.db2lineage.extract;

import com.example.db2lineage.metadata.ObjectMetadataRegistry;
import com.example.db2lineage.model.RelationshipDetailRow;
import com.example.db2lineage.source.SqlSourceFile;
import net.sf.jsqlparser.statement.Statement;

import java.util.List;
import java.util.Objects;

/**
 * Placeholder for UPDATE direct usage and mapping extraction.
 */
public final class UpdateExtractor {

    private final ObjectMetadataRegistry metadataRegistry;

    public UpdateExtractor(ObjectMetadataRegistry metadataRegistry) {
        this.metadataRegistry = Objects.requireNonNull(metadataRegistry, "metadataRegistry");
    }

    public List<RelationshipDetailRow> extract(Statement statement, SqlSourceFile sourceFile) {
        Objects.requireNonNull(statement, "statement");
        Objects.requireNonNull(sourceFile, "sourceFile");
        // TODO: implement direct single-hop extraction.
        return List.of();
    }
}
