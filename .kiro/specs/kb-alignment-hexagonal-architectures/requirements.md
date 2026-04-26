# Requirements Document

## Introduction

The Pragma Backend Chapter maintains a Knowledge Base (KB) of ~70 technical guideline documents that define the canonical standards for Java clean architecture projects. The archetype generator plugin (`archetype-generator-core`) and its FreeMarker templates (`archetype-generator-templates`) currently produce project scaffolding that deviates from these KB standards in 31 identified gaps. This feature aligns the generator output across all four supported architectures (hexagonal-single, hexagonal-multi, hexagonal-multi-granular, onion-single) and both paradigms (reactive, imperative) with the KB-defined standards for package structure, naming conventions, build system configuration, error handling, and quality tooling.

## Glossary

- **Generator**: The Gradle plugin (`archetype-generator-core`) that scaffolds clean architecture Java projects
- **Template_Repository**: The FreeMarker template collection (`archetype-generator-templates`) consumed by the Generator
- **KB**: The Knowledge Base of ~70 technical guideline documents maintained by Pragma's Backend Engineering Chapter
- **Hexagonal_Single**: Single-module hexagonal architecture (ports and adapters in one Gradle project)
- **Hexagonal_Multi**: Multi-module hexagonal architecture with 3 Gradle modules (domain, application, infrastructure)
- **Hexagonal_Multi_Granular**: Multi-module hexagonal architecture where each adapter is a separate Gradle module (8+ modules)
- **Onion_Single**: Single-module onion architecture with domain at center, surrounded by application and infrastructure layers
- **Port**: A Java interface in the domain layer that defines a contract for inbound or outbound communication
- **Gateway**: The KB-mandated suffix for domain port interfaces (e.g., `IAccountGateway`)
- **Adapter**: An infrastructure-layer class that implements a Port or exposes an entry point
- **UseCase**: A domain-layer class containing business logic, registered via regex component scan
- **UseCasesConfig**: A Spring `@Configuration` class that uses `@ComponentScan` with regex filter `^.+UseCase$` and an empty body
- **Version_Catalog**: A `gradle/libs.versions.toml` file that centralizes all dependency versions
- **Main_Gradle**: A `main.gradle` file that configures JaCoCo, PIT, SonarQube, OWASP, and MapStruct for all modules
- **ProblemDetail**: The RFC 7807 standard class in Spring for structured error responses in imperative projects
- **RouterFunction**: The Spring WebFlux functional endpoint pattern using `RouterFunction<ServerResponse>` and Handler classes
- **RestClient**: The Spring 6.1+ HTTP client that replaces the deprecated `RestTemplate`
- **MapStruct_Mapper**: A `@Mapper(componentModel = "spring")` interface that handles object-to-object mapping
- **ArchUnit**: A library for validating architecture rules as unit tests
- **FreeMarker_Template**: A `.ftl` file processed by the Generator to produce Java source files and configuration

## Requirements

### Requirement 1: Flat Domain Ports Package Structure

