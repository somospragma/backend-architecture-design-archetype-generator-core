package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating an input adapter (REST, GraphQL, etc.).
 * Represents the input needed to generate a complete input adapter.
 * 
 * Uses String for type instead of enum to support dynamic adapter discovery.
 */
@Builder
public record InputAdapterConfig(
        String name,
        String packageName,
        String type, // Changed from InputAdapterType enum to String for dynamic discovery
        String useCaseName,
        List<Endpoint> endpoints) {
}
