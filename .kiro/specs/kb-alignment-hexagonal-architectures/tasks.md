

## Overview

Este plan implementa los 31 gaps identificados entre el Knowledge Base (KB) del Backend Chapter y la salida actual del generador de arquetipos. Los cambios abarcan dos repositorios: `archetype-generator-templates` (FreeMarker templates, structure.yml, metadata.yml) y `archetype-generator-core` (plugin Gradle en Java). El enfoque es template-first: la mayoría de cambios son en templates y configuración, con cambios mínimos en el core.

## Tasks

- [x] 1. Structure alignment — Actualizar `structure.yml` y build system templates
  - [x] 1.1 Update `hexagonal-single/structure.yml`: cambiar `domain/port/in` + `domain/port/out` → `domain/ports` (flat), `application/usecase` → `domain/usecases`, actualizar `moduleMetadata` y `packages` sections
    - Modify `templates/architectures/hexagonal-single/structure.yml`
    - Update `packageTypes` in `moduleMetadata` to use `ports` (flat) and `usecases`
    - _Requirements: 1.1, 1.4, 30.1_

  - [x] 1.2 Update `hexagonal-multi/structure.yml`: cambiar `port/in` + `port/out` → `ports` (flat) en domain module, mover `usecase` package de application a domain module como `usecases`
    - Modify `templates/architectures/hexagonal-multi/structure.yml`
    - Update domain module packages and packageTypes
    - _Requirements: 1.2, 1.4, 30.2_

  - [x] 1.3 Update `hexagonal-multi-granular/structure.yml`: cambiar `ports` module packagePath de `domain.port` → `domain.ports`, renombrar `usecase` module a `usecases` con packagePath `domain.usecases`, agregar módulo `helpers` en `infrastructure/helpers`
    - Modify `templates/architectures/hexagonal-multi-granular/structure.yml`
    - Add `helpers` module with dependencies on `model` and `ports`
    - _Requirements: 1.3, 1.4, 20.1, 20.2, 20.3, 30.3_

  - [x] 1.4 Update `onion-single/structure.yml`: cambiar `infrastructure/adapter/in` → `infrastructure/entry-points`, `infrastructure/adapter/out` → `infrastructure/driven-adapters`, actualizar `adapterPaths`, `packages`, `packageMappings`, y `moduleMetadata`
    - Modify `templates/architectures/onion-single/structure.yml`
    - _Requirements: 23.1, 23.2, 23.3_

  - [x] 1.5 Add new `staticFiles` entries to all 4 `structure.yml` files for: `gradle.properties.ftl`, `main.gradle.ftl`, `libs.versions.toml.ftl`, `lombok.config.ftl`, `UseCasesConfig.java.ftl`, `ErrorType.java.ftl`
    - Add entries with correct `source` and `destination` paths
    - Use `{configPackagePath}` placeholder for Java files
    - _Requirements: 14.1, 15.1, 16.1, 17.1, 24.4, 24.5_

  - [x] 1.6 Create `gradle.properties.ftl` template in `architectures/hexagonal-single/project/`
    - Include `org.gradle.parallel=true`, `org.gradle.caching=true`, `org.gradle.daemon=true`
    - Include credential placeholders (`sonarToken`, `nexusUser`)
    - Include project version property
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

  - [x] 1.7 Create `libs.versions.toml.ftl` template in `architectures/hexagonal-single/project/`
    - Define `[versions]`, `[libraries]`, and `[plugins]` sections
    - Include Spring Boot 4.x, Java 21, MapStruct, Lombok, ArchUnit, springdoc-openapi versions
    - _Requirements: 14.1, 14.2, 18.1, 18.2_

  - [x] 1.8 Create `main.gradle.ftl` template in `architectures/hexagonal-single/project/`
    - Configure JaCoCo with 85% minimum line coverage
    - Configure PIT with 20% minimum mutation kill
    - Configure SonarQube with env-var properties
    - Configure OWASP Dependency Check
    - Configure MapStruct annotation processor
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6_

  - [x] 1.9 Create `lombok.config.ftl` template in `architectures/hexagonal-single/project/`
    - Include `config.stopBubbling = true` and `lombok.addLombokGeneratedAnnotation = true`
    - _Requirements: 17.1, 17.2, 17.3_

  - [x] 1.10 Update `build.gradle.kts.ftl` to reference version catalog aliases (`libs.mapstruct`, `libs.plugins.spring.boot`, etc.) instead of hardcoded versions, target Java 21, apply `main.gradle`
    - Modify `templates/architectures/hexagonal-single/project/build.gradle.kts.ftl`
    - _Requirements: 14.3, 18.1, 18.2, 18.4, 24.5_

