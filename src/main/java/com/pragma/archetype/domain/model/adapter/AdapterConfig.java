package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating an output adapter.
 * Represents the input needed to generate a complete adapter with its
 * implementation.
 */
@Builder
public record AdapterConfig(
    String name,
    String packageName,
    AdapterType type,
    String entityName,
    List<AdapterMethod> methods) {
}
