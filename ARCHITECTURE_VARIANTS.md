# Variantes de Arquitectura - Single Module vs Multi Module

## 🎯 El Problema

La misma arquitectura (Hexagonal) puede tener diferentes estructuras:

1. **Single Module**: Todo en un solo proyecto
2. **Multi Module**: Separado en módulos de Gradle/Maven

---

## 📦 Variantes de Hexagonal

### Variante 1: Hexagonal Single Module (Simple)

```
payment-service/                    # Un solo módulo
├── build.gradle.kts
└── src/main/java/com/company/payment/
    ├── domain/
    │   ├── model/
    │   ├── port/
    │   └── usecase/
    └── infrastructure/
        ├── adapter/
        └── config/
```

**Cuándo usar:**
- ✅ Proyectos pequeños/medianos
- ✅ Equipos pequeños
- ✅ Desarrollo rápido
- ✅ Menos complejidad

---

### Variante 2: Hexagonal Multi Module (Modular)

```
payment-service/                    # Proyecto raíz
├── settings.gradle.kts             # Define módulos
├── build.gradle.kts                # Config compartida
│
├── domain/                         # Módulo 1: Domain
│   ├── build.gradle.kts
│   └── src/main/java/com/company/payment/domain/
│       ├── model/
│       ├── port/
│       └── usecase/
│
├── application/                    # Módulo 2: Application (opcional)
│   ├── build.gradle.kts
│   └── src/main/java/com/company/payment/application/
│       └── service/
│
└── infrastructure/                 # Módulo 3: Infrastructure
    ├── build.gradle.kts
    └── src/main/java/com/company/payment/infrastructure/
        ├── adapter/
        └── config/
```

**Cuándo usar:**
- ✅ Proyectos grandes
- ✅ Equipos grandes (diferentes equipos por módulo)
- ✅ Reutilización del dominio en otros proyectos
- ✅ Despliegues independientes (con más trabajo)

---

### Variante 3: Hexagonal Multi Module Granular (Muy Modular)

```
payment-service/                    # Proyecto raíz
├── settings.gradle.kts
├── build.gradle.kts
│
├── domain/                         # Carpeta domain (NO es módulo)
│   ├── model/                      # Módulo 1: Entidades
│   │   ├── build.gradle.kts
│   │   └── src/main/java/.../domain/model/
│   │
│   ├── usecase/                    # Módulo 2: Casos de uso
│   │   ├── build.gradle.kts
│   │   └── src/main/java/.../domain/usecase/
│   │
│   └── ports/                      # Módulo 3: Puertos (interfaces)
│       ├── build.gradle.kts
│       └── src/main/java/.../domain/port/
│
├── application/                    # Carpeta application (NO es módulo)
│   └── app-service/                # Módulo 4: Servicios de aplicación
│       ├── build.gradle.kts
│       └── src/main/java/.../application/
│
├── infrastructure/                 # Carpeta infrastructure (NO es módulo)
│   ├── driven-adapters/            # Carpeta (NO es módulo)
│   │   ├── redis-repository/       # Módulo 5: Adaptador Redis
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/java/.../infrastructure/adapter/redis/
│   │   │
│   │   ├── dynamo-repository/      # Módulo 6: Adaptador DynamoDB
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/java/.../infrastructure/adapter/dynamodb/
│   │   │
│   │   └── kafka-publisher/        # Módulo 7: Adaptador Kafka
│   │       ├── build.gradle.kts
│   │       └── src/main/java/.../infrastructure/adapter/kafka/
│   │
│   └── entry-points/               # Carpeta (NO es módulo)
│       ├── rest-api/               # Módulo 8: API REST
│       │   ├── build.gradle.kts
│       │   └── src/main/java/.../infrastructure/entrypoint/rest/
│       │
│       └── kafka-listener/         # Módulo 9: Consumer Kafka
│           ├── build.gradle.kts
│           └── src/main/java/.../infrastructure/entrypoint/kafka/
│
└── applications/                   # Carpeta (NO es módulo)
    └── app-service/                # Módulo 10: Main application
        ├── build.gradle.kts
        └── src/main/java/.../config/
```

