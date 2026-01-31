# Estructura de Carpetas - Clean Architecture Generator

## 🎯 Principios de Arquitectura Limpia Aplicados

### Para el Plugin (core)
El plugin mismo sigue arquitectura hexagonal:
- **Domain**: Modelos, reglas de negocio, puertos
- **Application**: Casos de uso (tareas de Gradle)
- **Infrastructure**: Adaptadores (Freemarker, HTTP, File System)

### Para los Proyectos Generados
Soportamos 2 arquitecturas:
- **Hexagonal**: Puertos y adaptadores
- **Onion**: Capas concéntricas

---

## 📦 Repositorio 1: backend-architecture-design-archetype-generator-core

### Estructura Completa

```
backend-architecture-design-archetype-generator-core/
├── README.md
├── CLEAN_ARCH_GENERATOR_SPEC.md
├── COMMANDS_AND_RESPONSIBILITIES.md
├── PROJECT_STRUCTURE.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/com/pragma/archetype/
    │   │   │
    │   │   ├── domain/                          # 🔵 DOMAIN LAYER
    │   │   │   ├── model/
    │   │   │   │   ├── ProjectConfig.java
    │   │   │   │   ├── AdapterMetadata.java
    │   │   │   │   ├── ComponentInfo.java
    │   │   │   │   ├── GeneratedFile.java
    │   │   │   │   ├── ValidationResult.java
    │   │   │   │   └── TemplateData.java
    │   │   │   │
    │   │   │   ├── port/
    │   │   │   │   ├── in/                      # Puertos de entrada
    │   │   │   │   │   ├── InitializeProjectUseCase.java
    │   │   │   │   │   ├── GenerateAdapterUseCase.java
│   │   │   │   │   ├── GenerateUseCaseUseCase.java
│   │   │   │   │   ├── GenerateEntityUseCase.java
│   │   │   │   │   ├── ListComponentsUseCase.java
│   │   │   │   │   └── ValidateProjectUseCase.java
│   │   │   │   │
│   │   │   │   └── out/                     # Puertos de salida
│   │   │   │       ├── TemplateRepository.java
│   │   │   │       ├── FileSystemPort.java
│   │   │   │       ├── ConfigurationPort.java
│   │   │   │       └── HttpClientPort.java
│   │   │   │
│   │   │   ├── service/                     # Servicios de dominio
│   │   │   │   ├── ProjectValidator.java
│   │   │   │   ├── ComponentValidator.java
│   │   │   │   ├── NameValidator.java
│   │   │   │   └── DependencyResolver.java
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── ValidationException.java
│   │   │       ├── TemplateNotFoundException.java
│   │   │       └── ProjectNotInitializedException.java
│   │   │
│   │   ├── application/                     # 🟢 APPLICATION LAYER
│   │   │   ├── usecase/
│   │   │   │   ├── InitializeProjectUseCaseImpl.java
│   │   │   │   ├── GenerateAdapterUseCaseImpl.java
│   │   │   │   ├── GenerateUseCaseUseCaseImpl.java
│   │   │   │   ├── GenerateEntityUseCaseImpl.java
│   │   │   │   └── ListComponentsUseCaseImpl.java
│   │   │   │
│   │   │   └── generator/                   # Generadores específicos
│   │   │       ├── ProjectGenerator.java
│   │   │       ├── AdapterGenerator.java
│   │   │       ├── UseCaseGenerator.java
│   │   │       ├── EntityGenerator.java
│   │   │       └── MapperGenerator.java
│   │   │
│   │   └── infrastructure/                  # 🟡 INFRASTRUCTURE LAYER
│   │       │
│   │       ├── adapter/
│   │       │   ├── in/
│   │       │   │   └── gradle/              # Adaptador de entrada: Gradle Tasks
│   │       │   │       ├── InitCleanArchTask.java
│   │       │   │       ├── GenerateOutputAdapterTask.java
│   │       │   │       ├── GenerateInputAdapterTask.java
│   │       │   │       ├── GenerateUseCaseTask.java
│   │       │   │       ├── GenerateEntityTask.java
│   │       │   │       ├── GenerateMapperTask.java
│   │       │   │       ├── ListComponentsTask.java
│   │       │   │       ├── ListAdaptersTask.java
│   │       │   │       ├── AdapterInfoTask.java
│   │       │   │       ├── UpdateTemplatesTask.java
│   │       │   │       ├── ClearTemplateCacheTask.java
│   │       │   │       └── ValidateProjectTask.java
│   │       │   │
│   │       │   └── out/                     # Adaptadores de salida
│   │       │       ├── template/
│   │       │       │   ├── FreemarkerTemplateRepository.java
│   │       │       │   ├── TemplateCache.java
│   │       │       │   └── TemplateDownloader.java
│   │       │       │
│   │       │       ├── filesystem/
│   │       │       │   ├── LocalFileSystemAdapter.java
│   │       │       │   └── FileWriter.java
│   │       │       │
│   │       │       ├── config/
│   │       │       │   ├── YamlConfigurationAdapter.java
│   │       │       │   └── ConfigReader.java
│   │       │       │
│   │       │       └── http/
│   │       │           ├── OkHttpClientAdapter.java
│   │       │           └── GitHubRawContentClient.java
│   │       │
│   │       ├── config/
│   │       │   ├── CleanArchPlugin.java     # Plugin principal
│   │       │   ├── PluginExtension.java
│   │       │   └── DependencyInjection.java # DI manual o con framework
│   │       │
│   │       └── util/
│   │           ├── StringUtils.java
│   │           ├── NamingConventions.java
│   │           └── PathResolver.java
│   │
│   └── resources/
│       ├── META-INF/
│       │   └── gradle-plugins/
│       │       └── com.pragma.archetype-generator.properties
│       │
│       └── fallback-templates/               # Templates de respaldo (embebidos)
│           └── basic/
│               ├── build.gradle.kts.ftl
│               └── Application.java.ftl
│
└── src/test/
    └── java/com/pragma/archetype/
        ├── domain/
        │   └── service/
        │       ├── ProjectValidatorTest.java
        │       └── ComponentValidatorTest.java
        │
        ├── application/
        │   └── usecase/
        │       ├── InitializeProjectUseCaseTest.java
        │       └── GenerateAdapterUseCaseTest.java
        │
        └── infrastructure/
            ├── adapter/
            │   ├── in/
            │   │   └── gradle/
            │   │       └── InitCleanArchTaskTest.java
            │   └── out/
            │       └── template/
            │           └── FreemarkerTemplateRepositoryTest.java
            │
            └── integration/
                └── PluginIntegrationTest.java
```

