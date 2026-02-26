package com.pragma.archetype.domain.model.adapter;

import java.util.List;

import lombok.Builder;

/**
 * Configuration for generating an output adapter.
 * Represents the input needed to generate a complete adapter with its
 * implementation.
 * 
 * Uses String for type instead of enum to support dynamic adapter discovery.
 */
@Builder
public record OutputAdapterConfig(
        String name,
        String packageName,
        String type, // Changed from AdapterType enum to String for dynamic discovery
        String entityName,
        List<AdapterMethod> methods) {
}