**Estructura clave:**
- **Carpetas**: `domain/`, `application/`, `infrastructure/` → Organizan, NO son módulos
- **Subcarpetas**: `driven-adapters/`, `entry-points/` → Organizan, NO son módulos
- **Módulos**: Cada subcarpeta dentro (model, usecase, redis-repository, rest-api, etc.)

**Cuándo usar:**
- ✅ Proyectos muy grandes
- ✅ Múltiples equipos especializados
- ✅ Reutilización máxima
- ✅ Testing independiente por módulo
- ⚠️ Mayor complejidad de gestión

---

## 🔧 Solución: Arquitecturas con Variantes

### Estructura de Templates Actualizada

```
templates/
├── architectures/
│   ├── hexagonal-single/           # ← NUEVA
│   │   ├── structure.yml
│   │   └── project/
│   │
│   ├── hexagonal-multi/            # ← NUEVA
│   │   ├── structure.yml
│   │   ├── project/
│   │   │   ├── settings.gradle.kts.ftl
│   │   │   └── build.gradle.kts.ftl
│   │   └── modules/
│   │       ├── domain/
│   │       │   └── build.gradle.kts.ftl
│   │       ├── application/
│   │       │   └── build.gradle.kts.ftl
│   │       └── infrastructure/
│   │           └── build.gradle.kts.ftl
│   │
│   ├── hexagonal-multi-granular/   # ← NUEVA
│   │   ├── structure.yml
│   │   ├── project/
│   │   └── modules/
│   │       ├── domain-model/
│   │       ├── domain-ports/
│   │       ├── domain-usecases/
│   │       ├── adapter-rest/
│   │       ├── adapter-redis/
│   │       └── application/
│   │
│   ├── onion-single/               # ← NUEVA
│   │   └── structure.yml
│   │
│   └── onion-multi/                # ← NUEVA
│       └── structure.yml
│
└── frameworks/
    └── spring/
        └── reactive/
            └── (sin cambios)
```

---

## 📄 Ejemplo: structure.yml para cada variante

### hexagonal-single/structure.yml

```yaml
name: hexagonal-single
displayName: Hexagonal Architecture (Single Module)
description: Arquitectura hexagonal en un solo módulo
moduleType: single
version: 1.0.0

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

files:
  - name: build.gradle.kts
    template: build.gradle.kts.ftl
  - name: settings.gradle.kts
    template: settings.gradle.kts.ftl
```

---

### hexagonal-multi/structure.yml

```yaml
name: hexagonal-multi
displayName: Hexagonal Architecture (Multi Module)
description: Arquitectura hexagonal con módulos separados
moduleType: multi
version: 1.0.0

modules:
  - name: domain
    path: domain
    description: Domain layer with models, ports and use cases
    structure:
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
    dependencies: []
    
  - name: application
    path: application
    description: Application layer with services
    structure:
      service:
        path: service
    dependencies:
      - domain
    
  - name: infrastructure
    path: infrastructure
    description: Infrastructure layer with adapters
    structure:
      adapter:
        path: adapter
        subfolders:
          in:
            path: in
          out:
            path: out
      config:
        path: config
    dependencies:
      - domain
      - application

files:
  root:
    - name: settings.gradle.kts
      template: settings.gradle.kts.ftl
    - name: build.gradle.kts
      template: build.gradle.kts.ftl
  modules:
    - name: domain/build.gradle.kts
      template: modules/domain/build.gradle.kts.ftl
    - name: application/build.gradle.kts
      template: modules/application/build.gradle.kts.ftl
    - name: infrastructure/build.gradle.kts
      template: modules/infrastructure/build.gradle.kts.ftl
```

---

### hexagonal-multi-granular/structure.yml

