# Flujo de Uso del Plugin - Cómo Funciona

## 🎯 Concepto: El Plugin NO se agrega a un proyecto vacío

### ❌ Malentendido Común

```bash
# ESTO NO ES ASÍ:
cd mi-proyecto-existente/
# Agregar plugin a build.gradle.kts
./gradlew initCleanArch  # ❌ NO funciona así
```

### ✅ Flujo Correcto

El plugin se usa para **CREAR** un proyecto nuevo desde cero, no para convertir uno existente.

---

## 🚀 Flujo Completo Paso a Paso

### Opción 1: Crear Proyecto Nuevo (Recomendado)

```bash
# 1. Crear carpeta vacía
mkdir payment-service
cd payment-service

# 2. Crear SOLO build.gradle.kts con el plugin
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.pragma.archetype-generator") version "1.0.0"
}
EOF

# 3. Inicializar wrapper de Gradle (opcional, el plugin puede hacerlo)
gradle wrapper

# 4. Ejecutar initCleanArch
./gradlew initCleanArch \
  --architecture=hexagonal-multi-granular \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment

# 5. El plugin genera TODO:
# - settings.gradle.kts
# - build.gradle.kts (actualizado con dependencias)
# - Estructura de carpetas y módulos
# - .cleanarch.yml
# - application.yml
# - Clase Application.java
# - .gitignore
# - README.md
```

**Resultado:**
```
payment-service/
├── .cleanarch.yml                    # ← GENERADO
├── .gitignore                        # ← GENERADO
├── README.md                         # ← GENERADO
├── build.gradle.kts                  # ← ACTUALIZADO
├── settings.gradle.kts               # ← GENERADO
├── gradle/                           # ← GENERADO
├── gradlew                           # ← GENERADO
├── gradlew.bat                       # ← GENERADO
│
├── domain/                           # ← GENERADO
│   ├── model/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/...
│   ├── usecase/
│   └── ports/
│
├── application/                      # ← GENERADO
│   └── app-service/
│
├── infrastructure/                   # ← GENERADO
│   ├── driven-adapters/
│   └── entry-points/
│
└── applications/                     # ← GENERADO
    └── app-service/
        ├── build.gradle.kts
        └── src/
            ├── main/
            │   ├── java/.../PaymentServiceApplication.java
            │   └── resources/
            │       └── application.yml
            └── test/
```

---

### Opción 2: Usar Gradle Init Plugin (Alternativa)

Si quieres un flujo más "estándar" de Gradle:

```bash
# 1. Crear proyecto con Gradle init
mkdir payment-service
cd payment-service
gradle init --type basic --dsl kotlin

# 2. Agregar el plugin al build.gradle.kts generado
cat >> build.gradle.kts << 'EOF'

plugins {
    id("com.pragma.archetype-generator") version "1.0.0"
}
EOF

# 3. Ejecutar initCleanArch
./gradlew initCleanArch \
  --architecture=hexagonal-single \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment
```

---

## 🔍 ¿Qué hace `initCleanArch` exactamente?

### Validaciones Previas

1. **Verifica que el proyecto esté "vacío"**:
   - ✅ Permitido: `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `.git/`
   - ❌ No permitido: `src/`, `pom.xml`, otros archivos de código

2. **Valida parámetros**:
   - Arquitectura válida
   - Framework soportado
   - Paradigma válido
   - Paquete Java válido

### Generación

1. **Descarga templates** desde el repositorio remoto
2. **Procesa templates** con Freemarker
3. **Genera estructura** según la arquitectura elegida
4. **Crea archivos** de configuración
5. **Actualiza build.gradle.kts** con dependencias del framework
6. **Crea .cleanarch.yml** con la configuración del proyecto

---

## 📋 Contenido de .cleanarch.yml (Generado)

```yaml
# Generado automáticamente por clean-arch-generator
project:
  name: payment-service
  basePackage: com.company.payment
  createdAt: 2026-01-31T10:30:00Z
  pluginVersion: 1.0.0

architecture:
  type: hexagonal-multi-granular
  paradigm: reactive
  framework: spring

structure:
  domain:
    model: domain/model
    usecase: domain/usecase
    ports: domain/ports
  application:
    service: application/app-service
  infrastructure:
    drivenAdapters: infrastructure/driven-adapters
    entryPoints: infrastructure/entry-points
  applications:
    main: applications/app-service

dependencies:
  springBoot: 3.2.0
  java: 17
  kotlin: false
  webflux: true
  r2dbc: true
  mapstruct: 1.5.5.Final

modules:
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
  - name: app-service
    path: application/app-service
    type: library
    dependencies: [model, usecase, ports]
  - name: main-app
    path: applications/app-service
    type: application
    dependencies: [usecase, app-service]

components:
  adapters:
    input: []
    output: []
  usecases: []
  entities: []
  mappers: []

templates:
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  version: 1.0.0
  cache: true
```

---

## 🎨 Después de initCleanArch

Una vez inicializado, puedes generar componentes:

```bash
# Generar entidad
./gradlew generateEntity \
  --name=Payment \
  --fields="id:String,amount:BigDecimal"

