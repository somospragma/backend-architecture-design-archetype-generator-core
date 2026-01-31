# Roadmap de Implementación - Clean Architecture Generator

## 📋 Resumen de Decisiones Clave

### ✅ Decisiones Arquitectónicas

1. **Plugin de Gradle** (no CLI, no JAR ejecutable)
2. **3 Repositorios separados**:
   - `core`: Plugin (lógica)
   - `templates`: Templates Freemarker
   - `docs`: Documentación Docusaurus

3. **Arquitecturas soportadas**:
   - `hexagonal-single`: Un solo módulo
   - `hexagonal-multi`: 3 módulos (domain, application, infrastructure)
   - `hexagonal-multi-granular`: Módulos granulares (model, ports, usecase, cada adaptador)
   - `onion-single`: Un solo módulo
   - `onion-multi`: 3 módulos

4. **Frameworks soportados**:
   - Spring Boot (Fase 1)
   - Quarkus (Fase 4)

5. **Paradigmas**:
   - Reactive (WebFlux, R2DBC, Mutiny)
   - Imperative (Spring MVC, JPA)

6. **Separación clara**:
   - Arquitectura = Estructura de carpetas (DÓNDE)
   - Framework = Implementación (CÓMO)

---

## 🎯 Plan de Implementación

### Fase 0: Setup Inicial (1-2 días)

**Objetivo**: Preparar los 3 repositorios con estructura base

#### Repositorio: core
- [ ] Inicializar proyecto Gradle
- [ ] Configurar estructura de paquetes (domain, application, infrastructure)
- [ ] Configurar dependencias básicas (Gradle Plugin API, Freemarker, OkHttp)
- [ ] Configurar tests (JUnit 5, Mockito)
- [ ] Configurar CI/CD básico (GitHub Actions)

#### Repositorio: templates
- [ ] Crear estructura de carpetas
- [ ] Crear primer template: `hexagonal-single/structure.yml`
- [ ] Crear primer template: `spring/reactive/project/build.gradle.kts.ftl`
- [ ] Configurar validación de templates (CI)

#### Repositorio: docs
- [ ] Inicializar Docusaurus
- [ ] Crear estructura de documentación
- [ ] Página de inicio básica

---

### Fase 1: MVP - Hexagonal Single + Spring Reactive (1-2 semanas)

**Objetivo**: Comando `initCleanArch` funcional para el caso más simple

#### 1.1 Domain Layer (core)
- [ ] `ProjectConfig.java` - Modelo de configuración
- [ ] `GeneratedFile.java` - Modelo de archivo generado
- [ ] `ValidationResult.java` - Resultado de validaciones
- [ ] `TemplateRepository.java` (interface) - Puerto para templates
- [ ] `FileSystemPort.java` (interface) - Puerto para archivos
- [ ] `ConfigurationPort.java` (interface) - Puerto para config
- [ ] `ProjectValidator.java` - Validador de proyecto vacío

#### 1.2 Application Layer (core)
- [ ] `InitializeProjectUseCase.java` (interface)
- [ ] `InitializeProjectUseCaseImpl.java` - Caso de uso de inicialización
- [ ] `ProjectGenerator.java` - Generador de estructura

#### 1.3 Infrastructure Layer (core)
- [ ] `CleanArchPlugin.java` - Plugin principal de Gradle
- [ ] `InitCleanArchTask.java` - Tarea de Gradle
- [ ] `FreemarkerTemplateRepository.java` - Adaptador de templates
- [ ] `LocalFileSystemAdapter.java` - Adaptador de archivos
- [ ] `YamlConfigurationAdapter.java` - Adaptador de config
- [ ] `OkHttpClientAdapter.java` - Cliente HTTP para descargar templates

#### 1.4 Templates (templates)
- [ ] `architectures/hexagonal-single/structure.yml`
- [ ] `architectures/hexagonal-single/project/build.gradle.kts.ftl`
- [ ] `architectures/hexagonal-single/project/settings.gradle.kts.ftl`
- [ ] `architectures/hexagonal-single/project/application.yml.ftl`
- [ ] `architectures/hexagonal-single/project/Application.java.ftl`
- [ ] `architectures/hexagonal-single/project/.gitignore.ftl`
- [ ] `architectures/hexagonal-single/project/README.md.ftl`
- [ ] `frameworks/spring/reactive/metadata.yml`
- [ ] `frameworks/spring/reactive/project/build.gradle.kts.ftl`

#### 1.5 Tests
- [ ] `InitCleanArchTaskTest.java` - Test de tarea
- [ ] `ProjectValidatorTest.java` - Test de validador
- [ ] `ProjectGeneratorTest.java` - Test de generador
- [ ] Test de integración end-to-end

#### 1.6 Documentación
- [ ] README.md del core
- [ ] Guía de inicio rápido
- [ ] Documentación del comando initCleanArch

**Entregable**: Plugin que genera proyecto hexagonal single con Spring Reactive

---

### Fase 2: Generadores de Componentes (2-3 semanas)

**Objetivo**: Comandos para generar entidades, casos de uso y adaptadores

#### 2.1 Generar Entidad
- [ ] `GenerateEntityUseCase.java`
- [ ] `GenerateEntityTask.java`
- [ ] `EntityGenerator.java`
- [ ] Templates de entidad
- [ ] Tests

#### 2.2 Generar Caso de Uso
- [ ] `GenerateUseCaseUseCase.java`
- [ ] `GenerateUseCaseTask.java`
- [ ] `UseCaseGenerator.java`
- [ ] Templates de caso de uso
- [ ] Tests