### Explicación de Capas

#### 🔵 Domain Layer
- **Modelos puros**: Sin dependencias externas
- **Puertos**: Interfaces que definen contratos
- **Servicios de dominio**: Lógica de validación y reglas de negocio
- **Excepciones**: Excepciones del dominio

#### 🟢 Application Layer
- **Casos de uso**: Orquestación de la lógica de negocio
- **Generadores**: Lógica específica de generación de código

#### 🟡 Infrastructure Layer
- **Adaptadores de entrada**: Gradle Tasks (punto de entrada)
- **Adaptadores de salida**: Freemarker, File System, HTTP, Config
- **Configuración**: Plugin de Gradle, DI
- **Utilidades**: Helpers técnicos

---

## 📦 Repositorio 2: backend-architecture-design-archetype-generator-templates

### Estructura Completa

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
│       └── create-issues.yml
│
├── templates/
│   │
│   ├── architectures/                        # Definiciones de arquitecturas
│   │   ├── hexagonal/
│   │   │   ├── structure.yml                 # Define estructura de carpetas
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
│   └── frameworks/                           # Templates por framework
│       │
│       ├── spring/
│       │   ├── metadata.yml
│       │   │
│       │   ├── reactive/
│       │   │   ├── metadata.yml
│       │   │   ├── project/
│       │   │   │   ├── build.gradle.kts.ftl
│       │   │   │   ├── application.yml.ftl
│       │   │   │   └── Application.java.ftl
│       │   │   │
│       │   │   ├── adapters/
│       │   │   │   ├── input/
│       │   │   │   │   ├── rest/
│       │   │   │   │   │   ├── Controller.java.ftl
│       │   │   │   │   │   ├── RequestDTO.java.ftl
│       │   │   │   │   │   ├── ResponseDTO.java.ftl
│       │   │   │   │   │   ├── DTOMapper.java.ftl
│       │   │   │   │   │   ├── Config.java.ftl
│       │   │   │   │   │   ├── Test.java.ftl
│       │   │   │   │   │   └── metadata.yml
│       │   │   │   │   │
│       │   │   │   │   ├── kafka/
│       │   │   │   │   │   ├── Consumer.java.ftl
│       │   │   │   │   │   ├── Config.java.ftl
│       │   │   │   │   │   ├── Test.java.ftl
│       │   │   │   │   │   └── metadata.yml
│       │   │   │   │   │
│       │   │   │   │   └── index.json
│       │   │   │   │
│       │   │   │   └── output/
│       │   │   │       ├── redis/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       │
│       │   │   │       ├── dynamodb/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Entity.java.ftl
│       │   │   │       │   ├── Mapper.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       │
│       │   │   │       ├── postgresql/
│       │   │   │       │   ├── Adapter.java.ftl
│       │   │   │       │   ├── Entity.java.ftl
│       │   │   │       │   ├── Repository.java.ftl
│       │   │   │       │   ├── Mapper.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       │
│       │   │   │       ├── kafka/
│       │   │   │       │   ├── Producer.java.ftl
│       │   │   │       │   ├── Config.java.ftl
│       │   │   │       │   ├── Test.java.ftl
│       │   │   │       │   └── metadata.yml
│       │   │   │       │
│       │   │   │       └── index.json
│       │   │   │
│       │   │   └── usecase/
│       │   │       ├── UseCase.java.ftl
│       │   │       ├── InputPort.java.ftl
│       │   │       ├── Test.java.ftl
│       │   │       └── metadata.yml
│       │   │
│       │   └── imperative/
│       │       ├── metadata.yml
│       │       ├── project/
│       │       ├── adapters/
│       │       │   ├── input/
│       │       │   │   └── rest/
│       │       │   └── output/
│       │       │       ├── redis/
│       │       │       └── postgresql/
│       │       └── usecase/
│       │
│       └── quarkus/
│           ├── metadata.yml
│           ├── reactive/
│           │   ├── metadata.yml
│           │   ├── project/
│           │   ├── adapters/
│           │   └── usecase/
│           └── imperative/
│               ├── metadata.yml
│               ├── project/
│               ├── adapters/
│               └── usecase/
│
├── examples/                                 # Ejemplos de output esperado
│   ├── spring-reactive-hexagonal/
│   │   ├── input/
│   │   │   └── config.yml
│   │   └── expected-output/
│   │       └── src/
│   ├── spring-imperative-onion/
│   ├── quarkus-reactive-hexagonal/
│   └── README.md
│
├── tests/                                    # Scripts de validación
│   ├── validate-all.sh
│   ├── validate-freemarker.sh
│   ├── test-spring-reactive.sh
│   ├── test-quarkus-reactive.sh
│   └── README.md
│
└── docs/                                     # Documentación técnica
    ├── template-syntax.md
    ├── creating-adapters.md
    ├── creating-frameworks.md
    ├── metadata-schema.md
    └── testing-guide.md
