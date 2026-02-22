# Requirements Document

## Introduction

This document specifies requirements for completing Spring Framework support in the Clean Architecture Gradle Plugin. The plugin currently supports Spring Reactive (WebFlux) with limited adapters (Redis, REST). This feature adds complete Spring Imperative (MVC) paradigm support and implements all missing adapters for both reactive and imperative paradigms, enabling developers to choose between non-blocking reactive and traditional blocking I/O approaches.

This specification depends on Spec 1 (complete-architectures-and-template-system) being completed first, as it requires the template system and multi-architecture support to be functional.

## Glossary

- **Spring_Reactive**: Spring WebFlux paradigm using non-blocking I/O with reactive types (Mono, Flux)
- **Spring_Imperative**: Spring MVC paradigm using blocking I/O with synchronous types (POJO, List)
- **Paradigm**: A programming model defining how I/O operations are handled (reactive vs imperative)
- **Driven_Adapter**: An outbound adapter that the application uses to interact with external systems
- **Driving_Adapter**: An inbound adapter that receives requests and invokes the application
- **Adapter_Template**: A FreeMarker template set for generating adapter code (Adapter.java.ftl, Config.java.ftl, Test.java.ftl)
- **Metadata_File**: The metadata.yml file describing adapter properties, dependencies, and configuration
- **Application_Properties_Template**: The application-properties.yml.ftl file containing adapter-specific configuration
- **AWS_SDK_V2**: Amazon Web Services SDK version 2 with separate async and sync clients
- **WebClient**: Spring reactive HTTP client for non-blocking requests
- **RestTemplate**: Spring imperative HTTP client for blocking requests
- **R2DBC**: Reactive Relational Database Connectivity for non-blocking database access
- **JPA**: Java Persistence API for blocking database access with ORM
- **Lettuce**: Reactive Redis client used in Spring Reactive
- **Jedis**: Synchronous Redis client used in Spring Imperative
- **DynamoDB**: AWS NoSQL database service
- **SQS**: AWS Simple Queue Service for message queuing
- **GraphQL**: Query language for APIs with type system
- **gRPC**: High-performance RPC framework using Protocol Buffers
- **Testcontainers**: Library for running Docker containers in integration tests
- **Property_Based_Test**: Test that verifies properties hold for generated code across many inputs
- **Round_Trip_Property**: Property that verifies parsing then printing produces equivalent output

## Requirements

### Requirement 1: Spring Imperative Paradigm Foundation

**User Story:** As a developer, I want to generate projects using Spring Imperative (MVC), so that I can build applications with traditional blocking I/O when reactive programming is not needed.

#### Acceptance Criteria

1. WHEN the user selects Spring Imperative paradigm, THE Plugin SHALL generate projects using Spring Boot with Spring MVC instead of WebFlux
2. THE Plugin SHALL use JPA with Hibernate for database access instead of R2DBC
3. THE Plugin SHALL use RestTemplate for HTTP clients instead of WebClient
4. THE Plugin SHALL generate synchronous method signatures returning User instead of Mono<User>
5. THE Plugin SHALL generate synchronous collection methods returning List<User> instead of Flux<User>
6. THE Plugin SHALL include spring-boot-starter-web dependency instead of spring-boot-starter-webflux
7. THE Plugin SHALL include spring-boot-starter-data-jpa dependency for database adapters
8. THE Plugin SHALL generate @RestController annotations for REST adapters instead of @RestController with RouterFunction
9. THE Plugin SHALL configure Tomcat as the embedded servlet container
10. FOR ALL use case interfaces, imperative paradigm SHALL use synchronous return types while reactive paradigm SHALL use Mono/Flux types

### Requirement 2: HTTP Client Driven Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want an HTTP client adapter, so that I can make non-blocking HTTP requests to external APIs.

#### Acceptance Criteria

1. THE Plugin SHALL generate an HTTP client adapter using WebClient for Spring Reactive
2. THE Adapter SHALL support GET, POST, PUT, DELETE, and PATCH HTTP methods
3. THE Adapter SHALL return Mono<T> for single responses and Flux<T> for collection responses
4. THE Config SHALL create a WebClient bean with configurable base URL, timeout, and connection pool settings
5. THE Adapter SHALL handle HTTP errors and map them to domain exceptions
6. THE Adapter SHALL support request/response logging for debugging
7. THE Application_Properties_Template SHALL include http.client.base-url, http.client.timeout, and http.client.max-connections properties
8. THE Test SHALL use WireMock or MockWebServer for integration testing
9. THE Metadata_File SHALL include spring-boot-starter-webflux dependency
10. FOR ALL HTTP methods, the adapter SHALL correctly serialize request bodies and deserialize response bodies

