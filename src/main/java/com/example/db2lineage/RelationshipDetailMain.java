package com.example.db2lineage;

import com.example.db2lineage.extract.RelationshipExtractor;
import com.example.db2lineage.io.RelationshipDetailTsvWriter;
import com.example.db2lineage.metadata.ObjectMetadataRegistry;
import com.example.db2lineage.model.RelationshipDetailRow;
import com.example.db2lineage.source.SqlSourceFile;
import com.example.db2lineage.validate.RelationshipDetailValidator;

import java.nio.file.Path;
import java.util.List;

/**
 * Starter entry point for DB2 relationship_detail.tsv extraction.
 */
public final class RelationshipDetailMain {

    private RelationshipDetailMain() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: RelationshipDetailMain <input-sql-file> <output-tsv-file>");
            return;
        }

        SqlSourceFile sourceFile = SqlSourceFile.fromPath(Path.of(args[0]));
        ObjectMetadataRegistry metadataRegistry = ObjectMetadataRegistry.empty();
        RelationshipExtractor extractor = RelationshipExtractor.defaultExtractor(metadataRegistry);

        List<RelationshipDetailRow> rows = extractor.extract(sourceFile);
        RelationshipDetailValidator.validate(rows);
        RelationshipDetailTsvWriter.write(Path.of(args[1]), rows);

        System.out.printf("Generated %d relationship row(s).%n", rows.size());
    }
}
