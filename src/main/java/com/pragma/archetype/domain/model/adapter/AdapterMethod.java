package com.pragma.archetype.domain.model.adapter;

import java.util.List;

/**
 * Represents a method in the adapter.
 */
public record AdapterMethod(
    String name,
    String returnType,
    List<MethodParameter> parameters) {
}
