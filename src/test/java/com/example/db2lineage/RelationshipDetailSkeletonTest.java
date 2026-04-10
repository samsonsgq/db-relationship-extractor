package com.example.db2lineage;

import com.example.db2lineage.extract.RelationshipExtractor;
import com.example.db2lineage.metadata.ObjectMetadataRegistry;
import com.example.db2lineage.source.SqlSourceFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RelationshipDetailSkeletonTest {

    @Test
    void extractorSkeletonParsesSqlAndReturnsRowsList() throws Exception {
        Path sql = Files.createTempFile("skeleton", ".sql");
        Files.writeString(sql, "SELECT 1 FROM SYSIBM.SYSDUMMY1;\n");

        SqlSourceFile sourceFile = SqlSourceFile.fromPath(sql);
        RelationshipExtractor extractor = RelationshipExtractor.defaultExtractor(ObjectMetadataRegistry.empty());

        assertNotNull(extractor.extract(sourceFile));
    }
}
