# Comandos y Responsabilidades del Proyecto

## 📦 Repositorios y Responsabilidades

### 1️⃣ backend-architecture-design-archetype-generator-core
**Responsabilidad**: Motor del plugin de Gradle

**Qué contiene:**
- Lógica del plugin de Gradle
- Tareas (Tasks) de Gradle
- Validadores
- Motor de procesamiento de templates (Freemarker)
- Descarga y caché de templates remotos
- Generación de archivos
- Actualización de `.cleanarch.yml`

**Qué NO contiene:**
- ❌ Templates (.ftl files)
- ❌ Documentación de usuario
- ❌ Ejemplos de proyectos generados

**Publicación**: Maven Central o repositorio público
**Versionado**: `1.0.0`, `1.1.0`, etc.

---

### 2️⃣ backend-architecture-design-archetype-generator-templates
**Responsabilidad**: Templates y metadata de generación

**Qué contiene:**
- Templates Freemarker (.ftl) organizados por:
  - Framework (spring, quarkus)
  - Paradigma (reactive, imperative)
  - Tipo de adaptador (redis, kafka, dynamodb, etc.)
- Metadata de adaptadores (metadata.yml)
- Índices de adaptadores disponibles (index.json)
- Scripts de validación de templates
- Ejemplos de output esperado

**Qué NO contiene:**
- ❌ Código del plugin
- ❌ Lógica de negocio
- ❌ Documentación de usuario (solo docs técnicos de templates)

**Publicación**: GitHub (público)
**Versionado**: Tags de Git (`v1.0.0`, `v1.1.0`)

---

### 3️⃣ backend-architecture-design-site-docs
**Responsabilidad**: Documentación de usuario con Docusaurus

**Qué contiene:**
- Guías de inicio rápido
- Referencia de comandos
- Tutoriales paso a paso
- Guía de contribución
- Blog de anuncios
- Matriz de compatibilidad de adaptadores
- Ejemplos de uso

**Qué NO contiene:**
- ❌ Código del plugin
- ❌ Templates
- ❌ Lógica de generación

**Publicación**: GitHub Pages o Vercel
**URL**: `https://docs.clean-arch-generator.com` (o similar)

---

## 🎯 Comandos Disponibles del Plugin

### Comandos de Inicialización

#### `initCleanArch`
Inicializa un proyecto nuevo con arquitectura limpia.

```bash
./gradlew initCleanArch \
  --architecture=<hexagonal|onion> \
  --paradigm=<reactive|imperative> \
  --framework=<spring|quarkus> \
  --package=<com.company.service>
```

**Parámetros:**
- `architecture`: Tipo de arquitectura (hexagonal u onion)
- `paradigm`: Paradigma de programación (reactive o imperative)
- `framework`: Framework a usar (spring o quarkus)
- `package`: Paquete base del proyecto

**Genera:**
- Estructura completa de carpetas
- `build.gradle.kts` con dependencias
- `settings.gradle.kts`
- `.cleanarch.yml` (configuración del proyecto)
- `application.yml` o `application.properties`
- Clase principal (`Application.java`)
- `.gitignore`
- `README.md`

**Validaciones:**
- ✅ Proyecto vacío (solo archivos de Gradle permitidos)
- ✅ Parámetros válidos
- ✅ Formato de paquete Java válido

---

### Comandos de Generación de Componentes

#### `generateOutputAdapter`
Genera un adaptador de salida (repositorios, clientes externos, caché).

```bash
./gradlew generateOutputAdapter \
  --type=<redis|dynamodb|postgresql|mongodb|kafka|sqs|httpclient> \
  --name=<NombreDelAdaptador> \
  [--opciones-específicas]
```

**Ejemplo Redis:**
```bash
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache \
  --cacheStrategy=writeThrough \
  --ttl=3600 \
  --keyPrefix=payment
```

**Ejemplo Kafka Producer:**
```bash
./gradlew generateOutputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events \
  --partitions=3
```

**Genera:**
- Clase del adaptador (`{Name}RedisAdapter.java`)
- Interface del puerto (`{Name}Port.java`)
- Configuración específica (`RedisConfig.java`)
- Test vacío (`{Name}RedisAdapterTest.java`)
- Actualiza `.cleanarch.yml`

---

#### `generateInputAdapter`
Genera un adaptador de entrada (controllers, consumers).

```bash
./gradlew generateInputAdapter \
  --type=<rest|graphql|kafka|sqs|grpc> \
  --name=<NombreDelAdaptador> \
  [--opciones-específicas]
```

**Ejemplo REST:**
```bash
./gradlew generateInputAdapter \
  --type=rest \
  --name=Payment \
  --basePath=/api/v1/payments
```

**Ejemplo Kafka Consumer:**
```bash
./gradlew generateInputAdapter \
  --type=kafka \
  --name=PaymentEvents \
  --topic=payment-events \
  --groupId=payment-service-group
```

**Genera:**
- Controller o Consumer (`{Name}Controller.java`)
- DTOs de request/response
- Mapper de DTOs
- Test vacío
- Actualiza `.cleanarch.yml`

---

#### `generateUseCase`
Genera un caso de uso (lógica de negocio).

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
- Clase del caso de uso (`ProcessPaymentUseCase.java`)
- Interface del puerto de entrada (`ProcessPaymentPort.java`)
- Test vacío (`ProcessPaymentUseCaseTest.java`)
- Actualiza `.cleanarch.yml`

---

#### `generateEntity`
Genera una entidad de dominio.

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
- Clase de entidad (`Payment.java`)
- Enums si son necesarios (`PaymentStatus.java`)
- Builder pattern (opcional)
- Actualiza `.cleanarch.yml`

