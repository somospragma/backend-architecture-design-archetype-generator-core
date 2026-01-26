# Clean Architecture Generator - Especificación del Proyecto

## 1. Resumen Ejecutivo

### Objetivo
Crear un generador de código para microservicios con arquitectura limpia en Spring (imperativo y reactivo) que permita a los desarrolladores:
- Generar proyectos completos desde cero
- Agregar componentes dinámicamente (adaptadores, casos de uso, entidades)
- Concentrarse en la lógica de negocio mientras el generador maneja la estructura y conexiones externas

### Solución Elegida
**Plugin único de Gradle** que:
1. Se agrega a un proyecto vacío
2. Inicializa la estructura completa del proyecto
3. Permite generar componentes bajo demanda
4. Mantiene configuración centralizada en `.cleanarch.yml`

---

## 2. Decisiones de Arquitectura

### 2.1 Herramienta Principal
**Gradle Plugin** (opción elegida sobre CLI, JAR ejecutable, Maven Archetype)

**Razones:**
- Experiencia nativa para desarrolladores Java/Kotlin
- Versionado simple y claro
- No requiere instalaciones adicionales
- Actualización transparente (cambio de versión en `build.gradle.kts`)
- Integración perfecta con IDEs
- Funciona offline después de primera descarga

### 2.2 Flujo de Inicialización

**Opción elegida: Plugin con tarea de inicialización**

```bash
# 1. Crear proyecto vacío
mkdir payment-service
cd payment-service

# 2. Crear build.gradle.kts mínimo
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.pragma.archetype-generator") version "1.0.0"
}
EOF

# 3. Inicializar arquitectura
./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --package=com.company.payment

# 4. Generar componentes
./gradlew generateOutputAdapter --type=redis --name=HoldCards
```

**Validaciones:**
- El plugin valida que el proyecto esté vacío antes de `initCleanArch`
- Solo permite archivos: `build.gradle.kts`, `settings.gradle.kts`, carpetas de Gradle
- Muestra error claro si hay archivos adicionales

---

## 3. Características Principales

### 3.1 Frameworks Soportados
- **Spring Boot** (Fase 1)
  - Reactivo: WebFlux, R2DBC, WebClient, Mono/Flux
  - Imperativo: Spring MVC, JPA, RestTemplate
- **Quarkus** (Fase 4 - Futuro)
  - Reactivo: Mutiny, Hibernate Reactive, REST Client Reactive
  - Imperativo: RESTEasy, Hibernate ORM
- **Micronaut** (Futuro)
  - Reactivo y imperativo

### 3.2 Paradigmas Soportados
- **Imperativo**: Tipos síncronos, blocking I/O
- **Reactivo**: Tipos asíncronos, non-blocking I/O

### 3.3 Arquitecturas Soportadas
- **Hexagonal**: Puertos y adaptadores (domain/port/in, domain/port/out, infrastructure/adapter)
- **Onion (Cebolla)**: Capas concéntricas (core/domain, core/application, infrastructure)

### 3.4 Componentes Generables


#### Adaptadores de Salida (Output Adapters)
- Redis (con estrategias de caché configurables)
- DynamoDB
- PostgreSQL/MySQL (R2DBC para reactivo, JPA para imperativo)
- MongoDB
- HTTP Clients (WebClient/RestTemplate)
- SQS Producer
- Kafka Producer

#### Adaptadores de Entrada (Input Adapters)
- REST Controllers
- GraphQL (opcional)
- SQS Consumer
- Kafka Consumer
- gRPC (opcional)

#### Otros Componentes
- **Casos de Uso**: Lógica de negocio
- **Entidades de Dominio**: Modelos del dominio
- **Mappers**: MapStruct para conversión entre entidades

### 3.5 Generación Automática de Tests
- Por cada adaptador generado → archivo de test vacío
- Por cada caso de uso generado → archivo de test vacío
- Estructura de carpetas `src/test/` espejada

---

## 4. Configuración del Proyecto

### 4.1 Archivo `.cleanarch.yml`

Archivo central que mantiene la configuración del proyecto:

```yaml
# Generado automáticamente por clean-arch-generator
project:
  name: payment-service
  basePackage: com.company.payment
  createdAt: 2026-01-22T10:30:00Z
  pluginVersion: 1.0.0

architecture:
  type: hexagonal  # hexagonal | onion
  paradigm: reactive  # reactive | imperative
  framework: spring  # spring | quarkus | micronaut

structure:
  domain:
    model: domain/model
    usecase: domain/usecase
    ports:
      in: domain/port/in
      out: domain/port/out
  infrastructure:
    adapters:
      in: infrastructure/adapter/in
      out: infrastructure/adapter/out
    config: infrastructure/config

dependencies:
  springBoot: 3.2.0
  java: 17
  kotlin: false
  webflux: true
  r2dbc: true
  mapstruct: 1.5.5.Final

components:
  adapters:
    input: []
    output: []
  usecases: []
  entities: []
  mappers: []
```

**Propósito:**
- Mantener registro de componentes generados
- Validar duplicados
- Determinar qué implementaciones usar (reactivo vs imperativo)
- Versionable en Git
- Lectura automática por el plugin

---

## 5. Comandos del Plugin

### 5.1 Inicialización


```bash
./gradlew initCleanArch \
  --architecture=<hexagonal|onion> \
  --paradigm=<reactive|imperative> \
  --framework=<spring|quarkus> \
  --package=<com.company.service>
```

**Genera:**
- Estructura completa de carpetas
- `.cleanarch.yml`
- `build.gradle.kts` actualizado con dependencias
- `settings.gradle.kts`
- `.gitignore`
- `README.md`
- Archivos de configuración (`application.yml`)
- Clases base de configuración

### 5.2 Generar Adaptador de Salida

```bash
./gradlew generateOutputAdapter \
  --type=<redis|dynamodb|postgresql|mongodb|httpclient|sqs|kafka> \
  --name=<NombreDelAdaptador> \
  [--cacheStrategy=<writeThrough|writeBack|readThrough>] \
  [--ttl=<segundos>] \
  [--keyPrefix=<prefijo>]
```

**Ejemplo:**
```bash
./gradlew generateOutputAdapter \
  --type=redis \
  --name=HoldCards \
  --cacheStrategy=writeThrough \
  --ttl=3600
```

**Genera:**
- Clase del adaptador: `HoldCardsRedisAdapter.java`
- Interface del puerto: `HoldCardsPort.java` (si no existe)
- Configuración específica
- Test vacío: `HoldCardsRedisAdapterTest.java`
- Actualiza `.cleanarch.yml`

### 5.3 Generar Adaptador de Entrada

```bash
./gradlew generateInputAdapter \
  --type=<rest|graphql|sqs|kafka|grpc> \
  --name=<NombreDelAdaptador>
```

**Ejemplo:**
```bash
./gradlew generateInputAdapter \
  --type=rest \
  --name=Payment
```

**Genera:**
- Controller: `PaymentController.java`
- DTOs de request/response
- Configuración específica
- Test vacío: `PaymentControllerTest.java`
- Actualiza `.cleanarch.yml`

### 5.4 Generar Caso de Uso

```bash
./gradlew generateUseCase \
  --name=<NombreDelCasoDeUso> \
  [--input=<TipoEntrada>] \
  [--output=<TipoSalida>]
```

**Ejemplo:**
```bash
./gradlew generateUseCase \
  --name=ProcessPayment \
  --input=PaymentRequest \
  --output=PaymentResponse
```

**Genera:**
- Clase del caso de uso: `ProcessPaymentUseCase.java`
- Interface del puerto de entrada: `ProcessPaymentPort.java`
- Test vacío: `ProcessPaymentUseCaseTest.java`
- Actualiza `.cleanarch.yml`

### 5.5 Generar Entidad de Dominio

```bash
./gradlew generateEntity \
  --name=<NombreEntidad> \
  [--fields="campo1:Tipo1,campo2:Tipo2"]
```

**Ejemplo:**
```bash
./gradlew generateEntity \
  --name=Payment \
  --fields="id:String,amount:BigDecimal,status:PaymentStatus,createdAt:LocalDateTime"
```

**Genera:**
- Clase de entidad: `Payment.java`
- Enums si son necesarios
- Builder pattern (opcional)
- Actualiza `.cleanarch.yml`

### 5.6 Generar Mapper

```bash
./gradlew generateMapper \
  --from=<EntidadOrigen> \
  --to=<EntidadDestino> \
  [--name=<NombreMapper>]
```

**Ejemplo:**
```bash
./gradlew generateMapper \
  --from=Payment \
  --to=PaymentEntity \
  --name=PaymentMapper
```

**Genera:**
- Interface MapStruct: `PaymentMapper.java`
- Configuración de MapStruct
- Test vacío: `PaymentMapperTest.java`
- Actualiza `.cleanarch.yml`

### 5.7 Listar Componentes

```bash
./gradlew listComponents
```

**Muestra:**
- Todos los componentes generados
- Tipo y fecha de creación
- Dependencias entre componentes

---

## 6. Validaciones del Plugin

### 6.1 Validaciones en `initCleanArch`
- ✅ Proyecto vacío (solo archivos de Gradle permitidos)
- ✅ Parámetros válidos (architecture, paradigm, package)
- ✅ Formato de paquete Java válido

### 6.2 Validaciones en generación de componentes
- ✅ `.cleanarch.yml` existe (proyecto inicializado)
- ✅ No duplicar nombres de componentes
- ✅ Nombres válidos (sin caracteres especiales, PascalCase)
- ✅ Tipos de adaptadores soportados
- ✅ Parámetros requeridos según tipo de adaptador
- ✅ Entidades referenciadas existen (para mappers)

### 6.3 Mensajes de Error Claros

```
❌ El proyecto no está vacío.

Archivos encontrados: [src, pom.xml]

La tarea initCleanArch solo puede ejecutarse en un proyecto vacío.
```

```
❌ El adaptador 'HoldCardsRedisAdapter' ya existe.

Componentes existentes:
  - HoldCardsRedisAdapter (output/redis) - creado 2026-01-20

Use un nombre diferente o elimine el adaptador existente.
```

---

## 7. Estructura del Proyecto del Plugin

### Repositorio 1: backend-architecture-design-archetype-generator-core

