package com.example.db2lineage.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits DB2 SQL source objects into statement regions for later AST parsing.
 */
public final class Db2StatementRegionSplitter {

    private static final Pattern CREATE_OBJECT_PATTERN = Pattern.compile(
            "(?is)\\bCREATE\\s+(?:OR\\s+REPLACE\\s+)?(PROCEDURE|FUNCTION|VIEW)\\s+([A-Z0-9_.$]+)"
    );

    public List<StatementRegion> split(SqlSourceFile sourceFile) {
        SourceObjectInfo sourceObjectInfo = resolveSourceObjectInfo(sourceFile);
        if (isRoutine(sourceObjectInfo.sourceObjectType())) {
            return splitRoutineBodyStatements(sourceFile, sourceObjectInfo);
        }
        return splitTopLevelStatements(sourceFile, sourceObjectInfo);
    }

    private List<StatementRegion> splitTopLevelStatements(SqlSourceFile sourceFile, SourceObjectInfo sourceObjectInfo) {
        List<StatementRegion> regions = new ArrayList<>();
        List<String> lines = sourceFile.rawLines();

        int statementStartLine = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.equals("@")) {
                continue;
            }
            if (statementStartLine < 0 && !trimmed.isEmpty()) {
                statementStartLine = i + 1;
            }
            if (statementStartLine < 0) {
                continue;
            }

            if (endsWithStatementTerminator(trimmed)) {
                int statementEndLine = i + 1;
                String statementText = sliceLines(lines, statementStartLine, statementEndLine);
                regions.add(new StatementRegion(
                        sourceObjectInfo.sourceObjectName(),
                        sourceObjectInfo.sourceObjectType(),
                        statementStartLine,
                        statementEndLine,
                        statementText,
                        detectStatementType(statementText)
                ));
                statementStartLine = -1;
            }
        }

        if (statementStartLine > 0) {
            int statementEndLine = lines.size();
            String statementText = sliceLines(lines, statementStartLine, statementEndLine);
            if (!statementText.isBlank()) {
                regions.add(new StatementRegion(
                        sourceObjectInfo.sourceObjectName(),
                        sourceObjectInfo.sourceObjectType(),
                        statementStartLine,
                        statementEndLine,
                        statementText,
                        detectStatementType(statementText)
                ));
            }
        }
        return regions;
    }

    private List<StatementRegion> splitRoutineBodyStatements(SqlSourceFile sourceFile, SourceObjectInfo sourceObjectInfo) {
        List<StatementRegion> regions = new ArrayList<>();
        List<String> lines = sourceFile.rawLines();
        int beginLine = findRoutineBeginLine(lines);
        if (beginLine < 0) {
            return regions;
        }
        int bodyStartLine = beginLine + 1;
        int bodyEndLine = findRoutineBodyEndLine(lines, bodyStartLine);

        int statementStartLine = -1;
        int nestedBeginDepth = 0;
        for (int lineNo = bodyStartLine; lineNo <= bodyEndLine; lineNo++) {
            String line = lines.get(lineNo - 1);
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (statementStartLine < 0) {
                statementStartLine = lineNo;
            }

            String normalized = normalizeForTokenScan(line);
            nestedBeginDepth += countWord(normalized, "BEGIN");
            nestedBeginDepth -= countWord(normalized, "END");
            if (nestedBeginDepth < 0) {
                nestedBeginDepth = 0;
            }

            if (trimmed.endsWith(";") && nestedBeginDepth == 0) {
                String statementText = sliceLines(lines, statementStartLine, lineNo);
                regions.add(new StatementRegion(
                        sourceObjectInfo.sourceObjectName(),
                        sourceObjectInfo.sourceObjectType(),
                        statementStartLine,
                        lineNo,
                        statementText,
                        detectStatementType(statementText)
                ));
                statementStartLine = -1;
            }
        }
        return regions;
    }

    private int findRoutineBeginLine(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String normalized = normalizeForTokenScan(lines.get(i));
            if (normalized.matches(".*\\bBEGIN\\b.*")) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findRoutineBodyEndLine(List<String> lines, int bodyStartLine) {
        for (int lineNo = bodyStartLine; lineNo <= lines.size(); lineNo++) {
            String trimmed = lines.get(lineNo - 1).trim().toUpperCase(Locale.ROOT);
            if (trimmed.equals("@")) {
                return lineNo - 1;
            }
            if (trimmed.startsWith("END")) {
                return lineNo - 1;
            }
        }
        return lines.size();
    }

    private SourceObjectInfo resolveSourceObjectInfo(SqlSourceFile sourceFile) {
        Matcher matcher = CREATE_OBJECT_PATTERN.matcher(sourceFile.content().toUpperCase(Locale.ROOT));
        if (matcher.find()) {
            String sourceObjectType = matcher.group(1).toUpperCase(Locale.ROOT);
            String sourceObjectName = matcher.group(2).toUpperCase(Locale.ROOT);
            return new SourceObjectInfo(sourceObjectName, sourceObjectType);
        }

        String fileName = sourceFile.relativePath().getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        String sourceObjectName = (extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName)
                .toUpperCase(Locale.ROOT);
        return new SourceObjectInfo(sourceObjectName, "SCRIPT");
    }

    private String normalizeForTokenScan(String line) {
        String withoutComment = line.replaceAll("--.*$", "");
        String withoutStringLiterals = withoutComment.replaceAll("'([^']|'')*'", "''");
        return withoutStringLiterals.toUpperCase(Locale.ROOT);
    }

    private int countWord(String text, String keyword) {
        Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private boolean endsWithStatementTerminator(String trimmedLine) {
        return trimmedLine.endsWith(";") || trimmedLine.endsWith("@");
    }

    private String detectStatementType(String statementText) {
        String normalized = statementText.stripLeading().toUpperCase(Locale.ROOT);
        String[] statementTypes = {"INSERT", "UPDATE", "CALL", "RETURN", "SET", "DECLARE", "IF", "DELETE", "MERGE"};
        for (String statementType : statementTypes) {
            if (normalized.startsWith(statementType)) {
                return statementType;
            }
        }
        return null;
    }

    private String sliceLines(List<String> lines, int startLine, int endLine) {
        return String.join("\n", lines.subList(startLine - 1, endLine));
    }

    private boolean isRoutine(String sourceObjectType) {
        return "PROCEDURE".equals(sourceObjectType) || "FUNCTION".equals(sourceObjectType);
    }

    private record SourceObjectInfo(String sourceObjectName, String sourceObjectType) {
    }
}