```

### Organización de Templates

#### Por Framework y Paradigma
```
templates/frameworks/{framework}/{paradigm}/
```

Ejemplos:
- `spring/reactive/` - Spring WebFlux
- `spring/imperative/` - Spring MVC
- `quarkus/reactive/` - Quarkus con Mutiny
- `quarkus/imperative/` - Quarkus RESTEasy

#### Por Tipo de Adaptador
```
adapters/input/   - Controllers, Consumers
adapters/output/  - Repositories, Clients, Cache
```

---

## 📦 Repositorio 3: backend-architecture-design-site-docs

### Estructura Completa (Docusaurus)

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
├── docs/                                     # Documentación principal
│   ├── intro.md
│   │
│   ├── getting-started/
│   │   ├── installation.md
│   │   ├── quick-start.md
│   │   ├── first-project.md
│   │   └── concepts.md
│   │
│   ├── guides/
│   │   ├── architectures/
│   │   │   ├── hexagonal.md
│   │   │   ├── onion.md
│   │   │   └── choosing-architecture.md
│   │   │
│   │   ├── frameworks/
│   │   │   ├── spring-reactive.md
│   │   │   ├── spring-imperative.md
│   │   │   ├── quarkus-reactive.md
│   │   │   └── quarkus-imperative.md
│   │   │
│   │   └── adapters/
│   │       ├── redis.md
│   │       ├── kafka.md
│   │       ├── dynamodb.md
│   │       ├── postgresql.md
│   │       └── rest.md
│   │
│   ├── reference/
│   │   ├── commands/
│   │   │   ├── init-clean-arch.md
│   │   │   ├── generate-output-adapter.md
│   │   │   ├── generate-input-adapter.md
│   │   │   ├── generate-usecase.md
│   │   │   ├── generate-entity.md
│   │   │   └── generate-mapper.md
│   │   │
│   │   ├── configuration.md
│   │   ├── cleanarch-yml.md
│   │   └── metadata-schema.md
│   │
│   ├── contributing/
│   │   ├── overview.md
│   │   ├── creating-adapters.md
│   │   ├── creating-frameworks.md
│   │   ├── testing.md
│   │   └── pull-requests.md
│   │
│   └── troubleshooting/
│       ├── common-errors.md
│       ├── template-issues.md
│       └── faq.md
│
├── blog/                                     # Blog de anuncios
│   ├── 2026-01-31-announcing-v1.md
│   ├── 2026-02-15-kafka-adapter.md
│   └── authors.yml
│
├── src/                                      # Componentes React personalizados
│   ├── components/
│   │   ├── HomepageFeatures/
│   │   │   ├── index.js
│   │   │   └── styles.module.css
│   │   │
│   │   ├── AdapterMatrix/
│   │   │   ├── index.js
│   │   │   └── styles.module.css
│   │   │
│   │   └── CommandExample/
│   │       ├── index.js
│   │       └── styles.module.css
│   │
│   ├── css/
│   │   └── custom.css
│   │
│   └── pages/
│       ├── index.js
│       └── index.module.css
│
└── static/                                   # Archivos estáticos
    ├── img/
    │   ├── logo.svg
    │   ├── favicon.ico
    │   ├── architecture-hexagonal.png
    │   └── architecture-onion.png
    │
    └── examples/
        └── sample-projects/
            ├── payment-service.zip
            └── user-service.zip
```

