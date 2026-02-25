# Tasks: Spring Imperative Support

## PHASE 0: Domain Layer Refactoring (Technical Debt)

### Task 0.1: Add Lombok Dependency and Configuration
- [x] 0.1.1 Add Lombok dependency to build.gradle.kts
  - [ ] Add compileOnly("org.projectlombok:lombok:1.18.30")
  - [ ] Add annotationProcessor("org.projectlombok:lombok:1.18.30")
  - [ ] Add testCompileOnly("org.projectlombok:lombok:1.18.30")
  - [ ] Add testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
- [x] 0.1.2 Verify Lombok configuration
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Add Lombok dependency"

### Task 0.2: Create New Package Structure
- [ ] 0.2.1 Create new package directories
  - [ ] Create domain/model/adapter/
  - [ ] Create domain/model/config/
  - [ ] Create domain/model/entity/
  - [ ] Create domain/model/usecase/
  - [ ] Create domain/model/project/
  - [ ] Create domain/model/file/
  - [ ] Create domain/model/validation/
  - [ ] Create domain/model/structure/
- [ ] 0.2.2 Validation
  - [ ] Verify directory structure created correctly
  - [ ] Commit changes: "Create new domain package structure"

### Task 0.3: Move Classes to New Packages (Adapter Domain)
- [ ] 0.3.1 Move adapter-related classes
  - [ ] Move AdapterConfig.java to domain/model/adapter/
  - [ ] Move AdapterMetadata.java to domain/model/adapter/
  - [ ] Move InputAdapterConfig.java to domain/model/adapter/
- [ ] 0.3.2 Update imports in all files that use adapter classes
  - [ ] Search for imports: `import com.pragma.archetype.domain.model.AdapterConfig`
  - [ ] Replace with: `import com.pragma.archetype.domain.model.adapter.AdapterConfig`
  - [ ] Update all application layer files
  - [ ] Update all infrastructure layer files
  - [ ] Update all test files
- [ ] 0.3.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Move adapter classes to domain/model/adapter/"

### Task 0.4: Move Classes to New Packages (Config Domain)
- [ ] 0.4.1 Move config-related classes
  - [ ] Move ProjectConfig.java to domain/model/config/
  - [ ] Move TemplateConfig.java to domain/model/config/
  - [ ] Move TemplateMode.java to domain/model/config/
  - [ ] Move TemplateSource.java to domain/model/config/
- [ ] 0.4.2 Update imports in all files that use config classes
  - [ ] Search and replace all imports for ProjectConfig
  - [ ] Search and replace all imports for TemplateConfig
  - [ ] Search and replace all imports for TemplateMode
  - [ ] Search and replace all imports for TemplateSource
  - [ ] Update application layer files
  - [ ] Update infrastructure layer files
  - [ ] Update test files
- [ ] 0.4.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Move config classes to domain/model/config/"

### Task 0.5: Move Classes to New Packages (Entity, UseCase, Project)
- [ ] 0.5.1 Move entity-related classes
  - [ ] Move EntityConfig.java to domain/model/entity/
- [ ] 0.5.2 Move usecase-related classes
  - [ ] Move UseCaseConfig.java to domain/model/usecase/
- [ ] 0.5.3 Move project-related classes
  - [ ] Move ArchitectureType.java to domain/model/project/
  - [ ] Move Framework.java to domain/model/project/
  - [ ] Move Paradigm.java to domain/model/project/
- [ ] 0.5.4 Update imports for all moved classes
  - [ ] Update imports for EntityConfig
  - [ ] Update imports for UseCaseConfig
  - [ ] Update imports for ArchitectureType
  - [ ] Update imports for Framework
  - [ ] Update imports for Paradigm
  - [ ] Update application layer files
  - [ ] Update infrastructure layer files
  - [ ] Update test files
- [ ] 0.5.5 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Move entity, usecase, project classes to respective packages"

### Task 0.6: Move Classes to New Packages (File, Validation, Structure)
- [ ] 0.6.1 Move file-related classes
  - [ ] Move GeneratedFile.java to domain/model/file/
- [ ] 0.6.2 Move validation-related classes
  - [ ] Move ValidationResult.java to domain/model/validation/
- [ ] 0.6.3 Move structure-related classes
  - [ ] Move StructureMetadata.java to domain/model/structure/
  - [ ] Move LayerDependencies.java to domain/model/structure/
  - [ ] Move NamingConventions.java to domain/model/structure/
  - [ ] Move MergeResult.java to domain/model/structure/