### Requirement 3: HTTP Client Driven Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want an HTTP client adapter, so that I can make blocking HTTP requests to external APIs.

#### Acceptance Criteria

1. THE Plugin SHALL generate an HTTP client adapter using RestTemplate for Spring Imperative
2. THE Adapter SHALL support GET, POST, PUT, DELETE, and PATCH HTTP methods
3. THE Adapter SHALL return T for single responses and List<T> for collection responses
4. THE Config SHALL create a RestTemplate bean with configurable timeout and connection pool settings
5. THE Adapter SHALL handle HTTP errors using ResponseErrorHandler and map them to domain exceptions
6. THE Adapter SHALL support request/response logging using ClientHttpRequestInterceptor
7. THE Application_Properties_Template SHALL include http.client.base-url, http.client.timeout, and http.client.max-connections properties
8. THE Test SHALL use WireMock for integration testing
9. THE Metadata_File SHALL include spring-boot-starter-web dependency
10. FOR ALL HTTP methods, the adapter SHALL correctly serialize request bodies and deserialize response bodies

### Requirement 4: SQS Producer Driven Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want an SQS producer adapter, so that I can send messages to AWS SQS queues asynchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate an SQS producer adapter using AWS SDK v2 async client for Spring Reactive
2. THE Adapter SHALL send messages and return Mono<SendMessageResponse>
3. THE Adapter SHALL support sending messages with custom attributes and delay seconds
4. THE Config SHALL create SqsAsyncClient bean with configurable region and endpoint
5. THE Adapter SHALL handle AWS errors and map them to domain exceptions
6. THE Application_Properties_Template SHALL include aws.sqs.region, aws.sqs.queue-url, and aws.sqs.endpoint properties
7. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
8. THE Test SHALL use LocalStack Testcontainer for integration testing
9. THE Metadata_File SHALL include software.amazon.awssdk:sqs dependency version 2.x
10. FOR ALL message send operations, the adapter SHALL return the message ID from AWS

### Requirement 5: SQS Producer Driven Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want an SQS producer adapter, so that I can send messages to AWS SQS queues synchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate an SQS producer adapter using AWS SDK v2 sync client for Spring Imperative
2. THE Adapter SHALL send messages and return SendMessageResponse
3. THE Adapter SHALL support sending messages with custom attributes and delay seconds
4. THE Config SHALL create SqsClient bean with configurable region and endpoint
5. THE Adapter SHALL handle AWS errors and map them to domain exceptions
6. THE Application_Properties_Template SHALL include aws.sqs.region, aws.sqs.queue-url, and aws.sqs.endpoint properties
7. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
8. THE Test SHALL use LocalStack Testcontainer for integration testing
9. THE Metadata_File SHALL include software.amazon.awssdk:sqs dependency version 2.x
10. FOR ALL message send operations, the adapter SHALL return the message ID from AWS

### Requirement 6: DynamoDB Driven Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want a DynamoDB adapter, so that I can perform non-blocking NoSQL database operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate a DynamoDB adapter using AWS SDK v2 async client for Spring Reactive
2. THE Adapter SHALL support put, get, update, delete, query, and scan operations returning Mono or Flux
3. THE Adapter SHALL use DynamoDbAsyncClient with enhanced client for object mapping
4. THE Config SHALL create DynamoDbAsyncClient bean with configurable region and endpoint
5. THE Adapter SHALL map DynamoDB items to domain entities using attribute converters
6. THE Adapter SHALL handle AWS errors and map them to domain exceptions
7. THE Application_Properties_Template SHALL include aws.dynamodb.region, aws.dynamodb.table-name, and aws.dynamodb.endpoint properties
8. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
9. THE Test SHALL use LocalStack Testcontainer with DynamoDB for integration testing
10. THE Metadata_File SHALL include software.amazon.awssdk:dynamodb-enhanced dependency version 2.x
11. FOR ALL CRUD operations, the adapter SHALL correctly serialize domain entities to DynamoDB items and deserialize items to entities

