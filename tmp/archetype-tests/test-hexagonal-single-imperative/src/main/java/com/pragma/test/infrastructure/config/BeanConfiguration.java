package com.pragma.test.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Spring configuration for dependency injection.
 * This class is responsible for scanning and registering all application components.
 * 
 * By centralizing Spring configuration here, we keep the domain and application layers
 * free from framework-specific annotations, following the Dependency Inversion Principle.
 */
@Configuration
@ComponentScan(
    basePackages = {
        "com.pragma.test.application.usecase",
        "com.pragma.test.infrastructure.drivenadapters",
        "com.pragma.test.infrastructure.entrypoints"
    },
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE)
)
public class BeanConfiguration {
    // Spring will automatically detect and register all classes in the scanned packages
    // No need for @Service or @Component annotations in those packages
}