- [ ] 0.6.4 Update imports for all moved classes
  - [ ] Update imports for GeneratedFile
  - [ ] Update imports for ValidationResult
  - [ ] Update imports for StructureMetadata
  - [ ] Update imports for LayerDependencies
  - [ ] Update imports for NamingConventions
  - [ ] Update imports for MergeResult
  - [ ] Update application layer files
  - [ ] Update infrastructure layer files
  - [ ] Update test files
- [ ] 0.6.5 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Move file, validation, structure classes to respective packages"

### Task 0.7: Extract Nested Enums from AdapterConfig
- [ ] 0.7.1 Extract AdapterType enum
  - [ ] Create AdapterType.java in domain/model/adapter/
  - [ ] Copy enum content from AdapterConfig
  - [ ] Make it a top-level enum
  - [ ] Update AdapterConfig to reference the new enum
  - [ ] Update all imports that use AdapterConfig.AdapterType
- [ ] 0.7.2 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Extract AdapterType enum from AdapterConfig"

### Task 0.8: Extract Nested Records from AdapterConfig
- [ ] 0.8.1 Extract AdapterMethod record
  - [ ] Create AdapterMethod.java in domain/model/adapter/
  - [ ] Copy record content from AdapterConfig
  - [ ] Update AdapterConfig to reference the new record
  - [ ] Update all imports that use AdapterConfig.AdapterMethod
- [ ] 0.8.2 Extract MethodParameter record
  - [ ] Create MethodParameter.java in domain/model/adapter/
  - [ ] Copy record content from AdapterConfig
  - [ ] Update AdapterConfig to reference the new record
  - [ ] Update all imports that use AdapterConfig.MethodParameter
- [ ] 0.8.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Extract AdapterMethod and MethodParameter records"

### Task 0.9: Extract Nested Classes from InputAdapterConfig
- [ ] 0.9.1 Extract InputAdapterType enum
  - [ ] Create InputAdapterType.java in domain/model/adapter/
  - [ ] Copy enum content from InputAdapterConfig
  - [ ] Update InputAdapterConfig to reference the new enum
  - [ ] Update all imports
- [ ] 0.9.2 Extract HttpMethod enum
  - [ ] Create HttpMethod.java in domain/model/adapter/
  - [ ] Copy enum content from InputAdapterConfig
  - [ ] Update InputAdapterConfig to reference the new enum
  - [ ] Update all imports
- [ ] 0.9.3 Extract ParameterType enum
  - [ ] Create ParameterType.java in domain/model/adapter/
  - [ ] Copy enum content from InputAdapterConfig
  - [ ] Update InputAdapterConfig to reference the new enum
  - [ ] Update all imports
- [ ] 0.9.4 Extract Endpoint record
  - [ ] Create Endpoint.java in domain/model/adapter/
  - [ ] Copy record content from InputAdapterConfig
  - [ ] Update InputAdapterConfig to reference the new record
  - [ ] Update all imports
- [ ] 0.9.5 Extract EndpointParameter record
  - [ ] Create EndpointParameter.java in domain/model/adapter/
  - [ ] Copy record content from InputAdapterConfig
  - [ ] Update InputAdapterConfig to reference the new record
  - [ ] Update all imports
- [ ] 0.9.6 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Extract nested classes from InputAdapterConfig"

### Task 0.10: Extract Nested Classes from EntityConfig
- [ ] 0.10.1 Extract EntityField record
  - [ ] Create EntityField.java in domain/model/entity/
  - [ ] Copy record content from EntityConfig
  - [ ] Update EntityConfig to reference the new record
  - [ ] Update all imports that use EntityConfig.EntityField
- [ ] 0.10.2 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Extract EntityField record from EntityConfig"

### Task 0.11: Extract FileType Enum from GeneratedFile
- [ ] 0.11.1 Extract FileType enum
  - [ ] Create FileType.java in domain/model/file/
  - [ ] Copy enum content from GeneratedFile
  - [ ] Update GeneratedFile to reference the new enum
  - [ ] Update all imports that use GeneratedFile.FileType
- [ ] 0.11.2 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Extract FileType enum from GeneratedFile"

### Task 0.12: Replace Manual Builder in AdapterConfig with Lombok @Builder
- [ ] 0.12.1 Add @Builder annotation to AdapterConfig
  - [ ] Add `import lombok.Builder;` at top of file
  - [ ] Add `@Builder` annotation to AdapterConfig class
- [ ] 0.12.2 Delete manual Builder class
  - [ ] Remove the entire `public static class Builder { ... }` from AdapterConfig
  - [ ] Remove `public static Builder builder()` method