### Requirement 7: DynamoDB Driven Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want a DynamoDB adapter, so that I can perform blocking NoSQL database operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate a DynamoDB adapter using AWS SDK v2 sync client for Spring Imperative
2. THE Adapter SHALL support put, get, update, delete, query, and scan operations returning objects or List
3. THE Adapter SHALL use DynamoDbClient with enhanced client for object mapping
4. THE Config SHALL create DynamoDbClient bean with configurable region and endpoint
5. THE Adapter SHALL map DynamoDB items to domain entities using attribute converters
6. THE Adapter SHALL handle AWS errors and map them to domain exceptions
7. THE Application_Properties_Template SHALL include aws.dynamodb.region, aws.dynamodb.table-name, and aws.dynamodb.endpoint properties
8. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
9. THE Test SHALL use LocalStack Testcontainer with DynamoDB for integration testing
10. THE Metadata_File SHALL include software.amazon.awssdk:dynamodb-enhanced dependency version 2.x
11. FOR ALL CRUD operations, the adapter SHALL correctly serialize domain entities to DynamoDB items and deserialize items to entities

### Requirement 8: Redis Driven Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want a Redis adapter, so that I can perform blocking cache and data structure operations.

#### Acceptance Criteria

1. THE Plugin SHALL generate a Redis adapter using Jedis client for Spring Imperative
2. THE Adapter SHALL support get, set, delete, exists, and expire operations
3. THE Adapter SHALL support hash, list, set, and sorted set data structures
4. THE Config SHALL create JedisPool bean with configurable host, port, password, and connection pool settings
5. THE Adapter SHALL serialize domain objects to JSON for storage
6. THE Adapter SHALL handle Redis connection errors and map them to domain exceptions
7. THE Application_Properties_Template SHALL include redis.host, redis.port, redis.password, and redis.database properties
8. THE Test SHALL use Testcontainers Redis module for integration testing
9. THE Metadata_File SHALL include redis.clients:jedis dependency
10. FOR ALL Redis operations, the adapter SHALL correctly serialize objects to strings and deserialize strings to objects

### Requirement 9: REST Driving Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want a REST adapter, so that I can expose HTTP endpoints using Spring MVC controllers.

#### Acceptance Criteria

1. THE Plugin SHALL generate a REST adapter using @RestController with Spring MVC for Spring Imperative
2. THE Adapter SHALL support GET, POST, PUT, DELETE endpoints with path variables and request bodies
3. THE Adapter SHALL return ResponseEntity<T> or ResponseEntity<List<T>> for responses
4. THE Adapter SHALL use @Valid for request body validation
5. THE Adapter SHALL include exception handler using @ControllerAdvice for mapping domain exceptions to HTTP status codes
6. THE Config SHALL configure Jackson ObjectMapper for JSON serialization
7. THE Application_Properties_Template SHALL include server.port and spring.mvc properties
8. THE Test SHALL use MockMvc for controller testing
9. THE Metadata_File SHALL include spring-boot-starter-web and spring-boot-starter-validation dependencies
10. FOR ALL REST endpoints, the adapter SHALL correctly deserialize request bodies and serialize response bodies

### Requirement 10: GraphQL Driving Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want a GraphQL adapter, so that I can expose a GraphQL API with non-blocking resolvers.

#### Acceptance Criteria

1. THE Plugin SHALL generate a GraphQL adapter using Spring for GraphQL with WebFlux
2. THE Adapter SHALL include @QueryMapping and @MutationMapping annotated methods returning Mono or Flux
3. THE Adapter SHALL generate a GraphQL schema file (schema.graphqls) with types matching domain entities
4. THE Config SHALL configure GraphQL with configurable path and schema location
5. THE Adapter SHALL handle GraphQL errors and map domain exceptions to GraphQL errors
6. THE Adapter SHALL support DataLoader for batching and caching
7. THE Application_Properties_Template SHALL include spring.graphql.path and spring.graphql.schema.printer.enabled properties
8. THE Test SHALL use GraphQlTester for testing queries and mutations
9. THE Metadata_File SHALL include spring-boot-starter-graphql and spring-boot-starter-webflux dependencies
10. FOR ALL GraphQL operations, the adapter SHALL correctly resolve fields and handle nested queries

### Requirement 11: GraphQL Driving Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want a GraphQL adapter, so that I can expose a GraphQL API with blocking resolvers.

#### Acceptance Criteria

