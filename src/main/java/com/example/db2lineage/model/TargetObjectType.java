package com.example.db2lineage.model;

/**
 * Supported target object categories.
 */
public enum TargetObjectType {
    TABLE,
    VIEW,
    SESSION_TABLE,
    CTE,
    FUNCTION,
    PROCEDURE,
    CURSOR,
    VARIABLE,
    UNKNOWN
}