- [x] 2. Checkpoint — Verify structure.yml and build system templates
  - Ensure all 4 structure.yml files are valid YAML and consistent
  - Ensure all new static file templates exist and are referenced correctly
  - Ensure all tests pass, ask the user if questions arise.


- [x] 3. Naming convention templates — Entity, UseCase, InputPort, ports
  - [x] 3.1 Modify `Entity.java.ftl` (imperative): replace manual getters/setters/constructor with Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Remove all manual getter/setter methods and no-arg constructor. Ensure NO framework annotations (`@Entity`, `@Table`, `@Document`)
    - Modify `templates/frameworks/spring/imperative/domain/Entity.java.ftl`
    - Add Lombok imports: `lombok.Data`, `lombok.Builder`, `lombok.NoArgsConstructor`, `lombok.AllArgsConstructor`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 3.2 Create or modify reactive `Entity.java.ftl` with same Lombok annotations (if separate template exists for reactive)
    - Check `templates/frameworks/spring/reactive/domain/Entity.java.ftl`
    - Apply same Lombok pattern as imperative
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 3.3 Modify `InputPort.java.ftl` (imperative): rename interface from `${useCaseName}UseCase` to `I${useCaseName}UseCase` (add `I` prefix)
    - Modify `templates/frameworks/spring/imperative/usecase/InputPort.java.ftl`
    - _Requirements: 2.1, 2.4_

  - [x] 3.4 Modify `InputPort.java.ftl` (reactive): same `I` prefix change
    - Modify `templates/frameworks/spring/reactive/usecase/InputPort.java.ftl`
    - _Requirements: 2.1, 2.4_

  - [x] 3.5 Modify `UseCase.java.ftl` (imperative): rename class from `${useCaseName}UseCaseImpl` to `${useCaseName}UseCase`, add `implements I${useCaseName}UseCase`, add `@RequiredArgsConstructor`, remove manual constructor, ensure NO `@Service`/`@Component`
    - Modify `templates/frameworks/spring/imperative/usecase/UseCase.java.ftl`
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [x] 3.6 Modify `UseCase.java.ftl` (reactive): same naming and annotation changes
    - Modify `templates/frameworks/spring/reactive/usecase/UseCase.java.ftl`
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [x] 3.7 Write property test for Entity Lombok annotations (Property 5)
    - **Property 5: Domain Entities with Lombok**
    - For any valid entity name and field list, verify generated class has `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, no manual getters/setters, no framework annotations
    - **Validates: Requirements 5.1, 5.2, 5.3, 5.4**

  - [x] 3.8 Write property test for Interface I-Prefix (Property 2)
    - **Property 2: Interface I-Prefix on All Ports**
    - For any valid use case name, verify generated interface starts with `I` prefix
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

  - [x] 3.9 Write property test for Use Case Naming Convention (Property 11)
    - **Property 11: Use Case Naming Convention**
    - For any valid use case name, verify class is `${useCaseName}UseCase` (not `Impl`), has `@RequiredArgsConstructor`, no `@Service`/`@Component`
    - **Validates: Requirements 13.1, 13.2, 13.3, 13.4**

- [x] 4. Error handling and reactive REST templates
  - [x] 4.1 Create `UseCasesConfig.java.ftl` in `architectures/hexagonal-single/project/`: `@Configuration` + `@ComponentScan` with regex filter `^.+UseCase$`, empty body, no `@Bean` methods
    - Also create copies or symlinks for hexagonal-multi, hexagonal-multi-granular, onion-single
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 4.2 Create `ErrorType.java.ftl` in `architectures/hexagonal-single/project/`: enum with standard error types (VALIDATION_ERROR, NOT_FOUND, INTERNAL_ERROR, etc.)
    - _Requirements: 10.3, 11.4_

  - [x] 4.3 Create `GlobalExceptionHandler.java.ftl` in `frameworks/spring/imperative/project/`: `@RestControllerAdvice`, return `ProblemDetail` (RFC 7807), handle `MethodArgumentNotValidException`, `ConstraintViolationException`, generic `Exception`
    - Add as conditional staticFile in structure.yml (paradigm == imperative)
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 4.4 Create `GlobalErrorWebExceptionHandler.java.ftl` in `frameworks/spring/reactive/project/`: extend `AbstractErrorWebExceptionHandler`, override `getRoutingFunction`, NO `@RestControllerAdvice`
    - Add as conditional staticFile in structure.yml (paradigm == reactive)
    - _Requirements: 11.1, 11.2, 11.3_

  - [x] 4.5 Create `Router.java.ftl` in `frameworks/spring/reactive/adapters/entry-points/rest/`: `@Configuration` class with `RouterFunction<ServerResponse>` bean, define routes using `RouterFunctions.route()` with path-based routing
    - _Requirements: 8.1, 8.4_

  - [x] 4.6 Create `Handler.java.ftl` in `frameworks/spring/reactive/adapters/entry-points/rest/`: `@Component` + `@RequiredArgsConstructor`, methods accept `ServerRequest` return `Mono<ServerResponse>`, NO `@RestController` annotations
    - _Requirements: 8.2, 8.5_

  - [x] 4.7 Remove or replace reactive `Controller.java.ftl` — the existing `frameworks/spring/reactive/adapters/entry-points/rest/Controller.java.ftl` must be replaced by Router + Handler pattern
    - Delete or deprecate the old Controller.java.ftl for reactive REST
    - _Requirements: 8.3_

  - [x] 4.8 Create `RequestParameterValidator.java.ftl` in `frameworks/spring/reactive/project/`: abstract class for non-blocking request parameter validation
    - Add as conditional staticFile for reactive paradigm
    - _Requirements: 27.1, 27.2, 27.3_

  - [x] 4.9 Write property test for UseCasesConfig regex scan (Property 7)
    - **Property 7: UseCasesConfig with Regex Scan**
    - For any valid basePackage, verify `@Configuration`, `@ComponentScan` with regex `^.+UseCase$`, empty body
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**

  - [x] 4.10 Write property test for Reactive REST RouterFunction pattern (Property 8)
    - **Property 8: Reactive REST Uses RouterFunction Pattern**
    - For any valid controller name with paradigm reactive, verify Router + Handler output, no `@RestController`
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**

  - [x] 4.11 Write property test for Imperative Error Handling (Property 16)
    - **Property 16: Imperative Error Handling with ProblemDetail**
    - For any valid ProjectConfig with imperative paradigm, verify `GlobalExceptionHandler` with `@RestControllerAdvice` and `ProblemDetail`
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4**

  - [x] 4.12 Write property test for Reactive Error Handling (Property 17)
    - **Property 17: Reactive Error Handling with AbstractErrorWebExceptionHandler**
    - For any valid ProjectConfig with reactive paradigm, verify `GlobalErrorWebExceptionHandler` extends `AbstractErrorWebExceptionHandler`, no `@RestControllerAdvice`
    - **Validates: Requirements 11.1, 11.2, 11.3, 11.4**

- [x] 5. Checkpoint — Verify error handling and reactive REST templates
  - Ensure all new templates compile as valid FreeMarker
  - Ensure all tests pass, ask the user if questions arise.


- [x] 6. Adapter template updates — driven-adapters and entry-points
  - [x] 6.1 Modify imperative `Controller.java.ftl`: add OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse`), add `@Validated`, use DTOs as Java Records (`Create${controllerName}Request`, `${controllerName}Response`), add versioned path `/api/v1/`
    - Modify `templates/frameworks/spring/imperative/adapters/entry-points/rest/Controller.java.ftl`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 25.1, 25.2, 25.3_

  - [x] 6.2 Modify imperative PostgreSQL `Adapter.java.ftl`: change `implements ${entityName}Repository` → `implements I${entityName}Gateway`, inject MapStruct mapper via `@RequiredArgsConstructor`, update import paths to use `domain.ports` package
    - Modify `templates/frameworks/spring/imperative/adapters/driven-adapters/postgresql/Adapter.java.ftl`
    - _Requirements: 3.3, 6.1, 6.3, 6.4, 12.1_

  - [x] 6.3 Update imperative PostgreSQL `metadata.yml`: change `suffix: "Repository"` → `suffix: "Adapter"`
    - Modify `templates/frameworks/spring/imperative/adapters/driven-adapters/postgresql/metadata.yml`
    - _Requirements: 12.4_

  - [x] 6.4 Modify reactive MongoDB `Adapter.java.ftl`: change to implement `I${entityName}Gateway`, add `@RequiredArgsConstructor`, inject MapStruct mapper, update import paths
    - Modify `templates/frameworks/spring/reactive/adapters/driven-adapters/mongodb/Adapter.java.ftl`
    - _Requirements: 3.4, 6.1, 6.4, 12.1_

  - [x] 6.5 Modify imperative HTTP client `Adapter.java.ftl`: replace `RestTemplate` with `RestClient` (Spring 6.1+), use `RestClient.Builder` for construction, provide GET/POST/PUT/PATCH/DELETE methods
    - Modify `templates/frameworks/spring/imperative/adapters/driven-adapters/http-client/Adapter.java.ftl`
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [x] 6.6 Update all remaining driven adapter `Adapter.java.ftl` templates (Redis, DynamoDB, SQS, etc.) to implement `I${entityName}Gateway`, use `@RequiredArgsConstructor`, inject MapStruct mapper
    - Apply pattern consistently across all driven adapter templates in both imperative and reactive
    - _Requirements: 3.5, 6.1, 6.4, 12.1_

  - [x] 6.7 Update reactive REST `metadata.yml`: change `suffix: "Controller"` → `suffix: "Handler"`
    - Modify `templates/frameworks/spring/reactive/adapters/entry-points/rest/metadata.yml`
    - _Requirements: 12.2_

  - [x] 6.8 Write property test for Gateway Suffix on Output Ports (Property 3)
    - **Property 3: Gateway Suffix on Output Ports**
    - For any valid entity name and driven adapter type, verify interface ends with `Gateway` and adapter implements `I${entityName}Gateway`
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

  - [x] 6.9 Write property test for DTOs as Java Records (Property 4)
    - **Property 4: DTOs as Java Records**
    - For any valid controller name, verify request/response DTOs are Java Records, no `@Data`, no `DTO` suffix
    - **Validates: Requirements 4.1, 4.2, 4.4, 4.5**

  - [x] 6.10 Write property test for Consistent Adapter Suffix (Property 10)
    - **Property 10: Consistent Adapter Suffix**
    - For any valid adapter name, verify class ends with `Adapter` (never `Repository`), entry-points use `Controller` (imperative) or `Handler` (reactive)
    - **Validates: Requirements 12.1, 12.2, 12.3, 12.4**

  - [x] 6.11 Write property test for Imperative HTTP Client uses RestClient (Property 9)
    - **Property 9: Imperative HTTP Client Uses RestClient**
    - For any valid adapter name with imperative paradigm, verify `RestClient` usage, no `RestTemplate` references
    - **Validates: Requirements 9.1, 9.2, 9.3, 9.4**

  - [x] 6.12 Write property test for MapStruct Mapper Interfaces (Property 6)
    - **Property 6: MapStruct Mapper Interfaces**
    - For any valid entity name, verify mapper is interface with `@Mapper(componentModel = "spring")`, has `toDomain`/`toEntity` methods, no static methods
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5**

  - [x] 6.13 Write property test for OpenAPI Annotations (Property 18)
    - **Property 18: OpenAPI Annotations in Imperative Controllers**
    - For any valid controller name with imperative paradigm, verify `@Tag`, `@Operation`, `@ApiResponse` present
    - **Validates: Requirements 25.1, 25.2, 25.3**