```
backend-architecture-design-archetype-generator-core/
├── README.md
├── CLEAN_ARCH_GENERATOR_SPEC.md
├── build.gradle
├── settings.gradle
├── gradle/
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/pragma/archetype/generator/
    │   │       ├── CleanArchPlugin.java
    │   │       │
    │   │       ├── extension/
    │   │       │   └── CleanArchExtension.java
    │   │       │
    │   │       ├── tasks/
    │   │       │   ├── InitCleanArchTask.java
    │   │       │   ├── GenerateOutputAdapterTask.java
    │   │       │   ├── GenerateInputAdapterTask.java
    │   │       │   ├── GenerateUseCaseTask.java
    │   │       │   ├── GenerateEntityTask.java
    │   │       │   ├── GenerateMapperTask.java
    │   │       │   └── ListComponentsTask.java
    │   │       │
    │   │       ├── generators/
    │   │       │   ├── hexagonal/
    │   │       │   │   ├── HexagonalProjectGenerator.java
    │   │       │   │   ├── HexagonalAdapterGenerator.java
    │   │       │   │   └── HexagonalUseCaseGenerator.java
    │   │       │   ├── onion/
    │   │       │   │   ├── OnionProjectGenerator.java
    │   │       │   │   ├── OnionAdapterGenerator.java
    │   │       │   │   └── OnionUseCaseGenerator.java
    │   │       │   └── common/
    │   │       │       ├── EntityGenerator.java
    │   │       │       └── MapperGenerator.java
    │   │       │
    │   │       ├── config/
    │   │       │   ├── CleanArchConfig.java
    │   │       │   ├── ConfigReader.java
    │   │       │   ├── TemplateConfig.java
    │   │       │   └── TemplateRepository.java
    │   │       │
    │   │       ├── validators/
    │   │       │   ├── ProjectValidator.java
    │   │       │   ├── ComponentValidator.java
    │   │       │   ├── NameValidator.java
    │   │       │   └── AdapterValidator.java
    │   │       │
    │   │       ├── model/
    │   │       │   ├── AdapterMetadata.java
    │   │       │   ├── AdapterInfo.java
    │   │       │   ├── GeneratedFile.java
    │   │       │   └── ValidationResult.java
    │   │       │
    │   │       └── template/
    │   │           ├── TemplateEngine.java
    │   │           └── TemplateProcessor.java
    │   │
    │   └── resources/
    │       └── META-INF/
    │           └── gradle-plugins/
    │               └── com.pragma.archetype-generator.properties
    │
    └── test/
        └── java/
            └── com/pragma/archetype/generator/
                ├── tasks/
                │   ├── InitCleanArchTaskTest.java
                │   └── GenerateOutputAdapterTaskTest.java
                ├── generators/
                │   └── HexagonalProjectGeneratorTest.java
                ├── validators/
                │   └── ProjectValidatorTest.java
                └── integration/
                    └── PluginIntegrationTest.java
```

### Repositorio 2: backend-architecture-design-archetype-generator-templates

```
backend-architecture-design-archetype-generator-templates/
├── README.md
├── CONTRIBUTING.md
├── LICENSE
├── .gitignore
│
├── .github/
│   └── workflows/
│       ├── validate-templates.yml
│       ├── test-templates.yml
│       └── create-missing-implementation-issues.yml
│
├── templates/
│   │
│   ├── architectures/
│   │   ├── hexagonal/
│   │   │   ├── structure.yml
│   │   │   ├── project/
│   │   │   │   ├── build.gradle.kts.ftl
│   │   │   │   ├── settings.gradle.kts.ftl
│   │   │   │   ├── application.yml.ftl
│   │   │   │   ├── README.md.ftl
│   │   │   │   └── .gitignore.ftl
│   │   │   ├── domain/
│   │   │   │   ├── Entity.java.ftl
│   │   │   │   ├── Port.java.ftl
│   │   │   │   └── UseCase.java.ftl
│   │   │   └── infrastructure/
│   │   │       ├── Config.java.ftl
│   │   │       └── Application.java.ftl
│   │   │
│   │   └── onion/
│   │       ├── structure.yml
│   │       ├── project/
│   │       ├── core/
│   │       └── infrastructure/
│   │
│   └── frameworks/
│       │
│       ├── spring/
│       │   ├── metadata.yml
│       │   │
│       │   ├── reactive/
│       │   │   ├── metadata.yml
│       │   │   ├── project/
│       │   │   ├── adapters/
│       │   │   │   ├── input/
│       │   │   │   │   ├── rest/
│       │   │   │   │   ├── kafka/
│       │   │   │   │   └── index.json
│       │   │   │   └── output/
│       │   │   │       ├── redis/
│       │   │   │       ├── dynamodb/
│       │   │   │       ├── postgresql/
│       │   │   │       ├── kafka/
│       │   │   │       └── index.json
│       │   │   └── usecase/
│       │   │
│       │   └── imperative/
│       │       ├── metadata.yml
│       │       ├── project/
│       │       ├── adapters/
│       │       └── usecase/
│       │
│       └── quarkus/
│           ├── metadata.yml
│           ├── reactive/
│           └── imperative/
│
├── examples/
│   ├── spring-reactive-hexagonal/
│   ├── spring-imperative-onion/
│   ├── quarkus-reactive-hexagonal/
│   └── README.md
│
├── tests/
│   ├── validate-all.sh
│   ├── test-spring-reactive.sh
│   ├── test-quarkus-reactive.sh
│   └── README.md
│
└── docs/
    ├── template-syntax.md
    ├── creating-adapters.md
    ├── creating-frameworks.md
    ├── metadata-schema.md
    └── testing-guide.md
```

### Repositorio 3: backend-architecture-design-site-docs

```
backend-architecture-design-site-docs/
├── README.md
├── package.json
├── package-lock.json
├── docusaurus.config.js
├── sidebars.js
├── babel.config.js
├── .gitignore
│
├── docs/
│   ├── intro.md
│   ├── getting-started/
│   │   ├── installation.md
│   │   ├── quick-start.md
│   │   └── first-project.md
│   ├── guides/
│   │   ├── architectures/
│   │   ├── frameworks/
│   │   └── adapters/
│   ├── reference/
│   │   ├── commands.md
│   │   ├── configuration.md
│   │   └── metadata-schema.md
│   ├── contributing/
│   │   ├── overview.md
│   │   ├── creating-adapters.md
│   │   ├── creating-frameworks.md
│   │   └── testing.md
│   └── api/
│       └── plugin-api.md
│
├── blog/
│   ├── 2026-01-25-announcing-v1.md
│   └── authors.yml
│
├── src/
│   ├── components/
│   │   ├── HomepageFeatures/
│   │   └── AdapterMatrix/
│   ├── css/
│   │   └── custom.css
│   └── pages/
│       ├── index.js
│       └── index.module.css
│
└── static/
    ├── img/
    │   ├── logo.svg
    │   └── favicon.ico
    └── examples/
        └── sample-projects/
```

---

## 8. Plan de Implementación

### Fase 1: Core del Plugin (2-3 semanas)


**Objetivos:**
- ✅ Estructura base del plugin de Gradle
- ✅ Tarea `initCleanArch` funcional
- ✅ Generación de `.cleanarch.yml`
- ✅ Soporte para arquitectura hexagonal + paradigma reactivo (caso base)
- ✅ Validación de proyecto vacío
- ✅ Generación de estructura de carpetas completa
- ✅ Actualización de `build.gradle.kts` con dependencias

**Entregables:**
- Plugin publicable en repositorio Maven
- Documentación básica de uso
- Ejemplo funcional de proyecto generado

### Fase 2: Generadores Básicos (2-3 semanas)

**Objetivos:**
- ✅ `generateUseCase` - Casos de uso básicos
- ✅ `generateEntity` - Entidades de dominio
- ✅ `generateOutputAdapter` - Redis y DynamoDB
- ✅ `generateInputAdapter` - REST controllers
- ✅ Generación automática de tests vacíos
- ✅ Validación de no duplicados
- ✅ Actualización automática de `.cleanarch.yml`

**Entregables:**
- Generadores funcionales para componentes básicos
- Templates para adaptadores Redis, DynamoDB, REST
- Tests del plugin
- Documentación de comandos

### Fase 3: Expansión de Arquitecturas (2 semanas)

**Objetivos:**
- ✅ Soporte para arquitectura Onion
- ✅ Soporte para paradigma imperativo
- ✅ Templates para ambas combinaciones:
  - Hexagonal + Imperativo
  - Onion + Reactivo
  - Onion + Imperativo

**Entregables:**
- Generadores para arquitectura Onion
- Templates imperativos (JPA, RestTemplate)
- Ejemplos de proyectos para cada combinación
- Documentación de diferencias entre arquitecturas

### Fase 4: Más Adaptadores (2-3 semanas)

**Objetivos:**
- ✅ Adaptadores de salida adicionales:
  - PostgreSQL/MySQL (JPA y R2DBC)
  - MongoDB
  - HTTP Clients
  - SQS Producer
  - Kafka Producer
- ✅ Adaptadores de entrada adicionales:
  - SQS Consumer
  - Kafka Consumer
  - GraphQL (opcional)
  - gRPC (opcional)

**Entregables:**
- Templates para todos los adaptadores
- Configuraciones específicas por adaptador
- Documentación de parámetros por tipo

### Fase 5: Mappers y Entidades Avanzadas (1-2 semanas)

**Objetivos:**
- ✅ `generateMapper` con MapStruct
- ✅ Generación automática de mappers bidireccionales
- ✅ Soporte para campos complejos en entidades
- ✅ Generación de enums
- ✅ Builder pattern en entidades

**Entregables:**
- Generador de mappers funcional
- Integración con MapStruct
- Documentación de uso de mappers

### Fase 6: Refinamiento y Calidad (2 semanas)

**Objetivos:**
- ✅ Validaciones avanzadas
- ✅ Mensajes de error mejorados
- ✅ `listComponents` con información detallada
- ✅ Tests completos del plugin (cobertura >80%)
- ✅ Documentación completa
- ✅ Ejemplos de uso
- ✅ CI/CD para publicación automática

**Entregables:**
- Plugin estable y testeado
- Documentación completa (README, Wiki)
- Guías de uso y mejores prácticas
- Pipeline de CI/CD
- Publicación en Maven Central o repositorio público

---

## 9. Tecnologías y Dependencias

### 9.1 Plugin
- **Lenguaje**: Java (para el plugin)
- **Build Tool**: Gradle 8.x
- **Testing**: JUnit 5, Mockito
- **Templates**: Freemarker (para generación de código)

### 9.2 Proyectos Generados
- **Java**: 17+
- **Spring Boot**: 3.2.x
- **Spring WebFlux**: Para paradigma reactivo
- **Spring MVC**: Para paradigma imperativo
- **R2DBC**: Para bases de datos reactivas
- **JPA/Hibernate**: Para bases de datos imperativas
- **MapStruct**: Para mappers
- **Lombok**: Opcional (para reducir boilerplate)
- **JUnit 5**: Para tests

---

## 10. Publicación y Distribución

### 10.1 Repositorio Maven
- **Opción 1**: Maven Central (público, recomendado)
- **Opción 2**: Repositorio público propio
- **Opción 3**: GitHub Packages

### 10.2 Documentación con Docusaurus

**Sitio de documentación:** `https://docs.clean-arch-generator.com`

**Estructura:**
```
docs-site/
├── docs/
│   ├── getting-started/
│   │   ├── installation.md
│   │   ├── quick-start.md
│   │   └── first-project.md
│   ├── guides/
│   │   ├── architectures/
│   │   │   ├── hexagonal.md
│   │   │   └── onion.md
│   │   ├── frameworks/
│   │   │   ├── spring-reactive.md
│   │   │   ├── spring-imperative.md
│   │   │   ├── quarkus-reactive.md
│   │   │   └── quarkus-imperative.md
│   │   └── adapters/
│   │       ├── redis.md
│   │       ├── kafka.md
│   │       ├── dynamodb.md
│   │       └── postgresql.md
│   ├── reference/
│   │   ├── commands.md
│   │   ├── configuration.md
│   │   └── metadata-schema.md
│   ├── contributing/
│   │   ├── overview.md
│   │   ├── creating-adapters.md
│   │   ├── creating-frameworks.md
│   │   └── testing.md
│   └── api/
│       └── plugin-api.md
├── blog/
│   ├── 2026-01-22-announcing-v1.md
│   └── 2026-02-15-kafka-adapter.md
├── src/
│   ├── components/
│   └── pages/
├── static/
│   ├── img/
│   └── examples/
├── docusaurus.config.js
└── package.json
```

**Características de Docusaurus:**
- ✅ Búsqueda integrada (Algolia)
- ✅ Versionado de documentación
- ✅ Syntax highlighting para código
- ✅ Playground interactivo
- ✅ Blog integrado
- ✅ Responsive y rápido
- ✅ SEO optimizado
- ✅ Dark mode