---

## 🏗️ Estructura de Proyectos Generados

### Arquitectura Hexagonal (Spring Reactive)

```
payment-service/                              # Proyecto generado
├── .cleanarch.yml                            # Configuración del generador
├── .gitignore
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
├── gradlew
├── gradlew.bat
│
└── src/
    ├── main/
    │   ├── java/com/company/payment/
    │   │   │
    │   │   ├── domain/                       # 🔵 DOMAIN LAYER
    │   │   │   ├── model/                    # Entidades de dominio
    │   │   │   │   ├── Payment.java
    │   │   │   │   ├── PaymentStatus.java
    │   │   │   │   └── PaymentId.java
    │   │   │   │
    │   │   │   ├── port/
    │   │   │   │   ├── in/                   # Puertos de entrada (use cases)
    │   │   │   │   │   ├── ProcessPaymentPort.java
    │   │   │   │   │   ├── GetPaymentPort.java
    │   │   │   │   │   └── CancelPaymentPort.java
    │   │   │   │   │
    │   │   │   │   └── out/                  # Puertos de salida
    │   │   │   │       ├── PaymentRepositoryPort.java
    │   │   │   │       ├── PaymentCachePort.java
    │   │   │   │       └── PaymentEventPort.java
    │   │   │   │
    │   │   │   └── usecase/                  # Implementación de casos de uso
    │   │   │       ├── ProcessPaymentUseCase.java
    │   │   │       ├── GetPaymentUseCase.java
    │   │   │       └── CancelPaymentUseCase.java
    │   │   │
    │   │   └── infrastructure/               # 🟡 INFRASTRUCTURE LAYER
    │   │       │
    │   │       ├── adapter/
    │   │       │   ├── in/                   # Adaptadores de entrada
    │   │       │   │   └── rest/
    │   │       │   │       ├── PaymentController.java
    │   │       │   │       ├── dto/
    │   │       │   │       │   ├── PaymentRequest.java
    │   │       │   │       │   └── PaymentResponse.java
    │   │       │   │       └── mapper/
    │   │       │   │           └── PaymentDtoMapper.java
    │   │       │   │
    │   │       │   └── out/                  # Adaptadores de salida
    │   │       │       ├── redis/
    │   │       │       │   ├── PaymentCacheRedisAdapter.java
    │   │       │       │   └── config/
    │   │       │       │       └── RedisConfig.java
    │   │       │       │
    │   │       │       ├── dynamodb/
    │   │       │       │   ├── PaymentRepositoryDynamoDbAdapter.java
    │   │       │       │   ├── entity/
    │   │       │       │   │   └── PaymentEntity.java
    │   │       │       │   ├── mapper/
    │   │       │       │   │   └── PaymentEntityMapper.java
    │   │       │       │   └── config/
    │   │       │       │       └── DynamoDbConfig.java
    │   │       │       │
    │   │       │       └── kafka/
    │   │       │           ├── PaymentEventKafkaProducer.java
    │   │       │           └── config/
    │   │       │               └── KafkaConfig.java
    │   │       │
    │   │       └── config/
    │   │           ├── ApplicationConfig.java
    │   │           └── PaymentServiceApplication.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       └── application-prod.yml
    │
    └── test/
        └── java/com/company/payment/
            ├── domain/
            │   └── usecase/
            │       ├── ProcessPaymentUseCaseTest.java
            │       └── GetPaymentUseCaseTest.java
            │
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

### Arquitectura Onion (Spring Reactive)

```
payment-service/                              # Proyecto generado
├── .cleanarch.yml
├── .gitignore
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
│
└── src/
    ├── main/
    │   ├── java/com/company/payment/
    │   │   │
    │   │   ├── core/                         # 🔵 CORE (Capas internas)
    │   │   │   │
    │   │   │   ├── domain/                   # Capa más interna
    │   │   │   │   ├── Payment.java
    │   │   │   │   ├── PaymentStatus.java
    │   │   │   │   └── PaymentId.java
    │   │   │   │
    │   │   │   └── application/              # Capa de aplicación
    │   │   │       ├── service/
    │   │   │       │   ├── ProcessPaymentService.java
    │   │   │       │   ├── GetPaymentService.java
    │   │   │       │   └── CancelPaymentService.java
    │   │   │       │
    │   │   │       └── port/
    │   │   │           ├── in/
    │   │   │           │   ├── ProcessPaymentUseCase.java
    │   │   │           │   └── GetPaymentUseCase.java
    │   │   │           │
    │   │   │           └── out/
    │   │   │               ├── PaymentRepository.java
    │   │   │               ├── PaymentCache.java
    │   │   │               └── PaymentEventPublisher.java
    │   │   │
    │   │   └── infrastructure/               # 🟡 INFRASTRUCTURE (Capa externa)
    │   │       │
    │   │       ├── adapter/
    │   │       │   ├── in/
    │   │       │   │   └── rest/
    │   │       │   │       ├── PaymentController.java
    │   │       │   │       └── dto/
    │   │       │   │
    │   │       │   └── out/
    │   │       │       ├── persistence/
    │   │       │       │   ├── PaymentRepositoryAdapter.java
    │   │       │       │   └── entity/
    │   │       │       │
    │   │       │       ├── cache/
    │   │       │       │   └── PaymentCacheAdapter.java
    │   │       │       │
    │   │       │       └── messaging/
    │   │       │           └── PaymentEventPublisherAdapter.java
    │   │       │
    │   │       └── config/
    │   │           ├── ApplicationConfig.java
    │   │           └── PaymentServiceApplication.java
    │   │
    │   └── resources/
    │       └── application.yml
    │
    └── test/
        └── java/com/company/payment/
            ├── core/
            │   └── application/
            │       └── service/
            └── infrastructure/
                └── adapter/
