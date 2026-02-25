package com.pragma.archetype.domain.model.adapter;

import java.util.List;

/**
 * Represents an endpoint in the adapter.
 */
public record Endpoint(
    String path,
    HttpMethod method,
    String useCaseMethod,
    String returnType,
    List<EndpointParameter> parameters) {
}