**Ejemplo de página de adaptador:**
```markdown
---
id: kafka-adapter
title: Kafka Adapter
sidebar_label: Kafka
---

# Apache Kafka Adapter

Producer y Consumer para Apache Kafka con Spring Cloud Stream o Quarkus.

## Disponibilidad

| Framework | Reactive | Imperative |
|-----------|----------|------------|
| Spring    | ✅ v1.0.0 | ⏳ Pendiente |
| Quarkus   | ⏳ Pendiente | ⏳ Pendiente |

:::info Contribuye
¿Quieres implementar las versiones faltantes? 
[Ver guía de contribución](../contributing/creating-adapters)
:::

## Instalación

```bash
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events
```

## Parámetros

### Requeridos
- `name`: Nombre del adaptador
- `topic`: Nombre del topic de Kafka

### Opcionales
- `groupId`: Consumer group ID (default: `default-group`)
- `partitions`: Número de particiones (default: `3`)

## Ejemplos

### Producer básico
```bash
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events
```

### Consumer con grupo personalizado
```bash
./gradlew generateInputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events \
  --groupId=payment-service-group
```

## Código generado

<Tabs>
<TabItem value="adapter" label="Adapter">

```java
@Component
@RequiredArgsConstructor
public class PaymentEventsKafkaProducer implements PaymentEventsPort {
    // ...
}
```

</TabItem>
<TabItem value="config" label="Config">

```java
@Configuration
public class KafkaConfig {
    // ...
}
```

</TabItem>
</Tabs>

## Configuración

```yaml
spring:
  cloud:
    stream:
      kafka:
        binder:
          brokers: localhost:9092
      bindings:
        payment-events-out-0:
          destination: payment-events
```

## Testing

```java
@SpringBootTest
@Testcontainers
class PaymentEventsKafkaProducerTest {
    // ...
}
```
```

### 10.3 Versionado
- Semantic Versioning (SemVer): `MAJOR.MINOR.PATCH`
- Ejemplo: `1.0.0`, `1.1.0`, `1.1.1`

### 10.4 Actualización
Los desarrolladores actualizan cambiando la versión en `build.gradle.kts`:

```kotlin
plugins {
    id("com.pragma.archetype-generator") version "1.1.0"
}
```

---

## 11. Documentación para Usuarios

### 11.1 README Principal
- Introducción y propósito
- Instalación rápida
- Ejemplo completo de uso
- Link a documentación detallada

### 11.2 Wiki/Docs
- Guía de inicio rápido
- Referencia de comandos
- Tipos de adaptadores soportados
- Parámetros por tipo de adaptador
- Ejemplos de uso avanzado
- Troubleshooting
- FAQ

### 11.3 Ejemplos
- Proyecto hexagonal reactivo completo
- Proyecto hexagonal imperativo completo
- Proyecto onion reactivo completo
- Proyecto onion imperativo completo

---

## 12. Consideraciones Adicionales

### 12.1 Extensibilidad
- Permitir templates personalizados (futuro)
- Plugin de plugins para adaptadores custom (futuro)
- Configuración de naming conventions (futuro)

### 12.2 Integración con IDEs
- IntelliJ IDEA: Reconocimiento automático de tareas
- VS Code: Extensión para autocompletado (futuro)

### 12.3 Migraciones
- Script para migrar de versiones antiguas del plugin
- Changelog detallado de cambios breaking

### 12.4 Comunidad
- GitHub Issues para bugs y features
- Discussions para preguntas
- Contributing guide para colaboradores

---

## 13. Métricas de Éxito

### 13.1 Adopción
- Número de proyectos generados
- Número de descargas del plugin
- Feedback de desarrolladores

### 13.2 Calidad
- Cobertura de tests >80%
- Cero bugs críticos en producción
- Tiempo de generación <10 segundos

### 13.3 Productividad
- Reducción de tiempo de setup: de 2-3 horas a 5 minutos
- Reducción de errores de estructura: 100%
- Satisfacción de desarrolladores: >90%

---

## 14. Riesgos y Mitigaciones

### 14.1 Riesgos Técnicos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Incompatibilidad con versiones de Spring Boot | Media | Alto | Testear con múltiples versiones, documentar compatibilidad |
| Templates desactualizados | Alta | Medio | CI/CD que valide templates con cada release de Spring |
| Conflictos de nombres | Media | Bajo | Validaciones estrictas, mensajes claros |
| Performance en proyectos grandes | Baja | Medio | Generación incremental, caché de templates |

### 14.2 Riesgos de Adopción

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Resistencia al cambio | Media | Alto | Documentación clara, ejemplos, capacitación |
| Curva de aprendizaje | Baja | Medio | Guías paso a paso, videos tutoriales |
| Falta de soporte | Baja | Alto | Equipo dedicado, documentación exhaustiva |

---

## 15. Próximos Pasos Inmediatos

### Semana 1-2: Setup Inicial
1. ✅ Crear repositorio del proyecto
2. ✅ Configurar estructura base del plugin
3. ✅ Definir templates iniciales (hexagonal + reactivo)
4. ✅ Implementar `CleanArchPlugin.kt` base
5. ✅ Implementar `InitCleanArchTask.kt`

### Semana 3-4: Primera Versión Funcional
1. ✅ Generación completa de estructura hexagonal reactiva
2. ✅ Generación de `.cleanarch.yml`
3. ✅ Validación de proyecto vacío
4. ✅ Tests básicos
5. ✅ Publicar versión `0.1.0-SNAPSHOT` en repositorio de pruebas

### Semana 5-6: Primeros Generadores
1. ✅ Implementar `generateUseCase`
2. ✅ Implementar `generateEntity`
3. ✅ Implementar `generateOutputAdapter` (Redis)
4. ✅ Tests de generadores
5. ✅ Documentación básica

---

## 16. Contacto y Recursos

### Repositorio
- GitHub: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core`

### Documentación
- Wiki: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core/wiki`
- Ejemplos: `https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/examples`

### Soporte
- Issues: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core/issues`
- Discussions: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core/discussions`

---

## Apéndices

### A. Ejemplo Completo de Uso

```bash
# 1. Crear proyecto
mkdir payment-service
cd payment-service

# 2. Inicializar
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.pragma.archetype-generator") version "1.0.0"
}
EOF

./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --package=com.company.payment

# 3. Generar entidad
./gradlew generateEntity \
  --name=Payment \
  --fields="id:String,amount:BigDecimal,status:PaymentStatus"

# 4. Generar caso de uso
./gradlew generateUseCase \
  --name=ProcessPayment \
  --input=PaymentRequest \
  --output=PaymentResponse

# 5. Generar adaptador de salida (Redis)
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache \
  --cacheStrategy=writeThrough

# 6. Generar adaptador de salida (DynamoDB)
./gradlew generateOutputAdapter \
  --type=dynamodb \
  --name=PaymentRepository

# 7. Generar adaptador de entrada (REST)
./gradlew generateInputAdapter \
  --type=rest \
  --name=Payment

# 8. Generar mapper
./gradlew generateMapper \
  --from=Payment \
  --to=PaymentEntity

# 9. Ver componentes generados
./gradlew listComponents

# 10. Ejecutar aplicación
./gradlew bootRun
```

### B. Estructura Generada Completa

```
payment-service/
├── .cleanarch.yml
├── .gitignore
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── gradlew
├── gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/company/payment/
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Payment.java
    │   │   │   │   └── PaymentStatus.java
    │   │   │   ├── port/
    │   │   │   │   ├── in/
    │   │   │   │   │   └── ProcessPaymentPort.java
    │   │   │   │   └── out/
    │   │   │   │       ├── PaymentCachePort.java
    │   │   │   │       └── PaymentRepositoryPort.java
    │   │   │   └── usecase/
    │   │   │       └── ProcessPaymentUseCase.java
    │   │   └── infrastructure/
    │   │       ├── adapter/
    │   │       │   ├── in/
    │   │       │   │   └── rest/
    │   │       │   │       ├── PaymentController.java
    │   │       │   │       ├── dto/
    │   │       │   │       │   ├── PaymentRequest.java
    │   │       │   │       │   └── PaymentResponse.java
    │   │       │   │       └── mapper/
    │   │       │   │           └── PaymentDtoMapper.java
    │   │       │   └── out/
    │   │       │       ├── redis/
    │   │       │       │   ├── PaymentCacheRedisAdapter.java
    │   │       │       │   └── config/
    │   │       │       │       └── RedisConfig.java
    │   │       │       └── dynamodb/
    │   │       │           ├── PaymentRepositoryDynamoDbAdapter.java
    │   │       │           ├── entity/
    │   │       │           │   └── PaymentEntity.java
    │   │       │           ├── mapper/
    │   │       │           │   └── PaymentMapper.java
    │   │       │           └── config/
    │   │       │               └── DynamoDbConfig.java
    │   │       └── config/
    │   │           └── ApplicationConfig.java
    │   └── resources/
    │       ├── application.yml
    │       └── application-local.yml
    └── test/
        └── java/com/company/payment/
            ├── domain/
            │   └── usecase/
            │       └── ProcessPaymentUseCaseTest.java
            └── infrastructure/
                └── adapter/
                    ├── in/
                    │   └── rest/
                    │       └── PaymentControllerTest.java
                    └── out/
                        ├── redis/
                        │   └── PaymentCacheRedisAdapterTest.java
                        └── dynamodb/
                            └── PaymentRepositoryDynamoDbAdapterTest.java
```

---

**Documento creado:** 2026-01-22  
**Versión:** 1.0  
**Estado:** Especificación aprobada


---

## C. Gestión de Templates y Escalabilidad

### C.1 ¿Dónde están los templates?

Los templates están **empaquetados dentro del plugin** como recursos:

```
plugin/src/main/resources/
└── templates/
    ├── frameworks/
    │   ├── spring/
    │   │   ├── reactive/
    │   │   │   ├── adapters/
    │   │   │   │   ├── redis/
    │   │   │   │   │   ├── Adapter.java.ftl
    │   │   │   │   │   ├── Config.java.ftl
    │   │   │   │   │   └── Test.java.ftl
    │   │   │   │   ├── dynamodb/
    │   │   │   │   ├── postgresql/
    │   │   │   │   └── rest/
    │   │   │   └── usecase/
    │   │   │       └── UseCase.java.ftl
    │   │   └── imperative/
    │   │       ├── adapters/
    │   │       └── usecase/
    │   └── quarkus/                    # Fase 4+
    │       ├── reactive/               # Mutiny
    │       │   ├── adapters/
    │       │   └── usecase/
    │       └── imperative/             # RESTEasy
    │           ├── adapters/
    │           └── usecase/
    ├── architectures/
    │   ├── hexagonal/
    │   │   └── structure.yml
    │   └── onion/
    │       └── structure.yml
    └── common/
        ├── build.gradle.kts.ftl
        ├── application.yml.ftl
        └── README.md.ftl
```

### C.2 Ejemplo de template (Freemarker)

**Spring Reactive - Redis Adapter:**
```java
// templates/frameworks/spring/reactive/adapters/redis/Adapter.java.ftl
package ${package}.infrastructure.adapter.out.redis;

import ${package}.domain.port.out.${name}Port;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ${name}RedisAdapter implements ${name}Port {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "${keyPrefix}";
    private static final long TTL_SECONDS = ${ttl};
    
    <#if cacheStrategy == "writeThrough">
    @Override
    public Mono<Void> save(String key, Object value) {
        return redisTemplate.opsForValue()
            .set(KEY_PREFIX + key, value, Duration.ofSeconds(TTL_SECONDS))
            .then();
    }
    </#if>
    
    @Override
    public Mono<Object> get(String key) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + key);
    }
}
```

