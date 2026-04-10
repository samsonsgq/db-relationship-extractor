package com.example.db2lineage;

import com.example.db2lineage.source.SqlSourceFile;
import com.example.db2lineage.source.SqlSourceFileScanner;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Phase 1 entry point: scan SQL files and load raw source content into memory.
 */
public final class RelationshipDetailMain {

    private RelationshipDetailMain() {
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: RelationshipDetailMain <input-sql-dir> [<input-sql-dir> ...]");
            return;
        }

        List<Path> inputDirectories = Arrays.stream(args)
                .map(Path::of)
                .toList();

        SqlSourceFileScanner scanner = new SqlSourceFileScanner();
        List<Path> discoveredFiles = scanner.scanDirectories(inputDirectories);

        System.out.printf("Found %d SQL file(s).%n", discoveredFiles.size());
        for (Path sqlFile : discoveredFiles) {
            System.out.println(" - " + sqlFile);
        }

        List<SqlSourceFile> loadedFiles = scanner.loadFiles(inputDirectories);
        System.out.printf("Loaded %d SQL source file(s) into memory.%n", loadedFiles.size());
        System.out.println("Extraction and TSV writing are not implemented in Phase 1.");
    }
}