```

---

## 📋 Comparación de Arquitecturas

### Hexagonal vs Onion

| Aspecto | Hexagonal | Onion |
|---------|-----------|-------|
| **Estructura** | `domain/` + `infrastructure/` | `core/` + `infrastructure/` |
| **Puertos** | Explícitos en `domain/port/` | En `core/application/port/` |
| **Casos de Uso** | `domain/usecase/` | `core/application/service/` |
| **Entidades** | `domain/model/` | `core/domain/` |
| **Adaptadores** | `infrastructure/adapter/` | `infrastructure/adapter/` |
| **Énfasis** | Puertos y adaptadores | Capas concéntricas |

### Cuándo usar cada una

**Hexagonal:**
- ✅ Proyectos con múltiples adaptadores
- ✅ Énfasis en puertos explícitos
- ✅ Testing con mocks de puertos
- ✅ Equipos familiarizados con el patrón

**Onion:**
- ✅ Énfasis en capas de abstracción
- ✅ Separación clara core vs infraestructura
- ✅ Proyectos con lógica de dominio compleja
- ✅ Equipos familiarizados con DDD

---

## 🎯 Principios Aplicados

### 1. Dependency Rule
Las dependencias apuntan hacia adentro:
```
Infrastructure → Application → Domain
```

### 2. Separation of Concerns
- **Domain**: Lógica de negocio pura
- **Application**: Orquestación de casos de uso
- **Infrastructure**: Detalles técnicos

### 3. Dependency Inversion
```java
// Domain define el puerto (interface)
public interface PaymentRepositoryPort {
    Mono<Payment> save(Payment payment);
}

