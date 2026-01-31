# Explicación de Templates - Cómo Funciona

## 🤔 El Problema que Resuelve

Queremos generar código para:
- **2 arquitecturas**: Hexagonal, Onion
- **2+ frameworks**: Spring, Quarkus
- **2 paradigmas**: Reactive, Imperative
- **N adaptadores**: Redis, Kafka, DynamoDB, PostgreSQL, etc.

**Combinaciones posibles**: 2 × 2 × 2 × N = Muchas!

---

## 🎯 La Solución: Separación de Responsabilidades

### Concepto Clave: Arquitectura ≠ Framework

**Arquitectura** (Hexagonal, Onion):
- Define **DÓNDE** van las cosas (estructura de carpetas)
- Define **QUÉ** capas existen
- Es independiente del framework

**Framework** (Spring, Quarkus):
- Define **CÓMO** se implementan las cosas
- Define qué librerías usar
- Define sintaxis específica (anotaciones, APIs)

---

## 📁 Estructura de Templates Explicada

```
templates/
├── architectures/          # Define ESTRUCTURA (dónde van las cosas)
│   ├── hexagonal/
│   └── onion/
│
└── frameworks/             # Define IMPLEMENTACIÓN (cómo se hacen las cosas)
    ├── spring/
    │   ├── reactive/
    │   └── imperative/
    └── quarkus/
        ├── reactive/
        └── imperative/
```

---

## 🔍 Ejemplo Paso a Paso

### Escenario: Usuario quiere Spring Reactive con Hexagonal

```bash
./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment
```

### Paso 1: El plugin lee la arquitectura

```
templates/architectures/hexagonal/structure.yml
```

Este archivo define:
```yaml
name: hexagonal
structure:
  domain:
    path: domain
    subfolders:
      model:
        path: model
      port:
        path: port
        subfolders:
          in:
            path: in
          out:
            path: out
      usecase:
        path: usecase
  
  infrastructure:
    path: infrastructure
    subfolders:
      adapter:
        path: adapter
        subfolders:
          in:
            path: in
          out:
            path: out
      config:
        path: config
```

**Resultado**: El plugin crea esta estructura de carpetas:
```
src/main/java/com/company/payment/
├── domain/
│   ├── model/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── usecase/
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   └── out/
    └── config/
```

### Paso 2: El plugin usa templates del framework

```
templates/frameworks/spring/reactive/project/
├── build.gradle.kts.ftl
├── application.yml.ftl
└── Application.java.ftl
```

**build.gradle.kts.ftl** (Spring Reactive):
```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    java
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")  // ← Reactivo
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    // ...
}
```

**Application.java.ftl**:
```java
package ${basePackage}.infrastructure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "${basePackage}")  // ← Spring específico
public class ${projectName}Application {
    public static void main(String[] args) {
        SpringApplication.run(${projectName}Application.class, args);
    }
}
```

---

## 🔄 Ahora: ¿Cómo agregar Quarkus Hexagonal?

### Opción A: Quarkus usa la MISMA estructura hexagonal

```
templates/
├── architectures/
│   └── hexagonal/
│       └── structure.yml          # ← YA EXISTE, se reutiliza!
│
└── frameworks/
    ├── spring/
    │   └── reactive/               # ← Ya existe
    │
    └── quarkus/                    # ← NUEVO
        └── reactive/               # ← NUEVO
            ├── project/
            │   ├── build.gradle.kts.ftl
            │   ├── application.properties.ftl
            │   └── Application.java.ftl
            │
            └── adapters/
                └── output/
                    └── redis/
                        ├── Adapter.java.ftl
                        └── Config.java.ftl
```

### Lo que cambias para Quarkus:

#### 1. build.gradle.kts.ftl (Quarkus)
```kotlin
plugins {
    id("io.quarkus") version "3.6.0"  // ← Quarkus en lugar de Spring
    java
}

dependencies {
    implementation("io.quarkus:quarkus-resteasy-reactive")  // ← Quarkus
    implementation("io.quarkus:quarkus-redis-client")
    // ...
}
```