- [x] 7. Checkpoint — Verify adapter templates
  - Ensure all adapter templates are consistent across paradigms
  - Ensure all metadata.yml suffix values are correct
  - Ensure all tests pass, ask the user if questions arise.


- [x] 8. Core plugin changes — Variable context, multi-template support, settings.gradle
  - [x] 8.1 Extend template variable context in `ProjectGenerator` and/or `InitializeProjectUseCaseImpl`: add `springBootVersion`, `javaVersion`, `gradleVersion`, `mapstructVersion`, `lombokVersion`, `archunitVersion`, `configPackagePath`, `useCasesPackage`, `portsPackage`, `jacocoMinCoverage`, `pitMinMutationKill`
    - Modify `src/main/java/com/pragma/archetype/application/generator/ProjectGenerator.java`
    - These variables are consumed by the new FreeMarker templates
    - _Requirements: 18.1, 18.2, 18.3_

  - [x] 8.2 Modify `UnifiedAdapterGenerator` to support multi-template generation for reactive REST: iterate over all discovered templates (main + secondary) instead of only the main template, so both `Router.java.ftl` and `Handler.java.ftl` are generated
    - Modify `src/main/java/com/pragma/archetype/application/generator/UnifiedAdapterGenerator.java`
    - Update `generateMainFile()` or add `generateSecondaryFiles()` method
    - _Requirements: 8.1, 8.2_

  - [x] 8.3 Modify `ProjectGenerator.addModuleToSettings()` to generate flat module names with `projectDir` mappings for multi-module architectures: `include 'model'` + `project(':model').projectDir = file('domain/model')` instead of `include ':domain:model'`
    - Modify `src/main/java/com/pragma/archetype/application/generator/ProjectGenerator.java`
    - _Requirements: 19.1, 19.2, 19.3_

  - [x] 8.4 Extend `UnifiedAdapterGenerator.prepareTemplateData()` to include new variables (`configPackagePath`, `useCasesPackage`, `portsPackage`) in the adapter template context
    - Modify `src/main/java/com/pragma/archetype/application/generator/UnifiedAdapterGenerator.java`
    - _Requirements: 2.2, 3.1_

  - [x] 8.5 Write property test for Settings.gradle Flat Module Names (Property 19)
    - **Property 19: Settings.gradle Flat Module Names**
    - For any multi-module architecture, verify flat includes with `projectDir` mappings
    - **Validates: Requirements 19.1, 19.2, 19.3**

