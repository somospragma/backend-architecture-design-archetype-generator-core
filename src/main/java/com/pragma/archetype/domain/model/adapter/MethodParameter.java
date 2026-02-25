package com.pragma.archetype.domain.model.adapter;

/**
 * Represents a parameter in an adapter method.
 */
public record MethodParameter(
    String name,
    String type) {
}
