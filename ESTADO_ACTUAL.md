# Estado Actual del Proyecto - Clean Architecture Generator

**Fecha**: Febrero 2026  
**Versión**: 0.1.15-SNAPSHOT

## 📋 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Funcionalidades Implementadas](#funcionalidades-implementadas)
4. [Arquitecturas Soportadas](#arquitecturas-soportadas)
5. [Comandos Disponibles](#comandos-disponibles)
6. [Modelos de Dominio](#modelos-de-dominio)
7. [Generadores](#generadores)
8. [Validadores](#validadores)
9. [Adaptadores](#adaptadores)
10. [Templates](#templates)
11. [Configuración](#configuración)
12. [Limitaciones Conocidas](#limitaciones-conocidas)
13. [Próximos Pasos](#próximos-pasos)

---

## 1. Resumen Ejecutivo

El **Clean Architecture Generator** es un plugin de Gradle que permite generar proyectos con arquitectura limpia (Clean Architecture) de forma automatizada. Actualmente soporta:

- ✅ 3 variantes de arquitectura hexagonal
- ✅ Generación de proyectos single-module y multi-module
- ✅ Generación dinámica de adaptadores como módulos (granular)
- ✅ Soporte para paradigma reactivo con Spring WebFlux
- ✅ Configuración mediante `.cleanarch.yml`
- ✅ Templates descargables desde GitHub o locales

### Estado General
- **Core**: ✅ Funcional y estable
- **Templates**: ✅ Completos para hexagonal
- **Testing**: ⚠️ Cobertura parcial
- **Documentación**: ✅ Completa

---

## 2. Estructura del Proyecto

### 2.1 Repositorios

```
java-archetype-generator/
├── backend-architecture-design-archetype-generator-core/     # Plugin de Gradle
├── backend-architecture-design-archetype-generator-templates/ # Templates Freemarker
└── backend-architecture-design-site-docs/                    # Documentación Docusaurus
```

### 2.2 Estructura del Core (Clean Architecture)

```
src/main/java/com/pragma/archetype/
├── domain/                          # Capa de Dominio (Reglas de Negocio)
│   ├── model/                       # Entidades y Value Objects
│   │   ├── ProjectConfig.java       # Configuración del proyecto
│   │   ├── AdapterConfig.java       # Configuración de adaptadores
│   │   ├── UseCaseConfig.java       # Configuración de casos de uso
│   │   ├── EntityConfig.java        # Configuración de entidades
│   │   ├── InputAdapterConfig.java  # Configuración de adaptadores de entrada
│   │   ├── ArchitectureType.java    # Enum de arquitecturas
│   │   ├── Framework.java           # Enum de frameworks
│   │   ├── Paradigm.java            # Enum de paradigmas
│   │   ├── TemplateConfig.java      # Configuración de templates
│   │   ├── TemplateMode.java        # Modo de templates
│   │   ├── GeneratedFile.java       # Archivo generado
│   │   └── ValidationResult.java    # Resultado de validación
│   │
│   ├── port/                        # Puertos (Interfaces)
│   │   ├── in/                      # Puertos de entrada (Use Cases)
│   │   │   ├── InitializeProjectUseCase.java
│   │   │   ├── GenerateAdapterUseCase.java
│   │   │   ├── GenerateUseCaseUseCase.java
│   │   │   ├── GenerateEntityUseCase.java
│   │   │   └── GenerateInputAdapterUseCase.java
│   │   │
│   │   └── out/                     # Puertos de salida (Repositorios)
│   │       ├── FileSystemPort.java
│   │       ├── ConfigurationPort.java
│   │       ├── TemplateRepository.java
│   │       └── HttpClientPort.java
│   │
│   └── service/                     # Servicios de Dominio
│       ├── ProjectValidator.java
│       ├── AdapterValidator.java
│       ├── UseCaseValidator.java
│       ├── EntityValidator.java
│       └── InputAdapterValidator.java
│
├── application/                     # Capa de Aplicación (Orquestación)
│   ├── usecase/                     # Implementación de Use Cases
│   │   ├── InitializeProjectUseCaseImpl.java
│   │   ├── GenerateAdapterUseCaseImpl.java
│   │   ├── GenerateUseCaseUseCaseImpl.java
│   │   ├── GenerateEntityUseCaseImpl.java
│   │   └── GenerateInputAdapterUseCaseImpl.java
│   │
│   └── generator/                   # Generadores (Servicios de Aplicación)
│       ├── ProjectGenerator.java    # ⭐ Generador principal
│       ├── AdapterGenerator.java    # ⭐ Generador de adaptadores
│       ├── UseCaseGenerator.java
│       ├── EntityGenerator.java
│       └── InputAdapterGenerator.java
│
└── infrastructure/                  # Capa de Infraestructura (Detalles)
    ├── adapter/
    │   ├── in/                      # Adaptadores de Entrada
    │   │   └── gradle/              # Tasks de Gradle
    │   │       ├── InitCleanArchTask.java
    │   │       ├── GenerateOutputAdapterTask.java
    │   │       ├── GenerateInputAdapterTask.java
    │   │       ├── GenerateUseCaseTask.java
    │   │       ├── GenerateEntityTask.java
    │   │       ├── UpdateTemplatesTask.java
    │   │       └── ClearTemplateCacheTask.java
    │   │
    │   └── out/                     # Adaptadores de Salida
    │       ├── filesystem/
    │       │   └── LocalFileSystemAdapter.java
    │       ├── config/
    │       │   └── YamlConfigurationAdapter.java
    │       ├── template/
    │       │   ├── FreemarkerTemplateRepository.java
    │       │   ├── GitHubTemplateDownloader.java
    │       │   └── TemplateCache.java
    │       └── http/
    │           ├── OkHttpClientAdapter.java
    │           └── SimpleHttpClientAdapter.java
    │
    └── config/
        └── CleanArchPlugin.java     # Plugin principal de Gradle
```

---

## 3. Funcionalidades Implementadas

### 3.1 Inicialización de Proyectos ✅

**Comando**: `gradle initCleanArch`

**Parámetros**:
- `--architecture`: hexagonal-single, hexagonal-multi, hexagonal-multi-granular
- `--paradigm`: reactive, imperative
- `--framework`: spring, quarkus
- `--packageName`: Paquete base (ej: com.pragma.service)

**Genera**:
- Estructura de carpetas según arquitectura
- Archivos de configuración (build.gradle.kts, settings.gradle.kts)
- Archivo `.cleanarch.yml` con configuración del proyecto
- README.md con instrucciones
- .gitignore
- Configuración de Spring Boot (BeanConfiguration.java, Application.java)

**Características especiales**:
- ✅ Detección automática de templates locales (modo desarrollo)
- ✅ Descarga de templates desde GitHub (modo producción)
- ✅ Cache de templates
- ✅ Configuración de `adaptersAsModules` automática para granular

### 3.2 Generación de Adaptadores de Salida ✅

**Comando**: `gradle generateOutputAdapter`

**Parámetros**:
- `--name`: Nombre del adaptador (ej: UserRepository)
- `--entity`: Nombre de la entidad (ej: User)
- `--type`: redis, mongodb, postgresql, rest-client, kafka
- `--packageName`: (opcional) Se auto-detecta desde .cleanarch.yml
- `--methods`: (opcional) Métodos personalizados

**Genera**:
- Adaptador que implementa puerto de salida
- Mapper entre dominio y datos
- Entidad de datos (UserData)
- **Si `adaptersAsModules=true`**: Crea módulo de Gradle independiente

**Tipos soportados**:
- ✅ Redis (ReactiveRedisTemplate)
- ✅ MongoDB (ReactiveMongoRepository)
- ✅ PostgreSQL (R2DBC)
- ✅ REST Client (WebClient)
- ✅ Kafka (KafkaTemplate)

### 3.3 Generación de Adaptadores de Entrada ✅

**Comando**: `gradle generateInputAdapter`

**Parámetros**:
- `--name`: Nombre del adaptador (ej: UserController)
- `--type`: rest, graphql, grpc
- `--useCaseName`: Caso de uso a invocar
- `--endpoints`: Definición de endpoints

**Genera**:
- Controlador REST con endpoints
- DTOs de request/response
- Mappers entre DTOs y dominio

### 3.4 Generación de Casos de Uso ✅

**Comando**: `gradle generateUseCase`

**Parámetros**:
- `--name`: Nombre del caso de uso (ej: CreateUser)
- `--packageName`: (opcional)
- `--methods`: Métodos del caso de uso
- `--generatePort`: true/false (genera interfaz)
- `--generateImpl`: true/false (genera implementación)

**Genera**:
- Puerto (interfaz) en domain/port/in
- Implementación en domain/usecase

### 3.5 Generación de Entidades ✅

**Comando**: `gradle generateEntity`

**Parámetros**:
- `--name`: Nombre de la entidad (ej: User)
- `--packageName`: (opcional)
- `--fields`: Campos de la entidad
- `--hasId`: true/false
- `--idType`: String, Long, UUID

**Genera**:
- Entidad de dominio en domain/model
- Con Lombok (@Data, @Builder)

### 3.6 Gestión de Templates ✅

**Comando**: `gradle updateTemplates`
- Limpia cache y fuerza re-descarga de templates

**Comando**: `gradle clearTemplateCache`
- Limpia solo el cache local

---

## 4. Arquitecturas Soportadas

### 4.1 Hexagonal Single Module ✅

**Estructura**:
```
project/
├── src/main/java/
│   └── com.pragma.service/
│       ├── domain/
│       │   ├── model/
│       │   ├── port/
│       │   └── usecase/
│       ├── infrastructure/
│       │   ├── entrypoints/
│       │   └── drivenadapters/
│       └── config/
└── build.gradle.kts
```

**Características**:
- Un solo módulo de Gradle
- Todas las capas en el mismo módulo
- Ideal para proyectos pequeños

### 4.2 Hexagonal Multi Module ✅

**Estructura**:
```
project/
├── domain/
│   ├── src/main/java/
│   └── build.gradle.kts
├── application/
│   ├── src/main/java/
│   └── build.gradle.kts
├── infrastructure/
│   ├── src/main/java/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

**Características**:
- 3 módulos de Gradle
- Separación clara de responsabilidades
- Mejor para proyectos medianos/grandes

### 4.3 Hexagonal Multi Module Granular ✅ ⭐ NUEVO

**Estructura**:
```
project/
├── domain/
│   ├── model/          # Módulo independiente
│   ├── ports/          # Módulo independiente
│   └── usecase/        # Módulo independiente
├── application/
│   └── app-service/    # Módulo independiente
├── infrastructure/
│   ├── entry-points/   # Carpeta (no módulo)
│   └── driven-adapters/# Carpeta (no módulo)
│       └── userrepository/  # ⭐ Módulo creado dinámicamente
├── build.gradle.kts
└── settings.gradle.kts
```

**Características**:
- Cada componente es un módulo de Gradle
- Adaptadores se crean como módulos independientes
- `adaptersAsModules: true` en .cleanarch.yml
- Máxima modularidad y separación
- Ideal para microservicios complejos

**Flujo de generación de adaptadores**:
1. Se crea módulo en `infrastructure/driven-adapters/{nombre}/`
2. Se genera `build.gradle.kts` con dependencias específicas
3. Se actualiza `settings.gradle.kts` con `include("infrastructure:driven-adapters:nombre")`
4. Se actualiza `application/app-service/build.gradle.kts` con dependencia al módulo

---

## 5. Comandos Disponibles

### Tabla de Comandos

| Comando | Descripción | Estado |
|---------|-------------|--------|
| `initCleanArch` | Inicializa proyecto con arquitectura limpia | ✅ |
| `generateOutputAdapter` | Genera adaptador de salida (driven) | ✅ |
| `generateInputAdapter` | Genera adaptador de entrada (driving) | ✅ |
| `generateUseCase` | Genera caso de uso | ✅ |
| `generateEntity` | Genera entidad de dominio | ✅ |
| `updateTemplates` | Actualiza templates desde GitHub | ✅ |
| `clearTemplateCache` | Limpia cache de templates | ✅ |

### Ejemplo de Uso Completo

```bash
# 1. Crear proyecto inicial
gradle initCleanArch \
  --architecture=hexagonal-multi-granular \
  --paradigm=reactive \
  --framework=spring \
  --packageName=com.pragma.users

# 2. Generar wrapper de Gradle 8.5 (recomendado)
gradle wrapper --gradle-version 8.5

# 3. Generar entidad de dominio
./gradlew generateEntity \
  --name=User \
  --fields="id:String,name:String,email:String" \
  --hasId=true

# 4. Generar caso de uso
./gradlew generateUseCase \
  --name=CreateUser \
  --methods="execute:Mono<User>:request:CreateUserRequest"

# 5. Generar adaptador de salida (se crea como módulo)
./gradlew generateOutputAdapter \
  --name=UserRepository \
  --entity=User \
  --type=redis

# 6. Generar adaptador de entrada
./gradlew generateInputAdapter \
  --name=UserController \
  --type=rest \
  --useCaseName=CreateUser

# 7. Compilar y ejecutar
./gradlew build
./gradlew bootRun
```

---

## 6. Modelos de Dominio

### 6.1 ProjectConfig

```java
public record ProjectConfig(
    String name,                    // Nombre del proyecto
    String basePackage,             // Paquete base
    ArchitectureType architecture,  // Tipo de arquitectura
    Paradigm paradigm,              // Paradigma (reactive/imperative)
    Framework framework,            // Framework (spring/quarkus)
    String pluginVersion,           // Versión del plugin
    LocalDateTime createdAt,        // Fecha de creación
    boolean adaptersAsModules       // ⭐ Flag para módulos granulares
)
```

### 6.2 ArchitectureType

```java
public enum ArchitectureType {
    HEXAGONAL_SINGLE("hexagonal-single", false),
    HEXAGONAL_MULTI("hexagonal-multi", true),
    HEXAGONAL_MULTI_GRANULAR("hexagonal-multi-granular", true),  // ⭐ NUEVO
    ONION_SINGLE("onion-single", false),
    ONION_MULTI("onion-multi", true);
    
    public boolean isMultiModule() { ... }
}
```

### 6.3 AdapterConfig

```java
public record AdapterConfig(
    String name,                    // Nombre del adaptador
    String packageName,             // Paquete
    AdapterType type,               // redis, mongodb, etc.
    String entityName,              // Entidad relacionada
    List<AdapterMethod> methods     // Métodos personalizados
)

public enum AdapterType {
    REDIS, MONGODB, POSTGRESQL, REST_CLIENT, KAFKA
}
```

---

## 7. Generadores

### 7.1 ProjectGenerator ⭐

**Responsabilidad**: Generar estructura completa del proyecto

**Métodos principales**:
```java
// Genera proyecto según arquitectura
List<GeneratedFile> generate(Path projectPath, ProjectConfig config)

// Genera estructura multi-módulo granular
List<GeneratedFile> generateGranularStructure(...)

// Actualiza settings.gradle.kts
void addModuleToSettings(Path projectPath, String modulePath)

// Actualiza build.gradle.kts de un módulo
void addDependencyToModule(Path projectPath, String modulePath, String dependencyPath)
```

**Arquitecturas que maneja**:
- ✅ hexagonal-single
- ✅ hexagonal-multi
- ✅ hexagonal-multi-granular

### 7.2 AdapterGenerator ⭐

**Responsabilidad**: Generar adaptadores de salida

**Métodos principales**:
```java
// Genera adaptador (decide si módulo o in-place)
List<GeneratedFile> generate(Path projectPath, AdapterConfig config, ProjectConfig projectConfig)

// Genera adaptador como módulo de Gradle
List<GeneratedFile> generateAdapterAsModule(...)

// Genera adaptador en estructura existente
List<GeneratedFile> generateAdapterInPlace(...)
```

**Lógica de decisión**:
```java
if (projectConfig.adaptersAsModules()) {
    // Crear módulo en infrastructure/driven-adapters/{name}/
    // Generar build.gradle.kts
    // Actualizar settings.gradle.kts
    // Actualizar app-service/build.gradle.kts
} else {
    // Generar en infrastructure/drivenadapters/
}
```

### 7.3 UseCaseGenerator

**Responsabilidad**: Generar casos de uso

**Genera**:
- Puerto (interfaz) en domain/port/in
- Implementación en domain/usecase

### 7.4 EntityGenerator

**Responsabilidad**: Generar entidades de dominio

**Genera**:
- Clase en domain/model
- Con Lombok annotations

### 7.5 InputAdapterGenerator

**Responsabilidad**: Generar adaptadores de entrada

**Genera**:
- Controladores REST
- DTOs
- Mappers

---

## 8. Validadores

### 8.1 ProjectValidator

**Valida**:
- ✅ Proyecto no existe previamente
- ✅ Configuración no existe
- ✅ Nombre de proyecto válido
- ✅ Paquete base válido

### 8.2 AdapterValidator

**Valida**:
- ✅ Proyecto existe
- ✅ Configuración existe
- ✅ Nombre de adaptador válido
- ✅ Tipo de adaptador soportado

### 8.3 UseCaseValidator

**Valida**:
- ✅ Proyecto existe
- ✅ Nombre de caso de uso válido
- ✅ Métodos válidos

### 8.4 EntityValidator

**Valida**:
- ✅ Proyecto existe
- ✅ Nombre de entidad válido
- ✅ Campos válidos

---

## 9. Adaptadores

### 9.1 Adaptadores de Entrada (Gradle Tasks)

| Task | Responsabilidad |
|------|----------------|
| `InitCleanArchTask` | Inicializar proyecto |
| `GenerateOutputAdapterTask` | Generar adaptador de salida |
| `GenerateInputAdapterTask` | Generar adaptador de entrada |
| `GenerateUseCaseTask` | Generar caso de uso |
| `GenerateEntityTask` | Generar entidad |
| `UpdateTemplatesTask` | Actualizar templates |
| `ClearTemplateCacheTask` | Limpiar cache |

### 9.2 Adaptadores de Salida

| Adaptador | Puerto | Responsabilidad |
|-----------|--------|----------------|
| `LocalFileSystemAdapter` | `FileSystemPort` | Operaciones de archivos |
| `YamlConfigurationAdapter` | `ConfigurationPort` | Leer/escribir .cleanarch.yml |
| `FreemarkerTemplateRepository` | `TemplateRepository` | Procesar templates Freemarker |
| `GitHubTemplateDownloader` | - | Descargar templates desde GitHub |
| `OkHttpClientAdapter` | `HttpClientPort` | Cliente HTTP |

---

## 10. Templates

### 10.1 Estructura de Templates

```
templates/
├── architectures/
│   ├── hexagonal-single/
│   │   ├── structure.yml
│   │   └── project/
│   │       ├── build.gradle.kts.ftl
│   │       ├── settings.gradle.kts.ftl
│   │       ├── README.md.ftl
│   │       └── .gitignore.ftl
│   │
│   ├── hexagonal-multi/
│   │   ├── structure.yml
│   │   ├── project/
│   │   │   ├── build.gradle.kts.ftl
│   │   │   ├── settings.gradle.kts.ftl
│   │   │   └── ...
│   │   └── modules/
│   │       ├── domain/
│   │       ├── application/
│   │       └── infrastructure/
│   │
│   └── hexagonal-multi-granular/  ⭐
│       ├── structure.yml
│       ├── project/
│       │   ├── build.gradle.kts.ftl
│       │   ├── settings.gradle.kts.ftl
│       │   ├── gradle/wrapper/     # ⭐ Gradle 8.5 wrapper
│       │   ├── gradlew
│       │   └── gradlew.bat
│       └── modules/
│           ├── domain-model/
│           ├── domain-ports/
│           ├── domain-usecase/
│           ├── app-service/
│           └── driven-adapter-build.gradle.kts.ftl  ⭐
│
└── frameworks/
    └── spring/
        └── reactive/
            ├── adapters/
            │   ├── entry-points/
            │   │   └── rest/
            │   └── driven-adapters/
            │       ├── redis/
            │       ├── mongodb/
            │       ├── postgresql/
            │       ├── rest-client/
            │       └── kafka/
            ├── domain/
            └── usecase/
```

### 10.2 Variables de Template

**Variables globales**:
- `${projectName}`: Nombre del proyecto
- `${groupId}`: Group ID (basePackage)
- `${version}`: Versión del proyecto
- `${pluginVersion}`: Versión del plugin
- `${basePackage}`: Paquete base

**Variables de adaptador**:
- `${adapterName}`: Nombre del adaptador
- `${adapterType}`: Tipo (redis, mongodb, etc.)
- `${entityName}`: Nombre de la entidad
- `${packageName}`: Paquete del adaptador

---

## 11. Configuración

### 11.1 Archivo .cleanarch.yml

```yaml
project:
  name: test-hexagonal-granular
  basePackage: com.pragma.test
  pluginVersion: 0.1.15-SNAPSHOT
  createdAt: '2026-02-01T21:59:06.388053'

architecture:
  type: hexagonal-multi-granular
  paradigm: reactive
  framework: spring
  adaptersAsModules: true  # ⭐ Flag para módulos granulares
```

### 11.2 Configuración de Templates (opcional)

```yaml
templates:
  mode: developer  # o production
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  version: v1.0.0
  localPath: /path/to/local/templates
  cache: true
```

---

## 12. Limitaciones Conocidas

### 12.1 Técnicas

1. **Gradle Wrapper no se copia automáticamente**
   - Solución temporal: Ejecutar `gradle wrapper --gradle-version 8.5` manualmente
   - Mejora pendiente: Copiar archivos binarios en ProjectGenerator

2. **Compatibilidad de versiones**
   - Gradle 9.x tiene problemas con Spring Boot 3.3.0
   - Solución: Usar Gradle 8.5
   - Lombok 1.18.38 requerido para Java 21

3. **Templates solo en Freemarker**
   - No soporta otros motores de templates
   - Archivos binarios no se procesan

4. **Sin soporte para Kotlin**
   - Solo genera código Java
   - Mejora futura

### 12.2 Funcionales

1. **Solo arquitectura hexagonal**
   - Onion architecture no implementada
   - Clean architecture (Uncle Bob) no implementada

2. **Solo paradigma reactivo completo**
   - Paradigma imperativo parcialmente implementado
   - Templates solo para reactive

3. **Solo framework Spring**
   - Quarkus no implementado
   - Micronaut no implementado

4. **Adaptadores limitados**
   - Solo 5 tipos de adaptadores de salida
   - GraphQL y gRPC no implementados para entrada

---

## 13. Próximos Pasos

### 13.1 Prioridad Alta

- [ ] Copiar Gradle wrapper automáticamente
- [ ] Tests unitarios completos
- [ ] Tests de integración
- [ ] Validación de compatibilidad de versiones
- [ ] Manejo de errores mejorado

### 13.2 Prioridad Media

- [ ] Soporte para paradigma imperativo
- [ ] Más tipos de adaptadores
- [ ] Generación de tests automática
- [ ] Soporte para Kotlin
- [ ] CLI standalone (sin Gradle)

### 13.3 Prioridad Baja

- [ ] Soporte para Quarkus
- [ ] Soporte para Micronaut
- [ ] Arquitectura Onion
- [ ] Arquitectura Clean (Uncle Bob)
- [ ] GUI para configuración

---

## 📊 Métricas del Proyecto

### Código

- **Clases Java**: ~45
- **Líneas de código**: ~8,000
- **Cobertura de tests**: ~30%
- **Complejidad ciclomática**: Baja-Media

### Templates

- **Arquitecturas**: 3
- **Templates Freemarker**: ~50
- **Frameworks soportados**: 1 (Spring)
- **Paradigmas soportados**: 1 (Reactive)

### Documentación

- **Archivos MD**: 15
- **Diagramas Mermaid**: 8
- **Ejemplos**: 10+

---

## 🎯 Conclusión

El proyecto está en un estado **funcional y estable** para la arquitectura hexagonal con paradigma reactivo y Spring Boot. La implementación del flag `adaptersAsModules` permite una modularidad granular única en el mercado.

**Fortalezas**:
- ✅ Arquitectura limpia bien implementada
- ✅ Separación clara de responsabilidades
- ✅ Extensible y mantenible
- ✅ Documentación completa

**Áreas de mejora**:
- ⚠️ Cobertura de tests
- ⚠️ Manejo de errores
- ⚠️ Soporte multi-framework
- ⚠️ Automatización completa (wrapper)

---

**Última actualización**: Febrero 2026  
**Autor**: Equipo Pragma  
**Versión del documento**: 1.0
