# Design: Spring Imperative Support

## Overview

This design adds complete Spring Imperative (MVC) paradigm support to the Clean Architecture Gradle Plugin by creating the `spring/imperative/` template structure and implementing 14 missing adapters (7 adapter types × 2 paradigms).

**CRITICAL:** Before implementing new features, this design addresses technical debt in the domain layer through comprehensive refactoring.

**Key Insight:** The plugin's core infrastructure already exists. This is NOT a greenfield project. We're adding templates and adapters to an existing, working system.

**What Already Exists:**
- ✅ Core generators: `ProjectGenerator`, `AdapterGenerator`, `EntityGenerator`, `UseCaseGenerator`, `InputAdapterGenerator`
- ✅ Template system: `FreemarkerTemplateRepository` with local/remote/embedded modes
- ✅ Validators: `ProjectValidator`, `EntityValidator`, `InputAdapterValidator`, `TemplateValidator`
- ✅ Domain models: `ProjectConfig`, `AdapterConfig`, `EntityConfig`, `Paradigm` enum
- ✅ Path resolution: Dynamic template paths using `frameworks/{framework}/{paradigm}/...`
- ✅ Reactive templates: Complete structure at `templates/frameworks/spring/reactive/`

**What This Design Adds:**
1. **PHASE 0 (Refactoring):** Clean domain layer - Lombok, package reorganization, extract nested classes
2. **PHASE 1:** Template structure at `templates/frameworks/spring/imperative/`
3. **PHASE 2:** 14 new adapter templates (7 types × 2 paradigms)
4. **PHASE 3:** Metadata files (metadata.yml, index.json) for all adapters
5. **PHASE 4:** Configuration templates (application-properties.yml.ftl)
6. **PHASE 5:** Test templates for all adapters

**What This Design Does NOT Do:**
- ❌ Modify core plugin code (generators, validators work as-is)
- ❌ Change domain models (Paradigm enum already exists)
- ❌ Redesign template system (FreemarkerTemplateRepository works as-is)
- ❌ Add property-based testing framework
- ❌ Create complex schema generators (GraphQL/Protobuf are simple templates)

## PHASE 0: Domain Layer Refactoring Design

### Current Problems

**Problem 1: Flat Package Structure**
```
domain/model/
├── AdapterConfig.java (18 files all in one package)
├── AdapterMetadata.java
├── ArchitectureType.java
├── ... (15 more files)
```

**Problem 2: Nested Static Classes**
```java
public record AdapterConfig(...) {
  public enum AdapterType { ... }        // Should be separate file
  public record AdapterMethod(...) { ... } // Should be separate file
  public static class Builder { ... }     // Should use Lombok @Builder
}
```