1. THE Plugin SHALL generate a GraphQL adapter using Spring for GraphQL with Spring MVC
2. THE Adapter SHALL include @QueryMapping and @MutationMapping annotated methods returning objects or List
3. THE Adapter SHALL generate a GraphQL schema file (schema.graphqls) with types matching domain entities
4. THE Config SHALL configure GraphQL with configurable path and schema location
5. THE Adapter SHALL handle GraphQL errors and map domain exceptions to GraphQL errors
6. THE Adapter SHALL support DataLoader for batching and caching
7. THE Application_Properties_Template SHALL include spring.graphql.path and spring.graphql.schema.printer.enabled properties
8. THE Test SHALL use GraphQlTester for testing queries and mutations
9. THE Metadata_File SHALL include spring-boot-starter-graphql and spring-boot-starter-web dependencies
10. FOR ALL GraphQL operations, the adapter SHALL correctly resolve fields and handle nested queries

### Requirement 12: gRPC Driving Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want a gRPC adapter, so that I can expose high-performance RPC endpoints with reactive streaming.

#### Acceptance Criteria

1. THE Plugin SHALL generate a gRPC adapter using grpc-spring-boot-starter with reactive stubs
2. THE Adapter SHALL generate a Protocol Buffer definition file (.proto) with service and message definitions
3. THE Adapter SHALL implement gRPC service methods returning Mono or Flux
4. THE Config SHALL configure gRPC server with configurable port and security settings
5. THE Adapter SHALL map domain entities to Protocol Buffer messages
6. THE Adapter SHALL handle gRPC errors using StatusException
7. THE Application_Properties_Template SHALL include grpc.server.port and grpc.server.security properties
8. THE Test SHALL use gRPC in-process server for testing
9. THE Metadata_File SHALL include net.devh:grpc-spring-boot-starter and io.grpc:grpc-protobuf dependencies
10. THE Metadata_File SHALL include protobuf-gradle-plugin for generating Java code from .proto files
11. FOR ALL gRPC methods, the adapter SHALL correctly serialize and deserialize Protocol Buffer messages

### Requirement 13: gRPC Driving Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want a gRPC adapter, so that I can expose high-performance RPC endpoints with blocking calls.

#### Acceptance Criteria

1. THE Plugin SHALL generate a gRPC adapter using grpc-spring-boot-starter with blocking stubs
2. THE Adapter SHALL generate a Protocol Buffer definition file (.proto) with service and message definitions
3. THE Adapter SHALL implement gRPC service methods returning objects or Iterator
4. THE Config SHALL configure gRPC server with configurable port and security settings
5. THE Adapter SHALL map domain entities to Protocol Buffer messages
6. THE Adapter SHALL handle gRPC errors using StatusException
7. THE Application_Properties_Template SHALL include grpc.server.port and grpc.server.security properties
8. THE Test SHALL use gRPC in-process server for testing
9. THE Metadata_File SHALL include net.devh:grpc-spring-boot-starter and io.grpc:grpc-protobuf dependencies
10. THE Metadata_File SHALL include protobuf-gradle-plugin for generating Java code from .proto files
11. FOR ALL gRPC methods, the adapter SHALL correctly serialize and deserialize Protocol Buffer messages

### Requirement 14: SQS Consumer Driving Adapter - Spring Reactive

**User Story:** As a developer using Spring Reactive, I want an SQS consumer adapter, so that I can process messages from AWS SQS queues asynchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate an SQS consumer adapter using AWS SDK v2 async client with @SqsListener for Spring Reactive
2. THE Adapter SHALL poll messages from SQS queue and process them reactively
3. THE Adapter SHALL delete messages after successful processing
4. THE Adapter SHALL handle processing errors and implement retry logic with dead letter queue support
5. THE Config SHALL create SqsAsyncClient bean with configurable region, endpoint, and polling settings
6. THE Adapter SHALL deserialize message body to domain objects
7. THE Application_Properties_Template SHALL include aws.sqs.region, aws.sqs.queue-url, aws.sqs.max-messages, and aws.sqs.wait-time properties
8. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
9. THE Test SHALL use LocalStack Testcontainer for integration testing
10. THE Metadata_File SHALL include software.amazon.awssdk:sqs and io.awspring.cloud:spring-cloud-aws-starter-sqs dependencies
11. FOR ALL message processing operations, the adapter SHALL acknowledge successful processing by deleting messages

### Requirement 15: SQS Consumer Driving Adapter - Spring Imperative