**User Story:** As a developer using the Generator, I want the generated project to have a flat `domain/ports/` package without `in/` or `out/` sub-packages, so that the output complies with the KB hexagonal-layers reference.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` for Hexagonal_Single, THE Generator SHALL create the package `domain/ports/` as a flat directory containing all port interfaces without `in/` or `out/` sub-packages
2. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi, THE Generator SHALL create the package `domain/ports/` as a flat directory within the domain module without `in/` or `out/` sub-packages
3. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi_Granular, THE Generator SHALL create the `ports` module at path `domain/ports/` with a flat package structure without `in/` or `out/` sub-packages
4. THE Generator SHALL use the package segment `ports` (plural) instead of `port` (singular) in all architecture structure definitions
5. WHEN the Generator executes `generateUseCase`, THE Generator SHALL place generated port interfaces in the flat `domain/ports/` package

### Requirement 2: Interface Prefix `I` on All Ports

**User Story:** As a developer using the Generator, I want all generated port interfaces to have the `I` prefix, so that the output complies with KB Limit 006 (mandatory interface prefix).

#### Acceptance Criteria

1. WHEN the Generator executes `generateUseCase`, THE Generator SHALL name the input port interface with prefix `I` (e.g., `ICreateAccountUseCase`)
2. WHEN the Generator executes `generateOutputAdapter`, THE Generator SHALL name the output port interface with prefix `I` and suffix `Gateway` (e.g., `IAccountGateway`)
3. WHEN the Generator executes `generateEntity`, THE Generator SHALL name any associated port interfaces with prefix `I`
4. THE FreeMarker_Template for `InputPort.java.ftl` SHALL produce an interface named `I${useCaseName}UseCase` instead of `${useCaseName}UseCase`
5. THE FreeMarker_Template for output port interfaces SHALL produce an interface named `I${entityName}Gateway` instead of `${entityName}Repository`

### Requirement 3: Gateway Suffix on Domain Port Interfaces

**User Story:** As a developer using the Generator, I want all generated output port interfaces to use the `Gateway` suffix instead of `Repository`, so that the output complies with KB naming conventions.

#### Acceptance Criteria

1. WHEN the Generator executes `generateOutputAdapter`, THE Generator SHALL name the domain port interface with suffix `Gateway` (e.g., `IAccountGateway`)
2. THE FreeMarker_Template for driven adapter port interfaces SHALL use `Gateway` as the suffix instead of `Repository`
3. WHEN the Generator generates a PostgreSQL adapter, THE Generator SHALL produce an adapter class that implements `I${entityName}Gateway` instead of `${entityName}Repository`
4. WHEN the Generator generates a MongoDB adapter, THE Generator SHALL produce an adapter class that implements `I${entityName}Gateway`
5. WHEN the Generator generates any driven adapter, THE Generator SHALL produce an adapter class that implements the corresponding `I${entityName}Gateway` interface

### Requirement 4: DTOs as Java Records

**User Story:** As a developer using the Generator, I want all generated DTOs to be Java Records instead of classes with getters/setters, so that the output complies with KB Limit 004.

#### Acceptance Criteria

1. WHEN the Generator generates a REST entry-point adapter, THE Generator SHALL produce request DTOs as Java Records with suffix `Request` (e.g., `public record CreateAccountRequest(...)`)
2. WHEN the Generator generates a REST entry-point adapter, THE Generator SHALL produce response DTOs as Java Records with suffix `Response` (e.g., `public record AccountResponse(...)`)
3. THE FreeMarker_Template for REST DTOs SHALL include Jakarta Validation annotations on record components where applicable (e.g., `@NotBlank`, `@NotNull`)
4. THE FreeMarker_Template for REST DTOs SHALL NOT use `@Data`, `@Getter`, `@Setter`, or any Lombok annotations
5. THE FreeMarker_Template for REST DTOs SHALL NOT use the suffix `DTO` in any generated class name

### Requirement 5: Domain Entities with Lombok Annotations

**User Story:** As a developer using the Generator, I want generated domain entities to use Lombok annotations instead of manual getters/setters, so that the output complies with KB entity conventions.

#### Acceptance Criteria

1. WHEN the Generator executes `generateEntity`, THE Generator SHALL produce a domain entity class annotated with `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`
2. THE FreeMarker_Template `Entity.java.ftl` SHALL NOT generate manual getter or setter methods
3. THE FreeMarker_Template `Entity.java.ftl` SHALL NOT generate a manual no-argument constructor
4. THE FreeMarker_Template `Entity.java.ftl` SHALL NOT include any framework annotations (`@Entity`, `@Table`, `@Document`)

### Requirement 6: MapStruct Mapper Interfaces in All Adapters

**User Story:** As a developer using the Generator, I want all generated adapters to include MapStruct mapper interfaces, so that the output complies with KB Decision 010 and Limit 007.

#### Acceptance Criteria

1. WHEN the Generator generates a driven adapter, THE Generator SHALL produce a MapStruct mapper interface annotated with `@Mapper(componentModel = "spring")`
2. THE FreeMarker_Template for adapter mappers SHALL name the mapper using the convention `${entityName}EntityMapper` for persistence adapters, `${entityName}RestMapper` for REST adapters, and `${entityName}AdapterMapper` for external API adapters
3. THE FreeMarker_Template for adapter mappers SHALL define mapping methods between domain entities and infrastructure-specific entities (e.g., `toDomain`, `toEntity`)
4. THE FreeMarker_Template for adapter classes SHALL inject the MapStruct mapper via constructor injection
5. THE FreeMarker_Template for adapter mappers SHALL NOT contain static mapping methods or utility classes

### Requirement 7: UseCasesConfig with Regex Component Scan

**User Story:** As a developer using the Generator, I want the generated configuration class to be named `UseCasesConfig` with a regex-based component scan, so that the output complies with KB Decision 028.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a configuration class named `UseCasesConfig` instead of `BeanConfiguration`
2. THE FreeMarker_Template for `UseCasesConfig` SHALL include `@ComponentScan` with a regex filter matching `^.+UseCase$`
3. THE FreeMarker_Template for `UseCasesConfig` SHALL have an empty class body with no `@Bean` methods
4. THE FreeMarker_Template for `UseCasesConfig` SHALL be annotated with `@Configuration`
5. WHEN the Generator generates use case classes, THE Generator SHALL NOT annotate use case classes with `@Service` or `@Component`

### Requirement 8: Reactive REST Entry-Points with RouterFunction Pattern

**User Story:** As a developer using the Generator, I want reactive REST entry-points to use the `RouterFunction<ServerResponse>` and Handler pattern instead of `@RestController`, so that the output complies with KB Limit 002.

#### Acceptance Criteria

1. WHEN the Generator generates a REST entry-point adapter with paradigm `reactive`, THE Generator SHALL produce a Router class defining `RouterFunction<ServerResponse>` beans
2. WHEN the Generator generates a REST entry-point adapter with paradigm `reactive`, THE Generator SHALL produce a Handler class containing request handling methods that accept `ServerRequest` and return `Mono<ServerResponse>`
3. THE FreeMarker_Template for reactive REST entry-points SHALL NOT use `@RestController`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, or `@PatchMapping` annotations
4. THE FreeMarker_Template for reactive REST Router SHALL define routes using `RouterFunctions.route()` with `RequestPredicates`
5. THE FreeMarker_Template for reactive REST Handler SHALL use `ServerRequest` and `ServerResponse` instead of `HttpServletRequest` and `HttpServletResponse`

### Requirement 9: Imperative HTTP Client with RestClient

**User Story:** As a developer using the Generator, I want the imperative HTTP client adapter to use `RestClient` instead of `RestTemplate`, so that the output complies with the KB springboot-imperative archetype.

#### Acceptance Criteria

1. WHEN the Generator generates an HTTP client driven adapter with paradigm `imperative`, THE Generator SHALL produce an adapter class that uses `RestClient` from Spring 6.1+
2. THE FreeMarker_Template for imperative HTTP client adapters SHALL NOT reference `RestTemplate` in any import or field declaration
3. THE FreeMarker_Template for imperative HTTP client adapters SHALL use `RestClient.Builder` for constructing the client instance
4. THE FreeMarker_Template for imperative HTTP client adapters SHALL provide methods for GET, POST, PUT, PATCH, and DELETE operations using `RestClient`

### Requirement 10: RFC 7807 Error Handling for Imperative Projects

**User Story:** As a developer using the Generator, I want imperative projects to include RFC 7807 error handling with `ProblemDetail`, so that the output complies with KB error handling standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` with paradigm `imperative`, THE Generator SHALL produce a `GlobalExceptionHandler` class annotated with `@RestControllerAdvice`
2. THE FreeMarker_Template for `GlobalExceptionHandler` SHALL return `ProblemDetail` objects for all handled exceptions
3. WHEN the Generator executes `initCleanArch` with paradigm `imperative`, THE Generator SHALL produce an `ErrorType` enum defining standard error types
4. THE FreeMarker_Template for `GlobalExceptionHandler` SHALL handle at minimum `MethodArgumentNotValidException`, `ConstraintViolationException`, and a generic `Exception`