**Problem 3: Manual Boilerplate**
```java
// Manual builder (100+ lines)
public static class Builder {
  private String name;
  public Builder name(String name) { this.name = name; return this; }
  public AdapterConfig build() { return new AdapterConfig(...); }
}

// Manual getters/setters
public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

### Refactored Structure

**New Package Organization:**
```
domain/
├── model/
│   ├── adapter/
│   │   ├── AdapterConfig.java (@Builder, @Getter)
│   │   ├── AdapterMetadata.java (@Data)
│   │   ├── AdapterType.java (enum)
│   │   ├── AdapterMethod.java (record)
│   │   ├── MethodParameter.java (record)
│   │   ├── InputAdapterConfig.java (@Builder, @Getter)
│   │   ├── InputAdapterType.java (enum)
│   │   ├── HttpMethod.java (enum)
│   │   ├── ParameterType.java (enum)
│   │   ├── Endpoint.java (record)
│   │   └── EndpointParameter.java (record)
│   ├── config/
│   │   ├── ProjectConfig.java (@Builder, @Getter)
│   │   ├── TemplateConfig.java (@Data)
│   │   ├── TemplateMode.java (enum)
│   │   └── TemplateSource.java (enum)
│   ├── entity/
│   │   ├── EntityConfig.java (@Builder, @Getter)
│   │   └── EntityField.java (record)
│   ├── usecase/
│   │   └── UseCaseConfig.java (@Builder, @Getter)
│   ├── project/
│   │   ├── ArchitectureType.java (enum)
│   │   ├── Framework.java (enum)
│   │   └── Paradigm.java (enum)
│   ├── file/
│   │   ├── GeneratedFile.java (@Builder, @Getter)
│   │   └── FileType.java (enum)
│   ├── validation/
│   │   └── ValidationResult.java (@Builder, @Getter)
│   └── structure/
│       ├── StructureMetadata.java (@Data)
│       ├── LayerDependencies.java (@Data)
│       ├── NamingConventions.java (@Data)
│       └── MergeResult.java (@Builder, @Getter)
├── port/
│   ├── in/ (unchanged)
│   └── out/ (unchanged)
└── service/ (unchanged)
```

### Lombok Integration

**build.gradle.kts changes:**
```kotlin
dependencies {
    // Add Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**Before (Manual Builder):**
```java
public record AdapterConfig(
    String name,
    String packageName,
    AdapterType type,
    String entityName,
    List<AdapterMethod> methods) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String packageName;
    private AdapterType type;
    private String entityName;
    private List<AdapterMethod> methods;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    // ... 50 more lines ...

    public AdapterConfig build() {
      return new AdapterConfig(name, packageName, type, entityName, methods);
    }
  }
}
```

**After (Lombok @Builder):**
```java
package com.pragma.archetype.domain.model.adapter;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Builder
@Getter
public record AdapterConfig(
    String name,
    String packageName,
    AdapterType type,
    String entityName,
    List<AdapterMethod> methods) {
}
```

**Lombok Annotations Strategy:**
- `@Builder`: For classes with builder pattern (AdapterConfig, ProjectConfig, etc.)
- `@Getter`: For immutable classes (records, classes with final fields)
- `@Data`: For mutable POJOs (AdapterMetadata, TemplateConfig)
- `@Value`: For immutable POJOs (alternative to record)

### Migration Impact Analysis

**Files to Modify:**
- Domain models: 18 files
- Application layer: ~10 files (update imports)
- Infrastructure layer: ~15 files (update imports)
- Tests: ~30 files (update imports)

**Total: ~73 files**

**Risk Mitigation:**
1. Use IDE refactoring tools (IntelliJ "Move Class", "Rename")
2. Run tests after each major change
3. Use git commits for each refactoring step
4. Validate build after each phase

## PHASE 1-5: Spring Imperative Implementation

(Previous design content continues here...)

The plugin follows hexagonal architecture:

```
Domain Layer (Existing)
├── Model: ProjectConfig, AdapterConfig, Paradigm, Framework, ArchitectureType
├── Ports: TemplateRepository, FileSystemPort, ConfigurationPort
└── Services: ProjectValidator, EntityValidator, TemplateValidator

Application Layer (Existing)
├── ProjectGenerator: Generates project structure
├── AdapterGenerator: Generates adapters from templates
├── EntityGenerator: Generates domain entities
├── UseCaseGenerator: Generates use cases
└── InputAdapterGenerator: Generates entry points

Infrastructure Layer (Existing)
├── In: Gradle tasks (InitCleanArchTask, GenerateOutputAdapterTask, etc.)
├── Out:
    ├── FreemarkerTemplateRepository: Processes .ftl templates
    ├── LocalFileSystemAdapter: Reads/writes files
    └── YamlConfigurationAdapter: Reads .cleanarch.yml
```

### Template Path Resolution (Existing)

The plugin already resolves template paths dynamically:

```java
// In AdapterGenerator (existing code)
String framework = projectConfig.framework().name().toLowerCase();  // "spring"
String paradigm = projectConfig.paradigm().name().toLowerCase();    // "reactive" or "imperative"

String templatePath = String.format(
    "frameworks/%s/%s/adapters/%s/%s/Adapter.java.ftl",
    framework,    // spring
    paradigm,     // reactive or imperative
    category,     // driven-adapters or entry-points
    adapterType   // mongodb, redis, rest, etc.
);
```

**This means:** Once we create `spring/imperative/` templates, the existing code will automatically find and use them. No code changes needed.

### Template Structure Design

```
templates/frameworks/spring/
├── reactive/                           # Existing
│   ├── metadata.yml
│   ├── domain/
│   │   ├── Entity.java.ftl            # ← Paradigm-agnostic (POJOs)
│   │   └── metadata.yml
│   ├── usecase/
│   │   ├── UseCase.java.ftl           # ← Uses Mono/Flux
│   │   ├── InputPort.java.ftl         # ← Uses Mono/Flux
│   │   ├── Test.java.ftl
│   │   └── metadata.yml
│   ├── project/
│   │   ├── Application.java.ftl       # ← Paradigm-agnostic
│   │   └── application.yml.ftl        # ← WebFlux config
│   └── adapters/
│       ├── driven-adapters/
│       │   ├── index.json
│       │   ├── mongodb/               # Existing (Reactive Mongo)
│       │   ├── postgresql/            # Existing (R2DBC)
│       │   ├── redis/                 # Existing (Lettuce)
│       │   ├── http-client/           # NEW (WebClient)
│       │   ├── dynamodb/              # NEW (DynamoDbAsyncClient)
│       │   └── sqs-producer/          # NEW (SqsAsyncClient)
│       └── entry-points/
│           ├── index.json
│           ├── rest/                  # Existing (WebFlux)
│           ├── graphql/               # NEW (Reactive resolvers)
│           ├── grpc/                  # NEW (ReactorStub)
│           └── sqs-consumer/          # NEW (Async listener)
│
└── imperative/                         # NEW - This entire structure
    ├── metadata.yml                    # NEW
    ├── domain/
    │   ├── Entity.java.ftl            # SYMLINK → ../reactive/domain/Entity.java.ftl
    │   └── metadata.yml               # SYMLINK → ../reactive/domain/metadata.yml
    ├── usecase/
    │   ├── UseCase.java.ftl           # NEW (uses POJO/List)
    │   ├── InputPort.java.ftl         # NEW (uses POJO/List)
    │   ├── Test.java.ftl              # NEW (no reactor-test)
    │   └── metadata.yml               # NEW
    ├── project/
    │   ├── Application.java.ftl       # SYMLINK → ../reactive/project/Application.java.ftl
    │   └── application.yml.ftl        # NEW (MVC config)
    └── adapters/
        ├── driven-adapters/
        │   ├── index.json             # NEW
        │   ├── mongodb/               # NEW (JPA)
        │   ├── postgresql/            # NEW (JPA)
        │   ├── redis/                 # NEW (Jedis)
        │   ├── http-client/           # NEW (RestTemplate)
        │   ├── dynamodb/              # NEW (DynamoDbClient sync)
        │   └── sqs-producer/          # NEW (SqsClient sync)
        └── entry-points/
            ├── index.json             # NEW
            ├── rest/                  # NEW (Spring MVC)
            ├── graphql/               # NEW (Blocking resolvers)
            ├── grpc/                  # NEW (BlockingStub)
            └── sqs-consumer/          # NEW (Sync listener)
```

### Reuse Strategy

**Symlinks for Paradigm-Agnostic Templates:**
- `imperative/domain/Entity.java.ftl` → `reactive/domain/Entity.java.ftl`
- `imperative/domain/metadata.yml` → `reactive/domain/metadata.yml`
- `imperative/project/Application.java.ftl` → `reactive/project/Application.java.ftl`

**New Templates for Paradigm-Specific Code:**
- UseCase templates (Mono/Flux vs POJO/List)
- Adapter templates (different clients/libraries)
- Configuration templates (WebFlux vs MVC)

## Component Design

### 1. Imperative UseCase Templates

**UseCase.java.ftl (Imperative):**
```java
package ${implPackage};

import java.util.List;

/**
 * Use case implementation for ${useCaseName}.
 * Contains the business logic for this use case.
 */
public class ${useCaseName}UseCaseImpl {

  public ${useCaseName}UseCaseImpl() {
    // TODO: Initialize with dependencies
  }

  /**
   * Executes the main use case logic.
   */
  public Object execute(Object input) {
    // TODO: Implement business logic
    return input;
  }

  /**
   * Finds an entity by ID.
   */
  public Object findById(String id) {
    // TODO: Implement find by id logic
    return null;
  }

  /**
   * Finds all entities.
   */
  public List<Object> findAll() {
    // TODO: Implement find all logic
    return List.of();
  }

  /**
   * Updates an entity.
   */
  public Object update(String id, Object input) {
    // TODO: Implement update logic
    return input;
  }

  /**
   * Deletes an entity by ID.
   */
  public void delete(String id) {
    // TODO: Implement delete logic
  }
}
```

**Key Differences from Reactive:**
- No `import reactor.core.publisher.*`
- Return types: `Object` instead of `Mono<Object>`
- Return types: `List<Object>` instead of `Flux<Object>`
- Return types: `void` instead of `Mono<Void>`

### 2. Imperative Application Configuration

**application.yml.ftl (Imperative):**
```yaml
spring:
  application:
    name: ${projectName}
  
  # Spring MVC Configuration
  mvc:
    servlet:
      path: /api

server:
  port: 8080
  tomcat:
    threads:
      max: 200
      min-spare: 10

# Logging
logging:
  level:
    root: INFO
    ${basePackage}: DEBUG
    org.springframework.web: DEBUG

# Actuator (optional)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**Key Differences from Reactive:**
- `spring.mvc` instead of `spring.webflux`
- `server.tomcat` instead of `reactor.netty`
- No reactive-specific logging

### 3. REST Entry Point - Imperative

**Adapter.java.ftl:**
```java
package ${packageName};

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for ${controllerName}.
 * Exposes HTTP endpoints using Spring MVC.
 */
@RestController
@RequestMapping("${basePath}")
public class ${controllerName}Controller {

  private final ${useCaseName}UseCase useCase;

  public ${controllerName}Controller(${useCaseName}UseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping("/{id}")
  public ResponseEntity<${entityName}> findById(@PathVariable String id) {
    ${entityName} entity = useCase.findById(id);
    return entity != null 
        ? ResponseEntity.ok(entity)
        : ResponseEntity.notFound().build();
  }

  @GetMapping
  public ResponseEntity<List<${entityName}>> findAll() {
    List<${entityName}> entities = useCase.findAll();
    return ResponseEntity.ok(entities);
  }

  @PostMapping
  public ResponseEntity<${entityName}> create(@Valid @RequestBody ${entityName} entity) {
    ${entityName} created = useCase.execute(entity);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<${entityName}> update(
      @PathVariable String id,
      @Valid @RequestBody ${entityName} entity) {
    ${entityName} updated = useCase.update(id, entity);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    useCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

**metadata.yml:**
```yaml
name: rest
type: entry-point
description: REST API controller using Spring MVC

dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-web
    version: null  # Managed by Spring Boot
  - group: org.springframework.boot
    artifact: spring-boot-starter-validation
    version: null

testDependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-test
    version: null

applicationPropertiesTemplate: application-properties.yml.ftl
```

### 4. MongoDB Driven Adapter - Imperative

**Adapter.java.ftl:**
```java
package ${packageName};

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB adapter using Spring Data JPA.
 * Implements repository pattern for ${entityName}.
 */
@Component
public class ${adapterName}Adapter {

  private final ${entityName}JpaRepository repository;
  private final ${entityName}Mapper mapper;

  public ${adapterName}Adapter(
      ${entityName}JpaRepository repository,
      ${entityName}Mapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  public ${entityName} save(${entityName} entity) {
    ${entityName}Entity dataEntity = mapper.toData(entity);
    ${entityName}Entity saved = repository.save(dataEntity);
    return mapper.toDomain(saved);
  }

  public ${entityName} findById(String id) {
    Optional<${entityName}Entity> found = repository.findById(id);
    return found.map(mapper::toDomain).orElse(null);
  }

  public List<${entityName}> findAll() {
    List<${entityName}Entity> entities = repository.findAll();
    return entities.stream()
        .map(mapper::toDomain)
        .toList();
  }

  public ${entityName} update(String id, ${entityName} entity) {
    if (!repository.existsById(id)) {
      return null;
    }
    ${entityName}Entity dataEntity = mapper.toData(entity);
    dataEntity.setId(id);
    ${entityName}Entity updated = repository.save(dataEntity);
    return mapper.toDomain(updated);
  }

  public void delete(String id) {
    repository.deleteById(id);
  }
}
```

**Repository.java.ftl:**
```java
package ${packageName};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for ${entityName}.
 */
@Repository
public interface ${entityName}JpaRepository 
    extends JpaRepository<${entityName}Entity, String> {
}
```

**metadata.yml:**
```yaml
name: mongodb
type: driven
description: MongoDB database adapter using Spring Data JPA

dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-data-jpa
    version: null
  - group: org.mongodb
    artifact: mongodb-driver-sync
    version: 4.11.0

testDependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-test
    version: null
  - group: de.flapdoodle.embed
    artifact: de.flapdoodle.embed.mongo
    version: 4.11.0

applicationPropertiesTemplate: application-properties.yml.ftl

configurationClasses:
  - name: MongoConfig
    packagePath: config
    templatePath: MongoConfig.java.ftl
```

### 5. HTTP Client Driven Adapter - Imperative

**Adapter.java.ftl:**
```java
package ${packageName};

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.List;
import java.util.Arrays;

/**
 * HTTP client adapter using RestTemplate.
 * Makes blocking HTTP requests to external APIs.
 */
@Component
public class ${adapterName}HttpClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public ${adapterName}HttpClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.baseUrl = ""; // TODO: Inject from configuration
  }

  public <T> T get(String path, Class<T> responseType) {
    try {
      return restTemplate.getForObject(baseUrl + path, responseType);
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("HTTP GET failed: " + e.getMessage(), e);
    }
  }

  public <T> List<T> getList(String path, Class<T[]> responseType) {
    try {
      T[] array = restTemplate.getForObject(baseUrl + path, responseType);
      return array != null ? Arrays.asList(array) : List.of();
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("HTTP GET failed: " + e.getMessage(), e);
    }
  }

  public <T, R> R post(String path, T request, Class<R> responseType) {
    try {
      return restTemplate.postForObject(baseUrl + path, request, responseType);
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("HTTP POST failed: " + e.getMessage(), e);
    }
  }

  public <T> void put(String path, T request) {
    try {
      restTemplate.put(baseUrl + path, request);
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("HTTP PUT failed: " + e.getMessage(), e);
    }
  }

  public void delete(String path) {
    try {
      restTemplate.delete(baseUrl + path);
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("HTTP DELETE failed: " + e.getMessage(), e);
    }
  }
}
```

**Config.java.ftl:**
```java
package ${packageName}.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * Configuration for RestTemplate HTTP client.
 */
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
  }
}
```

**metadata.yml:**
```yaml
name: http-client
type: driven
description: HTTP client adapter using RestTemplate

dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-web
    version: null

testDependencies:
  - group: com.github.tomakehurst
    artifact: wiremock-jre8
    version: 2.35.0

applicationPropertiesTemplate: application-properties.yml.ftl

configurationClasses:
  - name: RestTemplateConfig
    packagePath: config
    templatePath: Config.java.ftl
```

### 6. DynamoDB Driven Adapter - Imperative

**Adapter.java.ftl:**
```java
package ${packageName};

import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DynamoDB adapter using AWS SDK v2 sync client.
 * Implements repository pattern for ${entityName}.
 */
@Component
public class ${adapterName}DynamoDbAdapter {

  private final DynamoDbTable<${entityName}DynamoEntity> table;
  private final ${entityName}Mapper mapper;

  public ${adapterName}DynamoDbAdapter(
      DynamoDbEnhancedClient enhancedClient,
      ${entityName}Mapper mapper) {
    this.table = enhancedClient.table(
        "${tableName}",
        TableSchema.fromBean(${entityName}DynamoEntity.class)
    );
    this.mapper = mapper;
  }

  public ${entityName} save(${entityName} entity) {
    ${entityName}DynamoEntity dynamoEntity = mapper.toDynamo(entity);
    table.putItem(dynamoEntity);
    return entity;
  }

  public ${entityName} findById(String id) {
    Key key = Key.builder().partitionValue(id).build();
    ${entityName}DynamoEntity found = table.getItem(key);
    return found != null ? mapper.toDomain(found) : null;
  }

  public List<${entityName}> findAll() {
    return table.scan().items().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  public void delete(String id) {
    Key key = Key.builder().partitionValue(id).build();
    table.deleteItem(key);
  }
}
```

**metadata.yml:**
```yaml
name: dynamodb
type: driven
description: DynamoDB adapter using AWS SDK v2 sync client

dependencies:
  - group: software.amazon.awssdk
    artifact: dynamodb-enhanced
    version: 2.20.0
  - group: software.amazon.awssdk
    artifact: apache-client
    version: 2.20.0

testDependencies:
  - group: org.testcontainers
    artifact: localstack
    version: 1.19.0
  - group: org.testcontainers
    artifact: junit-jupiter
    version: 1.19.0

applicationPropertiesTemplate: application-properties.yml.ftl

configurationClasses:
  - name: DynamoDbConfig
    packagePath: config
    templatePath: Config.java.ftl
```

### 7. Index Files

**driven-adapters/index.json:**
```json
{
  "version": "1.0.0",
  "lastUpdated": "2026-02-25T00:00:00Z",
  "adapters": [
    {
      "name": "mongodb",
      "displayName": "MongoDB Database",
      "description": "MongoDB adapter using Spring Data JPA",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "postgresql",
      "displayName": "PostgreSQL Database",
      "description": "PostgreSQL adapter using Spring Data JPA",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "redis",
      "displayName": "Redis Cache",
      "description": "Redis adapter using Jedis client",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "http-client",
      "displayName": "HTTP Client",
      "description": "HTTP client using RestTemplate",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "dynamodb",
      "displayName": "DynamoDB",
      "description": "DynamoDB adapter using AWS SDK v2 sync",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "sqs-producer",
      "displayName": "SQS Producer",
      "description": "SQS message producer using AWS SDK v2 sync",
      "type": "driven-adapter",
      "status": "stable",
      "version": "1.0.0"
    }
  ]
}
```

**entry-points/index.json:**
```json
{
  "version": "1.0.0",
  "lastUpdated": "2026-02-25T00:00:00Z",
  "adapters": [
    {
      "name": "rest",
      "displayName": "REST API",
      "description": "REST controller using Spring MVC",
      "type": "entry-point",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "graphql",
      "displayName": "GraphQL API",
      "description": "GraphQL API using Spring for GraphQL",
      "type": "entry-point",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "grpc",
      "displayName": "gRPC Service",
      "description": "gRPC service using grpc-spring-boot-starter",
      "type": "entry-point",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "sqs-consumer",
      "displayName": "SQS Consumer",
      "description": "SQS message consumer using Spring Cloud AWS",
      "type": "entry-point",
      "status": "stable",
      "version": "1.0.0"
    }
  ]
}
```

## Implementation Strategy

### Phase 0: Domain Layer Refactoring (Priority 0 - MUST DO FIRST)

**Goal:** Clean up technical debt before adding new features

**Step 1: Add Lombok Dependency**
1. Add Lombok to build.gradle.kts (compileOnly + annotationProcessor)
2. Configure IDE annotation processing
3. Run `./gradlew build` to verify
4. Run all tests to verify: `./gradlew test`
5. Verify coverage >85%: `./gradlew jacocoTestReport`

**Step 2: Reorganize Packages**
1. Create new package structure (adapter/, config/, entity/, usecase/, project/, file/, validation/, structure/)
2. Move classes to appropriate packages using IDE refactoring
3. Update all imports across codebase
4. Run `./gradlew build` to verify
5. Run all tests: `./gradlew test`
6. Verify coverage >85%

**Step 3: Extract Nested Classes**
1. Extract all nested enums to separate files
2. Extract all nested records to separate files
3. Extract all nested static classes to separate files
4. Update imports
5. Run `./gradlew build` to verify
6. Run all tests: `./gradlew test`
7. Verify coverage >85%

**Step 4: Replace Manual Builders**
1. Add @Builder to AdapterConfig, delete Builder class
2. Add @Builder to InputAdapterConfig, delete Builder class
3. Add @Builder to EntityConfig, delete Builder class
4. Add @Builder to ProjectConfig, delete Builder class
5. Add @Builder to UseCaseConfig, delete Builder class
6. Update all usages to use Lombok builders
7. Run `./gradlew build` to verify
8. Run all tests: `./gradlew test`
9. Verify coverage >85%

**Step 5: Replace Simple Getters/Setters**
1. Analyze all domain model classes for simple getters
2. Add @Getter annotation, delete simple getter methods
3. Keep complex getters (with logic) as methods
4. Analyze all domain model classes for simple setters
5. Add @Setter annotation, delete simple setter methods
6. Keep complex setters (with validation) as methods
7. Run `./gradlew build` to verify
8. Run all tests: `./gradlew test`
9. Verify coverage >85%

**Step 6: Apply @Data Where Appropriate**
1. Identify mutable POJOs (classes with setters)
2. Replace @Getter + @Setter with @Data
3. Run `./gradlew build` to verify
4. Run all tests: `./gradlew test`
5. Verify coverage >85%

**Deliverable:** Clean, maintainable domain layer with Lombok

**CRITICAL VALIDATION AFTER PHASE 0:**
- ✅ All tests pass: `./gradlew test`
- ✅ Coverage >85%: `./gradlew jacocoTestReport`
- ✅ Build succeeds: `./gradlew build`
- ✅ No compilation errors
- ✅ Plugin generates projects successfully

### Phase 1: Imperative Foundation (Priority 1)

**Goal:** Get basic imperative paradigm working

1. Create directory structure: `spring/imperative/`
2. Create metadata.yml for imperative paradigm
3. Create symlinks for shared templates (domain, application)
4. Create imperative usecase templates (UseCase.java.ftl, InputPort.java.ftl)
5. Create imperative application.yml.ftl
6. Test: Generate a basic imperative project

**Deliverable:** Can generate empty imperative project with use cases

### Phase 2: Essential Adapters (Priority 1)

**Goal:** Get most common adapters working

1. REST entry-point (imperative)
2. MongoDB driven-adapter (imperative)
3. PostgreSQL driven-adapter (imperative)
4. Update index.json files

**Deliverable:** Can generate imperative project with REST + database

### Phase 3: Reactive Missing Adapters (Priority 2)

**Goal:** Complete reactive paradigm

1. HTTP Client (reactive)
2. DynamoDB (reactive)
3. SQS Producer (reactive)
4. SQS Consumer (reactive)
5. GraphQL (reactive)
6. gRPC (reactive)

**Deliverable:** Reactive paradigm has all adapters

### Phase 4: Imperative Remaining Adapters (Priority 2)

**Goal:** Complete imperative paradigm

1. Redis (imperative)
2. HTTP Client (imperative)
3. DynamoDB (imperative)
4. SQS Producer (imperative)
5. SQS Consumer (imperative)
6. GraphQL (imperative)
7. gRPC (imperative)

**Deliverable:** Both paradigms have feature parity

### Phase 5: Documentation and Polish (Priority 3)

**Goal:** Make it production-ready

1. README.md for each adapter
2. Update main documentation
3. Add examples
4. Integration tests

**Deliverable:** Production-ready feature

## Testing Strategy

### Unit Tests (Existing Framework)

Use existing test templates:
- Mock external dependencies (repositories, HTTP clients, AWS clients)
- Test success and error paths
- Use JUnit 5 + Mockito

### Integration Tests

Use Testcontainers where applicable:
- PostgreSQL: `testcontainers/postgresql`
- MongoDB: `testcontainers/mongodb`
- Redis: `testcontainers/redis`
- AWS services: `testcontainers/localstack`

### Template Validation

Use existing `ValidateTemplatesTask`:
- Validates FreeMarker syntax
- Checks template variables
- Verifies file structure

## Migration Guide

### For Existing Reactive Projects

No changes needed. Reactive templates remain unchanged.

### For New Imperative Projects

```bash
# Initialize imperative project
./gradlew initCleanArch
# Select: Imperative paradigm

# Generate adapters
./gradlew generateOutputAdapter
# Select: MongoDB, PostgreSQL, Redis, etc.

./gradlew generateInputAdapter
# Select: REST, GraphQL, gRPC, etc.
```

### Mixing Paradigms (Not Recommended)

The plugin validates paradigm consistency. Mixing reactive and imperative adapters in the same project will generate warnings.

## Risks and Mitigations

### Risk 1: Template Complexity

**Risk:** 14 new adapters = lots of templates to maintain

**Mitigation:**
- Reuse templates via symlinks where possible
- Use consistent structure across all adapters
- Generate templates from a base template

### Risk 2: Dependency Conflicts

**Risk:** Mixing reactive and imperative dependencies could cause conflicts

**Mitigation:**
- Plugin validates paradigm consistency
- Warn users if mixing paradigms
- Document incompatibilities

### Risk 3: Testing Coverage

**Risk:** Hard to test all adapter combinations

**Mitigation:**
- Focus on essential adapters first
- Use Testcontainers for integration tests
- Document manual testing procedures

## Success Criteria

1. ✅ Can generate imperative projects with Spring MVC
2. ✅ Can generate all 14 adapters (7 types × 2 paradigms)
3. ✅ Generated code compiles without errors
4. ✅ Generated tests pass
5. ✅ Documentation is complete
6. ✅ No changes to core plugin code needed