**User Story:** As a developer using Spring Imperative, I want an SQS consumer adapter, so that I can process messages from AWS SQS queues synchronously.

#### Acceptance Criteria

1. THE Plugin SHALL generate an SQS consumer adapter using AWS SDK v2 sync client with @SqsListener for Spring Imperative
2. THE Adapter SHALL poll messages from SQS queue and process them synchronously
3. THE Adapter SHALL delete messages after successful processing
4. THE Adapter SHALL handle processing errors and implement retry logic with dead letter queue support
5. THE Config SHALL create SqsClient bean with configurable region, endpoint, and polling settings
6. THE Adapter SHALL deserialize message body to domain objects
7. THE Application_Properties_Template SHALL include aws.sqs.region, aws.sqs.queue-url, aws.sqs.max-messages, and aws.sqs.wait-time properties
8. THE Application_Properties_Template SHALL include a security warning comment about not storing AWS credentials in the file
9. THE Test SHALL use LocalStack Testcontainer for integration testing
10. THE Metadata_File SHALL include software.amazon.awssdk:sqs and io.awspring.cloud:spring-cloud-aws-starter-sqs dependencies
11. FOR ALL message processing operations, the adapter SHALL acknowledge successful processing by deleting messages

### Requirement 16: Adapter Metadata Completeness

**User Story:** As a contributor, I want complete metadata for all adapters, so that the plugin can generate fully functional adapter code with correct dependencies.

#### Acceptance Criteria

1. THE Metadata_File SHALL include name, type (driven or driving), description, and paradigm fields for all adapters
2. THE Metadata_File SHALL include complete dependencies array with groupId, artifactId, and version for each dependency
3. THE Metadata_File SHALL include testDependencies array with dependencies needed only for testing
4. THE Metadata_File SHALL include applicationProperties field referencing the application-properties.yml.ftl file
5. THE Metadata_File SHALL include configurationClasses array listing additional configuration classes to generate
6. THE Metadata_File SHALL include examples section showing typical usage code
7. THE Metadata_File SHALL specify correct dependencies for the paradigm (async vs sync AWS SDK, WebClient vs RestTemplate)
8. THE Plugin SHALL validate metadata.yml completeness before generating adapters
9. IF required metadata fields are missing, THEN THE Plugin SHALL display an error listing missing fields
10. FOR ALL adapters, metadata SHALL be sufficient to generate working code without manual dependency additions

### Requirement 17: Application Properties Template Completeness

**User Story:** As a developer, I want complete application properties templates, so that generated adapters have all necessary configuration with sensible defaults.

#### Acceptance Criteria

1. THE Application_Properties_Template SHALL include all configuration properties required by the adapter
2. THE Application_Properties_Template SHALL use environment variable placeholders for sensitive values (${AWS_ACCESS_KEY_ID})
3. THE Application_Properties_Template SHALL include comments explaining each property's purpose
4. THE Application_Properties_Template SHALL include security warning comments above credential properties
5. THE Application_Properties_Template SHALL provide sensible default values for non-sensitive properties
6. THE Application_Properties_Template SHALL organize properties hierarchically using YAML structure
7. THE Application_Properties_Template SHALL include example values in comments for complex properties
8. FOR ALL adapters with external dependencies, the template SHALL include connection and timeout configuration

### Requirement 18: Adapter Unit Test Templates

**User Story:** As a developer, I want generated unit tests for adapters, so that I can verify adapter functionality and have examples for writing additional tests.

#### Acceptance Criteria

1. THE Test template SHALL generate unit tests using JUnit 5 and Mockito
2. THE Test template SHALL include tests for successful operations and error handling
3. THE Test template SHALL mock external dependencies (HTTP clients, AWS clients, database clients)
4. THE Test template SHALL verify that domain exceptions are thrown for error conditions
5. THE Test template SHALL achieve at least 80% code coverage for the generated adapter
6. THE Test template SHALL use AssertJ for fluent assertions
7. THE Test template SHALL include setup and teardown methods for test fixtures
8. FOR ALL adapter operations, the test template SHALL include at least one test case

### Requirement 19: Adapter Integration Test Support

**User Story:** As a developer, I want integration test support for adapters, so that I can test against real external systems in a controlled environment.

#### Acceptance Criteria