- [ ] 0.12.3 Update all usages to use Lombok builder
  - [ ] Verify all `.builder()` calls still work
  - [ ] Update tests if needed
- [ ] 0.12.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace manual Builder with @Builder in AdapterConfig"

### Task 0.13: Replace Manual Builder in InputAdapterConfig with Lombok @Builder
- [ ] 0.13.1 Add @Builder annotation to InputAdapterConfig
  - [ ] Add `import lombok.Builder;`
  - [ ] Add `@Builder` annotation
- [ ] 0.13.2 Delete manual Builder class
  - [ ] Remove Builder class from InputAdapterConfig
  - [ ] Remove builder() method
- [ ] 0.13.3 Update all usages
  - [ ] Verify all builder calls work
  - [ ] Update tests if needed
- [ ] 0.13.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace manual Builder with @Builder in InputAdapterConfig"

### Task 0.14: Replace Manual Builder in EntityConfig with Lombok @Builder
- [ ] 0.14.1 Add @Builder annotation to EntityConfig
  - [ ] Add `import lombok.Builder;`
  - [ ] Add `@Builder` annotation
- [ ] 0.14.2 Delete manual Builder class
  - [ ] Remove Builder class from EntityConfig
  - [ ] Remove builder() method
- [ ] 0.14.3 Update all usages
  - [ ] Verify all builder calls work
  - [ ] Update tests if needed
- [ ] 0.14.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace manual Builder with @Builder in EntityConfig"

### Task 0.15: Replace Manual Builder in ProjectConfig with Lombok @Builder
- [ ] 0.15.1 Add @Builder annotation to ProjectConfig
  - [ ] Add `import lombok.Builder;`
  - [ ] Add `@Builder` annotation
- [ ] 0.15.2 Delete manual Builder class
  - [ ] Remove Builder class from ProjectConfig
  - [ ] Remove builder() method
- [ ] 0.15.3 Update all usages
  - [ ] Verify all builder calls work
  - [ ] Update tests if needed
- [ ] 0.15.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace manual Builder with @Builder in ProjectConfig"

### Task 0.16: Replace Manual Builder in UseCaseConfig with Lombok @Builder
- [ ] 0.16.1 Add @Builder annotation to UseCaseConfig
  - [ ] Add `import lombok.Builder;`
  - [ ] Add `@Builder` annotation
- [ ] 0.16.2 Delete manual Builder class
  - [ ] Remove Builder class from UseCaseConfig
  - [ ] Remove builder() method
- [ ] 0.16.3 Update all usages
  - [ ] Verify all builder calls work
  - [ ] Update tests if needed
- [ ] 0.16.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace manual Builder with @Builder in UseCaseConfig"

### Task 0.17: Replace Simple Getters with Lombok @Getter (Part 1)
- [ ] 0.17.1 Analyze AdapterConfig for simple getters
  - [ ] Identify getters that only return a field (no logic)
  - [ ] Add `@Getter` annotation to AdapterConfig
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters (with logic) as methods
- [ ] 0.17.2 Analyze AdapterMetadata for simple getters
  - [ ] Add `@Getter` annotation
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters
- [ ] 0.17.3 Analyze InputAdapterConfig for simple getters
  - [ ] Add `@Getter` annotation
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters
- [ ] 0.17.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace simple getters with @Getter (adapter classes)"

### Task 0.18: Replace Simple Getters with Lombok @Getter (Part 2)
- [ ] 0.18.1 Analyze ProjectConfig for simple getters
  - [ ] Add `@Getter` annotation
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters
- [ ] 0.18.2 Analyze TemplateConfig for simple getters
  - [ ] Add `@Getter` annotation
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters
- [ ] 0.18.3 Analyze EntityConfig for simple getters
  - [ ] Add `@Getter` annotation
  - [ ] Delete simple getter methods
  - [ ] Keep complex getters
- [ ] 0.18.4 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace simple getters with @Getter (config/entity classes)"

### Task 0.19: Replace Simple Getters with Lombok @Getter (Part 3)
- [ ] 0.19.1 Analyze remaining domain model classes
  - [ ] UseCaseConfig
  - [ ] GeneratedFile
  - [ ] ValidationResult
  - [ ] StructureMetadata
  - [ ] LayerDependencies
  - [ ] NamingConventions
  - [ ] MergeResult
- [ ] 0.19.2 Add @Getter and delete simple getters for each class
- [ ] 0.19.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace simple getters with @Getter (remaining classes)"

### Task 0.20: Replace Simple Setters with Lombok @Setter
- [ ] 0.20.1 Analyze all domain model classes for simple setters
  - [ ] Identify setters that only assign a field (no validation/logic)
  - [ ] List classes that have simple setters