#### 2. Application.java.ftl (Quarkus)
```java
package ${basePackage}.infrastructure.config;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain  // ← Quarkus específico
public class ${projectName}Application implements QuarkusApplication {
    @Override
    public int run(String... args) {
        Quarkus.waitForExit();
        return 0;
    }
}
```

#### 3. Redis Adapter (Quarkus Reactive)
```java
// templates/frameworks/quarkus/reactive/adapters/output/redis/Adapter.java.ftl
package ${basePackage}.infrastructure.adapter.out.redis;

import ${basePackage}.domain.port.out.${portName};
import io.quarkus.redis.datasource.ReactiveRedisDataSource;  // ← Quarkus
import io.smallrye.mutiny.Uni;  // ← Mutiny en lugar de Reactor
import jakarta.enterprise.context.ApplicationScoped;  // ← Jakarta en lugar de Spring

@ApplicationScoped  // ← Quarkus en lugar de @Component
public class ${name}RedisAdapter implements ${portName} {
    
    private final ReactiveRedisDataSource redis;
    
    public ${name}RedisAdapter(ReactiveRedisDataSource redis) {
        this.redis = redis;
    }
    
    @Override
    public Uni<Void> save(String key, Object value) {  // ← Uni en lugar de Mono
        return redis.value(String.class)
            .set(key, value.toString())
            .replaceWithVoid();
    }
}
```

### La estructura de carpetas ES LA MISMA

```
src/main/java/com/company/payment/
├── domain/                    # ← IGUAL para Spring y Quarkus
│   ├── model/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── usecase/
└── infrastructure/            # ← IGUAL para Spring y Quarkus
    ├── adapter/
    │   ├── in/
    │   └── out/
    │       └── redis/         # ← Aquí va el adaptador (Spring o Quarkus)
    └── config/
```

---

## 🎨 Visualización Completa

### Usuario ejecuta:
```bash
./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --framework=quarkus \
  --package=com.company.payment
```

### El plugin hace:

```
1. Lee: templates/architectures/hexagonal/structure.yml
   └─> Crea estructura de carpetas

2. Lee: templates/frameworks/quarkus/reactive/project/
   └─> Genera build.gradle.kts (con dependencias de Quarkus)
   └─> Genera application.properties (config de Quarkus)
   └─> Genera Application.java (con @QuarkusMain)

3. Resultado:
   payment-service/
   ├── build.gradle.kts          # ← Con Quarkus
   ├── src/main/java/com/company/payment/
   │   ├── domain/                # ← Estructura hexagonal
   │   │   ├── model/
   │   │   ├── port/
   │   │   └── usecase/
   │   └── infrastructure/
   │       ├── adapter/
   │       └── config/
   │           └── PaymentServiceApplication.java  # ← Con Quarkus
   └── src/main/resources/
       └── application.properties  # ← Quarkus usa .properties
```

---

## 🔧 Generación de Adaptadores

### Usuario genera adaptador Redis:
```bash
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache
```

### El plugin hace:

```
1. Lee .cleanarch.yml:
   framework: quarkus
   paradigm: reactive
   architecture: hexagonal

2. Busca template:
   templates/frameworks/quarkus/reactive/adapters/output/redis/Adapter.java.ftl

3. Genera en la ubicación que define la arquitectura:
   src/main/java/com/company/payment/infrastructure/driven-adapters/redis/
   └── PaymentCacheRedisAdapter.java  # ← Con código de Quarkus
```

---

## 📊 Matriz de Combinaciones

| Arquitectura | Framework | Paradigma | Templates Necesarios |
|--------------|-----------|-----------|---------------------|
| Hexagonal | Spring | Reactive | `architectures/hexagonal/` + `frameworks/spring/reactive/` |
| Hexagonal | Spring | Imperative | `architectures/hexagonal/` + `frameworks/spring/imperative/` |
| Hexagonal | Quarkus | Reactive | `architectures/hexagonal/` + `frameworks/quarkus/reactive/` |
| Hexagonal | Quarkus | Imperative | `architectures/hexagonal/` + `frameworks/quarkus/imperative/` |
| Onion | Spring | Reactive | `architectures/onion/` + `frameworks/spring/reactive/` |
| Onion | Quarkus | Reactive | `architectures/onion/` + `frameworks/quarkus/reactive/` |