**Quarkus Reactive - Redis Adapter:**
```java
// templates/frameworks/quarkus/reactive/adapters/redis/Adapter.java.ftl
package ${package}.infrastructure.adapter.out.redis;

import ${package}.domain.port.out.${name}Port;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ${name}RedisAdapter implements ${name}Port {
    
    private final ReactiveRedisDataSource redis;
    private static final String KEY_PREFIX = "${keyPrefix}";
    
    @Override
    public Uni<Void> save(String key, Object value) {
        return redis.value(String.class)
            .set(KEY_PREFIX + key, value.toString())
            .replaceWithVoid();
    }
    
    @Override
    public Uni<Object> get(String key) {
        return redis.value(String.class)
            .get(KEY_PREFIX + key);
    }
}
```

### C.3 Cómo se procesan los templates

```kotlin
// TemplateEngine.kt
class TemplateEngine {
    private val configuration = Configuration(Configuration.VERSION_2_3_31).apply {
        setClassForTemplateLoading(this::class.java, "/templates")
        defaultEncoding = "UTF-8"
    }
    
    fun process(templatePath: String, data: Map<String, Any>): String {
        val template = configuration.getTemplate(templatePath)
        val writer = StringWriter()
        template.process(data, writer)
        return writer.toString()
    }
}

// RedisAdapterGenerator.kt
class RedisAdapterGenerator(
    private val templateEngine: TemplateEngine,
    private val framework: String,
    private val paradigm: String
) {
    fun generate(config: CleanArchConfig, params: AdapterParams): GeneratedFile {
        val templatePath = "frameworks/${framework}/${paradigm}/adapters/redis/Adapter.java.ftl"
        
        val data = mapOf(
            "package" to config.basePackage,
            "name" to params.name,
            "keyPrefix" to params.keyPrefix,
            "ttl" to params.ttl,
            "cacheStrategy" to params.cacheStrategy
        )
        
        val code = templateEngine.process(templatePath, data)
        
        return GeneratedFile(
            path = "${config.outputPath}/infrastructure/adapter/out/redis/${params.name}RedisAdapter.java",
            content = code
        )
    }
}
```

### C.4 Patrón Strategy para frameworks

```kotlin
// FrameworkGenerator.kt (interface)
interface FrameworkGenerator {
    fun generateAdapter(config: CleanArchConfig, params: AdapterParams): List<GeneratedFile>
    fun generateUseCase(config: CleanArchConfig, params: UseCaseParams): List<GeneratedFile>
    fun generateBuildFile(config: CleanArchConfig): GeneratedFile
    fun getDependencies(config: CleanArchConfig): List<Dependency>
}

// SpringReactiveGenerator.kt
class SpringReactiveGenerator(
    private val templateEngine: TemplateEngine
) : FrameworkGenerator {
    
    override fun generateAdapter(config: CleanArchConfig, params: AdapterParams): List<GeneratedFile> {
        return when (params.type) {
            "redis" -> RedisAdapterGenerator(templateEngine, "spring", "reactive").generate(config, params)
            "dynamodb" -> DynamoDbAdapterGenerator(templateEngine, "spring", "reactive").generate(config, params)
            else -> throw UnsupportedAdapterException(params.type)
        }
    }
    
    override fun getDependencies(config: CleanArchConfig): List<Dependency> {
        return listOf(
            Dependency("org.springframework.boot", "spring-boot-starter-webflux"),
            Dependency("org.springframework.boot", "spring-boot-starter-data-r2dbc"),
            Dependency("org.springframework.boot", "spring-boot-starter-data-redis-reactive")
        )
    }
}

// QuarkusReactiveGenerator.kt
class QuarkusReactiveGenerator(
    private val templateEngine: TemplateEngine
) : FrameworkGenerator {
    
    override fun generateAdapter(config: CleanArchConfig, params: AdapterParams): List<GeneratedFile> {
        return when (params.type) {
            "redis" -> RedisAdapterGenerator(templateEngine, "quarkus", "reactive").generate(config, params)
            "dynamodb" -> DynamoDbAdapterGenerator(templateEngine, "quarkus", "reactive").generate(config, params)
            else -> throw UnsupportedAdapterException(params.type)
        }
    }
    
    override fun getDependencies(config: CleanArchConfig): List<Dependency> {
        return listOf(
            Dependency("io.quarkus", "quarkus-resteasy-reactive"),
            Dependency("io.quarkus", "quarkus-hibernate-reactive-panache"),
            Dependency("io.quarkus", "quarkus-redis-client")
        )
    }
}

// GeneratorFactory.kt
class GeneratorFactory(private val templateEngine: TemplateEngine) {
    
    fun create(framework: String, paradigm: String): FrameworkGenerator {
        return when (framework to paradigm) {
            "spring" to "reactive" -> SpringReactiveGenerator(templateEngine)
            "spring" to "imperative" -> SpringImperativeGenerator(templateEngine)
            "quarkus" to "reactive" -> QuarkusReactiveGenerator(templateEngine)
            "quarkus" to "imperative" -> QuarkusImperativeGenerator(templateEngine)
            else -> throw UnsupportedFrameworkException("$framework + $paradigm")
        }
    }
    
    fun getSupportedFrameworks(): List<String> = listOf("spring", "quarkus")
    fun getSupportedParadigms(): List<String> = listOf("reactive", "imperative")
}
```

### C.5 Agregar Quarkus - Proceso

**Paso 1: Crear templates de Quarkus**
```
plugin/src/main/resources/templates/frameworks/quarkus/
├── reactive/
│   ├── adapters/
│   │   ├── redis/
│   │   │   ├── Adapter.java.ftl
│   │   │   ├── Config.java.ftl
│   │   │   └── Test.java.ftl
│   │   ├── dynamodb/
│   │   └── rest/
│   └── usecase/
│       └── UseCase.java.ftl
└── imperative/
    ├── adapters/
    └── usecase/
```

**Paso 2: Implementar generadores**
```kotlin
// QuarkusReactiveGenerator.kt
// QuarkusImperativeGenerator.kt
```

**Paso 3: Registrar en factory**
```kotlin
class GeneratorFactory {
    fun create(framework: String, paradigm: String): FrameworkGenerator {
        return when (framework to paradigm) {
            "spring" to "reactive" -> SpringReactiveGenerator(templateEngine)
            "spring" to "imperative" -> SpringImperativeGenerator(templateEngine)
            "quarkus" to "reactive" -> QuarkusReactiveGenerator(templateEngine)  // ← Nuevo
            "quarkus" to "imperative" -> QuarkusImperativeGenerator(templateEngine)  // ← Nuevo
            else -> throw UnsupportedFrameworkException("$framework + $paradigm")
        }
    }
}
```

**Paso 4: Actualizar validaciones**
```kotlin
class FrameworkValidator {
    private val supportedFrameworks = setOf("spring", "quarkus")  // ← Agregar quarkus
    
    fun validate(framework: String) {
        if (framework !in supportedFrameworks) {
            throw ValidationException("Framework no soportado: $framework. Soportados: $supportedFrameworks")
        }
    }
}
```

**Paso 5: Publicar nueva versión**
```
v1.0.0 - Spring solamente
v1.1.0 - Spring + Quarkus  ← Nueva versión
```

### C.6 Uso con Quarkus

```bash
# Crear proyecto con Quarkus
mkdir payment-service
cd payment-service

cat > build.gradle.kts << 'EOF'
plugins {
    id("com.pragma.archetype-generator") version "1.1.0"  // Versión con Quarkus
}
EOF

./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --framework=quarkus \
  --package=com.company.payment

# Generar adaptador Redis con Mutiny
./gradlew generateOutputAdapter \
  --type=redis \
  --name=HoldCards \
  --cacheStrategy=writeThrough

# El código generado usa Uni<> y Mutiny en lugar de Mono<> y Reactor
```

### C.7 Ventajas de esta arquitectura

✅ **Escalable**: Agregar frameworks es solo agregar templates + generador
✅ **Mantenible**: Cada framework tiene su propio generador
✅ **Testeable**: Cada generador se puede testear independientemente
✅ **Versionable**: Templates versionados con el plugin
✅ **Extensible**: Fácil agregar Micronaut, Helidon, etc.

### C.8 Roadmap de frameworks

**Fase 1 (MVP)**: Spring Boot
- ✅ Spring Reactive (WebFlux)
- ✅ Spring Imperative (MVC)

**Fase 4**: Quarkus
- ✅ Quarkus Reactive (Mutiny)
- ✅ Quarkus Imperative (RESTEasy)

**Fase 5+** (Futuro):
- Micronaut Reactive
- Micronaut Imperative
- Helidon
- Vert.x

### C.9 Arquitectura de dos repositorios (RECOMENDADO)

Para máxima escalabilidad y colaboración, se recomienda separar en dos repositorios:

**Repositorio 1: `clean-arch-plugin`**
- Lógica del plugin
- Motor de generación
- Descarga y caché de templates

**Repositorio 2: `backend-architecture-design-archetype-generator-templates`**
- Templates organizados
- Contribuciones de la comunidad
- Versionado independiente

#### Configuración en `.cleanarch.yml`

```yaml
# Modo producción (default)
templates:
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  version: 1.2.0  # Tag específico (opcional)
  cache: true

# Modo developer (para contribuidores)
templates:
  mode: developer
  repository: https://github.com/juan/backend-architecture-design-archetype-generator-templates  # Fork
  branch: feature/kafka-adapter  # Rama de desarrollo
  localPath: /Users/juan/backend-architecture-design-archetype-generator-templates  # O path local
  cache: false
```

#### Flujo de contribución

```bash
# 1. Fork y clonar templates
git clone https://github.com/juan/backend-architecture-design-archetype-generator-templates
cd backend-architecture-design-archetype-generator-templates
git checkout -b feature/kafka-adapter

# 2. Crear templates
mkdir -p templates/frameworks/spring/reactive/adapters/kafka
# ... crear .ftl files y metadata.yml

# 3. Probar localmente
cd ~/mi-proyecto
# Configurar .cleanarch.yml en modo developer
./gradlew generateOutputAdapter --type=kafka --name=PaymentEvents

# 4. PR al repositorio principal
git push origin feature/kafka-adapter
# Crear Pull Request

# 5. Después del merge, TODOS pueden usar el nuevo adaptador
# Sin actualizar el plugin!
```

#### Ventajas

✅ **Escalable**: Agregar adaptadores no requiere nueva versión del plugin
✅ **Colaborativo**: Cualquiera puede contribuir templates
✅ **Rápido**: Ciclo de desarrollo más corto
✅ **Flexible**: Modo developer para experimentar
✅ **Mantenible**: Templates separados del código
✅ **Testeable**: CI/CD valida templates automáticamente

Ver Apéndice D para implementación completa.

---


## D. Implementación de Repositorio de Templates Separado

### D.1 Estructura del repositorio `backend-architecture-design-archetype-generator-templates`