- [ ] 0.20.2 Add @Setter annotation to classes with simple setters
  - [ ] Add `@Setter` annotation at class level or field level
  - [ ] Delete simple setter methods
  - [ ] Keep complex setters (with validation) as methods
- [ ] 0.20.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Replace simple setters with @Setter"

### Task 0.21: Apply @Data to Mutable POJOs
- [ ] 0.21.1 Identify mutable POJOs (classes with setters)
  - [ ] AdapterMetadata (if mutable)
  - [ ] TemplateConfig (if mutable)
  - [ ] Other mutable classes
- [ ] 0.21.2 Replace @Getter + @Setter with @Data
  - [ ] Add `import lombok.Data;`
  - [ ] Replace @Getter and @Setter with @Data
  - [ ] @Data generates: getters, setters, toString, equals, hashCode
- [ ] 0.21.3 Validation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Run `./gradlew jacocoTestReport` - verify coverage >85%
  - [ ] Commit changes: "Apply @Data to mutable POJOs"

### Task 0.22: Final Validation and Cleanup
- [ ] 0.22.1 Run full test suite
  - [ ] Run `./gradlew clean test` - all tests must pass
  - [ ] Fix any failing tests
- [ ] 0.22.2 Verify test coverage
  - [ ] Run `./gradlew jacocoTestReport`
  - [ ] Open build/reports/jacoco/test/html/index.html
  - [ ] Verify overall coverage >85%
  - [ ] If coverage <85%, add missing tests
- [ ] 0.22.3 Run full build
  - [ ] Run `./gradlew clean build` - must succeed
  - [ ] Fix any compilation errors
- [ ] 0.22.4 Test plugin functionality
  - [ ] Run `./gradlew initCleanArch` in a test project
  - [ ] Verify project generates successfully
  - [ ] Verify generated code compiles
- [ ] 0.22.5 Code review and cleanup
  - [ ] Remove unused imports
  - [ ] Format code according to project standards
  - [ ] Check for any remaining manual getters/setters
  - [ ] Check for any remaining manual builders
- [ ] 0.22.6 Final commit
  - [ ] Commit changes: "Complete Phase 0: Domain layer refactoring"
  - [ ] Tag: "phase-0-complete"

## PHASE 1: Spring Imperative Foundation

### Task 1.1: Create Imperative Template Directory Structure
- [ ] 1.1.1 Create base directories
  - [ ] Create templates/frameworks/spring/imperative/
  - [ ] Create templates/frameworks/spring/imperative/domain/
  - [ ] Create templates/frameworks/spring/imperative/usecase/
  - [ ] Create templates/frameworks/spring/imperative/project/
  - [ ] Create templates/frameworks/spring/imperative/adapters/
  - [ ] Create templates/frameworks/spring/imperative/adapters/driven-adapters/
  - [ ] Create templates/frameworks/spring/imperative/adapters/entry-points/
- [ ] 1.1.2 Create metadata.yml for imperative paradigm
  - [ ] Create templates/frameworks/spring/imperative/metadata.yml
  - [ ] Define paradigm: imperative
  - [ ] Define framework: spring
  - [ ] Define base dependencies (spring-boot-starter-web, spring-boot-starter-data-jpa)
- [ ] 1.1.3 Validation
  - [ ] Verify directory structure created
  - [ ] Commit changes: "Create imperative template directory structure"

### Task 1.2: Create Symlinks for Shared Templates
- [ ] 1.2.1 Create symlink for Entity template
  - [ ] ln -s ../reactive/domain/Entity.java.ftl imperative/domain/Entity.java.ftl
  - [ ] Verify symlink works
- [ ] 1.2.2 Create symlink for domain metadata
  - [ ] ln -s ../reactive/domain/metadata.yml imperative/domain/metadata.yml
  - [ ] Verify symlink works
- [ ] 1.2.3 Create symlink for Application template
  - [ ] ln -s ../reactive/project/Application.java.ftl imperative/project/Application.java.ftl
  - [ ] Verify symlink works
- [ ] 1.2.4 Validation
  - [ ] Test that symlinks resolve correctly
  - [ ] Commit changes: "Create symlinks for shared templates"

### Task 1.3: Create Imperative UseCase Templates
- [ ] 1.3.1 Create UseCase.java.ftl for imperative
  - [ ] Create templates/frameworks/spring/imperative/usecase/UseCase.java.ftl
  - [ ] Use synchronous return types (T instead of Mono<T>)
  - [ ] Use List<T> instead of Flux<T>
  - [ ] Use void instead of Mono<Void>
  - [ ] No reactor.core imports