#### 2.3 Generar Adaptador de Salida (Redis)
- [ ] `GenerateAdapterUseCase.java`
- [ ] `GenerateOutputAdapterTask.java`
- [ ] `AdapterGenerator.java`
- [ ] Templates de Redis (Spring Reactive)
- [ ] Tests

#### 2.4 Generar Adaptador de Entrada (REST)
- [ ] `GenerateInputAdapterTask.java`
- [ ] Templates de REST Controller
- [ ] Tests

**Entregable**: Plugin con generadores básicos funcionales

---

### Fase 3: Multi-Module Support (2 semanas)

**Objetivo**: Soporte para arquitecturas multi-módulo

#### 3.1 Hexagonal Multi (3 módulos)
- [ ] `hexagonal-multi/structure.yml`
- [ ] Templates de módulos
- [ ] Lógica de generación multi-módulo
- [ ] Tests

#### 3.2 Hexagonal Multi Granular
- [ ] `hexagonal-multi-granular/structure.yml`
- [ ] Templates de módulos granulares
- [ ] Lógica de actualización de settings.gradle.kts
- [ ] Tests

**Entregable**: Soporte completo para multi-módulo

---

### Fase 4: Más Frameworks y Paradigmas (2-3 semanas)

#### 4.1 Spring Imperative
- [ ] Templates de Spring MVC
- [ ] Templates de JPA
- [ ] Tests

#### 4.2 Onion Architecture
- [ ] Templates de Onion
- [ ] Generadores específicos
- [ ] Tests

#### 4.3 Más Adaptadores
- [ ] DynamoDB
- [ ] PostgreSQL (R2DBC y JPA)
- [ ] Kafka
- [ ] Tests

**Entregable**: Soporte para múltiples frameworks y arquitecturas, documentaciòn de cada adptador y template en docusaurus

---

### Fase 5: Quarkus (Futuro)
- [ ] Templates de Quarkus Reactive
- [ ] Templates de Quarkus Imperative
- [ ] Adaptadores específicos de Quarkus

---

## 🚀 Propuesta de Arranque AHORA

### Opción A: Empezar por el Core (Recomendado)

**Día 1-2: Setup + Domain Layer**
1. Inicializar proyecto `core` con Gradle
2. Crear estructura de paquetes
3. Implementar modelos del dominio
4. Implementar puertos (interfaces)
5. Implementar validadores

**Día 3-4: Application Layer**
6. Implementar caso de uso de inicialización
7. Implementar generador de proyecto
8. Tests unitarios

**Día 5-7: Infrastructure Layer**
9. Implementar plugin de Gradle
10. Implementar tarea InitCleanArch
11. Implementar adaptadores (FileSystem, Templates)
12. Tests de integración

**Día 8-10: Templates + Prueba End-to-End**
13. Crear templates básicos
14. Probar generación completa
15. Ajustes y refinamiento

### Opción B: Empezar por Templates (Alternativa)

**Día 1-2: Templates**
1. Crear estructura de templates
2. Crear templates de hexagonal-single
3. Crear templates de Spring Reactive

**Día 3-10: Core**
4. Implementar plugin completo
5. Integrar con templates

---

## 📦 Estructura de Archivos a Crear (Fase 1)

### core/src/main/java/com/pragma/archetype/

```
domain/
├── model/
│   ├── ProjectConfig.java
│   ├── ArchitectureType.java
│   ├── Paradigm.java
│   ├── Framework.java
│   ├── GeneratedFile.java
│   └── ValidationResult.java
├── port/
│   ├── in/
│   │   └── InitializeProjectUseCase.java
│   └── out/
│       ├── TemplateRepository.java
│       ├── FileSystemPort.java
│       └── ConfigurationPort.java
└── service/
    └── ProjectValidator.java

application/
├── usecase/
│   └── InitializeProjectUseCaseImpl.java
└── generator/
    └── ProjectGenerator.java

infrastructure/
├── adapter/
│   ├── in/
│   │   └── gradle/
│   │       └── InitCleanArchTask.java
│   └── out/
│       ├── template/
│       │   └── FreemarkerTemplateRepository.java
│       ├── filesystem/
│       │   └── LocalFileSystemAdapter.java
│       ├── config/
│       │   └── YamlConfigurationAdapter.java
│       └── http/
│           └── OkHttpClientAdapter.java
└── config/
    └── CleanArchPlugin.java
```

---

## ✅ Checklist de Arranque

- [ ] Decidir por dónde empezar (Opción A o B)
- [ ] Crear repositorio `core` en GitHub
- [ ] Crear repositorio `templates` en GitHub
- [ ] Crear repositorio `docs` en GitHub
- [ ] Configurar accesos y permisos
- [ ] Inicializar proyecto Gradle en `core`
- [ ] Crear primer commit

---

## 🎯 Mi Recomendación

**Empezar por Opción A (Core primero)** porque:
1. ✅ Establece la arquitectura limpia desde el inicio
2. ✅ Permite testear la lógica sin depender de templates
3. ✅ Los templates son más fáciles de ajustar después
4. ✅ Podemos usar templates embebidos temporalmente

**Primer objetivo concreto**: 
Tener `./gradlew initCleanArch` funcionando con templates embebidos (sin descargar) que genere un proyecto hexagonal-single con Spring Reactive.

---

## ❓ Preguntas antes de arrancar

1. ¿Tienes los 3 repositorios creados en GitHub?
2. ¿Prefieres Java o Kotlin para el plugin?
3. ¿Qué versión de Java usamos? (17, 21)
4. ¿Empezamos por Opción A (Core) u Opción B (Templates)?

---

**Creado:** 2026-01-31  
**Versión:** 1.0
