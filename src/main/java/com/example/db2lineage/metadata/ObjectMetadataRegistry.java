package com.example.db2lineage.metadata;

import com.example.db2lineage.model.TargetObjectType;

import java.util.Map;
import java.util.Optional;

/**
 * Schema/catalog metadata facade used to resolve object kinds and signatures.
 */
public final class ObjectMetadataRegistry {

    private final Map<String, TargetObjectType> objectTypeByName;

    private ObjectMetadataRegistry(Map<String, TargetObjectType> objectTypeByName) {
        this.objectTypeByName = Map.copyOf(objectTypeByName);
    }

    public static ObjectMetadataRegistry empty() {
        return new ObjectMetadataRegistry(Map.of());
    }

    public Optional<TargetObjectType> resolveObjectType(String objectName) {
        return Optional.ofNullable(objectTypeByName.get(objectName));
    }
}