# Generar caso de uso
./gradlew generateUseCase \
  --name=ProcessPayment

# Generar adaptador de salida (Redis)
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache

# Generar adaptador de entrada (REST)
./gradlew generateInputAdapter \
  --type=rest \
  --name=Payment

# Ver componentes generados
./gradlew listComponents
```

---

## ❓ ¿Y si ya tengo un proyecto existente?

### Escenario: Tengo un proyecto Spring Boot existente

**Opción 1: No usar el plugin (recomendado)**
- El plugin está diseñado para proyectos nuevos
- Refactorizar manualmente a arquitectura limpia

**Opción 2: Migración manual**
- Crear proyecto nuevo con el plugin
- Migrar código manualmente del proyecto viejo al nuevo
- Mantener la estructura generada

**Opción 3: Comando de migración (futuro)**
```bash
# Comando futuro (no implementado aún)
./gradlew migrateToCleanArch \
  --architecture=hexagonal-single \
  --backup=true
```

---

## 🔄 Flujo Visual Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Usuario crea carpeta vacía                              │
│    mkdir payment-service && cd payment-service             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Usuario crea build.gradle.kts con el plugin             │
│    plugins {                                                │
│      id("com.pragma.archetype-generator") version "1.0.0"  │
│    }                                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Usuario ejecuta initCleanArch                           │
│    ./gradlew initCleanArch \                                │
│      --architecture=hexagonal-multi-granular \              │
│      --paradigm=reactive \                                  │
│      --framework=spring \                                   │
│      --package=com.company.payment                          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Plugin valida                                            │
│    ✓ Proyecto vacío                                         │
│    ✓ Parámetros válidos                                     │
│    ✓ Arquitectura soportada                                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Plugin descarga templates                                │
│    - architectures/hexagonal-multi-granular/                │
│    - frameworks/spring/reactive/                            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. Plugin genera estructura completa                        │
│    - Carpetas y módulos                                     │
│    - build.gradle.kts (actualizado)                         │
│    - settings.gradle.kts                                    │
│    - .cleanarch.yml                                         │
│    - Application.java                                       │
│    - application.yml                                        │
│    - README.md                                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Proyecto listo para desarrollo                          │
│    ./gradlew build          ← Compila                       │
│    ./gradlew bootRun        ← Ejecuta                       │
│    ./gradlew generateEntity ← Genera componentes            │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 Ejemplo Completo Real

```bash
# Terminal 1: Crear proyecto
$ mkdir payment-service
$ cd payment-service

$ cat > build.gradle.kts << 'EOF'
plugins {
    id("com.pragma.archetype-generator") version "1.0.0"
}
EOF

$ gradle wrapper

$ ./gradlew initCleanArch \
  --architecture=hexagonal-multi-granular \
  --paradigm=reactive \
  --framework=spring \
  --package=com.company.payment

> Task :initCleanArch
✓ Validating project structure...
✓ Downloading templates...
✓ Generating project structure...
✓ Creating modules...
  - domain:model
  - domain:ports
  - domain:usecase
  - application:app-service
  - applications:app-service
✓ Generating configuration files...
✓ Updating build.gradle.kts...
✓ Creating .cleanarch.yml...

✅ Project initialized successfully!

Architecture: Hexagonal (Multi Module Granular)
Framework: Spring Boot 3.2.0 (Reactive)
Package: com.company.payment

Next steps:
  1. ./gradlew build
  2. ./gradlew generateEntity --name=Payment
  3. ./gradlew generateUseCase --name=ProcessPayment
  4. ./gradlew bootRun

BUILD SUCCESSFUL in 12s

$ tree -L 3
payment-service/
├── .cleanarch.yml
├── .gitignore
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── domain/
│   ├── model/
│   │   └── build.gradle.kts
│   ├── ports/
│   │   └── build.gradle.kts
│   └── usecase/
│       └── build.gradle.kts
├── application/
│   └── app-service/
│       └── build.gradle.kts
├── infrastructure/
│   ├── driven-adapters/
│   └── entry-points/
└── applications/
    └── app-service/
        ├── build.gradle.kts
        └── src/

$ ./gradlew build
BUILD SUCCESSFUL in 8s

$ ./gradlew bootRun
> Task :applications:app-service:bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2026-01-31 10:30:00.000  INFO --- [main] PaymentServiceApplication : Starting PaymentServiceApplication
2026-01-31 10:30:01.000  INFO --- [main] PaymentServiceApplication : Started PaymentServiceApplication in 2.5 seconds
```

---

## ✅ Resumen

| Pregunta | Respuesta |
|----------|-----------|
| ¿Dónde se agrega el plugin? | En un proyecto **vacío** (solo build.gradle.kts) |
| ¿Qué hace initCleanArch? | **Genera** toda la estructura del proyecto |
| ¿Puedo usarlo en proyecto existente? | No recomendado, mejor crear nuevo y migrar |
| ¿Qué archivos debe tener antes? | Solo `build.gradle.kts` con el plugin |
| ¿Qué genera? | TODO: estructura, módulos, configs, código base |

---

**Creado:** 2026-01-31  
**Versión:** 1.0
