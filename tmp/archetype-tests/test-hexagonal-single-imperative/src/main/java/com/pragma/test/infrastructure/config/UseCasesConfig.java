package com.pragma.test.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Spring configuration for use case registration.
 * Uses regex-based component scan to automatically detect and register
 * all classes matching the *UseCase naming convention.
 *
 * This keeps domain use case classes free from Spring annotations
 * (@Service, @Component), following the Dependency Inversion Principle.
 */
@Configuration
@ComponentScan(
    basePackages = "com.pragma.test.domain.usecases",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "^.+UseCase$"
    )
)
public class UseCasesConfig {
}
