# Estructura Multi-Módulo - Explicación Visual

## 🎯 Concepto Clave

**Carpetas organizadoras ≠ Módulos de Gradle**

```
payment-service/
├── domain/                    ← CARPETA (organiza, NO es módulo)
│   ├── model/                 ← MÓDULO de Gradle
│   ├── usecase/               ← MÓDULO de Gradle
│   └── ports/                 ← MÓDULO de Gradle
│
├── infrastructure/            ← CARPETA (organiza, NO es módulo)
│   ├── driven-adapters/       ← CARPETA (organiza, NO es módulo)
│   │   ├── redis-repository/  ← MÓDULO de Gradle
│   │   └── dynamo-repository/ ← MÓDULO de Gradle
│   │
│   └── entry-points/          ← CARPETA (organiza, NO es módulo)
│       └── rest-api/          ← MÓDULO de Gradle
│
└── applications/              ← CARPETA (organiza, NO es módulo)
    └── app-service/           ← MÓDULO de Gradle
```

---

## 📦 Estructura Completa Detallada

```
payment-service/                                    # Proyecto raíz
├── settings.gradle.kts                             # Define TODOS los módulos
├── build.gradle.kts                                # Config compartida
│
├── domain/                                         # 📁 CARPETA
│   │
│   ├── model/                                      # 📦 MÓDULO 1
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/java/com/company/payment/domain/model/
│   │       │   ├── Payment.java
│   │       │   ├── PaymentId.java
│   │       │   └── PaymentStatus.java
│   │       └── test/java/com/company/payment/domain/model/
│   │           └── PaymentTest.java
│   │
│   ├── usecase/                                    # 📦 MÓDULO 2
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/java/com/company/payment/domain/usecase/
│   │       │   ├── ProcessPaymentUseCase.java
│   │       │   ├── GetPaymentUseCase.java
│   │       │   └── CancelPaymentUseCase.java
│   │       └── test/java/com/company/payment/domain/usecase/
│   │           └── ProcessPaymentUseCaseTest.java
│   │
│   └── ports/                                      # 📦 MÓDULO 3
│       ├── build.gradle.kts
│       └── src/
│           └── main/java/com/company/payment/domain/port/
│               ├── in/
│               │   ├── ProcessPaymentPort.java
│               │   └── GetPaymentPort.java
│               └── out/
│                   ├── PaymentRepositoryPort.java
│                   └── PaymentCachePort.java
│
├── application/                                    # 📁 CARPETA
│   │
│   └── app-service/                                # 📦 MÓDULO 4
│       ├── build.gradle.kts
│       └── src/
│           ├── main/java/com/company/payment/application/
│           │   └── service/
│           │       └── PaymentService.java
│           └── test/java/com/company/payment/application/
│               └── service/
│                   └── PaymentServiceTest.java
│
├── infrastructure/                                 # 📁 CARPETA
│   │
│   ├── driven-adapters/                            # 📁 CARPETA (adaptadores de salida)
│   │   │
│   │   ├── redis-repository/                       # 📦 MÓDULO 5
│   │   │   ├── build.gradle.kts
│   │   │   └── src/
│   │   │       ├── main/java/com/company/payment/infrastructure/adapter/redis/
│   │   │       │   ├── PaymentCacheRedisAdapter.java
│   │   │       │   └── config/
│   │   │       │       └── RedisConfig.java
│   │   │       └── test/java/com/company/payment/infrastructure/adapter/redis/
│   │   │           └── PaymentCacheRedisAdapterTest.java
│   │   │
│   │   ├── dynamo-repository/                      # 📦 MÓDULO 6
│   │   │   ├── build.gradle.kts
│   │   │   └── src/
│   │   │       ├── main/java/com/company/payment/infrastructure/adapter/dynamodb/
│   │   │       │   ├── PaymentRepositoryDynamoDbAdapter.java
│   │   │       │   ├── entity/
│   │   │       │   │   └── PaymentEntity.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── PaymentEntityMapper.java
│   │   │       │   └── config/
│   │   │       │       └── DynamoDbConfig.java
│   │   │       └── test/
│   │   │
│   │   ├── postgres-repository/                    # 📦 MÓDULO 7
│   │   │   ├── build.gradle.kts
│   │   │   └── src/
│   │   │
│   │   └── kafka-publisher/                        # 📦 MÓDULO 8
│   │       ├── build.gradle.kts
│   │       └── src/
│   │           └── main/java/com/company/payment/infrastructure/adapter/kafka/
│   │               ├── PaymentEventKafkaProducer.java
│   │               └── config/
│   │                   └── KafkaConfig.java
│   │
│   └── entry-points/                               # 📁 CARPETA (adaptadores de entrada)
│       │
│       ├── rest-api/                               # 📦 MÓDULO 9
│       │   ├── build.gradle.kts
│       │   └── src/
│       │       ├── main/java/com/company/payment/infrastructure/entrypoint/rest/
│       │       │   ├── PaymentController.java
│       │       │   ├── dto/
│       │       │   │   ├── PaymentRequest.java
│       │       │   │   └── PaymentResponse.java
│       │       │   ├── mapper/
│       │       │   │   └── PaymentDtoMapper.java
│       │       │   └── config/
│       │       │       └── WebConfig.java
│       │       └── test/java/com/company/payment/infrastructure/entrypoint/rest/
│       │           └── PaymentControllerTest.java
│       │
│       ├── graphql-api/                            # 📦 MÓDULO 10 (opcional)
│       │   ├── build.gradle.kts
│       │   └── src/
│       │
│       └── kafka-listener/                         # 📦 MÓDULO 11
│           ├── build.gradle.kts
│           └── src/
│               └── main/java/com/company/payment/infrastructure/entrypoint/kafka/
│                   ├── PaymentEventConsumer.java
│                   └── config/
│                       └── KafkaConsumerConfig.java
│
└── applications/                                   # 📁 CARPETA
    │
    └── app-service/                                # 📦 MÓDULO 12 (Main)
        ├── build.gradle.kts
        └── src/
            ├── main/
            │   ├── java/com/company/payment/config/
            │   │   ├── PaymentServiceApplication.java
            │   │   └── BeanConfiguration.java
            │   └── resources/
            │       ├── application.yml
            │       ├── application-local.yml
            │       └── application-prod.yml
            └── test/
                └── java/com/company/payment/
                    └── PaymentServiceApplicationTest.java
```

