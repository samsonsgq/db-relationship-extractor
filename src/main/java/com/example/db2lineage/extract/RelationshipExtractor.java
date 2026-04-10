package com.example.db2lineage.extract;

import com.example.db2lineage.metadata.ObjectMetadataRegistry;
import com.example.db2lineage.model.RelationshipDetailRow;
import com.example.db2lineage.source.SqlSourceFile;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrates extraction of direct single-hop relationships.
 *
 * Skeleton only: parses statements and delegates to specialized extractors.
 */
public final class RelationshipExtractor {

    private final InsertSelectExtractor insertSelectExtractor;
    private final UpdateExtractor updateExtractor;

    private RelationshipExtractor(InsertSelectExtractor insertSelectExtractor,
                                  UpdateExtractor updateExtractor) {
        this.insertSelectExtractor = insertSelectExtractor;
        this.updateExtractor = updateExtractor;
    }

    public static RelationshipExtractor defaultExtractor(ObjectMetadataRegistry metadataRegistry) {
        Objects.requireNonNull(metadataRegistry, "metadataRegistry");
        return new RelationshipExtractor(
                new InsertSelectExtractor(metadataRegistry),
                new UpdateExtractor(metadataRegistry)
        );
    }

    public List<RelationshipDetailRow> extract(SqlSourceFile sourceFile) {
        List<RelationshipDetailRow> rows = new ArrayList<>();
        for (Statement statement : parseStatements(sourceFile.content())) {
            rows.addAll(insertSelectExtractor.extract(statement, sourceFile));
            rows.addAll(updateExtractor.extract(statement, sourceFile));
        }
        return rows;
    }

    private static List<Statement> parseStatements(String sql) {
        try {
            return CCJSqlParserUtil.parseStatements(sql).getStatements();
        } catch (Exception parseError) {
            return List.of();
        }
    }
}