- [x] 9. Checkpoint — Verify core plugin changes
  - Run `./gradlew build` on the core plugin to verify compilation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Onion architecture alignment and ArchUnit test templates
  - [x] 10.1 Update `ArchitectureTest.java.ftl`: remove `adapter.in`/`adapter.out` references in `onionArchitecture()` rule, replace with `entry-points`/`driven-adapters` patterns, add rules for domain layer isolation and application layer isolation
    - Modify `templates/architectures/hexagonal-single/tests/architecture/ArchitectureTest.java.ftl`
    - _Requirements: 22.1, 22.2, 22.7_

  - [x] 10.2 Update `NamingConventionTest.java.ftl`: add rules for `I` prefix on port interfaces, `*Gateway` suffix on output ports, `*UseCase` suffix on use case classes, `*Adapter` suffix on adapter classes
    - Modify `templates/architectures/hexagonal-single/tests/architecture/NamingConventionTest.java.ftl`
    - _Requirements: 22.3, 22.4, 22.5, 22.6_

  - [x] 10.3 Ensure ArchUnit test templates are consistent across all 4 architecture types (hexagonal-single, hexagonal-multi, hexagonal-multi-granular, onion-single)
    - Verify each architecture's `structure.yml` references the correct test template paths
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7_

  - [x] 10.4 Write property test for ArchUnit Rules Completeness (Property 14)
    - **Property 14: ArchUnit Rules Completeness**
    - For any valid basePackage and architecture type, verify rules for domain isolation, `I` prefix, `*Gateway` suffix, `*UseCase` suffix, `*Adapter` suffix, no `adapter.in`/`adapter.out`
    - **Validates: Requirements 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7**

