package com.pragma.archetype.domain.model.entity;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating a domain entity.
 *
 * @param name        Entity name (e.g., "User", "Product")
 * @param fields      List of entity fields
 * @param hasId       Whether entity has an ID field
 * @param idType      Type of ID field (String, Long, UUID)
 * @param packageName Package where entity will be generated
 */
@Builder(toBuilder = true)
public record EntityConfig(
        String name,
        List<EntityField> fields,
        boolean hasId,
        String idType,
        String packageName) {

    private static final boolean DEFAULT_HAS_ID = true;
    private static final String DEFAULT_ID_TYPE = "String";

    /**
     * Compact constructor with defaults.
     */
    public EntityConfig {
        // Apply defaults if not set
        if (idType == null || idType.isBlank()) {
            idType = DEFAULT_ID_TYPE;
        }
    }

    /**
     * Custom builder class to provide default values.
     */
    public static class EntityConfigBuilder {
        // Lombok generates the builder, we just need to set defaults
        private boolean hasId = DEFAULT_HAS_ID;
    }
}