```
backend-architecture-design-archetype-generator-templates/
├── README.md
├── CONTRIBUTING.md
├── LICENSE
├── .github/
│   └── workflows/
│       ├── validate-templates.yml
│       ├── test-templates.yml
│       └── publish-release.yml
├── templates/
│   ├── frameworks/
│   │   ├── spring/
│   │   │   ├── reactive/
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── redis/
│   │   │   │   │   │   ├── Adapter.java.ftl
│   │   │   │   │   │   ├── Config.java.ftl
│   │   │   │   │   │   ├── Test.java.ftl
│   │   │   │   │   │   └── metadata.yml
│   │   │   │   │   ├── kafka/
│   │   │   │   │   │   ├── Producer.java.ftl
│   │   │   │   │   │   ├── Consumer.java.ftl
│   │   │   │   │   │   ├── Config.java.ftl
│   │   │   │   │   │   ├── Test.java.ftl
│   │   │   │   │   │   └── metadata.yml
│   │   │   │   │   ├── dynamodb/
│   │   │   │   │   ├── postgresql/
│   │   │   │   │   └── index.json
│   │   │   │   └── usecase/
│   │   │   └── imperative/
│   │   └── quarkus/
│   └── architectures/
│       ├── hexagonal/
│       └── onion/
├── examples/
│   ├── kafka-producer/
│   │   └── expected-output/
│   └── redis-cache/
│       └── expected-output/
├── tests/
│   ├── validate-kafka.sh
│   └── validate-redis.sh
└── docs/
    ├── creating-templates.md
    ├── testing-templates.md
    └── metadata-schema.md
```

### D.2 Metadata de adaptador

```yaml
# templates/frameworks/spring/reactive/adapters/kafka/metadata.yml
name: kafka
displayName: Apache Kafka
description: Producer y Consumer para Apache Kafka con Spring Cloud Stream
framework: spring
paradigm: reactive
type: both  # input, output, o both
version: 1.0.0
author: Juan Pérez <juan.perez@company.com>
maintainers:
  - juan.perez@company.com
  - maria.garcia@company.com

parameters:
  required:
    - name: name
      type: string
      description: Nombre del adaptador
      example: PaymentEvents
    - name: topic
      type: string
      description: Nombre del topic de Kafka
      example: payment-events
  optional:
    - name: groupId
      type: string
      description: Consumer group ID
      default: default-group
      example: payment-service-group
    - name: partitions
      type: integer
      description: Número de particiones
      default: 3
      min: 1
      max: 100
    - name: replicationFactor
      type: integer
      description: Factor de replicación
      default: 1

dependencies:
  gradle:
    - groupId: org.springframework.cloud
      artifactId: spring-cloud-stream
      version: 4.0.0
    - groupId: org.springframework.cloud
      artifactId: spring-cloud-stream-binder-kafka
      version: 4.0.0
  maven:
    - groupId: org.springframework.cloud
      artifactId: spring-cloud-stream
      version: 4.0.0

files:
  - name: Producer.java.ftl
    output: "{name}KafkaProducer.java"
    description: Kafka producer implementation
  - name: Consumer.java.ftl
    output: "{name}KafkaConsumer.java"
    description: Kafka consumer implementation
  - name: Config.java.ftl
    output: "KafkaConfig.java"
    description: Kafka configuration
  - name: Test.java.ftl
    output: "{name}KafkaProducerTest.java"
    description: Unit tests

examples:
  - name: Simple producer
    description: Producer básico para eventos de pago
    command: |
      ./gradlew generateOutputAdapter \
        --type=kafka \
        --name=PaymentEvents \
        --topic=payment-events
  - name: Consumer with custom group
    description: Consumer con grupo personalizado
    command: |
      ./gradlew generateInputAdapter \
        --type=kafka \
        --name=PaymentEvents \
        --topic=payment-events \
        --groupId=payment-service-group

compatibility:
  plugin: ">=1.0.0"
  spring: ">=3.0.0"
  java: ">=17"

tags:
  - messaging
  - event-driven
  - kafka
  - streaming
```

### D.3 Index de adaptadores

```json
// templates/frameworks/spring/reactive/adapters/index.json
{
  "version": "1.2.0",
  "lastUpdated": "2026-01-22T10:00:00Z",
  "adapters": [
    {
      "name": "redis",
      "displayName": "Redis Cache",
      "description": "Adaptador de caché con Redis",
      "type": "output",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "kafka",
      "displayName": "Apache Kafka",
      "description": "Producer y Consumer para Kafka",
      "type": "both",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "dynamodb",
      "displayName": "AWS DynamoDB",
      "description": "Repositorio con DynamoDB",
      "type": "output",
      "status": "stable",
      "version": "1.0.0"
    },
    {
      "name": "sqs",
      "displayName": "AWS SQS",
      "description": "Producer y Consumer para SQS",
      "type": "both",
      "status": "beta",
      "version": "0.9.0"
    }
  ]
}
```

### D.4 Implementación en el plugin

```kotlin
// TemplateRepository.kt
class TemplateRepository(private val config: TemplateConfig) {
    
    private val cacheDir = File(System.getProperty("user.home"), ".cleanarch/templates-cache")
    private val httpClient = OkHttpClient()
    
    fun loadTemplate(
        framework: String, 
        paradigm: String, 
        type: String, 
        adapterType: String,
        fileName: String
    ): String {
        return when (config.mode) {
            TemplateMode.PRODUCTION -> loadFromRemote(framework, paradigm, type, adapterType, fileName)
            TemplateMode.DEVELOPER -> loadFromDeveloper(framework, paradigm, type, adapterType, fileName)
        }
    }
    
    fun loadMetadata(
        framework: String,
        paradigm: String,
        type: String,
        adapterType: String
    ): AdapterMetadata {
        val metadataContent = loadTemplate(framework, paradigm, type, adapterType, "metadata.yml")
        return parseMetadata(metadataContent)
    }
    
    private fun loadFromRemote(
        framework: String,
        paradigm: String,
        type: String,
        adapterType: String,
        fileName: String
    ): String {
        val templatePath = "templates/frameworks/$framework/$paradigm/$type/$adapterType/$fileName"
        val cacheKey = "${config.version ?: config.branch}/$templatePath"
        
        // 1. Verificar caché local
        if (config.cache) {
            val cached = loadFromCache(cacheKey)
            if (cached != null) {
                logger.debug("Using cached template: $templatePath")
                return cached
            }
        }
        
        // 2. Descargar desde repositorio remoto
        val url = buildRawUrl(config.repository, config.branch, templatePath)
        logger.info("Downloading template from: $url")
        
        val content = downloadFile(url)
        
        // 3. Cachear si está habilitado
        if (config.cache) {
            saveToCache(cacheKey, content)
        }
        
        return content
    }
    
    private fun loadFromDeveloper(
        framework: String,
        paradigm: String,
        type: String,
        adapterType: String,
        fileName: String
    ): String {
        val templatePath = "templates/frameworks/$framework/$paradigm/$type/$adapterType/$fileName"
        
        // Modo developer: usar path local o branch específica
        if (config.localPath != null) {
            logger.info("Using local templates from: ${config.localPath}")
            val localFile = File(config.localPath, templatePath)
            if (!localFile.exists()) {
                throw TemplateNotFoundException("Template not found: ${localFile.absolutePath}")
            }
            return localFile.readText()
        } else {
            // Usar branch específica del repositorio
            logger.info("Using developer branch: ${config.branch}")
            val url = buildRawUrl(config.repository, config.branch, templatePath)
            return downloadFile(url)
        }
    }
    
    fun listAvailableAdapters(framework: String, paradigm: String): List<AdapterInfo> {
        val indexPath = "templates/frameworks/$framework/$paradigm/adapters/index.json"
        val url = buildRawUrl(config.repository, config.branch, indexPath)
        
        val indexContent = downloadFile(url)
        val index = Json.decodeFromString<AdapterIndex>(indexContent)
        
        return index.adapters
    }
    
    private fun buildRawUrl(repo: String, branch: String, path: String): String {
        // Soportar GitHub, GitLab, Bitbucket
        return when {
            repo.contains("github.com") -> {
                val repoPath = repo.removePrefix("https://github.com/")
                "https://raw.githubusercontent.com/$repoPath/$branch/$path"
            }
            repo.contains("gitlab.com") -> {
                "$repo/-/raw/$branch/$path"
            }
            else -> "$repo/raw/$branch/$path"
        }
    }
    
    private fun downloadFile(url: String): String {
        val request = Request.Builder().url(url).build()
        
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw TemplateDownloadException("Failed to download: $url (${response.code})")
            }
            return response.body?.string() ?: throw TemplateDownloadException("Empty response")
        }
    }
    
    private fun loadFromCache(key: String): String? {
        val cacheFile = File(cacheDir, key)
        return if (cacheFile.exists()) cacheFile.readText() else null
    }
    
    private fun saveToCache(key: String, content: String) {
        val cacheFile = File(cacheDir, key)
        cacheFile.parentFile.mkdirs()
        cacheFile.writeText(content)
    }
    
    fun clearCache() {
        cacheDir.deleteRecursively()
        logger.info("Template cache cleared")
    }
}

// TemplateConfig.kt
data class TemplateConfig(
    val mode: TemplateMode = TemplateMode.PRODUCTION,
    val repository: String = "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
    val branch: String = "main",
    val version: String? = null,
    val localPath: String? = null,
    val cache: Boolean = true
) {
    companion object {
        fun fromCleanArchConfig(cleanArchConfig: CleanArchConfig): TemplateConfig {
            val templatesConfig = cleanArchConfig.templates ?: return TemplateConfig()
            
            return TemplateConfig(
                mode = if (templatesConfig.mode == "developer") TemplateMode.DEVELOPER else TemplateMode.PRODUCTION,
                repository = templatesConfig.repository ?: "https://github.com/somospragma/backend-architecture-design-archetype-generator-templates",
                branch = templatesConfig.branch ?: "main",
                version = templatesConfig.version,
                localPath = templatesConfig.localPath,
                cache = templatesConfig.cache ?: true
            )
        }
    }
}

enum class TemplateMode {
    PRODUCTION,
    DEVELOPER
}

// AdapterMetadata.kt
@Serializable
data class AdapterMetadata(
    val name: String,
    val displayName: String,
    val description: String,
    val framework: String,
    val paradigm: String,
    val type: String,
    val version: String,
    val author: String,
    val maintainers: List<String>,
    val parameters: Parameters,
    val dependencies: Dependencies,
    val files: List<FileInfo>,
    val examples: List<Example>,
    val compatibility: Compatibility,
    val tags: List<String>
)

@Serializable
data class Parameters(
    val required: List<Parameter>,
    val optional: List<Parameter>
)

@Serializable
data class Parameter(
    val name: String,
    val type: String,
    val description: String,
    val example: String? = null,
    val default: String? = null,
    val min: Int? = null,
    val max: Int? = null
)
```

### D.5 Comandos del plugin actualizados

```bash
# Listar adaptadores disponibles
./gradlew listAdapters --framework=spring --paradigm=reactive

# Output:
# Available adapters for spring/reactive:
#   redis (v1.0.0) - Redis Cache [stable]
#   kafka (v1.0.0) - Apache Kafka [stable]
#   dynamodb (v1.0.0) - AWS DynamoDB [stable]
#   sqs (v0.9.0) - AWS SQS [beta]

# Ver detalles de un adaptador
./gradlew adapterInfo --type=kafka

# Output:
# Kafka Adapter (v1.0.0)
# Description: Producer y Consumer para Apache Kafka
# Type: both (input/output)
# Author: Juan Pérez
# 
# Required parameters:
#   - name: Nombre del adaptador
#   - topic: Nombre del topic
# 
# Optional parameters:
#   - groupId: Consumer group ID (default: default-group)
#   - partitions: Número de particiones (default: 3)
# 
# Examples:
#   ./gradlew generateOutputAdapter --type=kafka --name=PaymentEvents --topic=payment-events

# Limpiar caché de templates
./gradlew clearTemplateCache

# Actualizar templates (re-descargar)
./gradlew updateTemplates
```