- [x] 11. Application configuration and cross-cutting templates
  - [x] 11.1 Update `application.yml` template (or create if not exists): add `server.shutdown: graceful`, `spring.lifecycle.timeout-per-shutdown-phase: 30s`, Actuator health probes enabled, secrets as `${PLACEHOLDER}` env vars, structured logging with JSON format support
    - _Requirements: 21.1, 21.2, 21.3, 21.4, 26.1, 26.2, 26.3_

  - [x] 11.2 Update `build.gradle.kts.ftl` to include `springdoc-openapi` dependency via version catalog alias
    - _Requirements: 25.4_

  - [x] 11.3 Write property test for Application Configuration Completeness (Property 13)
    - **Property 13: Application Configuration Completeness**
    - For any valid ProjectConfig, verify `server.shutdown: graceful`, Actuator probes, env var placeholders, structured logging
    - **Validates: Requirements 21.1, 21.2, 21.3, 21.4, 26.1, 26.2, 26.3**

  - [x] 11.4 Write property test for Build System Files Generation (Property 12)
    - **Property 12: Build System Files Generation**
    - For any valid ProjectConfig, verify `libs.versions.toml` with correct sections, `gradle.properties` with performance settings, `main.gradle` with JaCoCo/PIT/SonarQube/OWASP, `build.gradle.kts` with version catalog aliases
    - **Validates: Requirements 14.1, 14.2, 14.3, 15.1, 15.2, 15.3, 15.4, 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 18.1, 18.2**

  - [x] 11.5 Write property test for Mandatory Static Files Completeness (Property 20)
    - **Property 20: Mandatory Static Files Completeness**
    - For any valid ProjectConfig, verify output contains Dockerfile, gradle.properties, libs.versions.toml, main.gradle, lombok.config, build.gradle.kts, architecture tests
    - **Validates: Requirements 24.1, 24.4, 24.5**

