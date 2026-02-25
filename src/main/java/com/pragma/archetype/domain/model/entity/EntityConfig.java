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
@Builder
public record EntityConfig(
    String name,
    List<EntityField> fields,
    boolean hasId,
    String idType,
    String packageName) {
}