### D.6 CI/CD para el repositorio de templates

```yaml
# .github/workflows/validate-templates.yml
name: Validate Templates

on:
  pull_request:
    paths:
      - 'templates/**'
  push:
    branches:
      - main

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate metadata files
        run: |
          find templates -name "metadata.yml" -exec yamllint {} \;
      
      - name: Validate Freemarker syntax
        run: |
          # Script que valida sintaxis de .ftl
          ./scripts/validate-freemarker.sh
      
      - name: Test template generation
        run: |
          # Genera código de prueba con cada template
          ./scripts/test-templates.sh
      
      - name: Check for duplicates
        run: |
          # Verifica que no haya adaptadores duplicados
          ./scripts/check-duplicates.sh

  test-integration:
    runs-on: ubuntu-latest
    needs: validate
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup test project
        run: |
          mkdir test-project
          cd test-project
          # Crear proyecto de prueba
      
      - name: Test with plugin
        run: |
          # Probar que el plugin puede usar estos templates
          cd test-project
          ./gradlew generateOutputAdapter --type=kafka --name=Test
          ./gradlew build
```

### D.7 Guía de contribución (CONTRIBUTING.md)

```markdown
# Guía de Contribución

## Agregar un nuevo adaptador

### Importante: Contribuciones parciales son bienvenidas

**No necesitas implementar el adaptador para todos los frameworks y paradigmas.**

Puedes contribuir solo para:
- ✅ Spring Reactive únicamente
- ✅ Quarkus Imperative únicamente
- ✅ Cualquier combinación específica

El equipo o la comunidad puede completar las otras versiones después.

### 1. Fork y clonar

```bash
git clone https://github.com/tu-usuario/backend-architecture-design-archetype-generator-templates
cd backend-architecture-design-archetype-generator-templates
git checkout -b feature/kafka-adapter-spring-reactive
```

### 2. Decidir qué implementar

Ejemplo: Quieres agregar adaptador de Kafka solo para **Spring Reactive**

```bash
# Solo crear en la ruta específica
mkdir -p templates/frameworks/spring/reactive/adapters/output/kafka
cd templates/frameworks/spring/reactive/adapters/output/kafka
```

**No es necesario crear:**
- ❌ `templates/frameworks/spring/imperative/adapters/output/kafka`
- ❌ `templates/frameworks/quarkus/reactive/adapters/output/kafka`
- ❌ `templates/frameworks/quarkus/imperative/adapters/output/kafka`

### 3. Crear archivos

Archivos mínimos requeridos:
- `Adapter.java.ftl` - Template del adaptador
- `Config.java.ftl` - Template de configuración
- `Test.java.ftl` - Template de test
- `metadata.yml` - Metadata del adaptador

Archivos opcionales:
- `Producer.java.ftl` - Si tiene producer
- `Consumer.java.ftl` - Si tiene consumer
- `Entity.java.ftl` - Si necesita entidades
- `Mapper.java.ftl` - Si necesita mappers

### 4. Completar metadata.yml

```yaml
name: kafka
displayName: Apache Kafka
description: Producer y Consumer para Apache Kafka
framework: spring          # ← Específico
paradigm: reactive         # ← Específico
type: both
version: 1.0.0
author: Tu Nombre <tu.email@company.com>

# Marcar qué implementaciones existen
implementations:
  spring:
    reactive: true         # ← Esta existe (la que estás creando)
    imperative: false      # ← Esta NO existe aún
  quarkus:
    reactive: false        # ← Esta NO existe aún
    imperative: false      # ← Esta NO existe aún

# ... resto de metadata
```

### 5. Probar localmente

```bash
# En tu proyecto de prueba
cat > .cleanarch.yml << EOF
templates:
  mode: developer
  localPath: /ruta/a/backend-architecture-design-archetype-generator-templates
EOF

# Probar con Spring Reactive (la que implementaste)
./gradlew initCleanArch \
  --architecture=hexagonal \
  --framework=spring \
  --paradigm=reactive

./gradlew generateOutputAdapter --type=kafka --name=PaymentEvents
./gradlew build
./gradlew test
```

### 6. Validar

```bash
# En el repositorio de templates
./scripts/validate-freemarker.sh
./scripts/test-templates.sh spring reactive kafka
```

### 7. Commit y PR

```bash
git add .
git commit -m "Add Kafka adapter template for Spring Reactive"
git push origin feature/kafka-adapter-spring-reactive
```

### 8. Crear Pull Request

Incluir en el PR:
- ✅ Descripción del adaptador
- ✅ Framework y paradigma implementado
- ✅ Ejemplos de uso
- ✅ Screenshots del código generado
- ✅ Marcar en metadata.yml qué implementaciones faltan

**Ejemplo de descripción del PR:**

```markdown
## Kafka Adapter - Spring Reactive

### Implementado
- ✅ Spring Reactive (WebFlux + Spring Cloud Stream)

### Pendiente (puede ser implementado por otros)
- ⏳ Spring Imperative
- ⏳ Quarkus Reactive
- ⏳ Quarkus Imperative

### Características
- Producer con Spring Cloud Stream
- Consumer con Spring Cloud Stream
- Configuración de topics
- Tests con Testcontainers

### Ejemplo de uso
\`\`\`bash
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events
\`\`\`

### Código generado
[Screenshots o ejemplos del código generado]
```

### 9. Después del merge

El adaptador estará disponible **solo para Spring Reactive**.

Si alguien intenta usarlo con otro framework/paradigma:
```bash
./gradlew generateOutputAdapter --type=kafka --name=Test
# Con framework=quarkus, paradigm=reactive

# Error:
❌ Kafka adapter no está disponible para quarkus/reactive
✅ Disponible para: spring/reactive
⏳ Puedes contribuir la implementación para quarkus/reactive
```
```

### D.8 Ventajas finales

| Aspecto | Con templates separados | Con templates en plugin |
|---------|------------------------|-------------------------|
| Agregar adaptador | ✅ Solo PR al repo de templates | ❌ Requiere nueva versión del plugin |
| Tiempo de desarrollo | ✅ Minutos/horas | ❌ Días (build, test, release) |
| Colaboración | ✅ Múltiples personas en paralelo | ⚠️ Conflictos en el código |
| Actualización | ✅ Automática (sin cambiar plugin) | ❌ Usuarios deben actualizar plugin |
| Testing | ✅ CI/CD específico para templates | ⚠️ Mezclado con tests del plugin |
| Versionado | ✅ Independiente | ❌ Acoplado al plugin |
| Contribuciones | ✅ Fácil para externos | ❌ Requiere conocer el plugin |

---

**Actualizado:** 2026-01-22  
**Versión:** 1.2 - Agregada arquitectura de dos repositorios


## E. Estructura Completa del Repositorio de Templates

### E.1 Organización completa

```
backend-architecture-design-archetype-generator-templates/
├── README.md
├── CONTRIBUTING.md
├── LICENSE
├── .github/
│   └── workflows/
│       ├── validate-templates.yml
│       └── test-templates.yml
│
├── templates/
│   │
│   ├── architectures/
│   │   ├── hexagonal/
│   │   │   ├── structure.yml                    # Define estructura de carpetas
│   │   │   ├── project/
│   │   │   │   ├── build.gradle.kts.ftl
│   │   │   │   ├── settings.gradle.kts.ftl
│   │   │   │   ├── application.yml.ftl
│   │   │   │   ├── README.md.ftl
│   │   │   │   └── .gitignore.ftl
│   │   │   ├── domain/
│   │   │   │   ├── Entity.java.ftl
│   │   │   │   ├── Port.java.ftl              # Interface de puerto
│   │   │   │   └── UseCase.java.ftl
│   │   │   └── infrastructure/
│   │   │       ├── Config.java.ftl
│   │   │       └── Application.java.ftl
│   │   │
│   │   └── onion/
│   │       ├── structure.yml
│   │       ├── project/
│   │       │   ├── build.gradle.kts.ftl
│   │       │   ├── settings.gradle.kts.ftl
│   │       │   ├── application.yml.ftl
│   │       │   ├── README.md.ftl
│   │       │   └── .gitignore.ftl
│   │       ├── core/
│   │       │   ├── domain/
│   │       │   │   └── Entity.java.ftl
│   │       │   └── application/
│   │       │       ├── Service.java.ftl
│   │       │       └── Port.java.ftl
│   │       └── infrastructure/
│   │           ├── Config.java.ftl
│   │           └── Application.java.ftl
│   │
│   └── frameworks/
│       │
│       ├── spring/
│       │   ├── metadata.yml                     # Info del framework
│       │   ├── reactive/
│       │   │   ├── metadata.yml
│       │   │   ├── project/
│       │   │   │   ├── build.gradle.kts.ftl   # Dependencias WebFlux
│       │   │   │   ├── application.yml.ftl     # Config reactiva
│       │   │   │   └── Application.java.ftl
│       │   │   ├── adapters/
│       │   │   │   ├── input/
│       │   │   │   │   ├── rest/
│       │   │   │   │   │   ├── Controller.java.ftl
│       │   │   │   │   │   ├── DTO.java.ftl
│       │   │   │   │   │   ├── Mapper.java.ftl
│       │   │   │   │   │   ├── Test.java.ftl
│       │   │   │   │   │   └── metadata.yml
│       │   │   │   │   ├── kafka/
│       │   │   │   │   │   ├── Consumer.java.ftl
│       │   │   │   │   │   ├── Config.java.ftl
│       │   │   │   │   │   ├── Test.java.ftl
│       │   │   │   │   │   └── metadata.yml
│       │   │   │   │   └── index.json
│       │   │   │   └── output/
│       │   │   │       ├── redis/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       ├── dynamodb/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Entity.java.ftl
│       │   │   │       │   ├── Mapper.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       ├── postgresql/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Entity.java.ftl
│       │   │   │       │   ├── Repository.java.ftl
│       │   │   │       │   ├── Mapper.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       ├── kafka/
│       │   │   │       │   ├── Producer.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       └── index.json
│       │   │   └── usecase/
│       │   │       ├── UseCase.java.ftl
│       │   │       ├── Port.java.ftl
│       │   │       ├── Test.java.ftl
│       │   │       └── metadata.yml
│       │   │
│       │   └── imperative/
│       │       ├── metadata.yml
│       │       ├── project/
│       │       │   ├── build.gradle.kts.ftl   # Dependencias Spring MVC
│       │       │   ├── application.yml.ftl     # Config imperativa
│       │       │   └── Application.java.ftl
│       │       ├── adapters/
│       │       │   ├── input/
│       │       │   │   ├── rest/
│       │       │   │   │   ├── Controller.java.ftl
│       │       │   │   │   ├── DTO.java.ftl
│       │       │   │   │   ├── Mapper.java.ftl
│       │       │   │   │   ├── Test.java.ftl
│       │       │   │   │   └── metadata.yml
│       │       │   │   └── index.json
│       │       │   └── output/
│       │       │       ├── redis/
│       │       │       │   ├── Adapter.java.ftl
│       │       │       │   ├── Config.java.ftl
│       │       │       │   ├── Test.java.ftl
│       │       │       │   └── metadata.yml
│       │       │       ├── postgresql/
│       │       │       │   ├── Adapter.java.ftl
│       │       │       │   ├── Entity.java.ftl
│       │       │       │   ├── Repository.java.ftl  # JPA
│       │       │       │   ├── Mapper.java.ftl
│       │       │       │   ├── Config.java.ftl
│       │       │       │   ├── Test.java.ftl
│       │       │       │   └── metadata.yml
│       │       │       └── index.json
│       │       └── usecase/
│       │           ├── UseCase.java.ftl
│       │           ├── Port.java.ftl
│       │           ├── Test.java.ftl
│       │           └── metadata.yml
│       │
│       └── quarkus/
│           ├── metadata.yml
│           ├── reactive/
│           │   ├── metadata.yml
│           │   ├── project/
│           │   │   ├── build.gradle.kts.ftl   # Dependencias Quarkus Reactive
│           │   │   ├── application.properties.ftl
│           │   │   └── Application.java.ftl
│           │   ├── adapters/
│           │   │   ├── input/
│           │   │   │   ├── rest/
│           │   │   │   │   ├── Resource.java.ftl  # Quarkus usa Resource
│           │   │   │   │   ├── DTO.java.ftl
│           │   │   │   │   ├── Mapper.java.ftl
│           │   │   │   │   ├── Test.java.ftl
│           │   │   │   │   └── metadata.yml
│           │   │   │   └── index.json
│           │   │   └── output/
│           │   │       ├── redis/
│           │   │       │   ├── Adapter.java.ftl  # Usa Mutiny
│           │   │       │   ├── Config.java.ftl
│           │   │       │   ├── Test.java.ftl
│           │   │       │   └── metadata.yml
│           │   │       └── index.json
│           │   └── usecase/
│           │       ├── UseCase.java.ftl
│           │       ├── Port.java.ftl
│           │       ├── Test.java.ftl
│           │       └── metadata.yml
│           │
│           └── imperative/
│               ├── metadata.yml
│               ├── project/
│               ├── adapters/
│               └── usecase/
│
├── examples/
│   ├── spring-reactive-hexagonal/
│   │   ├── input/                              # Inputs de ejemplo
│   │   │   └── config.yml
│   │   └── expected-output/                    # Output esperado
│   │       └── src/
│   ├── spring-imperative-onion/
│   ├── quarkus-reactive-hexagonal/
│   └── README.md
│
├── tests/
│   ├── validate-all.sh
│   ├── test-spring-reactive.sh
│   ├── test-quarkus-reactive.sh
│   └── README.md
│
└── docs/
    ├── template-syntax.md                      # Guía de Freemarker
    ├── creating-adapters.md
    ├── creating-frameworks.md
    ├── metadata-schema.md
    └── testing-guide.md
