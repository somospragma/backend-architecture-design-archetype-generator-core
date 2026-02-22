# Flujo de Funciones del Generador de Arquitectura

Este documento describe el flujo de ejecución de las principales funciones del generador de arquitectura limpia.

## 1. Flujo de Inicialización del Proyecto (initCleanArch)

```mermaid
sequenceDiagram
    participant User
    participant InitCleanArchTask
    participant InitializeProjectUseCaseImpl
    participant ProjectValidator
    participant ProjectGenerator
    participant YamlConfigurationAdapter
    participant FileSystemPort

    User->>InitCleanArchTask: gradle initCleanArch --architecture=hexagonal-multi-granular
    InitCleanArchTask->>InitCleanArchTask: validateInputs()
    InitCleanArchTask->>InitCleanArchTask: createProjectConfig()
    Note over InitCleanArchTask: Si architecture == HEXAGONAL_MULTI_GRANULAR<br/>entonces adaptersAsModules = true
    
    InitCleanArchTask->>InitializeProjectUseCaseImpl: execute(projectPath, config)
    
    InitializeProjectUseCaseImpl->>ProjectValidator: validate(projectPath, config)
    ProjectValidator->>FileSystemPort: exists(projectPath)
    ProjectValidator->>YamlConfigurationAdapter: configurationExists(projectPath)
    ProjectValidator-->>InitializeProjectUseCaseImpl: ValidationResult
    
    alt Validation Failed
        InitializeProjectUseCaseImpl-->>InitCleanArchTask: InitializationResult(errors)
    else Validation Success
        InitializeProjectUseCaseImpl->>ProjectGenerator: generate(projectPath, config)
        
        alt architecture.isMultiModule()
            ProjectGenerator->>ProjectGenerator: generateMultiModuleStructure()
            Note over ProjectGenerator: Genera estructura según tipo:<br/>- hexagonal-multi<br/>- hexagonal-multi-granular
        else Single Module
            ProjectGenerator->>ProjectGenerator: generateSingleModuleStructure()
        end
        
        ProjectGenerator->>FileSystemPort: writeFile(generatedFiles)
        ProjectGenerator-->>InitializeProjectUseCaseImpl: List<GeneratedFile>
        
        InitializeProjectUseCaseImpl->>YamlConfigurationAdapter: writeConfiguration(projectPath, config)
        Note over YamlConfigurationAdapter: Escribe .cleanarch.yml con:<br/>- architecture.type<br/>- adaptersAsModules
        
        InitializeProjectUseCaseImpl-->>InitCleanArchTask: InitializationResult(success, files)
    end
    
    InitCleanArchTask-->>User: ✓ Project initialized successfully!
```

## 2. Flujo de Generación de Adaptador (generateOutputAdapter)

```mermaid
sequenceDiagram
    participant User
    participant GenerateOutputAdapterTask
    participant GenerateAdapterUseCaseImpl
    participant AdapterValidator
    participant AdapterGenerator
    participant ProjectGenerator
    participant YamlConfigurationAdapter
    participant FileSystemPort

    User->>GenerateOutputAdapterTask: gradle generateOutputAdapter --name=UserRepository --type=redis
    GenerateOutputAdapterTask->>GenerateOutputAdapterTask: validateInputs()
    GenerateOutputAdapterTask->>GenerateOutputAdapterTask: resolvePackageName()
    GenerateOutputAdapterTask->>GenerateOutputAdapterTask: createAdapterConfig()
    
    GenerateOutputAdapterTask->>GenerateAdapterUseCaseImpl: execute(projectPath, adapterConfig)
    
    GenerateAdapterUseCaseImpl->>AdapterValidator: validate(projectPath, adapterConfig)
    AdapterValidator->>YamlConfigurationAdapter: readConfiguration(projectPath)
    AdapterValidator-->>GenerateAdapterUseCaseImpl: ValidationResult
    
    alt Validation Failed
        GenerateAdapterUseCaseImpl-->>GenerateOutputAdapterTask: GenerationResult(errors)
    else Validation Success
        GenerateAdapterUseCaseImpl->>YamlConfigurationAdapter: readConfiguration(projectPath)
        YamlConfigurationAdapter-->>GenerateAdapterUseCaseImpl: ProjectConfig
        Note over GenerateAdapterUseCaseImpl: Lee adaptersAsModules del config
        
        GenerateAdapterUseCaseImpl->>AdapterGenerator: generate(projectPath, adapterConfig, projectConfig)
        
        alt projectConfig.adaptersAsModules() == true
            AdapterGenerator->>AdapterGenerator: generateAdapterAsModule()
            Note over AdapterGenerator: 1. Crea módulo en infrastructure/driven-adapters/{name}
            AdapterGenerator->>AdapterGenerator: generateModuleBuildFile()
            Note over AdapterGenerator: 2. Genera build.gradle.kts del módulo
            AdapterGenerator->>AdapterGenerator: generateAdapterInModule()
            Note over AdapterGenerator: 3. Genera archivos Java del adaptador
            AdapterGenerator->>AdapterGenerator: generateMapperInModule()
            AdapterGenerator->>AdapterGenerator: generateDataEntityInModule()
            
            AdapterGenerator->>ProjectGenerator: addModuleToSettings(projectPath, modulePath)
            Note over ProjectGenerator: 4. Actualiza settings.gradle.kts<br/>include("infrastructure:driven-adapters:userrepository")
            
            AdapterGenerator->>ProjectGenerator: addDependencyToModule(projectPath, "application/app-service", modulePath)
            Note over ProjectGenerator: 5. Actualiza app-service/build.gradle.kts<br/>implementation(project(":infrastructure:driven-adapters:userrepository"))
        else adaptersAsModules == false
            AdapterGenerator->>AdapterGenerator: generateAdapterInPlace()
            Note over AdapterGenerator: Genera archivos en estructura existente
        end
        
        AdapterGenerator-->>GenerateAdapterUseCaseImpl: List<GeneratedFile>
        
        GenerateAdapterUseCaseImpl->>FileSystemPort: writeFile(generatedFiles)
        GenerateAdapterUseCaseImpl-->>GenerateOutputAdapterTask: GenerationResult(success, files)
    end
    
    GenerateOutputAdapterTask-->>User: ✓ Adapter generated successfully!
```