```yaml
name: hexagonal-multi-granular
displayName: Hexagonal Architecture (Multi Module Granular)
description: Arquitectura hexagonal con módulos granulares organizados en carpetas
moduleType: multi-granular
version: 1.0.0

# Carpetas organizadoras (NO son módulos)
folders:
  - domain
  - application
  - infrastructure
  - infrastructure/driven-adapters
  - infrastructure/entry-points
  - applications

# Módulos reales
modules:
  # Domain modules
  - name: model
    path: domain/model
    description: Domain entities and value objects
    packagePath: domain.model
    dependencies: []
    
  - name: usecase
    path: domain/usecase
    description: Use case implementations
    packagePath: domain.usecase
    dependencies:
      - model
      - ports
    
  - name: ports
    path: domain/ports
    description: Input and output ports (interfaces)
    packagePath: domain.port
    dependencies:
      - model
  
  # Application modules
  - name: app-service
    path: application/app-service
    description: Application services
    packagePath: application.service
    dependencies:
      - model
      - usecase
      - ports
  
  # Infrastructure - Driven Adapters (salida)
  - name: redis-repository
    path: infrastructure/driven-adapters/redis-repository
    description: Redis cache adapter
    packagePath: infrastructure.adapter.redis
    dependencies:
      - model
      - ports
    
  - name: dynamo-repository
    path: infrastructure/driven-adapters/dynamo-repository
    description: DynamoDB repository adapter
    packagePath: infrastructure.adapter.dynamodb
    dependencies:
      - model
      - ports
    
  - name: kafka-publisher
    path: infrastructure/driven-adapters/kafka-publisher
    description: Kafka event publisher
    packagePath: infrastructure.adapter.kafka
    dependencies:
      - model
      - ports
  
  # Infrastructure - Entry Points (entrada)
  - name: rest-api
    path: infrastructure/entry-points/rest-api
    description: REST API controllers
    packagePath: infrastructure.entrypoint.rest
    dependencies:
      - model
      - usecase
      - ports
    
  - name: kafka-listener
    path: infrastructure/entry-points/kafka-listener
    description: Kafka event consumer
    packagePath: infrastructure.entrypoint.kafka
    dependencies:
      - model
      - usecase
      - ports
  
  # Main application
  - name: main-app
    path: applications/app-service
    description: Main application that wires everything
    packagePath: config
    dependencies:
      - usecase
      - app-service
      - redis-repository
      - dynamo-repository
      - kafka-publisher
      - rest-api
      - kafka-listener

files:
  root:
    - name: settings.gradle.kts
      template: settings.gradle.kts.ftl
    - name: build.gradle.kts
      template: build.gradle.kts.ftl
  modules:
    - name: domain/model/build.gradle.kts
      template: modules/domain/model/build.gradle.kts.ftl
    - name: domain/usecase/build.gradle.kts
      template: modules/domain/usecase/build.gradle.kts.ftl
    - name: domain/ports/build.gradle.kts
      template: modules/domain/ports/build.gradle.kts.ftl
    - name: application/app-service/build.gradle.kts
      template: modules/application/app-service/build.gradle.kts.ftl
    - name: infrastructure/driven-adapters/redis-repository/build.gradle.kts
      template: modules/infrastructure/driven-adapters/redis-repository/build.gradle.kts.ftl
    - name: infrastructure/entry-points/rest-api/build.gradle.kts
      template: modules/infrastructure/entry-points/rest-api/build.gradle.kts.ftl
    - name: applications/app-service/build.gradle.kts
      template: modules/applications/app-service/build.gradle.kts.ftl
```

---

## 🎯 Uso del Plugin

### Comando actualizado con variante

```bash
./gradlew initCleanArch \
  --architecture=hexagonal-multi \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment
```

### Opciones de arquitectura

```bash
# Single module (simple)
--architecture=hexagonal-single
--architecture=onion-single

# Multi module (3 módulos: domain, application, infrastructure)
--architecture=hexagonal-multi
--architecture=onion-multi

# Multi module granular (un módulo por adaptador)
--architecture=hexagonal-multi-granular
```

---

## 📝 Templates para Multi Module

### settings.gradle.kts.ftl (hexagonal-multi)

```kotlin
// templates/architectures/hexagonal-multi/project/settings.gradle.kts.ftl
rootProject.name = "${projectName}"

include(
    "domain",
    "application",
    "infrastructure"
)
```