```

### E.2 Ejemplo de template de arquitectura hexagonal

```yaml
# templates/architectures/hexagonal/structure.yml
name: hexagonal
displayName: Hexagonal Architecture
description: Arquitectura de puertos y adaptadores
version: 1.0.0

structure:
  domain:
    path: domain
    description: Lógica de negocio y reglas del dominio
    subfolders:
      model:
        path: model
        description: Entidades del dominio
      port:
        path: port
        description: Interfaces de puertos
        subfolders:
          in:
            path: in
            description: Puertos de entrada (use cases)
          out:
            path: out
            description: Puertos de salida (repositorios, servicios externos)
      usecase:
        path: usecase
        description: Implementación de casos de uso
  
  infrastructure:
    path: infrastructure
    description: Implementaciones técnicas
    subfolders:
      adapter:
        path: adapter
        description: Adaptadores
        subfolders:
          in:
            path: in
            description: Adaptadores de entrada (controllers, consumers)
          out:
            path: out
            description: Adaptadores de salida (repositories, clients)
      config:
        path: config
        description: Configuración de Spring/Quarkus

files:
  - name: Application.java
    path: infrastructure
    template: Application.java.ftl
  - name: ApplicationConfig.java
    path: infrastructure/config
    template: Config.java.ftl
```

### E.3 Ejemplo de template de proyecto Spring Reactive

```kotlin
// templates/frameworks/spring/reactive/project/build.gradle.kts.ftl
plugins {
    id("org.springframework.boot") version "${springBootVersion}"
    id("io.spring.dependency-management") version "1.1.4"
    <#if language == "kotlin">
    kotlin("jvm") version "${kotlinVersion}"
    kotlin("plugin.spring") version "${kotlinVersion}"
    <#else>
    java
    </#if>
}

group = "${groupId}"
version = "${version}"

<#if language == "java">
java {
    sourceCompatibility = JavaVersion.VERSION_${javaVersion}
}
</#if>

repositories {
    mavenCentral()
}

dependencies {
    // Spring WebFlux (Reactive)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // R2DBC (Reactive Database)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    
    // Redis Reactive
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // MapStruct
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    
    // Lombok (opcional)
    <#if useLombok>
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    </#if>
    
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
```

### E.4 Ejemplo de template de adaptador Redis (Spring Reactive)

```java
// templates/frameworks/spring/reactive/adapters/output/redis/Adapter.java.ftl
package ${basePackage}.infrastructure.adapter.out.redis;

import ${basePackage}.domain.model.${entityName};
import ${basePackage}.domain.port.out.${portName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Adaptador de Redis para ${entityName}
 * Estrategia de caché: ${cacheStrategy}
 * 
 * @author Generated by Clean Arch Generator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ${name}RedisAdapter implements ${portName} {
    
    private final ReactiveRedisTemplate<String, ${entityName}> redisTemplate;
    
    private static final String KEY_PREFIX = "${keyPrefix}:";
    private static final Duration TTL = Duration.ofSeconds(${ttl});
    
    <#if cacheStrategy == "writeThrough" || cacheStrategy == "writeBack">
    @Override
    public Mono<Void> save(String key, ${entityName} entity) {
        log.debug("Saving to Redis cache: {}", key);
        return redisTemplate.opsForValue()
            .set(KEY_PREFIX + key, entity, TTL)
            .doOnSuccess(result -> log.debug("Saved to cache: {}", key))
            .doOnError(error -> log.error("Error saving to cache: {}", key, error))
            .then();
    }
    </#if>
    
    <#if cacheStrategy == "readThrough" || cacheStrategy == "writeThrough">
    @Override
    public Mono<${entityName}> get(String key) {
        log.debug("Getting from Redis cache: {}", key);
        return redisTemplate.opsForValue()
            .get(KEY_PREFIX + key)
            .doOnSuccess(result -> {
                if (result != null) {
                    log.debug("Cache hit: {}", key);
                } else {
                    log.debug("Cache miss: {}", key);
                }
            })
            .doOnError(error -> log.error("Error getting from cache: {}", key, error));
    }
    </#if>
    
    @Override
    public Mono<Void> delete(String key) {
        log.debug("Deleting from Redis cache: {}", key);
        return redisTemplate.delete(KEY_PREFIX + key)
            .doOnSuccess(result -> log.debug("Deleted from cache: {}", key))
            .doOnError(error -> log.error("Error deleting from cache: {}", key, error))
            .then();
    }
    
    @Override
    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(KEY_PREFIX + key);
    }
}
```

### E.5 Ejemplo de template de adaptador Redis (Quarkus Reactive)

```java
// templates/frameworks/quarkus/reactive/adapters/output/redis/Adapter.java.ftl
package ${basePackage}.infrastructure.adapter.out.redis;

import ${basePackage}.domain.model.${entityName};
import ${basePackage}.domain.port.out.${portName};
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Adaptador de Redis para ${entityName}
 * Estrategia de caché: ${cacheStrategy}
 * 
 * @author Generated by Clean Arch Generator
 */
@Slf4j
@ApplicationScoped
public class ${name}RedisAdapter implements ${portName} {
    
    private final ReactiveValueCommands<String, ${entityName}> commands;
    
    private static final String KEY_PREFIX = "${keyPrefix}:";
    private static final Duration TTL = Duration.ofSeconds(${ttl});
    
    public ${name}RedisAdapter(ReactiveRedisDataSource redis) {
        this.commands = redis.value(${entityName}.class);
    }
    
    <#if cacheStrategy == "writeThrough" || cacheStrategy == "writeBack">
    @Override
    public Uni<Void> save(String key, ${entityName} entity) {
        log.debug("Saving to Redis cache: {}", key);
        return commands.setex(KEY_PREFIX + key, TTL.getSeconds(), entity)
            .invoke(() -> log.debug("Saved to cache: {}", key))
            .onFailure().invoke(error -> log.error("Error saving to cache: {}", key, error))
            .replaceWithVoid();
    }
    </#if>
    
    <#if cacheStrategy == "readThrough" || cacheStrategy == "writeThrough">
    @Override
    public Uni<${entityName}> get(String key) {
        log.debug("Getting from Redis cache: {}", key);
        return commands.get(KEY_PREFIX + key)
            .invoke(result -> {
                if (result != null) {
                    log.debug("Cache hit: {}", key);
                } else {
                    log.debug("Cache miss: {}", key);
                }
            })
            .onFailure().invoke(error -> log.error("Error getting from cache: {}", key, error));
    }
    </#if>
    
    @Override
    public Uni<Void> delete(String key) {
        log.debug("Deleting from Redis cache: {}", key);
        return commands.getdel(KEY_PREFIX + key)
            .invoke(() -> log.debug("Deleted from cache: {}", key))
            .onFailure().invoke(error -> log.error("Error deleting from cache: {}", key, error))
            .replaceWithVoid();
    }
}
```

### E.6 Motor de templates: Freemarker

**¿Por qué Freemarker?**

1. **Sintaxis clara para código Java**
```java
<#if condition>
    // código
</#if>

<#list items as item>
    ${item}
</#list>
```

2. **Directivas útiles**
```java
<#assign variable = value>
<#include "otro-template.ftl">
<#macro nombreMacro parametro>
    // contenido reutilizable
</#macro>
```

3. **Funciones built-in**
```java
${name?cap_first}           // Capitalizar
${name?lower_case}          // Minúsculas
${name?upper_case}          // Mayúsculas
${name?replace("_", "")}    // Reemplazar
```

4. **Condicionales complejos**
```java
<#if paradigm == "reactive">
    import reactor.core.publisher.Mono;
<#elseif paradigm == "imperative">
    import java.util.Optional;
</#if>
```

### E.7 Alternativas consideradas

| Motor | Pros | Contras | Recomendación |
|-------|------|---------|---------------|
| **Freemarker** | ✅ Maduro, potente, buena docs | ⚠️ Sintaxis verbosa | ⭐ **RECOMENDADO** |
| Velocity | ✅ Simple, rápido | ❌ Menos features, menos mantenido | ⚠️ Solo si necesitas simplicidad |
| Mustache | ✅ Logic-less, multi-lenguaje | ❌ Muy limitado para código | ❌ No recomendado |
| Thymeleaf | ✅ Natural templates | ❌ Orientado a HTML, no código | ❌ No recomendado |
| Kotlin DSL | ✅ Type-safe, refactorable | ❌ Complejo, curva de aprendizaje | 🔮 Futuro |

### E.8 Configuración de Freemarker en el plugin

```kotlin
// TemplateEngine.kt
class TemplateEngine {
    
    private val configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        // Encoding
        defaultEncoding = "UTF-8"
        outputEncoding = "UTF-8"
        
        // Template loading
        templateLoader = StringTemplateLoader()
        
        // Error handling
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        logTemplateExceptions = true
        
        // Whitespace handling
        whitespaceStripping = true
        
        // Number format
        numberFormat = "0.######"
        
        // Custom directives
        setSharedVariable("camelCase", CamelCaseMethod())
        setSharedVariable("pascalCase", PascalCaseMethod())
        setSharedVariable("snakeCase", SnakeCaseMethod())
    }
    
    fun process(templateContent: String, data: Map<String, Any>): String {
        val template = Template("template", templateContent, configuration)
        val writer = StringWriter()
        template.process(data, writer)
        return writer.toString()
    }
}

