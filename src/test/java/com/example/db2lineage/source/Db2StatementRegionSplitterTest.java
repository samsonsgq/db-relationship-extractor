package com.example.db2lineage.source;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Db2StatementRegionSplitterTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsMultiStatementRoutineBodyWithLineRanges() throws Exception {
        String sql = """
                CREATE OR REPLACE PROCEDURE APP.P_DEMO()
                LANGUAGE SQL
                BEGIN
                    INSERT INTO T_A(C1) VALUES (1);
                    UPDATE T_A SET C1 = 2 WHERE C1 = 1;
                    CALL APP.LOG_PROC('X');
                    RETURN;
                END
                @
                """;
        SqlSourceFile sourceFile = writeSource("P_DEMO.sql", sql);

        Db2StatementRegionSplitter splitter = new Db2StatementRegionSplitter();
        List<StatementRegion> regions = splitter.split(sourceFile);

        assertEquals(4, regions.size());
        assertStatement(regions.get(0), "APP.P_DEMO", "PROCEDURE", 4, 4, "INSERT");
        assertStatement(regions.get(1), "APP.P_DEMO", "PROCEDURE", 5, 5, "UPDATE");
        assertStatement(regions.get(2), "APP.P_DEMO", "PROCEDURE", 6, 6, "CALL");
        assertStatement(regions.get(3), "APP.P_DEMO", "PROCEDURE", 7, 7, "RETURN");
    }

    @Test
    void supportsScriptTopLevelStatementSplitting() throws Exception {
        String sql = """
                INSERT INTO T_A(C1) VALUES (1);
                UPDATE T_A SET C1 = 2 WHERE C1 = 1;
                CALL APP.LOG_PROC('X');
                """;
        SqlSourceFile sourceFile = writeSource("edge_script.sql", sql);

        Db2StatementRegionSplitter splitter = new Db2StatementRegionSplitter();
        List<StatementRegion> regions = splitter.split(sourceFile);

        assertEquals(3, regions.size());
        assertStatement(regions.get(0), "EDGE_SCRIPT", "SCRIPT", 1, 1, "INSERT");
        assertStatement(regions.get(1), "EDGE_SCRIPT", "SCRIPT", 2, 2, "UPDATE");
        assertStatement(regions.get(2), "EDGE_SCRIPT", "SCRIPT", 3, 3, "CALL");
    }

    private SqlSourceFile writeSource(String fileName, String content) throws Exception {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content);
        return SqlSourceFile.fromPath(file, tempDir);
    }

    private void assertStatement(
            StatementRegion region,
            String sourceName,
            String sourceType,
            int startLine,
            int endLine,
            String statementType
    ) {
        assertEquals(sourceName, region.sourceObjectName());
        assertEquals(sourceType, region.sourceObjectType());
        assertEquals(startLine, region.startLine());
        assertEquals(endLine, region.endLine());
        assertEquals(statementType, region.statementType());
    }
}