- [ ] 1.3.2 Create InputPort.java.ftl for imperative
  - [ ] Create templates/frameworks/spring/imperative/usecase/InputPort.java.ftl
  - [ ] Define interface with synchronous method signatures
  - [ ] No reactor.core imports
- [ ] 1.3.3 Create Test.java.ftl for imperative
  - [ ] Create templates/frameworks/spring/imperative/usecase/Test.java.ftl
  - [ ] Use standard JUnit assertions (no reactor-test)
  - [ ] Test synchronous methods
- [ ] 1.3.4 Create metadata.yml for usecase
  - [ ] Create templates/frameworks/spring/imperative/usecase/metadata.yml
  - [ ] Define dependencies
- [ ] 1.3.5 Validation
  - [ ] Test template generation with plugin
  - [ ] Verify generated code compiles
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Commit changes: "Create imperative usecase templates"

### Task 1.4: Create Imperative Application Configuration
- [ ] 1.4.1 Create application.yml.ftl for imperative
  - [ ] Create templates/frameworks/spring/imperative/project/application.yml.ftl
  - [ ] Configure Spring MVC (not WebFlux)
  - [ ] Configure Tomcat server
  - [ ] Remove reactive-specific configuration
- [ ] 1.4.2 Validation
  - [ ] Test template generation
  - [ ] Verify generated application.yml is valid YAML
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Commit changes: "Create imperative application.yml template"

### Task 1.5: Test Imperative Foundation
- [ ] 1.5.1 Generate test project with imperative paradigm
  - [ ] Run plugin to generate imperative project
  - [ ] Verify project structure created
  - [ ] Verify use cases generated with synchronous signatures
- [ ] 1.5.2 Compile generated project
  - [ ] cd to generated project
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Verify no compilation errors
- [ ] 1.5.3 Final validation
  - [ ] Run plugin tests: `./gradlew test`
  - [ ] Verify coverage >85%: `./gradlew jacocoTestReport`
  - [ ] Run full build: `./gradlew build`
  - [ ] Commit changes: "Complete Phase 1: Imperative foundation"
  - [ ] Tag: "phase-1-complete"

## PHASE 2: Essential Adapters (Imperative)

### Task 2.1: Create REST Entry Point (Imperative)
- [ ] 2.1.1 Create REST adapter directory
  - [ ] Create templates/frameworks/spring/imperative/adapters/entry-points/rest/
- [ ] 2.1.2 Create Adapter.java.ftl
  - [ ] Use @RestController with Spring MVC
  - [ ] Return ResponseEntity<T> or ResponseEntity<List<T>>
  - [ ] Support GET, POST, PUT, DELETE, PATCH
  - [ ] Use @Valid for validation
- [ ] 2.1.3 Create ExceptionHandler.java.ftl
  - [ ] Use @ControllerAdvice
  - [ ] Map domain exceptions to HTTP status codes
- [ ] 2.1.4 Create Test.java.ftl
  - [ ] Use MockMvc for testing
- [ ] 2.1.5 Create metadata.yml
  - [ ] Include spring-boot-starter-web
  - [ ] Include spring-boot-starter-validation
- [ ] 2.1.6 Create application-properties.yml.ftl
  - [ ] Configure server.port
  - [ ] Configure spring.mvc
- [ ] 2.1.7 Validation
  - [ ] Generate REST adapter with plugin
  - [ ] Verify generated code compiles
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Verify coverage >85%
  - [ ] Commit changes: "Add REST entry point (imperative)"

### Task 2.2: Create MongoDB Driven Adapter (Imperative)
- [ ] 2.2.1 Create MongoDB adapter directory
  - [ ] Create templates/frameworks/spring/imperative/adapters/driven-adapters/mongodb/
- [ ] 2.2.2 Create Adapter.java.ftl
  - [ ] Use Spring Data JPA (not Reactive MongoDB)
  - [ ] Return T or List<T> (not Mono/Flux)
- [ ] 2.2.3 Create Repository.java.ftl
  - [ ] Extend JpaRepository<Entity, ID>
- [ ] 2.2.4 Create Entity.java.ftl
  - [ ] JPA entity with @Entity annotation
- [ ] 2.2.5 Create Mapper.java.ftl
  - [ ] Map between domain and JPA entities
- [ ] 2.2.6 Create Config.java.ftl
  - [ ] Configure JPA EntityManager
- [ ] 2.2.7 Create Test.java.ftl
  - [ ] Use @DataJpaTest