---

#### `generateMapper`
Genera un mapper con MapStruct.

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
- Interface MapStruct (`PaymentMapper.java`)
- Configuración de MapStruct
- Test vacío (`PaymentMapperTest.java`)
- Actualiza `.cleanarch.yml`

---

### Comandos de Información

#### `listComponents`
Lista todos los componentes generados en el proyecto.

```bash
./gradlew listComponents
```

**Output:**
```
Components in payment-service:

Input Adapters:
  - PaymentController (rest) - created 2026-01-31

Output Adapters:
  - PaymentCacheRedisAdapter (redis) - created 2026-01-31
  - PaymentRepositoryDynamoDbAdapter (dynamodb) - created 2026-01-31

Use Cases:
  - ProcessPaymentUseCase - created 2026-01-31

Entities:
  - Payment - created 2026-01-31

Mappers:
  - PaymentMapper (Payment -> PaymentEntity) - created 2026-01-31
```

---

#### `listAdapters`
Lista adaptadores disponibles en el repositorio de templates.

```bash
./gradlew listAdapters [--framework=spring] [--paradigm=reactive] [--detailed]
```

**Output:**
```
Available adapters for spring/reactive:

Output Adapters:
  ✅ redis (v1.0.0) - Redis Cache [stable]
  ✅ kafka (v1.0.0) - Apache Kafka [stable]
  ✅ dynamodb (v1.0.0) - AWS DynamoDB [stable]
  ⏳ sqs (v0.9.0) - AWS SQS [beta]

Input Adapters:
  ✅ rest (v1.0.0) - REST API [stable]
  ✅ kafka (v1.0.0) - Kafka Consumer [stable]
```

---

#### `adapterInfo`
Muestra información detallada de un adaptador específico.

```bash
./gradlew adapterInfo --type=kafka
```

**Output:**
```
Kafka Adapter (v1.0.0)

Description: Producer y Consumer para Apache Kafka
Type: both (input/output)
Author: Juan Pérez

Availability Matrix:
  ✅ spring/reactive (v1.0.0)
  ⏳ spring/imperative (planned) - Issue #42
  ⏳ quarkus/reactive (wanted) - Issue #43

Required Parameters:
  - name: Nombre del adaptador
  - topic: Nombre del topic

Optional Parameters:
  - groupId: Consumer group ID (default: default-group)
  - partitions: Número de particiones (default: 3)

Examples:
  ./gradlew generateOutputAdapter --type=kafka --name=PaymentEvents --topic=payment-events
```

---

### Comandos de Mantenimiento

#### `updateTemplates`
Actualiza los templates desde el repositorio remoto.

```bash
./gradlew updateTemplates
```

**Qué hace:**
- Re-descarga templates desde el repositorio
- Actualiza caché local
- Muestra qué cambió

---

#### `clearTemplateCache`
Limpia la caché local de templates.

```bash
./gradlew clearTemplateCache
```

**Qué hace:**
- Elimina `~/.cleanarch/templates-cache/`
- Próxima generación descargará templates frescos

---

#### `validateProject`
Valida la estructura del proyecto y `.cleanarch.yml`.

```bash
./gradlew validateProject
```

**Qué valida:**
- ✅ `.cleanarch.yml` existe y es válido
- ✅ Estructura de carpetas correcta
- ✅ Componentes declarados existen
- ✅ No hay archivos huérfanos

---

## 🔄 Flujo de Trabajo Típico

### Crear un nuevo proyecto

```bash
# 1. Crear carpeta
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
  --framework=spring \
  --package=com.company.payment

# 4. Ver adaptadores disponibles
./gradlew listAdapters --framework=spring --paradigm=reactive

# 5. Generar entidad de dominio
./gradlew generateEntity \
  --name=Payment \
  --fields="id:String,amount:BigDecimal,status:PaymentStatus"

# 6. Generar caso de uso
./gradlew generateUseCase \
  --name=ProcessPayment \
  --input=PaymentRequest \
  --output=PaymentResponse

# 7. Generar adaptador de salida (Redis)
./gradlew generateOutputAdapter \
  --type=redis \
  --name=PaymentCache \
  --cacheStrategy=writeThrough \
  --ttl=3600

# 8. Generar adaptador de salida (DynamoDB)
./gradlew generateOutputAdapter \
  --type=dynamodb \
  --name=PaymentRepository \
  --tableName=payments

# 9. Generar adaptador de entrada (REST)
./gradlew generateInputAdapter \
  --type=rest \
  --name=Payment \
  --basePath=/api/v1/payments

# 10. Generar mapper
./gradlew generateMapper \
  --from=Payment \
  --to=PaymentEntity

# 11. Ver componentes generados
./gradlew listComponents

# 12. Compilar y ejecutar
./gradlew build
./gradlew bootRun
```

---

## 📋 Resumen de Responsabilidades

| Repositorio | Responsabilidad | Comandos |
|-------------|-----------------|----------|
| **core** | Motor del plugin | Ejecuta todos los comandos `./gradlew` |
| **templates** | Templates y metadata | Ninguno (consumido por core) |
| **docs** | Documentación | `npm start`, `npm run build` |

---

## 🎯 Próximos Pasos

1. ✅ Definir comandos y responsabilidades (este documento)
2. ⏳ Implementar estructura base del plugin (core)
3. ⏳ Crear templates iniciales (templates)
4. ⏳ Configurar Docusaurus (docs)
5. ⏳ Implementar primer comando: `initCleanArch`
6. ⏳ Implementar generadores básicos
7. ⏳ Publicar versión 0.1.0-SNAPSHOT

---

**Creado:** 2026-01-31  
**Versión:** 1.0