---

## 📝 settings.gradle.kts

```kotlin
rootProject.name = "payment-service"

// Domain modules (dentro de carpeta domain/)
include("domain:model")
include("domain:usecase")
include("domain:ports")

// Application modules (dentro de carpeta application/)
include("application:app-service")

// Infrastructure - Driven Adapters (dentro de infrastructure/driven-adapters/)
include("infrastructure:driven-adapters:redis-repository")
include("infrastructure:driven-adapters:dynamo-repository")
include("infrastructure:driven-adapters:postgres-repository")
include("infrastructure:driven-adapters:kafka-publisher")

// Infrastructure - Entry Points (dentro de infrastructure/entry-points/)
include("infrastructure:entry-points:rest-api")
include("infrastructure:entry-points:graphql-api")
include("infrastructure:entry-points:kafka-listener")

// Main application (dentro de carpeta applications/)
include("applications:app-service")
```

**Nota**: Gradle usa `:` para separar niveles de carpetas en módulos anidados.

---

## 🔗 Dependencias entre Módulos

### Gráfico de Dependencias

```
                    applications:app-service
                            |
                            | (depende de todo)
                            |
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
  domain:usecase    infrastructure:      infrastructure:
                    entry-points:*       driven-adapters:*
        │
        │ (depende de)
        │
        ▼
   domain:ports ──────────────────────┐
        │                             │
        │ (depende de)                │ (depende de)
        │                             │
        ▼                             ▼
   domain:model ◄────────────────────┘
```

### Reglas de Dependencias