1. WHERE applicable, THE Test template SHALL include integration tests using Testcontainers
2. THE Test template SHALL use LocalStack Testcontainer for AWS service adapters (DynamoDB, SQS)
3. THE Test template SHALL use Redis Testcontainer for Redis adapters
4. THE Test template SHALL use WireMock or MockWebServer for HTTP client adapters
5. THE Test template SHALL include @Testcontainers and @Container annotations
6. THE Test template SHALL configure test containers with required settings (ports, environment variables)
7. THE Metadata_File SHALL include testcontainers dependencies in testDependencies
8. THE Test template SHALL clean up test data after each test
9. FOR ALL integration tests, containers SHALL start before tests and stop after tests complete

### Requirement 20: Template Generation Property-Based Tests

**User Story:** As a contributor, I want property-based tests for template generation, so that I can verify templates work correctly across many input combinations.

#### Acceptance Criteria

1. THE Plugin SHALL include property-based tests using Kotest or JUnit QuickCheck
2. THE Tests SHALL generate random valid adapter configurations and verify template processing succeeds
3. THE Tests SHALL verify generated code compiles successfully
4. THE Tests SHALL verify generated code follows Java naming conventions
5. THE Tests SHALL verify generated imports reference existing classes
6. THE Tests SHALL verify generated configuration files are valid YAML
7. THE Tests SHALL test both reactive and imperative paradigms
8. THE Tests SHALL test all adapter types (HTTP client, DynamoDB, SQS, GraphQL, gRPC, Redis)
9. FOR ALL valid adapter configurations, template generation SHALL produce compilable code (invariant property)
10. FOR ALL generated application properties, parsing the YAML SHALL succeed without errors (invariant property)

### Requirement 21: GraphQL Schema Parser and Printer

**User Story:** As a developer, I want reliable GraphQL schema generation, so that my schema files are syntactically correct and match my domain model.

#### Acceptance Criteria

1. THE Plugin SHALL generate GraphQL schema files (.graphqls) from domain entity metadata
2. THE Plugin SHALL map Java types to GraphQL types (String, Int, Float, Boolean, ID)
3. THE Plugin SHALL generate Query and Mutation types with operations
4. THE Plugin SHALL support GraphQL lists, non-null types, and custom scalar types
5. THE Plugin SHALL validate generated schema syntax before writing files
6. IF schema generation fails, THEN THE Plugin SHALL display an error with the invalid type or field
7. THE Plugin SHALL include comments in schema describing types and fields
8. FOR ALL valid domain entities, generating then parsing the GraphQL schema SHALL produce an equivalent schema (round-trip property)

### Requirement 22: Protocol Buffer Definition Generator

**User Story:** As a developer, I want Protocol Buffer definitions generated from domain entities, so that my gRPC services have type-safe message definitions.

#### Acceptance Criteria

1. THE Plugin SHALL generate .proto files from domain entity metadata for gRPC adapters
2. THE Plugin SHALL map Java types to Protocol Buffer types (string, int32, int64, bool, double)
3. THE Plugin SHALL generate service definitions with rpc methods
4. THE Plugin SHALL support repeated fields for collections and nested message types
5. THE Plugin SHALL use proto3 syntax
6. THE Plugin SHALL validate generated .proto syntax before writing files
7. IF .proto generation fails, THEN THE Plugin SHALL display an error with the invalid field or type
8. THE Plugin SHALL include comments in .proto files describing messages and fields
9. FOR ALL valid domain entities, the generated .proto file SHALL compile successfully with protoc

### Requirement 23: Paradigm-Specific Dependency Management

**User Story:** As a developer, I want correct dependencies for my chosen paradigm, so that my project builds without dependency conflicts.

#### Acceptance Criteria

1. WHEN generating Spring Reactive adapters, THE Plugin SHALL include reactive dependencies (spring-boot-starter-webflux, r2dbc, lettuce, AWS SDK async)
2. WHEN generating Spring Imperative adapters, THE Plugin SHALL include imperative dependencies (spring-boot-starter-web, spring-boot-starter-data-jpa, jedis, AWS SDK sync)
3. THE Plugin SHALL NOT mix reactive and imperative dependencies in the same project
4. THE Plugin SHALL validate paradigm consistency across all adapters in a project
5. IF mixing paradigms is detected, THEN THE Plugin SHALL display a warning about potential conflicts
6. THE Plugin SHALL use Spring Boot dependency management for version resolution
7. THE Plugin SHALL specify explicit versions only for dependencies not managed by Spring Boot
8. FOR ALL adapter generation operations, dependencies SHALL be compatible with the selected paradigm