### Requirement 11: Reactive Error Handling with AbstractErrorWebExceptionHandler

**User Story:** As a developer using the Generator, I want reactive projects to include a `GlobalErrorWebExceptionHandler`, so that the output complies with KB Limit 005.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` with paradigm `reactive`, THE Generator SHALL produce a `GlobalErrorWebExceptionHandler` class that extends `AbstractErrorWebExceptionHandler`
2. THE FreeMarker_Template for `GlobalErrorWebExceptionHandler` SHALL override the `getRoutingFunction` method to define error routing
3. THE FreeMarker_Template for `GlobalErrorWebExceptionHandler` SHALL NOT use `@RestControllerAdvice`
4. WHEN the Generator executes `initCleanArch` with paradigm `reactive`, THE Generator SHALL produce an `ErrorType` enum defining standard error types

### Requirement 12: Consistent Adapter Suffix

**User Story:** As a developer using the Generator, I want all generated adapter classes to consistently use the `Adapter` suffix, so that the output complies with KB naming conventions.

#### Acceptance Criteria

1. WHEN the Generator generates any driven adapter, THE Generator SHALL name the adapter class with suffix `Adapter` (e.g., `AccountJpaAdapter`, `AccountMongoAdapter`)
2. WHEN the Generator generates any entry-point adapter, THE Generator SHALL name the entry-point class with a consistent suffix appropriate to its type (`Controller` for imperative REST, `Handler` for reactive REST)
3. THE Generator SHALL NOT use the suffix `Repository` for any adapter class name
4. THE FreeMarker_Template metadata for PostgreSQL adapters SHALL specify suffix `Adapter` instead of `Repository`

### Requirement 13: Use Case Suffix Convention

**User Story:** As a developer using the Generator, I want all generated use case classes to follow the `*UseCase` naming convention, so that the output complies with KB naming standards and the regex component scan.

#### Acceptance Criteria

1. WHEN the Generator executes `generateUseCase`, THE Generator SHALL name the use case implementation class with suffix `UseCase` (e.g., `CreateAccountUseCase`)
2. THE FreeMarker_Template `UseCase.java.ftl` SHALL produce a class named `${useCaseName}UseCase` instead of `${useCaseName}UseCaseImpl`
3. THE FreeMarker_Template for use case classes SHALL annotate the class with `@RequiredArgsConstructor` from Lombok for dependency injection
4. THE FreeMarker_Template for use case classes SHALL NOT include any Spring annotations (`@Service`, `@Component`, `@Bean`)


### Requirement 14: Version Catalog (`libs.versions.toml`)

**User Story:** As a developer using the Generator, I want the generated project to include a `gradle/libs.versions.toml` version catalog, so that dependency versions are centralized per KB standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `gradle/libs.versions.toml` file containing version declarations for all dependencies
2. THE FreeMarker_Template for `libs.versions.toml` SHALL define `[versions]`, `[libraries]`, and `[plugins]` sections
3. THE FreeMarker_Template for `build.gradle` SHALL reference dependencies using version catalog aliases (e.g., `libs.mapstruct`) instead of hardcoded version strings
4. WHEN the Generator generates an adapter, THE Generator SHALL add the adapter's dependencies to the version catalog if they are not already present

### Requirement 15: `gradle.properties` Generation

**User Story:** As a developer using the Generator, I want the generated project to include a `gradle.properties` file, so that the output complies with KB Decision 022.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `gradle.properties` file at the project root
2. THE FreeMarker_Template for `gradle.properties` SHALL include Gradle performance settings (`org.gradle.parallel=true`, `org.gradle.caching=true`, `org.gradle.daemon=true`)
3. THE FreeMarker_Template for `gradle.properties` SHALL include placeholder entries for credential configuration (e.g., `sonarToken`, `nexusUser`)
4. THE FreeMarker_Template for `gradle.properties` SHALL include the project version property

### Requirement 16: `main.gradle` Generation

**User Story:** As a developer using the Generator, I want the generated project to include a `main.gradle` file with quality tool configurations, so that the output complies with KB build system standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `main.gradle` file at the project root
2. THE FreeMarker_Template for `main.gradle` SHALL configure JaCoCo with a minimum line coverage threshold of 85%
3. THE FreeMarker_Template for `main.gradle` SHALL configure PIT mutation testing with a minimum mutation kill threshold of 20%
4. THE FreeMarker_Template for `main.gradle` SHALL configure SonarQube with environment-variable-based properties
5. THE FreeMarker_Template for `main.gradle` SHALL configure OWASP Dependency Check for vulnerability scanning
6. THE FreeMarker_Template for `main.gradle` SHALL configure MapStruct annotation processor settings

### Requirement 17: `lombok.config` Generation

**User Story:** As a developer using the Generator, I want the generated project to include a `lombok.config` file, so that the output complies with KB tooling standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `lombok.config` file at the project root
2. THE FreeMarker_Template for `lombok.config` SHALL include `config.stopBubbling = true`
3. THE FreeMarker_Template for `lombok.config` SHALL include `lombok.addLombokGeneratedAnnotation = true`

### Requirement 18: Spring Boot 4.x and Gradle Version Alignment

**User Story:** As a developer using the Generator, I want the generated project to target Spring Boot 4.x and a compatible Gradle version, so that the output complies with the KB tech stack requirements.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` with framework `spring`, THE Generator SHALL configure Spring Boot version 4.x in the version catalog
2. THE FreeMarker_Template for `build.gradle` SHALL target Java 21 as the language version
3. THE FreeMarker_Template for Gradle wrapper properties SHALL specify a Gradle version compatible with Spring Boot 4.x
4. THE FreeMarker_Template for `build.gradle` SHALL use Jakarta EE 11 namespace for all Jakarta imports

