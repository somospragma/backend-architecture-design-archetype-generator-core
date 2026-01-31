# Implementación del Sistema de Descarga desde GitHub

## ✅ Componentes Implementados

### 1. Domain Layer (Modelos y Puertos)

#### Modelos
- ✅ **TemplateConfig.java** - Configuración de templates
  - Modo: PRODUCTION o DEVELOPER
  - Repository URL
  - Branch/Version
  - Local path (opcional)
  - Cache habilitado/deshabilitado

- ✅ **TemplateMode.java** - Enum para modos
  - PRODUCTION: Descarga desde repositorio oficial
  - DEVELOPER: Usa local path o fork/branch custom

#### Puertos
- ✅ **HttpClientPort.java** - Puerto para cliente HTTP
  - `downloadContent(url)` - Descarga contenido
  - `isAccessible(url)` - Verifica accesibilidad

### 2. Infrastructure Layer (Adaptadores)

#### HTTP Client
- ✅ **OkHttpClientAdapter.java** - Implementación con OkHttp
  - Timeouts configurados (30s)
  - Manejo de errores
  - Seguimiento de redirects

#### Template Management
- ✅ **TemplateCache.java** - Sistema de caché local
  - Ubicación: `~/.cleanarch/templates-cache/`
  - Operaciones: get, put, exists, clear
  - Gestión de tamaño y limpieza

- ✅ **GitHubTemplateDownloader.java** - Descarga desde Git
  - Soporta: GitHub, GitLab, Bitbucket
  - Construcción de URLs raw
  - Integración con caché
  - Manejo de versiones/branches

- ✅ **FreemarkerTemplateRepository.java** - Actualizado
  - Soporta 3 modos de carga:
    1. Local filesystem (developer mode con localPath)
    2. Remote repository (production o developer sin localPath)
    3. Embedded resources (fallback)
  - Prioridad: Local → Remote → Embedded

#### Configuration
- ✅ **YamlConfigurationAdapter.java** - Actualizado
  - Lee sección `templates` de `.cleanarch.yml`
  - Parsea configuración de templates
  - Retorna TemplateConfig con defaults

### 3. Application Layer (Tasks)

#### Nuevas Tasks de Gradle
- ✅ **UpdateTemplatesTask.java**
  - Comando: `./gradlew updateTemplates`
  - Limpia caché y fuerza re-descarga
  - Muestra configuración actual

- ✅ **ClearTemplateCacheTask.java**
  - Comando: `./gradlew clearTemplateCache`
  - Limpia caché local
  - Muestra espacio liberado

#### Plugin
- ✅ **CleanArchPlugin.java** - Actualizado
  - Registra nuevas tasks
  - Grupo: "clean architecture"

## 📋 Configuración en .cleanarch.yml

### Modo Producción (Default)
```yaml
templates:
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  version: 1.2.0  # Opcional - usa tag específico
  cache: true
```

### Modo Developer con Fork
```yaml
templates:
  mode: developer
  repository: https://github.com/juan/backend-architecture-design-archetype-generator-templates
  branch: feature/kafka-adapter
  cache: false
```

### Modo Developer con Path Local
```yaml
templates:
  mode: developer
  localPath: /Users/juan/backend-architecture-design-archetype-generator-templates
  cache: false
```

## 🔄 Flujo de Carga de Templates

### 1. Modo Local (Developer con localPath)
```
FreemarkerTemplateRepository
  → Lee desde filesystem local
  → No usa caché
  → No descarga nada
```

### 2. Modo Remoto (Production o Developer sin localPath)
```
FreemarkerTemplateRepository
  → GitHubTemplateDownloader
    → Verifica caché (si habilitado)
    → Si no está en caché:
      → OkHttpClientAdapter
        → Descarga desde GitHub/GitLab/Bitbucket
      → Guarda en TemplateCache
    → Retorna contenido
```

