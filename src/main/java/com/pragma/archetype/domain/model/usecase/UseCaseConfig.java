package com.pragma.archetype.domain.model.usecase;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Configuration for generating a use case.
 * Represents the input needed to generate a complete use case with its port and
 * implementation.
 */
@Getter
@Accessors(fluent = true)
@Builder
public class UseCaseConfig {
    /**
     * Represents a method in the use case.
     */
    public record UseCaseMethod(
            String name,
            String returnType,
            List<MethodParameter> parameters) {
    }

    /**
     * Represents a parameter in a use case method.
     */
    public record MethodParameter(
            String name,
            String type) {
    }

    private final String name;
    private final String packageName;
    private final List<UseCaseMethod> methods;

    @Builder.Default
    private final boolean generatePort = true;

    @Builder.Default
    private final boolean generateImpl = true;
}