- [x] 12. Checkpoint — Verify onion alignment, ArchUnit, and application config
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Remaining property tests and cross-cutting validations
  - [x] 13.1 Write property test for Flat Domain Ports Package (Property 1)
    - **Property 1: Flat Domain Ports Package**
    - For any valid ProjectConfig and architecture type, verify `domain/ports/` is flat (no `in/`/`out/`), uses `ports` (plural)
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**

  - [x] 13.2 Write property test for Domain Use Cases Package Path (Property 15)
    - **Property 15: Domain Use Cases Package Path**
    - For any valid architecture type and use case name, verify use case placed in `domain/usecases/` (not `application/usecase/`)
    - **Validates: Requirements 30.1, 30.2, 30.3, 30.4**

  - [x] 13.3 Write unit tests for example-based validations
    - Test `lombok.config` exact content (Req 17)
    - Test Jakarta Validation in DTOs (Req 4.3)
    - Test Onion package alignment `entry-points`/`driven-adapters` (Req 23)
    - Test `helpers` module in multi-granular with correct dependencies (Req 20)
    - Test HTTP client naming `{system}-client-api` (Req 29)
    - Test `RequestParameterValidator` generation for reactive (Req 27)
    - _Requirements: 4.3, 17.1, 17.2, 17.3, 20.1, 20.3, 23.1, 23.2, 27.1, 29.1_

  - [x] 13.4 Write integration tests for end-to-end flows
    - Test `initCleanArch` → `generateOutputAdapter` → `generateInputAdapter` produces compilable project (Req 31.3)
    - Test adapter-architecture-paradigm matrix for all supported combinations (Req 28)
    - _Requirements: 28.1, 28.2, 28.3, 28.4, 28.5, 31.3_

- [x] 14. Build verification and wiring
  - [x] 14.1 Run `./gradlew build` on the core plugin to verify all changes compile
    - _Requirements: 31.1_

  - [x] 14.2 Run existing test suite to verify backward compatibility — update any tests that fail due to KB-compliant output changes
    - _Requirements: 31.2, 31.4_

  - [x] 14.3 Verify all 4 architecture types generate correctly with both paradigms by running integration tests
    - _Requirements: 28.1, 28.2, 28.3, 28.4_

- [x] 15. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases
- Changes span two repositories: `archetype-generator-templates` (tasks 1, 3, 4, 6, 10, 11) and `archetype-generator-core` (tasks 8, 14)
- The template repository changes are the majority of the work; core plugin changes are minimal and additive
