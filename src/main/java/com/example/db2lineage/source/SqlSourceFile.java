package com.example.db2lineage.source;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Raw SQL source text holder used for exact line anchoring.
 */
public record SqlSourceFile(Path path, String content, List<String> lines) {

    public SqlSourceFile {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(lines, "lines");
    }

    public static SqlSourceFile fromPath(Path path) {
        try {
            String content = Files.readString(path);
            List<String> lines = Files.readAllLines(path);
            return new SqlSourceFile(path, content, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read SQL source file: " + path, e);
        }
    }

    public String lineAt(int oneBasedLineNo) {
        if (oneBasedLineNo < 1 || oneBasedLineNo > lines.size()) {
            return "";
        }
        return lines.get(oneBasedLineNo - 1);
    }
}