**Clave**: La arquitectura se reutiliza, solo cambias el framework!

---

## 🎯 Ejemplo Concreto: Agregar Quarkus Reactive Hexagonal

### Paso 1: La arquitectura hexagonal YA EXISTE

No necesitas crear nada en `architectures/hexagonal/` porque ya está definida.

### Paso 2: Crear templates de Quarkus Reactive

```bash
cd backend-architecture-design-archetype-generator-templates

# Crear estructura para Quarkus Reactive
mkdir -p templates/frameworks/quarkus/reactive/project
mkdir -p templates/frameworks/quarkus/reactive/adapters/output/redis
mkdir -p templates/frameworks/quarkus/reactive/adapters/input/rest
mkdir -p templates/frameworks/quarkus/reactive/usecase
```

### Paso 3: Crear metadata del framework

```yaml
# templates/frameworks/quarkus/metadata.yml
name: quarkus
displayName: Quarkus
description: Supersonic Subatomic Java Framework
version: 3.6.0
website: https://quarkus.io

paradigms:
  - reactive
  - imperative

dependencies:
  reactive:
    - io.quarkus:quarkus-resteasy-reactive
    - io.quarkus:quarkus-hibernate-reactive-panache
    - io.quarkus:quarkus-redis-client
  imperative:
    - io.quarkus:quarkus-resteasy
    - io.quarkus:quarkus-hibernate-orm-panache
```

### Paso 4: Crear templates de proyecto

#### build.gradle.kts.ftl
```kotlin
// templates/frameworks/quarkus/reactive/project/build.gradle.kts.ftl
plugins {
    id("java")
    id("io.quarkus") version "3.6.0"
}

group = "${groupId}"
version = "${version}"

java {
    sourceCompatibility = JavaVersion.VERSION_${javaVersion}
}

repositories {
    mavenCentral()
}

dependencies {
    // Quarkus Reactive
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.6.0"))
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    
    // Reactive Database
    implementation("io.quarkus:quarkus-hibernate-reactive-panache")
    implementation("io.quarkus:quarkus-reactive-pg-client")
    
    // Redis
    implementation("io.quarkus:quarkus-redis-client")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    
    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
```

#### application.properties.ftl
```properties
# templates/frameworks/quarkus/reactive/project/application.properties.ftl
# Application
quarkus.application.name=${projectName}

# HTTP
quarkus.http.port=8080

# Logging
quarkus.log.level=INFO
quarkus.log.category."${basePackage}".level=DEBUG

# Redis
quarkus.redis.hosts=redis://localhost:6379

# Database (PostgreSQL Reactive)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.reactive.url=postgresql://localhost:5432/${projectName}
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres

# Hibernate Reactive
quarkus.hibernate-orm.database.generation=update
```

#### Application.java.ftl
```java
// templates/frameworks/quarkus/reactive/project/Application.java.ftl
package ${basePackage}.infrastructure.config;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Main application class for ${projectName}
 * 
 * @author Generated by Clean Arch Generator
 */
@QuarkusMain
public class ${projectName}Application implements QuarkusApplication {
    
    @Override
    public int run(String... args) {
        System.out.println("${projectName} is running...");
        Quarkus.waitForExit();
        return 0;
    }
    
    public static void main(String[] args) {
        Quarkus.run(${projectName}Application.class, args);
    }
}
```

### Paso 5: Crear template de adaptador Redis