// Custom methods para naming conventions
class CamelCaseMethod : TemplateMethodModelEx {
    override fun exec(args: List<*>): String {
        val input = args[0].toString()
        return input.toCamelCase()
    }
}
```

### E.9 Ejemplo de uso completo

```bash
# 1. Usuario inicializa proyecto
./gradlew initCleanArch \
  --architecture=hexagonal \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment

# El plugin:
# 1. Descarga templates/architectures/hexagonal/structure.yml
# 2. Descarga templates/frameworks/spring/reactive/project/*.ftl
# 3. Procesa con Freemarker
# 4. Genera estructura completa

# 2. Usuario genera adaptador Redis
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache \
  --cacheStrategy=writeThrough \
  --ttl=3600

# El plugin:
# 1. Lee .cleanarch.yml (framework=spring, paradigm=reactive)
# 2. Descarga templates/frameworks/spring/reactive/adapters/output/redis/Adapter.java.ftl
# 3. Procesa con Freemarker usando los parámetros
# 4. Genera PaymentCacheRedisAdapter.java
```

---

**Actualizado:** 2026-01-22  
**Versión:** 1.3 - Agregada estructura completa de templates y Freemarker


## F. Contribuciones Parciales y Matriz de Compatibilidad

### F.1 Contribuciones parciales son bienvenidas

**Principio clave:** No necesitas implementar un adaptador para todos los frameworks y paradigmas.

#### Escenarios válidos de contribución:

**Escenario 1: Solo Spring Reactive**
```
Juan implementa Kafka solo para Spring Reactive
✅ templates/frameworks/spring/reactive/adapters/output/kafka/
❌ No implementa: spring/imperative, quarkus/reactive, quarkus/imperative
```

**Escenario 2: Solo Quarkus (ambos paradigmas)**
```
María implementa Redis para Quarkus Reactive e Imperative
✅ templates/frameworks/quarkus/reactive/adapters/output/redis/
✅ templates/frameworks/quarkus/imperative/adapters/output/redis/
❌ No implementa: spring/reactive, spring/imperative
```

**Escenario 3: Todos los reactivos**
```
Pedro implementa DynamoDB para todos los frameworks reactivos
✅ templates/frameworks/spring/reactive/adapters/output/dynamodb/
✅ templates/frameworks/quarkus/reactive/adapters/output/dynamodb/
❌ No implementa: imperativos
```

### F.2 Metadata con matriz de implementaciones

```yaml
# templates/frameworks/spring/reactive/adapters/output/kafka/metadata.yml
name: kafka
displayName: Apache Kafka
description: Producer y Consumer para Apache Kafka
type: both
version: 1.0.0
author: Juan Pérez <juan.perez@company.com>

# Matriz de implementaciones disponibles
implementations:
  spring:
    reactive:
      available: true
      version: 1.0.0
      author: Juan Pérez
      lastUpdated: 2026-01-22
    imperative:
      available: false
      status: planned
      issue: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/issues/42
  quarkus:
    reactive:
      available: false
      status: wanted
      issue: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/issues/43
    imperative:
      available: false
      status: wanted

# Resto de metadata...
parameters:
  required:
    - name: name
      type: string
    - name: topic
      type: string
```

### F.3 Validación en el plugin

```kotlin
// AdapterValidator.kt
class AdapterValidator(private val templateRepository: TemplateRepository) {
    
    fun validateAdapterAvailability(
        adapterType: String,
        framework: String,
        paradigm: String
    ): ValidationResult {
        
        // Intentar cargar metadata
        val metadata = try {
            templateRepository.loadMetadata(framework, paradigm, "adapters/output", adapterType)
        } catch (e: TemplateNotFoundException) {
            return ValidationResult.NotAvailable(
                message = buildNotAvailableMessage(adapterType, framework, paradigm)
            )
        }
        
        // Verificar si está marcado como disponible
        val implementation = metadata.implementations
            ?.get(framework)
            ?.get(paradigm)
        
        return when {
            implementation?.available == true -> {
                ValidationResult.Available(metadata)
            }
            else -> {
                ValidationResult.NotAvailable(
                    message = buildNotAvailableMessage(adapterType, framework, paradigm),
                    alternatives = findAlternatives(metadata, framework, paradigm)
                )
            }
        }
    }
    
    private fun buildNotAvailableMessage(
        adapterType: String,
        framework: String,
        paradigm: String
    ): String {
        return """
            ❌ El adaptador '$adapterType' no está disponible para $framework/$paradigm
            
            Puedes:
            1. Usar otro framework/paradigma donde esté disponible
            2. Contribuir la implementación (ver guía de contribución)
            3. Esperar a que alguien lo implemente
        """.trimIndent()
    }
    
    private fun findAlternatives(
        metadata: AdapterMetadata,
        currentFramework: String,
        currentParadigm: String
    ): List<Alternative> {
        val alternatives = mutableListOf<Alternative>()
        
        metadata.implementations?.forEach { (framework, paradigms) ->
            paradigms.forEach { (paradigm, impl) ->
                if (impl.available && (framework != currentFramework || paradigm != currentParadigm)) {
                    alternatives.add(
                        Alternative(
                            framework = framework,
                            paradigm = paradigm,
                            version = impl.version ?: "unknown"
                        )
                    )
                }
            }
        }
        
        return alternatives
    }
}

sealed class ValidationResult {
    data class Available(val metadata: AdapterMetadata) : ValidationResult()
    data class NotAvailable(
        val message: String,
        val alternatives: List<Alternative> = emptyList()
    ) : ValidationResult()
}

data class Alternative(
    val framework: String,
    val paradigm: String,
    val version: String
)
```

### F.4 Mensajes de error informativos

```bash
# Usuario intenta usar Kafka con Quarkus Reactive (no implementado)
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events

# Output:
❌ El adaptador 'kafka' no está disponible para quarkus/reactive

✅ Disponible en:
  - spring/reactive (v1.0.0) - por Juan Pérez

⏳ Pendiente:
  - spring/imperative (planeado) - Issue #42
  - quarkus/reactive (buscando contribuidor) - Issue #43
  - quarkus/imperative (buscando contribuidor)

💡 Opciones:
  1. Cambiar a Spring Reactive:
     ./gradlew initCleanArch --framework=spring --paradigm=reactive
  
  2. Contribuir la implementación para Quarkus Reactive:
     https://docs.clean-arch-generator.com/contributing/creating-adapters
  
  3. Ver issue #43 para seguimiento:
     https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/issues/43
```

### F.5 Comando para ver matriz de compatibilidad

```bash
# Ver todos los adaptadores y su disponibilidad
./gradlew listAdapters --detailed

# Output:
Available Adapters:

Redis Cache
  spring/reactive:    ✅ v1.0.0
  spring/imperative:  ✅ v1.0.0
  quarkus/reactive:   ✅ v1.0.0
  quarkus/imperative: ⏳ Planned (Issue #40)

Kafka
  spring/reactive:    ✅ v1.0.0
  spring/imperative:  ⏳ Planned (Issue #42)
  quarkus/reactive:   ⏳ Wanted (Issue #43)
  quarkus/imperative: ⏳ Wanted

DynamoDB
  spring/reactive:    ✅ v1.0.0
  spring/imperative:  ✅ v1.0.0
  quarkus/reactive:   ⏳ Wanted
  quarkus/imperative: ⏳ Wanted

PostgreSQL
  spring/reactive:    ✅ v1.0.0 (R2DBC)
  spring/imperative:  ✅ v1.0.0 (JPA)
  quarkus/reactive:   ✅ v1.0.0 (Hibernate Reactive)
  quarkus/imperative: ✅ v1.0.0 (Hibernate ORM)

# Ver solo un adaptador específico
./gradlew adapterInfo --type=kafka

# Output:
Kafka Adapter

Description: Producer y Consumer para Apache Kafka
Type: both (input/output)
Author: Juan Pérez

Availability Matrix:
  ✅ spring/reactive (v1.0.0)
     Author: Juan Pérez
     Last updated: 2026-01-22
     
  ⏳ spring/imperative (planned)
     Status: Planeado para v1.1.0
     Issue: #42
     
  ⏳ quarkus/reactive (wanted)
     Status: Buscando contribuidor
     Issue: #43
     Help wanted: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/issues/43
```

### F.6 Issues automáticos para implementaciones faltantes

Cuando alguien contribuye un adaptador parcial, el CI/CD puede crear issues automáticamente:

```yaml
# .github/workflows/create-missing-implementation-issues.yml
name: Create Missing Implementation Issues

on:
  pull_request:
    types: [closed]
    paths:
      - 'templates/frameworks/**'

jobs:
  create-issues:
    if: github.event.pull_request.merged == true
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Detect new adapter
        id: detect
        run: |
          # Script que detecta qué adaptador se agregó
          # y para qué framework/paradigma
          
      - name: Create issues for missing implementations
        run: |
          # Si se agregó kafka para spring/reactive
          # Crear issues para:
          # - spring/imperative
          # - quarkus/reactive
          # - quarkus/imperative
```

**Issue creado automáticamente:**

```markdown
Title: Implement Kafka adapter for Spring Imperative

## Description
The Kafka adapter was recently implemented for Spring Reactive by @juanperez in #123.

We need the implementation for **Spring Imperative**.

## Reference Implementation
- Spring Reactive: `templates/frameworks/spring/reactive/adapters/output/kafka/`
- PR: #123

## What needs to be done
- [ ] Create `templates/frameworks/spring/imperative/adapters/output/kafka/`
- [ ] Implement `Adapter.java.ftl` (using blocking APIs)
- [ ] Implement `Config.java.ftl`
- [ ] Implement `Test.java.ftl`
- [ ] Update `metadata.yml`
- [ ] Test with example project

## Differences from Reactive version
- Use blocking Kafka APIs instead of reactive
- Use `KafkaTemplate` instead of `ReactiveKafkaTemplate`
- Return types should be synchronous (no `Mono<>` or `Flux<>`)

## Resources
- [Contributing Guide](https://docs.clean-arch-generator.com/contributing/creating-adapters)
- [Spring Kafka Docs](https://spring.io/projects/spring-kafka)

Labels: enhancement, help-wanted, good-first-issue, spring, imperative, kafka
```

### F.7 Badge de compatibilidad en documentación

En Docusaurus, cada página de adaptador muestra badges:

```markdown
# Kafka Adapter

![Spring Reactive](https://img.shields.io/badge/Spring_Reactive-✅_v1.0.0-green)
![Spring Imperative](https://img.shields.io/badge/Spring_Imperative-⏳_Planned-yellow)
![Quarkus Reactive](https://img.shields.io/badge/Quarkus_Reactive-⏳_Wanted-orange)
![Quarkus Imperative](https://img.shields.io/badge/Quarkus_Imperative-⏳_Wanted-orange)
```

### F.8 Ventajas de este enfoque

| Aspecto | Ventaja |
|---------|---------|
| **Contribuciones** | ✅ Más fáciles - no necesitas saber todos los frameworks |
| **Velocidad** | ✅ Adaptadores disponibles más rápido |
| **Especialización** | ✅ Expertos en Spring contribuyen Spring, expertos en Quarkus contribuyen Quarkus |
| **Transparencia** | ✅ Usuarios saben qué está disponible y qué no |
| **Colaboración** | ✅ Issues claros para que otros contribuyan |
| **Calidad** | ✅ Cada implementación por alguien que conoce el framework |

---

**Actualizado:** 2026-01-22  
**Versión:** 1.4 - Agregado soporte para contribuciones parciales y Docusaurus
