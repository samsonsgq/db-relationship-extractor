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
public record SqlSourceFile(Path absolutePath, Path relativePath, String content, List<String> rawLines) {

    public SqlSourceFile {
        Objects.requireNonNull(absolutePath, "absolutePath");
        Objects.requireNonNull(relativePath, "relativePath");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(rawLines, "rawLines");
    }

    public static SqlSourceFile fromPath(Path path) {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path baseDirectory = absolutePath.getParent() == null ? absolutePath : absolutePath.getParent();
        return fromPath(absolutePath, baseDirectory);
    }

    public static SqlSourceFile fromPath(Path path, Path baseDirectory) {
        try {
            Path absolutePath = path.toAbsolutePath().normalize();
            Path normalizedBaseDirectory = baseDirectory.toAbsolutePath().normalize();
            Path relativePath = normalizedBaseDirectory.relativize(absolutePath);
            String content = Files.readString(absolutePath);
            List<String> rawLines = Files.readAllLines(absolutePath);
            return new SqlSourceFile(absolutePath, relativePath, content, rawLines);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read SQL source file: " + path, e);
        }
    }

    public String lineAt(int oneBasedLineNo) {
        if (oneBasedLineNo < 1 || oneBasedLineNo > rawLines.size()) {
            return "";
        }
        return rawLines.get(oneBasedLineNo - 1);
    }
}