```java
// templates/frameworks/quarkus/reactive/adapters/output/redis/Adapter.java.ftl
package ${basePackage}.infrastructure.adapter.out.redis;

import ${basePackage}.domain.model.${entityName};
import ${basePackage}.domain.port.out.${portName};
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Duration;

/**
 * Redis adapter for ${entityName}
 * Cache strategy: ${cacheStrategy}
 * 
 * @author Generated by Clean Arch Generator
 */
@ApplicationScoped
public class ${name}RedisAdapter implements ${portName} {
    
    private static final Logger LOG = Logger.getLogger(${name}RedisAdapter.class);
    
    private final ReactiveValueCommands<String, ${entityName}> commands;
    
    private static final String KEY_PREFIX = "${keyPrefix}:";
    private static final Duration TTL = Duration.ofSeconds(${ttl});
    
    public ${name}RedisAdapter(ReactiveRedisDataSource redis) {
        this.commands = redis.value(${entityName}.class);
    }
    
    <#if cacheStrategy == "writeThrough" || cacheStrategy == "writeBack">
    @Override
    public Uni<Void> save(String key, ${entityName} entity) {
        LOG.debugf("Saving to Redis cache: %s", key);
        return commands.setex(KEY_PREFIX + key, TTL.getSeconds(), entity)
            .invoke(() -> LOG.debugf("Saved to cache: %s", key))
            .onFailure().invoke(error -> LOG.errorf(error, "Error saving to cache: %s", key))
            .replaceWithVoid();
    }
    </#if>
    
    <#if cacheStrategy == "readThrough" || cacheStrategy == "writeThrough">
    @Override
    public Uni<${entityName}> get(String key) {
        LOG.debugf("Getting from Redis cache: %s", key);
        return commands.get(KEY_PREFIX + key)
            .invoke(result -> {
                if (result != null) {
                    LOG.debugf("Cache hit: %s", key);
                } else {
                    LOG.debugf("Cache miss: %s", key);
                }
            })
            .onFailure().invoke(error -> LOG.errorf(error, "Error getting from cache: %s", key));
    }
    </#if>
    
    @Override
    public Uni<Void> delete(String key) {
        LOG.debugf("Deleting from Redis cache: %s", key);
        return commands.getdel(KEY_PREFIX + key)
            .invoke(() -> LOG.debugf("Deleted from cache: %s", key))
            .onFailure().invoke(error -> LOG.errorf(error, "Error deleting from cache: %s", key))
            .replaceWithVoid();
    }
    
    @Override
    public Uni<Boolean> exists(String key) {
        return commands.get(KEY_PREFIX + key)
            .map(value -> value != null);
    }
}
```

### Paso 6: Crear metadata del adaptador

```yaml
# templates/frameworks/quarkus/reactive/adapters/output/redis/metadata.yml
name: redis
displayName: Redis Cache
description: Adaptador de caché con Redis usando Quarkus Redis Client
framework: quarkus
paradigm: reactive
type: output
version: 1.0.0
author: Clean Arch Generator Team

parameters:
  required:
    - name: name
      type: string
      description: Nombre del adaptador
      example: PaymentCache
    - name: entityName
      type: string
      description: Nombre de la entidad a cachear
      example: Payment
  optional:
    - name: cacheStrategy
      type: string
      description: Estrategia de caché
      default: writeThrough
      options: [writeThrough, writeBack, readThrough]
    - name: ttl
      type: integer
      description: Time to live en segundos
      default: 3600
    - name: keyPrefix
      type: string
      description: Prefijo para las keys
      default: cache

dependencies:
  gradle:
    - groupId: io.quarkus
      artifactId: quarkus-redis-client
      version: 3.6.0

files:
  - name: Adapter.java.ftl
    output: "{name}RedisAdapter.java"
    description: Redis adapter implementation
  - name: Test.java.ftl
    output: "{name}RedisAdapterTest.java"
    description: Unit tests

examples:
  - name: Simple cache
    description: Caché básico con TTL
    command: |
      ./gradlew generateOutputAdapter \
        --type=redis \
        --name=PaymentCache \
        --entityName=Payment \
        --ttl=3600
```

### Paso 7: Actualizar index.json

```json
// templates/frameworks/quarkus/reactive/adapters/output/index.json
{
  "version": "1.0.0",
  "lastUpdated": "2026-01-31T10:00:00Z",
  "adapters": [
    {
      "name": "redis",
      "displayName": "Redis Cache",
      "description": "Adaptador de caché con Redis",
      "type": "output",
      "status": "stable",
      "version": "1.0.0"
    }
  ]
}
```

---

## 🚀 Uso Final

### Usuario crea proyecto con Quarkus Reactive Hexagonal

