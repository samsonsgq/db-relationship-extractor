package com.example.db2lineage.source;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Scans configured directories for SQL files and loads them as {@link SqlSourceFile}.
 */
public final class SqlSourceFileScanner {

    public List<Path> scanDirectories(List<Path> directories) {
        Set<Path> discoveredFiles = new LinkedHashSet<>();

        for (Path directory : directories) {
            Path absoluteDirectory = directory.toAbsolutePath().normalize();
            if (!Files.isDirectory(absoluteDirectory)) {
                continue;
            }

            try (var stream = Files.walk(absoluteDirectory)) {
                stream.filter(Files::isRegularFile)
                        .filter(this::isSqlFile)
                        .map(path -> path.toAbsolutePath().normalize())
                        .forEach(discoveredFiles::add);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to scan SQL directory: " + directory, e);
            }
        }

        return discoveredFiles.stream()
                .sorted(Comparator.comparing(Path::toString))
                .toList();
    }

    public List<SqlSourceFile> loadFiles(List<Path> directories) {
        List<SqlSourceFile> loadedFiles = new ArrayList<>();

        for (Path directory : directories) {
            Path absoluteDirectory = directory.toAbsolutePath().normalize();
            if (!Files.isDirectory(absoluteDirectory)) {
                continue;
            }

            List<Path> sqlFilesInDirectory;
            try (var stream = Files.walk(absoluteDirectory)) {
                sqlFilesInDirectory = stream.filter(Files::isRegularFile)
                        .filter(this::isSqlFile)
                        .map(path -> path.toAbsolutePath().normalize())
                        .sorted(Comparator.comparing(Path::toString))
                        .toList();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to load SQL directory: " + directory, e);
            }

            for (Path sqlPath : sqlFilesInDirectory) {
                loadedFiles.add(SqlSourceFile.fromPath(sqlPath, absoluteDirectory));
            }
        }

        return loadedFiles;
    }

    private boolean isSqlFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".sql");
    }
}
