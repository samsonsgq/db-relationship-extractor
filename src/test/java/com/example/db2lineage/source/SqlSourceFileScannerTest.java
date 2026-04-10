package com.example.db2lineage.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SqlSourceFileScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsSqlFileIntoSqlSourceFileModel() throws Exception {
        Path sqlFile = tempDir.resolve("nested").resolve("demo.sql");
        Files.createDirectories(sqlFile.getParent());
        Files.writeString(sqlFile, "SELECT 1;\n");

        SqlSourceFileScanner scanner = new SqlSourceFileScanner();
        List<SqlSourceFile> loadedFiles = scanner.loadFiles(List.of(tempDir));

        assertEquals(1, loadedFiles.size());
        SqlSourceFile sourceFile = loadedFiles.get(0);
        assertEquals(sqlFile.toAbsolutePath().normalize(), sourceFile.absolutePath());
        assertEquals(Path.of("nested", "demo.sql"), sourceFile.relativePath());
        assertEquals("SELECT 1;\n", sourceFile.content());
        assertEquals(List.of("SELECT 1;"), sourceFile.rawLines());
    }

    @Test
    void preservesRawLineTextExactly() throws Exception {
        Path sqlFile = tempDir.resolve("format.sql");
        String sqlText = "SELECT 1;\n  FROM SOME_TABLE;\n\tWHERE ID = 7;\n\n";
        Files.writeString(sqlFile, sqlText);

        SqlSourceFile sourceFile = SqlSourceFile.fromPath(sqlFile, tempDir);

        assertEquals(sqlText, sourceFile.content());
        assertIterableEquals(
                List.of("SELECT 1;", "  FROM SOME_TABLE;", "\tWHERE ID = 7;", ""),
                sourceFile.rawLines()
        );
    }

    @Test
    void supportsLineLookupByOneBasedLineNumber() throws Exception {
        Path sqlFile = tempDir.resolve("line_lookup.sql");
        Files.writeString(sqlFile, "LINE_1\nLINE_2\nLINE_3\n");

        SqlSourceFile sourceFile = SqlSourceFile.fromPath(sqlFile, tempDir);

        assertEquals("LINE_1", sourceFile.lineAt(1));
        assertEquals("LINE_2", sourceFile.lineAt(2));
        assertEquals("", sourceFile.lineAt(0));
        assertEquals("", sourceFile.lineAt(999));
        assertEquals("LINE_3", sourceFile.lineAt(3));
    }
}