### Requirement 19: `settings.gradle` with Flat Module Names and `projectDir` Mappings

**User Story:** As a developer using the Generator, I want multi-module projects to have a `settings.gradle` with flat module names and `projectDir` mappings, so that the output complies with KB build system conventions.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi, THE Generator SHALL produce a `settings.gradle` with flat module includes (e.g., `include 'model'`) and `projectDir` mappings (e.g., `project(':model').projectDir = file('domain/model')`)
2. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi_Granular, THE Generator SHALL produce a `settings.gradle` with flat module includes and `projectDir` mappings for all initial modules
3. WHEN the Generator generates an adapter for a multi-module architecture, THE Generator SHALL append the new module to `settings.gradle` with the correct `projectDir` mapping

### Requirement 20: Hexagonal Multi-Granular `helpers` Module

**User Story:** As a developer using the Generator, I want the hexagonal-multi-granular architecture to include an `infrastructure/helpers/` module, so that the output complies with the KB hexagonal-layers reference.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi_Granular, THE Generator SHALL create an `infrastructure/helpers/` Gradle module
2. THE `helpers` module SHALL be included in `settings.gradle` with the correct `projectDir` mapping
3. THE `helpers` module SHALL have a `build.gradle` with dependencies on the `model` and `ports` modules
4. WHEN the Generator generates a driven adapter for Hexagonal_Multi_Granular, THE Generator SHALL include `helpers` as a dependency of the adapter module if cross-cutting utilities are needed