1. **domain:model** → Sin dependencias (puro Java)
2. **domain:ports** → Depende de `domain:model`
3. **domain:usecase** → Depende de `domain:model` + `domain:ports`
4. **application:app-service** → Depende de `domain:*`
5. **infrastructure:driven-adapters:*** → Depende de `domain:model` + `domain:ports`
6. **infrastructure:entry-points:*** → Depende de `domain:model` + `domain:ports` + `domain:usecase`
7. **applications:app-service** → Depende de TODO

---

## 🎯 Ejemplo: build.gradle.kts de cada módulo

### domain/model/build.gradle.kts

```kotlin
plugins {
    id("java-library")
}

description = "Domain models - Pure business entities"

dependencies {
    // Sin dependencias externas
}
```

### domain/ports/build.gradle.kts

```kotlin
plugins {
    id("java-library")
}

description = "Domain ports - Interfaces"

dependencies {
    api(project(":domain:model"))
}
```

### domain/usecase/build.gradle.kts

```kotlin
plugins {
    id("java-library")
}

description = "Domain use cases - Business logic"

dependencies {
    api(project(":domain:model"))
    api(project(":domain:ports"))
}
```

### infrastructure/driven-adapters/redis-repository/build.gradle.kts

```kotlin
plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

description = "Redis cache adapter"

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}
```

### infrastructure/entry-points/rest-api/build.gradle.kts

```kotlin
plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

description = "REST API"

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    implementation(project(":domain:usecase"))
    
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

### applications/app-service/build.gradle.kts

```kotlin
plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "Main application"

dependencies {
    // Domain
    implementation(project(":domain:usecase"))
    
    // Application
    implementation(project(":application:app-service"))
    
    // Infrastructure - Driven Adapters
    implementation(project(":infrastructure:driven-adapters:redis-repository"))
    implementation(project(":infrastructure:driven-adapters:dynamo-repository"))
    implementation(project(":infrastructure:driven-adapters:kafka-publisher"))
    
    // Infrastructure - Entry Points
    implementation(project(":infrastructure:entry-points:rest-api"))
    implementation(project(":infrastructure:entry-points:kafka-listener"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
}
```

---

## 🚀 Comandos de Gradle

### Compilar todo
```bash
./gradlew build
```

### Compilar solo un módulo
```bash
./gradlew :domain:model:build
./gradlew :infrastructure:driven-adapters:redis-repository:build
```

### Ejecutar la aplicación
```bash
./gradlew :applications:app-service:bootRun
```

### Ver dependencias de un módulo
```bash
./gradlew :applications:app-service:dependencies
```

---

## ✅ Ventajas de esta Estructura

1. **Organización clara**: Carpetas agrupan módulos relacionados
2. **Compilación independiente**: Cada módulo se compila por separado
3. **Testing aislado**: Puedes testear cada módulo independientemente
4. **Reutilización**: `domain:model` puede usarse en otros proyectos
5. **Equipos especializados**: Un equipo por módulo
6. **Build incremental**: Solo recompila lo que cambió

---

## 📋 Resumen

| Elemento | Tipo | Propósito |
|----------|------|-----------|
| `domain/` | Carpeta | Organiza módulos de dominio |
| `domain/model/` | Módulo | Entidades de negocio |
| `domain/ports/` | Módulo | Interfaces (puertos) |
| `domain/usecase/` | Módulo | Lógica de negocio |
| `infrastructure/` | Carpeta | Organiza módulos de infraestructura |
| `infrastructure/driven-adapters/` | Carpeta | Organiza adaptadores de salida |
| `infrastructure/driven-adapters/redis-repository/` | Módulo | Implementación Redis |
| `infrastructure/entry-points/` | Carpeta | Organiza adaptadores de entrada |
| `infrastructure/entry-points/rest-api/` | Módulo | Implementación REST |
| `applications/` | Carpeta | Organiza aplicaciones |
| `applications/app-service/` | Módulo | Aplicación principal |

---

**Creado:** 2026-01-31  
**Versión:** 1.0