### Requirement 24: Adapter Documentation Pages

**User Story:** As a developer, I want documentation for each adapter, so that I understand how to use and configure adapters in my projects.

#### Acceptance Criteria

1. THE Documentation_Site SHALL include a page for each adapter in docs/adapters/
2. THE Adapter page SHALL include adapter description, use cases, and when to use it
3. THE Adapter page SHALL include configuration properties with descriptions and examples
4. THE Adapter page SHALL include code examples showing typical usage
5. THE Adapter page SHALL document differences between reactive and imperative versions
6. THE Adapter page SHALL include troubleshooting section for common issues
7. THE Adapter page SHALL link to relevant external documentation (AWS SDK, Spring, gRPC)
8. THE docs/adapters/index.md page SHALL list all available adapters organized by type (driven, driving) and paradigm
9. THE Documentation SHALL be written in Spanish for user-facing content
10. FOR ALL adapters, documentation SHALL be complete before the adapter is considered production-ready

### Requirement 25: Reactive to Imperative Migration Guide

**User Story:** As a developer, I want a migration guide from reactive to imperative, so that I can convert existing reactive projects to imperative when needed.

#### Acceptance Criteria

1. THE Documentation_Site SHALL include docs/guides/reactive-to-imperative-migration.md
2. THE Guide SHALL explain when to migrate from reactive to imperative
3. THE Guide SHALL provide step-by-step instructions for migrating each adapter type
4. THE Guide SHALL include code examples showing reactive code and equivalent imperative code
5. THE Guide SHALL document type conversions (Mono<T> to T, Flux<T> to List<T>)
6. THE Guide SHALL explain dependency changes required for migration
7. THE Guide SHALL document testing changes (WebTestClient to MockMvc)
8. THE Guide SHALL include a checklist for verifying migration completeness
9. THE Guide SHALL be written in Spanish

### Requirement 26: Adapter Version Compatibility Matrix

**User Story:** As a developer, I want a version compatibility matrix, so that I know which adapter versions work with which Spring Boot versions.

#### Acceptance Criteria

1. THE Documentation_Site SHALL include docs/reference/version-compatibility.md
2. THE Matrix SHALL list Spring Boot versions and compatible adapter dependency versions
3. THE Matrix SHALL include AWS SDK v2, gRPC, GraphQL, and Redis client versions
4. THE Matrix SHALL indicate tested combinations with checkmarks
5. THE Matrix SHALL include notes about known incompatibilities
6. THE Matrix SHALL be updated when new Spring Boot versions are released
7. THE Plugin SHALL validate generated dependency versions against the compatibility matrix
8. IF incompatible versions are detected, THEN THE Plugin SHALL display a warning with recommended versions

### Requirement 27: Adapter Template Validation

**User Story:** As a contributor, I want validation for adapter templates, so that I catch errors before templates are used in production.

#### Acceptance Criteria

1. THE Plugin SHALL validate that all required template files exist (Adapter.java.ftl, Config.java.ftl, Test.java.ftl)
2. THE Plugin SHALL validate that metadata.yml references existing template files
3. THE Plugin SHALL validate that application-properties.yml.ftl contains valid YAML structure
4. THE Plugin SHALL validate that template variables used in FreeMarker templates are defined
5. THE Plugin SHALL validate that generated Java code follows naming conventions
6. THE Plugin SHALL provide a validation command for contributors to test templates locally
7. WHEN validation succeeds, THE Plugin SHALL display a success message listing validated adapters
8. IF validation fails, THEN THE Plugin SHALL display all errors with file paths and line numbers

### Requirement 28: Adapter Code Generation Consistency

**User Story:** As a developer, I want consistent code generation across adapters, so that all adapters follow the same patterns and conventions.

#### Acceptance Criteria

1. THE Plugin SHALL use consistent package naming for all adapters (infrastructure.adapter.driven.*, infrastructure.adapter.driving.*)
2. THE Plugin SHALL use consistent class naming suffixes (Adapter, Config, Test)
3. THE Plugin SHALL use consistent exception handling patterns across all adapters
4. THE Plugin SHALL use consistent logging patterns with SLF4J
5. THE Plugin SHALL use consistent configuration property prefixes (adapter.http, adapter.dynamodb, adapter.sqs)
6. THE Plugin SHALL generate consistent JavaDoc comments for all public methods
7. THE Plugin SHALL use consistent code formatting (indentation, line length, import organization)
8. FOR ALL adapters, the generated code SHALL pass checkstyle and ktlint validation