## 3. Flujo de Generación Multi-Módulo Granular

```mermaid
flowchart TD
    A[ProjectGenerator.generate] --> B{architecture type?}
    
    B -->|HEXAGONAL_MULTI_GRANULAR| C[generateGranularStructure]
    B -->|HEXAGONAL_MULTI| D[generateMultiModuleStructure]
    B -->|HEXAGONAL_SINGLE| E[generateSingleModuleStructure]
    
    C --> C1[generateProjectFiles]
    C1 --> C1a[build.gradle.kts con BOM]
    C1 --> C1b[settings.gradle.kts con pluginManagement]
    C1 --> C1c[README.md]
    C1 --> C1d[.gitignore]
    
    C1 --> C2[generateGranularDomainModelModule]
    C2 --> C2a[domain/model/build.gradle.kts]
    C2 --> C2b[domain/model/src/main/java]
    
    C2 --> C3[generateGranularDomainPortsModule]
    C3 --> C3a[domain/ports/build.gradle.kts]
    C3 --> C3b[domain/ports/src/main/java]
    
    C3 --> C4[generateGranularDomainUseCaseModule]
    C4 --> C4a[domain/usecase/build.gradle.kts]
    C4 --> C4b[domain/usecase/src/main/java]
    
    C4 --> C5[generateGranularAppServiceModule]
    C5 --> C5a[application/app-service/build.gradle.kts]
    C5 --> C5b[application/app-service/src/main/java]
    C5 --> C5c[BeanConfiguration.java]
    C5 --> C5d[Application.java]
    
    C5 --> C6[Crear carpetas infrastructure]
    C6 --> C6a[infrastructure/entry-points/]
    C6 --> C6b[infrastructure/driven-adapters/]
    
    C6 --> F[Return GeneratedFiles]
    
    style C fill:#e1f5ff
    style C1 fill:#fff4e1
    style C2 fill:#e8f5e9
    style C3 fill:#e8f5e9
    style C4 fill:#e8f5e9
    style C5 fill:#fff3e0
    style C6 fill:#f3e5f5
```

## 4. Flujo de Actualización de Archivos de Configuración

```mermaid
flowchart TD
    A[AdapterGenerator.generateAdapterAsModule] --> B[Generar archivos del módulo]
    
    B --> C[ProjectGenerator.addModuleToSettings]
    C --> C1[Leer settings.gradle.kts]
    C1 --> C2{¿Ya incluido?}
    C2 -->|Sí| C3[Skip]
    C2 -->|No| C4[Agregar include statement]
    C4 --> C5[Escribir settings.gradle.kts]
    
    C5 --> D[ProjectGenerator.addDependencyToModule]
    D --> D1[Leer app-service/build.gradle.kts]
    D1 --> D2{¿Ya existe dependencia?}
    D2 -->|Sí| D3[Skip]
    D2 -->|No| D4[Buscar bloque dependencies]
    D4 --> D5[Insertar implementation statement]
    D5 --> D6[Escribir build.gradle.kts]
    
    D6 --> E[Módulo listo para usar]
    
    style C fill:#e3f2fd
    style D fill:#f3e5f5
    style E fill:#c8e6c9
```

## 5. Estructura de Clases y Responsabilidades