```bash
# 1. Inicializar
./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --framework=quarkus \
  --package=com.company.payment

# 2. Generar entidad
./gradlew generateEntity \
  --name=Payment \
  --fields="id:String,amount:BigDecimal"

# 3. Generar caso de uso
./gradlew generateUseCase \
  --name=ProcessPayment

# 4. Generar adaptador Redis (usa template de Quarkus)
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache \
  --entityName=Payment

# 5. Compilar y ejecutar
./gradlew quarkusDev
```

### Resultado

```
payment-service/
├── build.gradle.kts                    # ← Con Quarkus
├── src/main/java/com/company/payment/
│   ├── domain/                         # ← Estructura hexagonal (reutilizada)
│   │   ├── model/
│   │   │   └── Payment.java
│   │   ├── port/
│   │   │   ├── in/
│   │   │   │   └── ProcessPaymentPort.java
│   │   │   └── out/
│   │   │       └── PaymentCachePort.java
│   │   └── usecase/
│   │       └── ProcessPaymentUseCase.java
│   └── infrastructure/
│       ├── adapter/
│       │   └── out/
│       │       └── redis/
│       │           └── PaymentCacheRedisAdapter.java  # ← Con Quarkus/Mutiny
│       └── config/
│           └── PaymentServiceApplication.java  # ← Con @QuarkusMain
└── src/main/resources/
    └── application.properties          # ← Quarkus usa .properties
```

---

## 📋 Resumen: Qué Necesitas para Agregar Quarkus

### ✅ Lo que YA existe (reutilizable):
- `templates/architectures/hexagonal/structure.yml` ← Define estructura de carpetas
- `templates/architectures/onion/structure.yml` ← Define estructura de carpetas

### ✨ Lo que DEBES crear:
```
templates/frameworks/quarkus/
├── metadata.yml                        # Info del framework
├── reactive/
│   ├── metadata.yml                    # Info del paradigma
│   ├── project/                        # Templates de proyecto
│   │   ├── build.gradle.kts.ftl
│   │   ├── application.properties.ftl
│   │   └── Application.java.ftl
│   ├── adapters/
│   │   ├── input/
│   │   │   └── rest/
│   │   │       ├── Controller.java.ftl
│   │   │       └── metadata.yml
│   │   └── output/
│   │       ├── redis/
│   │       │   ├── Adapter.java.ftl
│   │       │   └── metadata.yml
│   │       └── index.json
│   └── usecase/
│       ├── UseCase.java.ftl
│       └── metadata.yml
└── imperative/
    └── (similar estructura)
```

---

## 🎯 Diferencias Clave entre Spring y Quarkus

| Aspecto | Spring Reactive | Quarkus Reactive |
|---------|----------------|------------------|
| **Tipos reactivos** | `Mono<T>`, `Flux<T>` | `Uni<T>`, `Multi<T>` |
| **Anotación de componente** | `@Component` | `@ApplicationScoped` |
| **Configuración** | `application.yml` | `application.properties` |
| **Main class** | `@SpringBootApplication` | `@QuarkusMain` |
| **Logging** | `Slf4j` | `JBoss Logging` |
| **Redis client** | `ReactiveRedisTemplate` | `ReactiveRedisDataSource` |
| **Database** | `R2DBC` | `Hibernate Reactive` |

**Pero la estructura de carpetas ES LA MISMA** porque ambos usan arquitectura hexagonal!

---

## ✅ Checklist para Agregar un Nuevo Framework

- [ ] Crear `templates/frameworks/{framework}/metadata.yml`
- [ ] Crear `templates/frameworks/{framework}/{paradigm}/metadata.yml`
- [ ] Crear templates de proyecto (build, config, main)
- [ ] Crear templates de adaptadores (input/output)
- [ ] Crear templates de casos de uso
- [ ] Crear metadata.yml para cada adaptador
- [ ] Crear index.json con lista de adaptadores
- [ ] Agregar ejemplos en `examples/`
- [ ] Agregar tests en `tests/`
- [ ] Documentar en `docs/`

---

**Creado:** 2026-01-31  
**Versión:** 1.0