### Requirement 29: Adapter Error Handling Patterns

**User Story:** As a developer, I want consistent error handling in adapters, so that I can handle failures predictably across all adapter types.

#### Acceptance Criteria

1. THE Plugin SHALL generate adapters that catch external system exceptions and map them to domain exceptions
2. THE Plugin SHALL include a base AdapterException class for all adapter errors
3. THE Plugin SHALL generate specific exception classes for each adapter type (HttpClientException, DynamoDbException, SqsException)
4. THE Plugin SHALL include error context in exceptions (request details, error codes, timestamps)
5. THE Plugin SHALL log errors with appropriate levels (ERROR for failures, WARN for retries, DEBUG for details)
6. THE Plugin SHALL implement retry logic with exponential backoff for transient errors
7. THE Plugin SHALL include circuit breaker pattern for adapters calling external services
8. THE Metadata_File SHALL include resilience4j dependencies for retry and circuit breaker support
9. FOR ALL adapter operations, exceptions SHALL include sufficient context for debugging

### Requirement 30: Adapter Performance Configuration

**User Story:** As a developer, I want configurable performance settings for adapters, so that I can tune adapters for my application's requirements.

#### Acceptance Criteria

1. THE Application_Properties_Template SHALL include timeout configuration for all adapters
2. THE Application_Properties_Template SHALL include connection pool settings for HTTP and database adapters
3. THE Application_Properties_Template SHALL include batch size configuration for SQS and DynamoDB adapters
4. THE Application_Properties_Template SHALL include thread pool configuration for imperative adapters
5. THE Application_Properties_Template SHALL include backpressure configuration for reactive adapters
6. THE Config SHALL apply performance settings from application properties
7. THE Config SHALL provide sensible defaults for all performance settings
8. THE Documentation SHALL explain performance implications of each setting
9. FOR ALL adapters with external dependencies, performance settings SHALL be configurable without code changes

## Non-Functional Requirements

### Performance

1. THE Plugin SHALL generate adapter code in less than 5 seconds per adapter
2. THE Generated adapters SHALL have minimal performance overhead compared to hand-written code
3. THE Reactive adapters SHALL support backpressure to prevent memory overflow
4. THE Imperative adapters SHALL use connection pooling to minimize connection overhead

### Usability

1. THE Plugin SHALL provide clear error messages when adapter generation fails
2. THE Plugin SHALL display progress indicators when generating multiple adapters
3. THE Documentation SHALL include quick-start examples for each adapter
4. THE Plugin SHALL support both Spanish and English error messages

### Maintainability

1. THE Adapter templates SHALL be organized by paradigm (reactive/, imperative/) for easy maintenance
2. THE Plugin SHALL separate paradigm-specific logic from common adapter logic
3. THE Plugin SHALL include comprehensive unit tests for all adapter generation logic
4. THE Plugin SHALL achieve at least 80% code coverage for adapter generation code

### Extensibility

1. THE Plugin SHALL support adding new adapter types without modifying core generation logic
2. THE Plugin SHALL support adding new paradigms beyond reactive and imperative
3. THE Adapter template system SHALL support custom template variables for future extensions
4. THE Metadata format SHALL support additional fields for future adapter capabilities

### Reliability

1. THE Plugin SHALL validate all adapter configurations before generating code
2. THE Plugin SHALL rollback changes if adapter generation fails
3. THE Generated adapters SHALL handle connection failures gracefully
4. THE Generated adapters SHALL include health check endpoints for monitoring

### Security

1. THE Application_Properties_Template SHALL NOT include default credentials
2. THE Application_Properties_Template SHALL use environment variable placeholders for sensitive values
3. THE Application_Properties_Template SHALL include security warning comments
4. THE Generated adapters SHALL support TLS/SSL configuration for external connections
5. THE Documentation SHALL include security best practices for each adapter type

### Testability

1. THE Generated adapters SHALL be testable with mocked dependencies
2. THE Test templates SHALL provide examples of unit and integration testing
3. THE Generated adapters SHALL support test containers for integration testing
4. THE Plugin SHALL include property-based tests verifying template correctness across many inputs