```mermaid
classDiagram
    class InitCleanArchTask {
        +String architecture
        +String paradigm
        +String framework
        +String packageName
        +initializeProject()
        -validateInputs()
        -createProjectConfig()
    }
    
    class InitializeProjectUseCaseImpl {
        -ProjectValidator validator
        -ProjectGenerator generator
        -ConfigurationPort configurationPort
        +execute(Path, ProjectConfig) InitializationResult
    }
    
    class ProjectGenerator {
        -TemplateRepository templateRepository
        -FileSystemPort fileSystemPort
        +generate(Path, ProjectConfig) List~GeneratedFile~
        +addModuleToSettings(Path, String)
        +addDependencyToModule(Path, String, String)
        -generateGranularStructure()
        -generateMultiModuleStructure()
        -generateSingleModuleStructure()
    }
    
    class AdapterGenerator {
        -TemplateRepository templateRepository
        -FileSystemPort fileSystemPort
        -ProjectGenerator projectGenerator
        +generate(Path, AdapterConfig, ProjectConfig) List~GeneratedFile~
        -generateAdapterAsModule()
        -generateAdapterInPlace()
        -generateModuleBuildFile()
    }
    
    class YamlConfigurationAdapter {
        +readConfiguration(Path) Optional~ProjectConfig~
        +writeConfiguration(Path, ProjectConfig)
        -parseConfiguration(Map) ProjectConfig
        -toYamlMap(ProjectConfig) Map
    }
    
    class ProjectConfig {
        +String name
        +String basePackage
        +ArchitectureType architecture
        +boolean adaptersAsModules
    }
    
    InitCleanArchTask --> InitializeProjectUseCaseImpl
    InitializeProjectUseCaseImpl --> ProjectGenerator
    InitializeProjectUseCaseImpl --> YamlConfigurationAdapter
    GenerateOutputAdapterTask --> GenerateAdapterUseCaseImpl
    GenerateAdapterUseCaseImpl --> AdapterGenerator
    AdapterGenerator --> ProjectGenerator
    YamlConfigurationAdapter --> ProjectConfig
```

## 6. Decisiones Clave en el Flujo

### 6.1 ¿Cuándo se activa adaptersAsModules?

```mermaid
flowchart TD
    A[InitCleanArchTask.createProjectConfig] --> B{architecture type?}
    B -->|HEXAGONAL_MULTI_GRANULAR| C[adaptersAsModules = true]
    B -->|Otros| D[adaptersAsModules = false]
    
    C --> E[ProjectConfig con flag]
    D --> E
    
    E --> F[YamlConfigurationAdapter.writeConfiguration]
    F --> G[.cleanarch.yml generado]
    
    style C fill:#c8e6c9
    style D fill:#ffccbc
```

### 6.2 ¿Cómo decide el AdapterGenerator qué hacer?

```mermaid
flowchart TD
    A[AdapterGenerator.generate] --> B[Leer ProjectConfig]
    B --> C{projectConfig.adaptersAsModules?}
    
    C -->|true| D[generateAdapterAsModule]
    D --> D1[Crear módulo Gradle]
    D1 --> D2[Generar build.gradle.kts]
    D2 --> D3[Generar archivos Java]
    D3 --> D4[Actualizar settings.gradle.kts]
    D4 --> D5[Actualizar app-service/build.gradle.kts]
    
    C -->|false| E[generateAdapterInPlace]
    E --> E1[Generar en estructura existente]
    E1 --> E2[Sin módulo separado]
    
    D5 --> F[Return GeneratedFiles]
    E2 --> F
    
    style D fill:#e1f5ff
    style E fill:#fff4e1
```

## 7. Archivos Generados por Arquitectura

### Hexagonal Multi-Granular (adaptersAsModules = true)

```
test-hexagonal-granular/
├── build.gradle.kts                    # Root con plugin y BOM
├── settings.gradle.kts                 # Con pluginManagement
├── .cleanarch.yml                      # Con adaptersAsModules: true
├── domain/
│   ├── model/
│   │   └── build.gradle.kts           # Módulo independiente
│   ├── ports/
│   │   └── build.gradle.kts           # Módulo independiente
│   └── usecase/
│       └── build.gradle.kts           # Módulo independiente
├── application/
│   └── app-service/
│       └── build.gradle.kts           # Módulo con dependencias
└── infrastructure/
    ├── entry-points/                   # Carpeta (no módulo)
    └── driven-adapters/                # Carpeta (no módulo)
        └── userrepository/             # Módulo creado dinámicamente
            ├── build.gradle.kts        # Con dependencias de Spring/Redis
            └── src/main/java/...       # Código del adaptador
```

## 8. Puntos de Mejora Identificados

1. **Copia de archivos binarios**: El wrapper de Gradle no se copia automáticamente
2. **Validación de versiones**: No hay validación de compatibilidad Lombok/Java
3. **Manejo de errores**: Los errores de template no son muy descriptivos
4. **Testing**: Falta cobertura de tests para el flujo completo
5. **Documentación**: Falta documentación inline en algunos métodos complejos

---

**Nota**: Este documento describe el estado actual de la implementación. Para code review, enfocarse en:
- Separación de responsabilidades
- Manejo de errores
- Validaciones
- Claridad del código
- Tests unitarios
