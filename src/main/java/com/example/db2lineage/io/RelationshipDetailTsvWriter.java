package com.example.db2lineage.io;

import com.example.db2lineage.model.RelationshipDetailRow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

/**
 * Writes relationship rows to relationship_detail.tsv format.
 */
public final class RelationshipDetailTsvWriter {

    private static final String HEADER = String.join("\t",
            "source_object_type",
            "source_object",
            "source_field",
            "target_object_type",
            "target_object",
            "target_field",
            "relationship",
            "line_no",
            "line_relation_seq",
            "line_content",
            "confidence"
    );

    private RelationshipDetailTsvWriter() {
    }

    public static void write(Path outputPath, List<RelationshipDetailRow> rows) {
        List<String> lines = rows.stream().map(RelationshipDetailTsvWriter::toTsvLine).toList();
        try {
            Path parent = outputPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputPath, buildOutput(lines));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write TSV: " + outputPath, e);
        }
    }

    private static List<String> buildOutput(List<String> dataLines) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(HEADER), dataLines.stream()).toList();
    }

    private static String toTsvLine(RelationshipDetailRow row) {
        return new StringJoiner("\t")
                .add(row.sourceObjectType())
                .add(row.sourceObject())
                .add(row.sourceField())
                .add(row.targetObjectType().name())
                .add(row.targetObject())
                .add(row.targetField())
                .add(row.relationship().name())
                .add(String.valueOf(row.lineNo()))
                .add(String.valueOf(row.lineRelationSeq()))
                .add(row.lineContent())
                .add(row.confidence().name())
                .toString();
    }
}
