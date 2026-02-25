# Requirements: Spring Imperative Support

## Introduction

This document specifies requirements for adding complete Spring Imperative (MVC) paradigm support to the Clean Architecture Gradle Plugin. Currently, the plugin only supports Spring Reactive (WebFlux) with limited adapters (MongoDB, PostgreSQL, Redis, REST). This feature adds the imperative paradigm with blocking I/O and implements all missing adapters for both paradigms.

**IMPORTANT:** Before implementing new features, this spec addresses critical technical debt in the domain layer through refactoring.

**Current State:**
- ✅ Plugin core infrastructure exists (generators, validators, template system)
- ✅ Spring Reactive templates exist at `templates/frameworks/spring/reactive/`
- ✅ Reactive adapters: MongoDB, PostgreSQL, Redis (driven), REST (entry-point)
- ❌ NO imperative paradigm support (`templates/frameworks/spring/imperative/` doesn't exist)
- ❌ Missing adapters for both paradigms: HTTP Client, DynamoDB, SQS, GraphQL, gRPC
- ❌ Technical debt: Domain model needs refactoring (flat structure, manual builders, nested classes)

**What This Spec Does:**
1. **PHASE 0 (Refactoring):** Clean up domain layer technical debt
2. **PHASE 1:** Create `spring/imperative/` template structure with MVC support
3. **PHASE 2:** Implement missing adapters for both reactive and imperative paradigms

## Glossary

- **Paradigm**: Programming model (Reactive = non-blocking, Imperative = blocking)
- **Spring_Reactive**: WebFlux with Mono/Flux, R2DBC, Lettuce, WebClient
- **Spring_Imperative**: MVC with POJO/List, JPA, Jedis, RestTemplate
- **Driven_Adapter**: Outbound adapter (database, cache, HTTP client, message queue)
- **Entry_Point**: Inbound adapter (REST controller, GraphQL resolver, gRPC service)
- **Template**: FreeMarker (.ftl) file for code generation
- **Metadata**: YAML file describing adapter dependencies and configuration
- **Lombok**: Java library that reduces boilerplate code via annotations
- **Technical_Debt**: Code that needs refactoring to improve maintainability

## Requirements

## PHASE 0: Domain Layer Refactoring (Technical Debt)

### Requirement 0.1: Add Lombok Dependency

**User Story:** As a developer, I want Lombok integrated, so that I can reduce boilerplate code with annotations.

#### Acceptance Criteria

1. THE build.gradle.kts SHALL include Lombok dependency with correct version
2. THE Lombok annotation processor SHALL be configured for compilation
3. THE IDE annotation processing SHALL be enabled
4. THE Lombok version SHALL be compatible with Java 21
5. THE Build SHALL compile successfully with Lombok dependency

### Requirement 0.2: Reorganize Domain Model by Subdomains

**User Story:** As a developer, I want domain models organized by subdomain, so that the codebase is easier to navigate.

#### Acceptance Criteria

1. THE domain/model/ SHALL be reorganized into subpackages:
   - `domain/model/config/` - ProjectConfig, TemplateConfig, TemplateMode, TemplateSource
   - `domain/model/adapter/` - AdapterConfig, AdapterMetadata, InputAdapterConfig
   - `domain/model/entity/` - EntityConfig
   - `domain/model/usecase/` - UseCaseConfig
   - `domain/model/project/` - ArchitectureType, Framework, Paradigm
   - `domain/model/file/` - GeneratedFile
   - `domain/model/validation/` - ValidationResult
   - `domain/model/structure/` - StructureMetadata, LayerDependencies, NamingConventions, MergeResult
2. ALL imports across the codebase SHALL be updated to reflect new package structure
3. THE Tests SHALL pass after reorganization
4. THE Build SHALL compile successfully

### Requirement 0.3: Extract Nested Static Classes

**User Story:** As a developer, I want nested static classes extracted to separate files, so that each class has its own file.

#### Acceptance Criteria

1. THE AdapterConfig.Builder SHALL be extracted to AdapterConfigBuilder.java
2. THE AdapterConfig.AdapterType enum SHALL be extracted to AdapterType.java in domain/model/adapter/
3. THE AdapterConfig.AdapterMethod record SHALL be extracted to AdapterMethod.java
4. THE AdapterConfig.MethodParameter record SHALL be extracted to MethodParameter.java
5. THE InputAdapterConfig.Builder SHALL be extracted to InputAdapterConfigBuilder.java
6. THE InputAdapterConfig.InputAdapterType enum SHALL be extracted to InputAdapterType.java
7. THE InputAdapterConfig.HttpMethod enum SHALL be extracted to HttpMethod.java
8. THE InputAdapterConfig.ParameterType enum SHALL be extracted to ParameterType.java
9. THE InputAdapterConfig.Endpoint record SHALL be extracted to Endpoint.java
10. THE InputAdapterConfig.EndpointParameter record SHALL be extracted to EndpointParameter.java
11. THE EntityConfig.Builder SHALL be extracted to EntityConfigBuilder.java
12. THE EntityConfig.EntityField record SHALL be extracted to EntityField.java
13. THE ProjectConfig.Builder SHALL be extracted to ProjectConfigBuilder.java
14. THE UseCaseConfig.Builder SHALL be extracted to UseCaseConfigBuilder.java
15. THE GeneratedFile.FileType enum SHALL be extracted to FileType.java in domain/model/file/
16. ALL imports SHALL be updated across the codebase
17. THE Tests SHALL pass after extraction
18. THE Build SHALL compile successfully

### Requirement 0.4: Replace Manual Builders with Lombok @Builder

**User Story:** As a developer, I want Lombok @Builder instead of manual builders, so that builder code is generated automatically.

#### Acceptance Criteria

1. THE AdapterConfig SHALL use @Builder annotation instead of manual Builder class
2. THE InputAdapterConfig SHALL use @Builder annotation instead of manual Builder class
3. THE EntityConfig SHALL use @Builder annotation instead of manual Builder class
4. THE ProjectConfig SHALL use @Builder annotation instead of manual Builder class
5. THE UseCaseConfig SHALL use @Builder annotation instead of manual Builder class
6. THE Manual Builder classes SHALL be deleted after Lombok @Builder is applied
7. ALL usages of .builder() SHALL continue to work with Lombok-generated builders
8. THE Tests SHALL pass after replacing builders
9. THE Build SHALL compile successfully

### Requirement 0.5: Replace Simple Getters with Lombok @Getter

**User Story:** As a developer, I want Lombok @Getter for simple getters, so that getter boilerplate is eliminated.

#### Acceptance Criteria

1. ALL domain model classes SHALL be analyzed for getter methods
2. SIMPLE getters (return field; with no logic) SHALL be replaced with @Getter annotation
3. COMPLEX getters (with logic, calculations, or transformations) SHALL be kept as methods
4. THE @Getter annotation SHALL be applied at class level for all-field getters
5. THE @Getter annotation SHALL be applied at field level for selective getters
6. THE Simple getter methods SHALL be deleted after @Getter is applied
7. ALL usages of getters SHALL continue to work with Lombok-generated getters
8. THE Tests SHALL pass after replacing getters
9. THE Build SHALL compile successfully

### Requirement 0.6: Replace Simple Setters with Lombok @Setter

**User Story:** As a developer, I want Lombok @Setter for simple setters, so that setter boilerplate is eliminated.

#### Acceptance Criteria

1. ALL domain model classes SHALL be analyzed for setter methods
2. SIMPLE setters (this.field = field; with no logic) SHALL be replaced with @Setter annotation
3. COMPLEX setters (with validation, side effects, or transformations) SHALL be kept as methods
4. THE @Setter annotation SHALL be applied at class level for all-field setters
5. THE @Setter annotation SHALL be applied at field level for selective setters
6. THE Simple setter methods SHALL be deleted after @Setter is applied
7. ALL usages of setters SHALL continue to work with Lombok-generated setters
8. THE Tests SHALL pass after replacing setters
9. THE Build SHALL compile successfully

### Requirement 0.7: Use Lombok @Data for Mutable POJOs

**User Story:** As a developer, I want @Data for mutable POJOs, so that getters, setters, equals, hashCode, and toString are generated.

#### Acceptance Criteria

1. MUTABLE domain model classes (with setters) SHALL use @Data annotation
2. THE @Data annotation SHALL replace @Getter, @Setter, @ToString, @EqualsAndHashCode
3. IMMUTABLE classes (records or final fields) SHALL NOT use @Data
4. THE Tests SHALL pass after applying @Data
5. THE Build SHALL compile successfully

### Requirement 0.8: Validation and Testing

**User Story:** As a developer, I want comprehensive validation after refactoring, so that no functionality is broken.

#### Acceptance Criteria

1. ALL unit tests SHALL pass after refactoring
2. ALL integration tests SHALL pass after refactoring
3. THE Test coverage SHALL be above 85%
4. THE Gradle build SHALL complete successfully (./gradlew build)
5. THE Plugin SHALL generate projects successfully after refactoring
6. NO compilation errors SHALL exist
7. NO warnings about missing Lombok annotations SHALL exist
8. THE Generated bytecode SHALL be equivalent to pre-refactoring code

## PHASE 1: Spring Imperative Foundation

### Requirement 1: Spring Imperative Template Structure

**User Story:** As a developer, I want to generate Spring Imperative projects, so that I can build applications with traditional blocking I/O.

#### Acceptance Criteria

1. THE Plugin SHALL create directory structure `templates/frameworks/spring/imperative/`
2. THE Structure SHALL include subdirectories: `domain/`, `usecase/`, `project/`, `adapters/`
3. THE `domain/` templates SHALL be reused from reactive via symlinks (Entity.java.ftl is paradigm-agnostic)
4. THE `usecase/` templates SHALL use synchronous return types (T instead of Mono<T>, List<T> instead of Flux<T>)
5. THE `project/Application.java.ftl` SHALL be reused from reactive via symlink (paradigm-agnostic)
6. THE `project/application.yml.ftl` SHALL configure Spring MVC instead of WebFlux
7. THE `adapters/` SHALL have subdirectories: `driven-adapters/` and `entry-points/`
8. EACH adapter subdirectory SHALL include `index.json` listing available adapters
9. THE Plugin SHALL resolve template paths using pattern: `frameworks/{framework}/{paradigm}/...`
10. FOR ALL imperative templates, imports SHALL use blocking libraries (no reactor.core imports)

### Requirement 2: Imperative UseCase Templates

**User Story:** As a developer, I want use case templates with blocking signatures, so that generated code matches imperative paradigm.

#### Acceptance Criteria

1. THE `UseCase.java.ftl` SHALL generate methods returning T instead of Mono<T>
2. THE `UseCase.java.ftl` SHALL generate methods returning List<T> instead of Flux<T>
3. THE `InputPort.java.ftl` SHALL define interface with synchronous method signatures
4. THE Templates SHALL NOT import reactor.core.publisher classes
5. THE `Test.java.ftl` SHALL use standard JUnit assertions without reactor-test
6. THE Generated use cases SHALL be registered as Spring beans via component scanning
7. FOR ALL CRUD operations, methods SHALL use blocking return types

### Requirement 3: REST Entry Point - Imperative

**User Story:** As a developer, I want REST controllers using Spring MVC, so that I can expose blocking HTTP endpoints.

#### Acceptance Criteria

1. THE Plugin SHALL generate REST adapter at `imperative/adapters/entry-points/rest/`
2. THE Adapter SHALL use @RestController with Spring MVC (not WebFlux RouterFunction)
3. THE Controller methods SHALL return ResponseEntity<T> or ResponseEntity<List<T>>
4. THE Adapter SHALL support GET, POST, PUT, DELETE, PATCH methods
5. THE Adapter SHALL use @Valid for request body validation
6. THE Adapter SHALL include @ControllerAdvice for exception handling
7. THE metadata.yml SHALL include spring-boot-starter-web dependency
8. THE metadata.yml SHALL include spring-boot-starter-validation dependency
9. THE Test template SHALL use MockMvc for controller testing
10. FOR ALL endpoints, the adapter SHALL correctly serialize/deserialize JSON

### Requirement 4: MongoDB Driven Adapter - Imperative

**User Story:** As a developer, I want MongoDB adapter using JPA, so that I can perform blocking database operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate MongoDB adapter at `imperative/adapters/driven-adapters/mongodb/`
2. THE Adapter SHALL use Spring Data JPA with Hibernate (not Reactive MongoDB)
3. THE Repository SHALL extend JpaRepository<Entity, ID>
4. THE Adapter methods SHALL return T or List<T> (not Mono/Flux)
5. THE Config SHALL configure JPA EntityManager and transaction management
6. THE metadata.yml SHALL include spring-boot-starter-data-jpa dependency
7. THE metadata.yml SHALL include mongodb-driver-sync dependency
8. THE application-properties.yml.ftl SHALL include JPA and MongoDB configuration
9. THE Test SHALL use @DataJpaTest for repository testing
10. FOR ALL CRUD operations, the adapter SHALL use blocking JPA methods

### Requirement 5: PostgreSQL Driven Adapter - Imperative

**User Story:** As a developer, I want PostgreSQL adapter using JPA, so that I can perform blocking relational database operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate PostgreSQL adapter at `imperative/adapters/driven-adapters/postgresql/`
2. THE Adapter SHALL use Spring Data JPA with Hibernate
3. THE Repository SHALL extend JpaRepository<Entity, ID>
4. THE Adapter methods SHALL return T or List<T>
5. THE Config SHALL configure DataSource with HikariCP connection pool
6. THE metadata.yml SHALL include spring-boot-starter-data-jpa dependency
7. THE metadata.yml SHALL include postgresql driver dependency
8. THE application-properties.yml.ftl SHALL include datasource configuration (url, username, password)
9. THE Test SHALL use @DataJpaTest with Testcontainers PostgreSQL
10. FOR ALL CRUD operations, the adapter SHALL use blocking JPA methods

### Requirement 6: Redis Driven Adapter - Imperative

**User Story:** As a developer, I want Redis adapter using Jedis, so that I can perform blocking cache operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate Redis adapter at `imperative/adapters/driven-adapters/redis/`
2. THE Adapter SHALL use RedisTemplate with Jedis client (not Lettuce reactive)
3. THE Adapter SHALL support get, set, delete, exists, expire operations
4. THE Adapter SHALL support hash, list, set, sorted set data structures
5. THE Config SHALL create JedisConnectionFactory and RedisTemplate beans
6. THE metadata.yml SHALL include spring-boot-starter-data-redis dependency
7. THE metadata.yml SHALL include jedis client dependency
8. THE application-properties.yml.ftl SHALL include redis configuration (host, port, password)
9. THE Test SHALL use Testcontainers Redis module
10. FOR ALL operations, the adapter SHALL serialize objects to JSON

### Requirement 7: HTTP Client Driven Adapter - Reactive

**User Story:** As a developer using Reactive, I want HTTP client adapter, so that I can make non-blocking HTTP requests.

#### Acceptance Criteria

1. THE Plugin SHALL generate HTTP client at `reactive/adapters/driven-adapters/http-client/`
2. THE Adapter SHALL use WebClient for non-blocking requests
3. THE Adapter SHALL support GET, POST, PUT, DELETE, PATCH methods
4. THE Adapter methods SHALL return Mono<T> or Flux<T>
5. THE Config SHALL create WebClient bean with base URL and timeout configuration
6. THE Adapter SHALL handle HTTP errors and map to domain exceptions
7. THE metadata.yml SHALL include spring-boot-starter-webflux dependency
8. THE application-properties.yml.ftl SHALL include http.client configuration
9. THE Test SHALL use MockWebServer for integration testing
10. FOR ALL methods, the adapter SHALL correctly serialize/deserialize JSON

### Requirement 8: HTTP Client Driven Adapter - Imperative

**User Story:** As a developer using Imperative, I want HTTP client adapter, so that I can make blocking HTTP requests.

#### Acceptance Criteria

1. THE Plugin SHALL generate HTTP client at `imperative/adapters/driven-adapters/http-client/`
2. THE Adapter SHALL use RestTemplate for blocking requests
3. THE Adapter SHALL support GET, POST, PUT, DELETE, PATCH methods
4. THE Adapter methods SHALL return T or List<T>
5. THE Config SHALL create RestTemplate bean with timeout and connection pool settings
6. THE Adapter SHALL use ResponseErrorHandler for error handling
7. THE metadata.yml SHALL include spring-boot-starter-web dependency
8. THE application-properties.yml.ftl SHALL include http.client configuration
9. THE Test SHALL use WireMock for integration testing
10. FOR ALL methods, the adapter SHALL correctly serialize/deserialize JSON

### Requirement 9: DynamoDB Driven Adapter - Reactive

**User Story:** As a developer using Reactive, I want DynamoDB adapter, so that I can perform non-blocking NoSQL operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate DynamoDB adapter at `reactive/adapters/driven-adapters/dynamodb/`
2. THE Adapter SHALL use AWS SDK v2 DynamoDbAsyncClient
3. THE Adapter SHALL support put, get, update, delete, query, scan operations
4. THE Adapter methods SHALL return Mono<T> or Flux<T>
5. THE Config SHALL create DynamoDbAsyncClient with region and endpoint configuration
6. THE Adapter SHALL use DynamoDbEnhancedAsyncClient for object mapping
7. THE metadata.yml SHALL include software.amazon.awssdk:dynamodb-enhanced dependency
8. THE application-properties.yml.ftl SHALL include AWS DynamoDB configuration with security warning
9. THE Test SHALL use LocalStack Testcontainer
10. FOR ALL operations, the adapter SHALL map domain entities to DynamoDB items

### Requirement 10: DynamoDB Driven Adapter - Imperative

**User Story:** As a developer using Imperative, I want DynamoDB adapter, so that I can perform blocking NoSQL operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate DynamoDB adapter at `imperative/adapters/driven-adapters/dynamodb/`
2. THE Adapter SHALL use AWS SDK v2 DynamoDbClient (sync)
3. THE Adapter SHALL support put, get, update, delete, query, scan operations
4. THE Adapter methods SHALL return T or List<T>
5. THE Config SHALL create DynamoDbClient with region and endpoint configuration
6. THE Adapter SHALL use DynamoDbEnhancedClient for object mapping
7. THE metadata.yml SHALL include software.amazon.awssdk:dynamodb-enhanced dependency
8. THE application-properties.yml.ftl SHALL include AWS DynamoDB configuration with security warning
9. THE Test SHALL use LocalStack Testcontainer
10. FOR ALL operations, the adapter SHALL map domain entities to DynamoDB items

### Requirement 11: SQS Producer Driven Adapter - Reactive

**User Story:** As a developer using Reactive, I want SQS producer, so that I can send messages asynchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate SQS producer at `reactive/adapters/driven-adapters/sqs-producer/`
2. THE Adapter SHALL use AWS SDK v2 SqsAsyncClient
3. THE Adapter methods SHALL return Mono<SendMessageResponse>
4. THE Adapter SHALL support message attributes and delay seconds
5. THE Config SHALL create SqsAsyncClient with region and endpoint configuration
6. THE metadata.yml SHALL include software.amazon.awssdk:sqs dependency
7. THE application-properties.yml.ftl SHALL include AWS SQS configuration with security warning
8. THE Test SHALL use LocalStack Testcontainer
9. FOR ALL send operations, the adapter SHALL return message ID

### Requirement 12: SQS Producer Driven Adapter - Imperative

**User Story:** As a developer using Imperative, I want SQS producer, so that I can send messages synchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate SQS producer at `imperative/adapters/driven-adapters/sqs-producer/`
2. THE Adapter SHALL use AWS SDK v2 SqsClient (sync)
3. THE Adapter methods SHALL return SendMessageResponse
4. THE Adapter SHALL support message attributes and delay seconds
5. THE Config SHALL create SqsClient with region and endpoint configuration
6. THE metadata.yml SHALL include software.amazon.awssdk:sqs dependency
7. THE application-properties.yml.ftl SHALL include AWS SQS configuration with security warning
8. THE Test SHALL use LocalStack Testcontainer
9. FOR ALL send operations, the adapter SHALL return message ID

### Requirement 13: SQS Consumer Entry Point - Reactive

**User Story:** As a developer using Reactive, I want SQS consumer, so that I can process messages asynchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate SQS consumer at `reactive/adapters/entry-points/sqs-consumer/`
2. THE Adapter SHALL use @SqsListener with SqsAsyncClient
3. THE Adapter SHALL process messages reactively and return Mono<Void>
4. THE Adapter SHALL delete messages after successful processing
5. THE Config SHALL configure polling settings (max messages, wait time)
6. THE metadata.yml SHALL include io.awspring.cloud:spring-cloud-aws-starter-sqs dependency
7. THE application-properties.yml.ftl SHALL include SQS consumer configuration
8. THE Test SHALL use LocalStack Testcontainer
9. FOR ALL processing, the adapter SHALL handle errors with retry logic

### Requirement 14: SQS Consumer Entry Point - Imperative

**User Story:** As a developer using Imperative, I want SQS consumer, so that I can process messages synchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate SQS consumer at `imperative/adapters/entry-points/sqs-consumer/`
2. THE Adapter SHALL use @SqsListener with SqsClient (sync)
3. THE Adapter SHALL process messages synchronously
4. THE Adapter SHALL delete messages after successful processing
5. THE Config SHALL configure polling settings (max messages, wait time)
6. THE metadata.yml SHALL include io.awspring.cloud:spring-cloud-aws-starter-sqs dependency
7. THE application-properties.yml.ftl SHALL include SQS consumer configuration
8. THE Test SHALL use LocalStack Testcontainer
9. FOR ALL processing, the adapter SHALL handle errors with retry logic

### Requirement 15: GraphQL Entry Point - Reactive

**User Story:** As a developer using Reactive, I want GraphQL adapter, so that I can expose reactive GraphQL API.

#### Acceptance Criteria

1. THE Plugin SHALL generate GraphQL adapter at `reactive/adapters/entry-points/graphql/`
2. THE Adapter SHALL use @QueryMapping and @MutationMapping with Mono/Flux return types
3. THE Adapter SHALL include schema.graphqls template with type definitions
4. THE Config SHALL configure GraphQL path and schema location
5. THE Adapter SHALL support DataLoader for batching
6. THE metadata.yml SHALL include spring-boot-starter-graphql dependency
7. THE metadata.yml SHALL include spring-boot-starter-webflux dependency
8. THE application-properties.yml.ftl SHALL include GraphQL configuration
9. THE Test SHALL use GraphQlTester
10. FOR ALL operations, the adapter SHALL resolve fields correctly

### Requirement 16: GraphQL Entry Point - Imperative

**User Story:** As a developer using Imperative, I want GraphQL adapter, so that I can expose blocking GraphQL API.

#### Acceptance Criteria

1. THE Plugin SHALL generate GraphQL adapter at `imperative/adapters/entry-points/graphql/`
2. THE Adapter SHALL use @QueryMapping and @MutationMapping with POJO/List return types
3. THE Adapter SHALL include schema.graphqls template with type definitions
4. THE Config SHALL configure GraphQL path and schema location
5. THE Adapter SHALL support DataLoader for batching
6. THE metadata.yml SHALL include spring-boot-starter-graphql dependency
7. THE metadata.yml SHALL include spring-boot-starter-web dependency
8. THE application-properties.yml.ftl SHALL include GraphQL configuration
9. THE Test SHALL use GraphQlTester
10. FOR ALL operations, the adapter SHALL resolve fields correctly

### Requirement 17: gRPC Entry Point - Reactive

**User Story:** As a developer using Reactive, I want gRPC adapter, so that I can expose reactive RPC endpoints.

#### Acceptance Criteria

1. THE Plugin SHALL generate gRPC adapter at `reactive/adapters/entry-points/grpc/`
2. THE Adapter SHALL use grpc-spring-boot-starter with ReactorStub
3. THE Adapter SHALL include .proto template with service and message definitions
4. THE Adapter methods SHALL return Mono<T> or Flux<T>
5. THE Config SHALL configure gRPC server port and security
6. THE metadata.yml SHALL include net.devh:grpc-spring-boot-starter dependency
7. THE metadata.yml SHALL include com.salesforce.servicelibs:reactor-grpc-stub dependency
8. THE application-properties.yml.ftl SHALL include gRPC configuration
9. THE Test SHALL use in-process gRPC server
10. FOR ALL methods, the adapter SHALL serialize/deserialize Protocol Buffers

### Requirement 18: gRPC Entry Point - Imperative

**User Story:** As a developer using Imperative, I want gRPC adapter, so that I can expose blocking RPC endpoints.

#### Acceptance Criteria

1. THE Plugin SHALL generate gRPC adapter at `imperative/adapters/entry-points/grpc/`
2. THE Adapter SHALL use grpc-spring-boot-starter with BlockingStub
3. THE Adapter SHALL include .proto template with service and message definitions
4. THE Adapter methods SHALL return T or Iterator<T>
5. THE Config SHALL configure gRPC server port and security
6. THE metadata.yml SHALL include net.devh:grpc-spring-boot-starter dependency
7. THE application-properties.yml.ftl SHALL include gRPC configuration
8. THE Test SHALL use in-process gRPC server
9. FOR ALL methods, the adapter SHALL serialize/deserialize Protocol Buffers

### Requirement 19: Adapter Index Files

**User Story:** As a developer, I want adapter discovery, so that the plugin can list available adapters.

#### Acceptance Criteria

1. EACH adapter directory SHALL include index.json file
2. THE index.json SHALL list all adapters with name, displayName, description, type, status, version
3. THE Plugin SHALL read index.json to discover available adapters
4. THE index.json SHALL be updated when new adapters are added
5. FOR ALL paradigms, index.json SHALL accurately reflect available adapters

### Requirement 20: Template Reuse Strategy

**User Story:** As a maintainer, I want to reuse templates where possible, so that maintenance is easier.

#### Acceptance Criteria

1. THE Domain Entity template SHALL be shared via symlink (paradigm-agnostic)
2. THE Application.java template SHALL be shared via symlink (paradigm-agnostic)
3. THE UseCase templates SHALL be paradigm-specific (different for reactive/imperative)
4. THE Adapter templates SHALL be paradigm-specific (different clients/libraries)
5. THE application.yml template SHALL be paradigm-specific (different Spring starters)
6. FOR ALL shared templates, symlinks SHALL point to reactive/ as source

### Requirement 21: Metadata Completeness

**User Story:** As a developer, I want complete metadata, so that generated code has correct dependencies.

#### Acceptance Criteria

1. EACH adapter SHALL have metadata.yml with name, type, description
2. THE metadata.yml SHALL include dependencies array with groupId, artifactId, version
3. THE metadata.yml SHALL include testDependencies for testing libraries
4. THE metadata.yml SHALL reference applicationPropertiesTemplate file
5. THE metadata.yml SHALL list configurationClasses if needed
6. FOR ALL adapters, metadata SHALL be sufficient to generate working code

### Requirement 22: Application Properties Templates

**User Story:** As a developer, I want complete configuration templates, so that adapters work out of the box.

#### Acceptance Criteria

1. EACH adapter SHALL have application-properties.yml.ftl template
2. THE Template SHALL include all required configuration properties
3. THE Template SHALL use environment variable placeholders for sensitive values
4. THE Template SHALL include comments explaining each property
5. THE Template SHALL include security warnings for credentials
6. FOR ALL adapters, properties SHALL have sensible defaults

### Requirement 23: Test Templates

**User Story:** As a developer, I want generated tests, so that I can verify adapter functionality.

#### Acceptance Criteria

1. EACH adapter SHALL have Test.java.ftl template
2. THE Test SHALL use JUnit 5 and Mockito
3. THE Test SHALL include tests for success and error cases
4. THE Test SHALL mock external dependencies
5. WHERE applicable, integration tests SHALL use Testcontainers
6. FOR ALL adapters, tests SHALL achieve reasonable code coverage

### Requirement 24: Documentation

**User Story:** As a developer, I want adapter documentation, so that I understand how to use them.

#### Acceptance Criteria

1. EACH adapter SHALL have README.md with description and usage examples
2. THE Documentation SHALL explain configuration properties
3. THE Documentation SHALL show code examples
4. THE Documentation SHALL list dependencies
5. FOR ALL adapters, documentation SHALL be clear and complete