- [ ] 2.2.8 Create metadata.yml
  - [ ] Include spring-boot-starter-data-jpa
  - [ ] Include mongodb-driver-sync
- [ ] 2.2.9 Create application-properties.yml.ftl
  - [ ] Configure JPA and MongoDB
- [ ] 2.2.10 Validation
  - [ ] Generate MongoDB adapter
  - [ ] Verify code compiles
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Verify coverage >85%
  - [ ] Commit changes: "Add MongoDB adapter (imperative)"

### Task 2.3: Create PostgreSQL Driven Adapter (Imperative)
- [ ] 2.3.1 Create PostgreSQL adapter directory
  - [ ] Create templates/frameworks/spring/imperative/adapters/driven-adapters/postgresql/
- [ ] 2.3.2 Create Adapter.java.ftl
  - [ ] Use Spring Data JPA
  - [ ] Return T or List<T>
- [ ] 2.3.3 Create Repository.java.ftl
  - [ ] Extend JpaRepository<Entity, ID>
- [ ] 2.3.4 Create Entity.java.ftl
  - [ ] JPA entity with @Entity, @Table
- [ ] 2.3.5 Create Mapper.java.ftl
- [ ] 2.3.6 Create Config.java.ftl
  - [ ] Configure DataSource with HikariCP
- [ ] 2.3.7 Create Test.java.ftl
  - [ ] Use @DataJpaTest with Testcontainers
- [ ] 2.3.8 Create metadata.yml
  - [ ] Include spring-boot-starter-data-jpa
  - [ ] Include postgresql driver
- [ ] 2.3.9 Create application-properties.yml.ftl
  - [ ] Configure datasource (url, username, password)
- [ ] 2.3.10 Validation
  - [ ] Generate PostgreSQL adapter
  - [ ] Verify code compiles
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Verify coverage >85%
  - [ ] Commit changes: "Add PostgreSQL adapter (imperative)"

### Task 2.4: Update Index Files
- [ ] 2.4.1 Create driven-adapters/index.json
  - [ ] List mongodb, postgresql adapters
  - [ ] Include name, displayName, description, type, status, version
- [ ] 2.4.2 Create entry-points/index.json
  - [ ] List rest adapter
  - [ ] Include metadata
- [ ] 2.4.3 Validation
  - [ ] Verify JSON is valid
  - [ ] Commit changes: "Add index files for imperative adapters"

### Task 2.5: Test Essential Adapters Integration
- [ ] 2.5.1 Generate complete imperative project
  - [ ] Generate project with REST + MongoDB + PostgreSQL
  - [ ] Verify all adapters generated
- [ ] 2.5.2 Compile and test generated project
  - [ ] Run `./gradlew build` in generated project
  - [ ] Verify no errors
- [ ] 2.5.3 Final validation
  - [ ] Run plugin tests: `./gradlew test`
  - [ ] Verify coverage >85%: `./gradlew jacocoTestReport`
  - [ ] Run full build: `./gradlew build`
  - [ ] Commit changes: "Complete Phase 2: Essential adapters"
  - [ ] Tag: "phase-2-complete"

## PHASE 3: Reactive Missing Adapters

### Task 3.1: Create HTTP Client Adapter (Reactive)
- [ ] 3.1.1 Create adapter directory
  - [ ] Create templates/frameworks/spring/reactive/adapters/driven-adapters/http-client/
- [ ] 3.1.2 Create Adapter.java.ftl
  - [ ] Use WebClient
  - [ ] Return Mono<T> or Flux<T>
  - [ ] Support GET, POST, PUT, DELETE, PATCH
- [ ] 3.1.3 Create Config.java.ftl
  - [ ] Configure WebClient with base URL, timeout
- [ ] 3.1.4 Create Test.java.ftl
  - [ ] Use MockWebServer
- [ ] 3.1.5 Create metadata.yml and application-properties.yml.ftl
- [ ] 3.1.6 Validation
  - [ ] Generate adapter, verify compilation
  - [ ] Run `./gradlew build` - must succeed
  - [ ] Run `./gradlew test` - all tests must pass
  - [ ] Verify coverage >85%
  - [ ] Commit changes: "Add HTTP Client adapter (reactive)"

### Task 3.2: Create DynamoDB Adapter (Reactive)
- [ ] 3.2.1 Create adapter with DynamoDbAsyncClient
- [ ] 3.2.2 Create all templates (Adapter, Config, Entity, Test, metadata)
- [ ] 3.2.3 Validation (build, test, coverage >85%)
- [ ] 3.2.4 Commit changes: "Add DynamoDB adapter (reactive)"