// Infrastructure implementa el adaptador
@Component
public class PaymentRepositoryDynamoDbAdapter implements PaymentRepositoryPort {
    @Override
    public Mono<Payment> save(Payment payment) {
        // Implementación con DynamoDB
    }
}
```

### 4. Single Responsibility
Cada clase tiene una única responsabilidad:
- **Controller**: Recibir requests HTTP
- **UseCase**: Ejecutar lógica de negocio
- **Adapter**: Comunicarse con sistemas externos
- **Mapper**: Convertir entre tipos

### 5. Open/Closed
Abierto para extensión, cerrado para modificación:
- Agregar nuevos adaptadores sin modificar el dominio
- Agregar nuevos casos de uso sin modificar adaptadores

---

## 📝 Convenciones de Nombres

### Clases

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Entity | `{Name}` | `Payment` |
| Port (in) | `{Action}{Name}Port` | `ProcessPaymentPort` |
| Port (out) | `{Name}{Action}Port` | `PaymentRepositoryPort` |
| UseCase | `{Action}{Name}UseCase` | `ProcessPaymentUseCase` |
| Adapter (in) | `{Name}Controller` | `PaymentController` |
| Adapter (out) | `{Name}{Technology}Adapter` | `PaymentCacheRedisAdapter` |
| DTO | `{Name}Request/Response` | `PaymentRequest` |
| Mapper | `{Name}Mapper` | `PaymentMapper` |
| Config | `{Technology}Config` | `RedisConfig` |
| Test | `{ClassName}Test` | `ProcessPaymentUseCaseTest` |

### Paquetes

| Tipo | Patrón |
|------|--------|
| Base | `com.{company}.{service}` |
| Domain | `{base}.domain` |
| Model | `{base}.domain.model` |
| Port In | `{base}.domain.port.in` |
| Port Out | `{base}.domain.port.out` |
| UseCase | `{base}.domain.usecase` |
| Infrastructure | `{base}.infrastructure` |
| Adapter In | `{base}.infrastructure.adapter.in.{type}` |
| Adapter Out | `{base}.infrastructure.adapter.out.{technology}` |

---

## ✅ Checklist de Estructura

### Para el Plugin (core)
- [ ] Separación clara de capas (domain, application, infrastructure)
- [ ] Puertos definidos como interfaces
- [ ] Casos de uso independientes de frameworks
- [ ] Adaptadores implementan puertos
- [ ] Tests por capa

### Para Templates (templates)
- [ ] Organizados por framework/paradigma
- [ ] Metadata completo en cada adaptador
- [ ] Index.json actualizado
- [ ] Ejemplos de output esperado
- [ ] Scripts de validación

### Para Documentación (docs)
- [ ] Guía de inicio rápido
- [ ] Referencia de comandos
- [ ] Guía de contribución
- [ ] Ejemplos prácticos
- [ ] Matriz de compatibilidad

### Para Proyectos Generados
- [ ] Arquitectura limpia (hexagonal u onion)
- [ ] Separación de capas
- [ ] Puertos e interfaces
- [ ] Tests espejados
- [ ] README con instrucciones

---

**Creado:** 2026-01-31  
**Versión:** 1.0