### Requirement 21: Application Configuration (`application.yml`)

**User Story:** As a developer using the Generator, I want the generated `application.yml` to include mandatory operational settings, so that the output complies with KB observability and resilience standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce an `application.yml` with `server.shutdown: graceful` configured
2. THE FreeMarker_Template for `application.yml` SHALL include `spring.lifecycle.timeout-per-shutdown-phase: 30s`
3. THE FreeMarker_Template for `application.yml` SHALL enable Spring Boot Actuator health probes (`management.endpoint.health.probes.enabled: true`, `management.endpoints.web.exposure.include: health,info,prometheus`)
4. THE FreeMarker_Template for `application.yml` SHALL configure secrets as environment variable placeholders (e.g., `${DB_PASSWORD}`) instead of hardcoded values

### Requirement 22: ArchUnit Rules Completeness

**User Story:** As a developer using the Generator, I want the generated ArchUnit tests to include all 10 KB-mandated rules, so that the output enforces architecture compliance.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce ArchUnit test classes that validate domain layer isolation (domain depends on nothing external)
2. THE FreeMarker_Template for `ArchitectureTest.java.ftl` SHALL include rules validating that application layer depends only on domain
3. THE FreeMarker_Template for `NamingConventionTest.java.ftl` SHALL include rules validating the `I` prefix on port interfaces
4. THE FreeMarker_Template for `NamingConventionTest.java.ftl` SHALL include rules validating the `*Gateway` suffix on output port interfaces
5. THE FreeMarker_Template for `NamingConventionTest.java.ftl` SHALL include rules validating the `*UseCase` suffix on use case classes
6. THE FreeMarker_Template for `NamingConventionTest.java.ftl` SHALL include rules validating the `*Adapter` suffix on adapter classes
7. THE FreeMarker_Template for `ArchitectureTest.java.ftl` SHALL NOT reference `adapter.in` or `adapter.out` package patterns for hexagonal architectures

