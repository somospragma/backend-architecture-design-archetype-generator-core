package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating an input adapter (REST, GraphQL, etc.).
 * Represents the input needed to generate a complete input adapter.
 */
@Builder
public record InputAdapterConfig(
    String name,
    String packageName,
    InputAdapterType type,
    String useCaseName,
    List<Endpoint> endpoints) {
}