### build.gradle.kts.ftl (raíz)

```kotlin
// templates/architectures/hexagonal-multi/project/build.gradle.kts.ftl
plugins {
    id("java")
    id("org.springframework.boot") version "${springBootVersion}" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "${groupId}"
    version = "${version}"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    
    java {
        sourceCompatibility = JavaVersion.VERSION_${javaVersion}
    }
    
    dependencies {
        // Dependencias comunes
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.mockito:mockito-core:5.7.0")
    }
    
    tasks.test {
        useJUnitPlatform()
    }
}
```

### domain/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi/modules/domain/build.gradle.kts.ftl
plugins {
    id("java-library")
}

description = "Domain layer - Business logic and rules"

dependencies {
    // Sin dependencias externas (dominio puro)
    // Solo Java estándar
}
```

### application/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi/modules/application/build.gradle.kts.ftl
plugins {
    id("java-library")
}

description = "Application layer - Use case orchestration"

dependencies {
    // Depende del dominio
    api(project(":domain"))
    
    // Dependencias de aplicación (si las hay)
}
```

### infrastructure/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi/modules/infrastructure/build.gradle.kts.ftl
plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Infrastructure layer - Adapters and technical details"

dependencies {
    // Depende de domain y application
    implementation(project(":domain"))
    implementation(project(":application"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
```

---

## 📝 Templates para Multi Module Granular

### settings.gradle.kts.ftl (hexagonal-multi-granular)

```kotlin
// templates/architectures/hexagonal-multi-granular/project/settings.gradle.kts.ftl
rootProject.name = "${projectName}"

// Domain modules
include("domain:model")
include("domain:usecase")
include("domain:ports")

// Application modules
include("application:app-service")

// Infrastructure - Driven Adapters
include("infrastructure:driven-adapters:redis-repository")
include("infrastructure:driven-adapters:dynamo-repository")
include("infrastructure:driven-adapters:kafka-publisher")

// Infrastructure - Entry Points
include("infrastructure:entry-points:rest-api")
include("infrastructure:entry-points:kafka-listener")

// Main application
include("applications:app-service")
```

### domain-model/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/domain/model/build.gradle.kts.ftl
plugins {
    id("java-library")
}

description = "Domain models - Pure business entities"

dependencies {
    // Sin dependencias (modelos puros)
}
```

### domain-ports/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/domain/ports/build.gradle.kts.ftl
plugins {
    id("java-library")
}

description = "Domain ports - Interfaces for adapters"

dependencies {
    // Depende solo de los modelos
    api(project(":domain:model"))
}
```

### domain-usecases/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/domain/usecase/build.gradle.kts.ftl
plugins {
    id("java-library")
}

description = "Domain use cases - Business logic implementation"

dependencies {
    // Depende de modelos y puertos
    api(project(":domain:model"))
    api(project(":domain:ports"))
}
```

### redis-repository/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/infrastructure/driven-adapters/redis-repository/build.gradle.kts.ftl
plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

description = "Redis adapter - Cache implementation"

dependencies {
    // Depende de puertos y modelos
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    
    // Spring Redis Reactive
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}
```

### rest-api/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/infrastructure/entry-points/rest-api/build.gradle.kts.ftl
plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

description = "REST adapter - HTTP API"

dependencies {
    // Depende de puertos y casos de uso
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    implementation(project(":domain:usecase"))
    
    // Spring WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

### main-app/build.gradle.kts.ftl

```kotlin
// templates/architectures/hexagonal-multi-granular/modules/applications/app-service/build.gradle.kts.ftl
plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Main application - Wires everything together"

dependencies {
    // Depende de todos los módulos necesarios
    implementation(project(":domain:usecase"))
    implementation(project(":application:app-service"))
    implementation(project(":infrastructure:driven-adapters:redis-repository"))
    implementation(project(":infrastructure:driven-adapters:dynamo-repository"))
    implementation(project(":infrastructure:driven-adapters:kafka-publisher"))
    implementation(project(":infrastructure:entry-points:rest-api"))
    implementation(project(":infrastructure:entry-points:kafka-listener"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
}
```

---

## 🎨 Resultado Visual

### Hexagonal Single Module

```
payment-service/
├── build.gradle.kts
└── src/main/java/com/company/payment/
    ├── domain/
    │   ├── model/Payment.java
    │   ├── port/
    │   │   ├── in/ProcessPaymentPort.java
    │   │   └── out/PaymentRepositoryPort.java
    │   └── usecase/ProcessPaymentUseCase.java
    └── infrastructure/
        ├── adapter/
        │   ├── in/rest/PaymentController.java
        │   └── out/
        │       ├── redis/PaymentCacheRedisAdapter.java
        │       └── dynamodb/PaymentRepositoryDynamoDbAdapter.java
        └── config/PaymentServiceApplication.java
```

---

### Hexagonal Multi Module

```
payment-service/
├── settings.gradle.kts
├── build.gradle.kts
│
├── domain/
│   ├── build.gradle.kts
│   └── src/main/java/com/company/payment/domain/
│       ├── model/Payment.java
│       ├── port/
│       │   ├── in/ProcessPaymentPort.java
│       │   └── out/PaymentRepositoryPort.java
│       └── usecase/ProcessPaymentUseCase.java
│
├── application/
│   ├── build.gradle.kts
│   └── src/main/java/com/company/payment/application/
│       └── service/PaymentService.java
│
└── infrastructure/
    ├── build.gradle.kts
    └── src/main/java/com/company/payment/infrastructure/
        ├── adapter/
        │   ├── in/rest/PaymentController.java
        │   └── out/
        │       ├── redis/PaymentCacheRedisAdapter.java
        │       └── dynamodb/PaymentRepositoryDynamoDbAdapter.java
        └── config/PaymentServiceApplication.java
```

---

### Hexagonal Multi Module Granular

```
payment-service/
├── settings.gradle.kts
├── build.gradle.kts
│
├── domain/                                    # Carpeta (NO módulo)
│   ├── model/                                 # Módulo
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/company/payment/domain/model/
│   │       └── Payment.java
│   │
│   ├── usecase/                               # Módulo
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/company/payment/domain/usecase/
│   │       └── ProcessPaymentUseCase.java
│   │
│   └── ports/                                 # Módulo
│       ├── build.gradle.kts
│       └── src/main/java/com/company/payment/domain/port/
│           ├── in/ProcessPaymentPort.java
│           └── out/PaymentRepositoryPort.java
│
├── application/                               # Carpeta (NO módulo)
│   └── app-service/                           # Módulo
│       ├── build.gradle.kts
│       └── src/main/java/com/company/payment/application/
│           └── service/PaymentService.java
│
├── infrastructure/                            # Carpeta (NO módulo)
│   ├── driven-adapters/                       # Carpeta (NO módulo)
│   │   ├── redis-repository/                  # Módulo
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/java/com/company/payment/infrastructure/adapter/redis/
│   │   │       └── PaymentCacheRedisAdapter.java
│   │   │
│   │   ├── dynamo-repository/                 # Módulo
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/java/com/company/payment/infrastructure/adapter/dynamodb/
│   │   │       └── PaymentRepositoryDynamoDbAdapter.java
│   │   │
│   │   └── kafka-publisher/                   # Módulo
│   │       ├── build.gradle.kts
│   │       └── src/main/java/com/company/payment/infrastructure/adapter/kafka/
│   │           └── PaymentEventKafkaProducer.java
│   │
│   └── entry-points/                          # Carpeta (NO módulo)
│       ├── rest-api/                          # Módulo
│       │   ├── build.gradle.kts
│       │   └── src/main/java/com/company/payment/infrastructure/entrypoint/rest/
│       │       └── PaymentController.java
│       │
│       └── kafka-listener/                    # Módulo
│           ├── build.gradle.kts
│           └── src/main/java/com/company/payment/infrastructure/entrypoint/kafka/
│               └── PaymentEventConsumer.java
│
└── applications/                              # Carpeta (NO módulo)
    └── app-service/                           # Módulo
        ├── build.gradle.kts
        └── src/main/java/com/company/payment/config/
            └── PaymentServiceApplication.java
```

---

## 🔄 Generación de Adaptadores en Multi Module

### Comando

```bash
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents
```

### Comportamiento según arquitectura

#### Single Module
```
Genera en:
infrastructure/driven-adapters/kafka/PaymentEventsKafkaProducer.java
```

#### Multi Module
```
Genera en:
infrastructure/src/main/java/.../infrastructure/driven-adapters/kafka/PaymentEventsKafkaProducer.java
```

#### Multi Module Granular
```
1. Crea nuevo módulo en: infrastructure/driven-adapters/kafka-publisher/
2. Genera build.gradle.kts con dependencias
3. Genera código en: infrastructure/driven-adapters/kafka-publisher/src/main/java/.../adapter/kafka/
4. Actualiza settings.gradle.kts: include("infrastructure:driven-adapters:kafka-publisher")
5. Actualiza applications/app-service/build.gradle.kts para depender del nuevo módulo
```

---

## 📋 Comparación de Variantes

| Aspecto | Single | Multi (3 módulos) | Multi Granular |
|---------|--------|-------------------|----------------|
| **Complejidad** | Baja | Media | Alta |
| **Build time** | Rápido | Medio | Lento |
| **Reutilización** | Baja | Media | Alta |
| **Testing** | Todo junto | Por capa | Por módulo |
| **Equipos** | 1-3 personas | 3-6 personas | 6+ personas |
| **Despliegue** | Monolito | Monolito | Potencial micro |
| **Mantenimiento** | Simple | Medio | Complejo |

---

## ✅ Recomendaciones

### Usa Single Module si:
- ✅ Proyecto pequeño/mediano (< 50k líneas)
- ✅ Equipo pequeño (1-3 devs)
- ✅ Prototipo o MVP
- ✅ Quieres velocidad de desarrollo

### Usa Multi Module (3 módulos) si:
- ✅ Proyecto mediano/grande (50k-200k líneas)
- ✅ Equipo mediano (3-6 devs)
- ✅ Quieres separación clara de capas
- ✅ Planeas reutilizar el dominio

### Usa Multi Module Granular si:
- ✅ Proyecto muy grande (> 200k líneas)
- ✅ Equipo grande (6+ devs)
- ✅ Múltiples equipos especializados
- ✅ Planeas migrar a microservicios
- ✅ Necesitas despliegues independientes

---

## 🎯 Configuración en .cleanarch.yml

```yaml
project:
  name: payment-service
  basePackage: com.company.payment
  architecture: hexagonal-multi-granular  # ← Define la variante
  paradigm: reactive
  framework: spring

modules:                                   # ← Generado automáticamente
  # Domain
  - name: model
    path: domain/model
    type: library
    dependencies: []
  - name: ports
    path: domain/ports
    type: library
    dependencies: [model]
  - name: usecase
    path: domain/usecase
    type: library
    dependencies: [model, ports]
  
  # Application
  - name: app-service
    path: application/app-service
    type: library
    dependencies: [model, usecase, ports]
  
  # Infrastructure - Driven Adapters
  - name: redis-repository
    path: infrastructure/driven-adapters/redis-repository
    type: library
    dependencies: [model, ports]
  - name: dynamo-repository
    path: infrastructure/driven-adapters/dynamo-repository
    type: library
    dependencies: [model, ports]
  
  # Infrastructure - Entry Points
  - name: rest-api
    path: infrastructure/entry-points/rest-api
    type: library
    dependencies: [model, ports, usecase]
  
  # Main Application
  - name: main-app
    path: applications/app-service
    type: application
    dependencies: [usecase, app-service, redis-repository, dynamo-repository, rest-api]
```

---

## 🚀 Próximos Pasos

1. ✅ Definir variantes de arquitectura
2. ⏳ Crear templates para cada variante
3. ⏳ Implementar lógica de multi-module en el plugin
4. ⏳ Agregar comando para convertir entre variantes
5. ⏳ Documentar cuándo usar cada variante

---

**Creado:** 2026-01-31  
**Versión:** 1.0