### Requirement 23: Onion Architecture Package Alignment

**User Story:** As a developer using the Generator, I want the onion-single architecture to use `entry-points` and `driven-adapters` package names instead of `adapter/in` and `adapter/out`, so that the output complies with KB conventions.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` for Onion_Single, THE Generator SHALL create packages using `infrastructure/entry-points/` instead of `infrastructure/adapter/in/`
2. WHEN the Generator executes `initCleanArch` for Onion_Single, THE Generator SHALL create packages using `infrastructure/driven-adapters/` instead of `infrastructure/adapter/out/`
3. THE `onion-single/structure.yml` SHALL define adapter paths as `infrastructure/driven-adapters/{name}` and `infrastructure/entry-points/{name}`
4. WHEN the Generator generates adapters for Onion_Single, THE Generator SHALL place them in the correct `entry-points` or `driven-adapters` package

### Requirement 24: Mandatory Static Files Generation

**User Story:** As a developer using the Generator, I want all mandatory project files to be generated during `initCleanArch`, so that the output includes every file required by the KB.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `Dockerfile` with multi-stage build and non-root user
2. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `README.md` file with project setup instructions
3. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a `.gitignore` file with standard Java/Gradle exclusions
4. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce `gradle.properties`, `gradle/libs.versions.toml`, `main.gradle`, and `lombok.config` files
5. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce `build.gradle` files that reference the version catalog and apply `main.gradle`

### Requirement 25: OpenAPI/SpringDoc Annotations in Controllers

**User Story:** As a developer using the Generator, I want generated imperative REST controllers to include OpenAPI annotations, so that the output complies with KB Decision 012.

#### Acceptance Criteria

1. WHEN the Generator generates a REST entry-point adapter with paradigm `imperative`, THE Generator SHALL include `@Tag` annotation on the controller class
2. THE FreeMarker_Template for imperative REST controllers SHALL include `@Operation` annotations on each endpoint method
3. THE FreeMarker_Template for imperative REST controllers SHALL include `@ApiResponse` annotations documenting success and error responses
4. THE FreeMarker_Template for `build.gradle` SHALL include the `springdoc-openapi` dependency in the version catalog

### Requirement 26: Structured Logging Configuration

**User Story:** As a developer using the Generator, I want the generated project to include structured JSON logging configuration, so that the output complies with KB Decision 019.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch`, THE Generator SHALL produce a logging configuration that outputs JSON format in production profile
2. THE FreeMarker_Template for `application.yml` SHALL include a `logging` section with structured output configuration
3. THE FreeMarker_Template for logging configuration SHALL include correlation ID support via MDC or Micrometer tracing