### Task 3.3: Create SQS Producer Adapter (Reactive)
- [ ] 3.3.1 Create adapter with SqsAsyncClient
- [ ] 3.3.2 Create all templates
- [ ] 3.3.3 Validation (build, test, coverage >85%)
- [ ] 3.3.4 Commit changes: "Add SQS Producer adapter (reactive)"

### Task 3.4: Create SQS Consumer Entry Point (Reactive)
- [ ] 3.4.1 Create entry point with @SqsListener
- [ ] 3.4.2 Create all templates
- [ ] 3.4.3 Validation (build, test, coverage >85%)
- [ ] 3.4.4 Commit changes: "Add SQS Consumer entry point (reactive)"

### Task 3.5: Create GraphQL Entry Point (Reactive)
- [ ] 3.5.1 Create entry point with @QueryMapping/@MutationMapping
- [ ] 3.5.2 Create schema.graphqls.ftl template
- [ ] 3.5.3 Create all templates
- [ ] 3.5.4 Validation (build, test, coverage >85%)
- [ ] 3.5.5 Commit changes: "Add GraphQL entry point (reactive)"

### Task 3.6: Create gRPC Entry Point (Reactive)
- [ ] 3.6.1 Create entry point with ReactorStub
- [ ] 3.6.2 Create .proto template
- [ ] 3.6.3 Create all templates
- [ ] 3.6.4 Validation (build, test, coverage >85%)
- [ ] 3.6.5 Commit changes: "Add gRPC entry point (reactive)"

### Task 3.7: Update Reactive Index Files
- [ ] 3.7.1 Update driven-adapters/index.json
  - [ ] Add http-client, dynamodb, sqs-producer
- [ ] 3.7.2 Update entry-points/index.json
  - [ ] Add graphql, grpc, sqs-consumer
- [ ] 3.7.3 Validation
  - [ ] Verify JSON valid
  - [ ] Commit changes: "Update reactive index files"

### Task 3.8: Test Reactive Adapters Integration
- [ ] 3.8.1 Generate project with all reactive adapters
- [ ] 3.8.2 Compile and test
- [ ] 3.8.3 Final validation (build, test, coverage >85%)
- [ ] 3.8.4 Commit: "Complete Phase 3: Reactive adapters"
- [ ] 3.8.5 Tag: "phase-3-complete"

## PHASE 4: Imperative Remaining Adapters

### Task 4.1: Create Redis Adapter (Imperative)
- [ ] 4.1.1 Create adapter with Jedis/RedisTemplate
- [ ] 4.1.2 Create all templates
- [ ] 4.1.3 Validation (build, test, coverage >85%)
- [ ] 4.1.4 Commit changes: "Add Redis adapter (imperative)"

### Task 4.2: Create HTTP Client Adapter (Imperative)
- [ ] 4.2.1 Create adapter with RestTemplate
- [ ] 4.2.2 Create all templates
- [ ] 4.2.3 Validation (build, test, coverage >85%)
- [ ] 4.2.4 Commit changes: "Add HTTP Client adapter (imperative)"

### Task 4.3: Create DynamoDB Adapter (Imperative)
- [ ] 4.3.1 Create adapter with DynamoDbClient (sync)
- [ ] 4.3.2 Create all templates
- [ ] 4.3.3 Validation (build, test, coverage >85%)
- [ ] 4.3.4 Commit changes: "Add DynamoDB adapter (imperative)"

### Task 4.4: Create SQS Producer Adapter (Imperative)
- [ ] 4.4.1 Create adapter with SqsClient (sync)
- [ ] 4.4.2 Create all templates
- [ ] 4.4.3 Validation (build, test, coverage >85%)
- [ ] 4.4.4 Commit changes: "Add SQS Producer adapter (imperative)"

### Task 4.5: Create SQS Consumer Entry Point (Imperative)
- [ ] 4.5.1 Create entry point with @SqsListener (sync)
- [ ] 4.5.2 Create all templates
- [ ] 4.5.3 Validation (build, test, coverage >85%)
- [ ] 4.5.4 Commit changes: "Add SQS Consumer entry point (imperative)"

### Task 4.6: Create GraphQL Entry Point (Imperative)
- [ ] 4.6.1 Create entry point with blocking resolvers
- [ ] 4.6.2 Create schema.graphqls.ftl template
- [ ] 4.6.3 Create all templates
- [ ] 4.6.4 Validation (build, test, coverage >85%)
- [ ] 4.6.5 Commit changes: "Add GraphQL entry point (imperative)"