### 3. Fallback a Embedded
```
Si falla local y remoto:
  → Busca en resources/templates/ (embebido en JAR)
  → Si no existe: lanza excepción
```

## 🎯 URLs Soportadas

### GitHub
```
Repo: https://github.com/owner/repo
Raw:  https://raw.githubusercontent.com/owner/repo/branch/path
```

### GitLab
```
Repo: https://gitlab.com/owner/repo
Raw:  https://gitlab.com/owner/repo/-/raw/branch/path
```

### Bitbucket
```
Repo: https://bitbucket.org/owner/repo
Raw:  https://bitbucket.org/owner/repo/raw/branch/path
```

## 🚀 Comandos Disponibles

### Limpiar Caché
```bash
./gradlew clearTemplateCache
```
Salida:
```
Clearing template cache...
✓ Template cache cleared successfully
  Freed: 2.45 MB
  Location: /Users/user/.cleanarch/templates-cache
```

### Actualizar Templates
```bash
./gradlew updateTemplates
```
Salida:
```
Updating templates...
Template configuration:
  Mode: PRODUCTION
  Repository: https://github.com/somospragma/...
  Branch: main

✓ Template cache cleared
  Freed: 2.45 MB

Templates will be re-downloaded on next use from:
  https://github.com/somospragma/.../main
```

## 📦 Dependencias Agregadas

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.freemarker:freemarker:2.3.32")
}
```

## ✅ Ventajas de la Implementación

1. **Separación de Concerns**
   - Templates separados del core
   - Actualización independiente

2. **Flexibilidad**
   - Modo producción para usuarios
   - Modo developer para contribuidores
   - Soporte para múltiples Git hosts

3. **Performance**
   - Caché local automático
   - Descarga solo cuando necesario
   - Fallback a embedded

4. **Developer Experience**
   - Path local para desarrollo
   - Fork/branch custom para testing
   - Comandos simples de gestión

5. **Escalabilidad**
   - Fácil agregar nuevos adaptadores
   - No requiere nueva versión del plugin
   - Comunidad puede contribuir

## 🔄 Próximos Pasos

### Fase 1: Testing ✅
- [x] Implementar componentes core
- [ ] Probar descarga desde GitHub
- [ ] Probar modo local
- [ ] Probar caché

### Fase 2: Integración
- [ ] Actualizar tasks existentes para usar TemplateConfig
- [ ] Migrar de templates embebidos a remotos
- [ ] Actualizar documentación

### Fase 3: Publicación
- [ ] Publicar templates en GitHub
- [ ] Publicar nueva versión del plugin
- [ ] Actualizar guías de usuario

## 📝 Archivos Creados/Modificados

### Creados (9 archivos)
1. `domain/model/TemplateConfig.java`
2. `domain/model/TemplateMode.java`
3. `domain/port/out/HttpClientPort.java`
4. `infrastructure/adapter/out/http/OkHttpClientAdapter.java`
5. `infrastructure/adapter/out/template/TemplateCache.java`
6. `infrastructure/adapter/out/template/GitHubTemplateDownloader.java`
7. `infrastructure/adapter/in/gradle/UpdateTemplatesTask.java`
8. `infrastructure/adapter/in/gradle/ClearTemplateCacheTask.java`
9. `GITHUB_DOWNLOAD_IMPLEMENTATION.md` (este archivo)

### Modificados (3 archivos)
1. `infrastructure/adapter/out/config/YamlConfigurationAdapter.java`
2. `infrastructure/adapter/out/template/FreemarkerTemplateRepository.java`
3. `infrastructure/config/CleanArchPlugin.java`

## 🎉 Estado Actual

✅ **Sistema de descarga implementado completamente**
- Todos los componentes creados
- Integración con caché
- Soporte para múltiples modos
- Tasks de gestión disponibles

🔄 **Pendiente**
- Testing end-to-end
- Migración de tasks existentes
- Documentación de usuario