### Requirement 27: WebFlux Request Parameter Validator

**User Story:** As a developer using the Generator, I want reactive projects to include a `RequestParameterValidator` abstract class in the helpers module, so that the output complies with KB validation standards.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` with paradigm `reactive`, THE Generator SHALL produce a `RequestParameterValidator` abstract class
2. THE FreeMarker_Template for `RequestParameterValidator` SHALL provide methods for validating request parameters in a non-blocking manner
3. WHEN the architecture is Hexagonal_Multi_Granular, THE Generator SHALL place `RequestParameterValidator` in the `infrastructure/helpers/` module

### Requirement 28: End-to-End Adapter Compatibility

**User Story:** As a developer using the Generator, I want every supported adapter to generate correctly for all four architectures and both paradigms, so that the generator produces valid projects in all combinations.

#### Acceptance Criteria

1. WHEN the Generator executes `generateOutputAdapter` for any supported driven adapter type (MongoDB, PostgreSQL, Redis, DynamoDB, SQS producer, HTTP client), THE Generator SHALL produce compilable code for all four architecture types
2. WHEN the Generator executes `generateInputAdapter` for any supported entry-point adapter type (REST, GraphQL, gRPC, SQS consumer, Scheduled), THE Generator SHALL produce compilable code for all four architecture types
3. WHEN the Generator generates adapters with paradigm `reactive`, THE Generator SHALL use reactive types (`Mono`, `Flux`) in all adapter method signatures
4. WHEN the Generator generates adapters with paradigm `imperative`, THE Generator SHALL use synchronous types (`Optional`, `List`, direct returns) in all adapter method signatures
5. IF the Generator encounters an unsupported adapter-architecture-paradigm combination, THEN THE Generator SHALL return a descriptive error message identifying the unsupported combination

### Requirement 29: HTTP Client Driven Adapter Naming

**User Story:** As a developer using the Generator, I want HTTP client driven adapter modules to follow the `{system}-client-api` naming convention, so that the output complies with KB naming standards.

#### Acceptance Criteria

1. WHEN the Generator generates an HTTP client driven adapter for Hexagonal_Multi_Granular, THE Generator SHALL name the module using the pattern `{system}-client-api` (e.g., `transactions-client-api`)
2. THE Generator SHALL prompt the user for the target system name when generating an HTTP client adapter
3. THE FreeMarker_Template for HTTP client adapter modules SHALL NOT use `http-client` or `rest-consumer` as the module name

### Requirement 30: Domain Use Cases Package Path

**User Story:** As a developer using the Generator, I want use cases to be placed in `domain/usecases/` instead of `application/usecase/`, so that the output complies with the KB hexagonal structure.

#### Acceptance Criteria

1. WHEN the Generator executes `initCleanArch` for Hexagonal_Single, THE Generator SHALL create the package `domain/usecases/` for use case classes
2. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi, THE Generator SHALL place use case classes in the domain module under `domain/usecases/`
3. WHEN the Generator executes `initCleanArch` for Hexagonal_Multi_Granular, THE Generator SHALL create a `usecase` module at path `domain/usecase/` with package `domain.usecases`
4. WHEN the Generator executes `generateUseCase`, THE Generator SHALL place the generated use case class in the `domain/usecases/` package

### Requirement 31: Build and Release Verification

**User Story:** As a developer maintaining the Generator, I want the plugin to compile, pass all tests, and produce a stable release after all KB alignment changes, so that the updated generator is production-ready.

#### Acceptance Criteria

1. WHEN all KB alignment changes are applied, THE Generator SHALL compile without errors using `./gradlew build`
2. WHEN all KB alignment changes are applied, THE Generator SHALL pass all existing unit tests and integration tests
3. WHEN the Generator executes `initCleanArch` followed by `generateOutputAdapter` and `generateInputAdapter` end-to-end, THE Generator SHALL produce a project that compiles with `./gradlew build`
4. IF any existing test fails after KB alignment changes, THEN THE Generator test suite SHALL be updated to reflect the new KB-compliant output
