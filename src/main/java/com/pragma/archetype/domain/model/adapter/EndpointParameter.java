package com.pragma.archetype.domain.model.adapter;

/**
 * Represents a parameter in an endpoint.
 */
public record EndpointParameter(
    String name,
    String type,
    ParameterType paramType) {
}