### Task 4.7: Create gRPC Entry Point (Imperative)
- [ ] 4.7.1 Create entry point with BlockingStub
- [ ] 4.7.2 Create .proto template
- [ ] 4.7.3 Create all templates
- [ ] 4.7.4 Validation (build, test, coverage >85%)
- [ ] 4.7.5 Commit changes: "Add gRPC entry point (imperative)"

### Task 4.8: Update Imperative Index Files
- [ ] 4.8.1 Update driven-adapters/index.json
  - [ ] Add redis, http-client, dynamodb, sqs-producer
- [ ] 4.8.2 Update entry-points/index.json
  - [ ] Add graphql, grpc, sqs-consumer
- [ ] 4.8.3 Validation
  - [ ] Verify JSON valid
  - [ ] Commit changes: "Update imperative index files"

### Task 4.9: Test Imperative Adapters Integration
- [ ] 4.9.1 Generate project with all imperative adapters
- [ ] 4.9.2 Compile and test
- [ ] 4.9.3 Final validation (build, test, coverage >85%)
- [ ] 4.9.4 Commit: "Complete Phase 4: Imperative adapters"
- [ ] 4.9.5 Tag: "phase-4-complete"

## PHASE 5: Documentation and Polish

### Task 5.1: Create Adapter Documentation
- [ ] 5.1.1 Create README.md for each adapter
  - [ ] Description and use cases
  - [ ] Configuration properties
  - [ ] Code examples
  - [ ] Dependencies list
- [ ] 5.1.2 Validation
  - [ ] Review documentation for completeness
  - [ ] Commit changes: "Add adapter documentation"

### Task 5.2: Update Main Documentation
- [ ] 5.2.1 Update plugin README.md
  - [ ] Add imperative paradigm to features
  - [ ] Update adapter list
  - [ ] Add examples
- [ ] 5.2.2 Update getting-started.md
  - [ ] Add imperative paradigm selection
  - [ ] Add adapter generation examples
- [ ] 5.2.3 Create migration guide
  - [ ] Document differences between reactive and imperative
  - [ ] Provide migration examples
- [ ] 5.2.4 Validation
  - [ ] Review documentation
  - [ ] Commit changes: "Update main documentation"

### Task 5.3: Add Integration Tests
- [ ] 5.3.1 Create integration tests for imperative adapters
  - [ ] Test REST adapter with MockMvc
  - [ ] Test MongoDB adapter with Testcontainers
  - [ ] Test PostgreSQL adapter with Testcontainers
  - [ ] Test Redis adapter with Testcontainers
  - [ ] Test AWS adapters with LocalStack
- [ ] 5.3.2 Create integration tests for reactive adapters
  - [ ] Test new reactive adapters
- [ ] 5.3.3 Validation
  - [ ] Run all integration tests
  - [ ] Verify coverage >85%
  - [ ] Commit changes: "Add integration tests"

### Task 5.4: Final Validation and Release
- [ ] 5.4.1 Run complete test suite
  - [ ] Run `./gradlew clean test` - all tests must pass
  - [ ] Fix any failing tests
- [ ] 5.4.2 Verify test coverage
  - [ ] Run `./gradlew jacocoTestReport`
  - [ ] Verify overall coverage >85%
  - [ ] Add tests if coverage is low
- [ ] 5.4.3 Run full build
  - [ ] Run `./gradlew clean build` - must succeed
  - [ ] Fix any compilation errors
- [ ] 5.4.4 Test plugin end-to-end
  - [ ] Generate reactive project with all adapters
  - [ ] Generate imperative project with all adapters
  - [ ] Verify both projects compile and run
- [ ] 5.4.5 Code quality checks
  - [ ] Run static analysis tools
  - [ ] Fix any warnings
  - [ ] Format code
- [ ] 5.4.6 Final commit and tag
  - [ ] Commit changes: "Complete Phase 5: Documentation and polish"
  - [ ] Tag: "phase-5-complete"
  - [ ] Tag: "spring-imperative-support-complete"

## Notes

**CRITICAL VALIDATION RULES:**
- After EVERY major task, you MUST:
  1. Run `./gradlew test` - ALL tests must pass
  2. Run `./gradlew jacocoTestReport` - Coverage must be >85%
  3. Run `./gradlew build` - Build must succeed
  4. If any step fails, FIX IT before proceeding to next task

**IMPORT UPDATE RULES:**
- When moving/renaming classes, ALWAYS update imports in:
  - Application layer files
  - Infrastructure layer files
  - Test files
  - Use IDE refactoring tools to ensure all references are updated

**COMMIT STRATEGY:**
- Commit after each completed task
- Use descriptive commit messages
- Tag major milestones (phase-0-complete, phase-1-complete, etc.)
