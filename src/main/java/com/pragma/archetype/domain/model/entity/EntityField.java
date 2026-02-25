package com.pragma.archetype.domain.model.entity;

/**
 * Represents a field in an entity.
 *
 * @param name     Field name
 * @param type     Field type (String, Integer, LocalDateTime, etc.)
 * @param nullable Whether field can be null
 */
public record EntityField(
    String name,
    String type,
    boolean nullable) {
}
