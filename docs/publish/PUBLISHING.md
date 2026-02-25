# Guía de Publicación - Gradle Plugin Portal y Maven Central

Esta guía explica cómo publicar el plugin en Gradle Plugin Portal y Maven Central.

## 📋 Requisitos Previos

### 1. Cuenta en Gradle Plugin Portal

1. Crea una cuenta en: https://plugins.gradle.org/
2. Ve a tu perfil: https://plugins.gradle.org/u/[tu-usuario]
3. Genera API Keys:
   - Click en "API Keys" en el menú
   - Click en "Generate New Key"
   - Guarda el **Key** y el **Secret** (los necesitarás después)

### 2. Cuenta en Sonatype (Maven Central)

1. Crea una cuenta en: https://issues.sonatype.org/secure/Signup!default.jspa
2. Crea un ticket JIRA para reclamar el namespace `com.pragma`:
   - Tipo: "New Project"
   - Group Id: `com.pragma`
   - Project URL: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core`
   - SCM URL: `https://github.com/somospragma/backend-architecture-design-archetype-generator-core.git`
3. Espera aprobación (puede tomar 1-2 días hábiles)
4. Una vez aprobado, genera token en: https://s01.oss.sonatype.org/
   - Login → Profile → User Token → Access User Token
   - Guarda el **Username** y **Password**

### 3. Generar GPG Key (Para firmar artefactos)

```bash
# Generar key
gpg --gen-key
# Usa tu email de Pragma
# Guarda la passphrase

# Listar keys
gpg --list-keys

# Exportar public key (reemplaza KEY_ID con tu key)
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID

# También enviar a otros keyservers
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
gpg --keyserver pgp.mit.edu --send-keys KEY_ID
```

### 4. Configurar Credenciales Locales

Crea o edita el archivo `~/.gradle/gradle.properties`:

```properties
# Gradle Plugin Portal
gradle.publish.key=YOUR_API_KEY
gradle.publish.secret=YOUR_API_SECRET

# Maven Central (Sonatype OSSRH)
ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD

# GPG Signing
signing.keyId=LAST_8_CHARS_OF_YOUR_GPG_KEY
signing.password=YOUR_GPG_PASSPHRASE
signing.secretKeyRingFile=/Users/[tu-usuario]/.gnupg/secring.gpg
```

**⚠️ IMPORTANTE:** Nunca commitees estas credenciales al repositorio.

## 🚀 Publicación Manual

### Opción A: Solo Gradle Plugin Portal (Más Rápido)

#### Paso 1: Actualizar Versión

Edita `build.gradle.kts`:

```kotlin
version = "1.0.0-PRERELEASE"  // Cambiar de SNAPSHOT a PRERELEASE
```

#### Paso 2: Verificar Build

```bash
# Limpiar build anterior
./gradlew clean

# Compilar y ejecutar tests
./gradlew build

# Verificar que todo pasa
./gradlew test jacocoTestReport
```

#### Paso 3: Publicar al Plugin Portal

```bash
# Publicar
./gradlew publishPlugins

# Si todo sale bien, verás:
# ✓ Published com.pragma.archetype-generator version 1.0.0-PRERELEASE
```

#### Paso 4: Verificar Publicación

1. Ve a: https://plugins.gradle.org/plugin/com.pragma.archetype-generator
2. Deberías ver la versión `1.0.0-PRERELEASE` listada
3. Puede tomar 10-30 minutos en aparecer

---

### Opción B: Gradle Plugin Portal + Maven Central (Distribución Completa)

#### Paso 1: Actualizar Versión

Edita `build.gradle.kts`:

```kotlin
version = "1.0.0-PRERELEASE"
```

#### Paso 2: Verificar Build

```bash
./gradlew clean build test
```

#### Paso 3: Publicar a Gradle Plugin Portal

```bash
./gradlew publishPlugins
```

#### Paso 4: Publicar a Maven Central

```bash
# Publicar a Sonatype OSSRH
./gradlew publishPluginMavenPublicationToOSSRHRepository

# Verificar que se subió correctamente
# Ve a: https://s01.oss.sonatype.org/
# Login → Staging Repositories
```

#### Paso 5: Cerrar y Liberar en Sonatype

1. Ve a: https://s01.oss.sonatype.org/
2. Login con tus credenciales
3. Click en "Staging Repositories"
4. Busca tu repositorio (com.pragma-XXXX)
5. Selecciónalo y click en "Close"
6. Espera validación (5-10 minutos)
7. Una vez cerrado, click en "Release"
8. El artefacto estará en Maven Central en 10-30 minutos
9. Sincronización completa puede tomar 2-4 horas

#### Paso 6: Verificar en Maven Central

Después de 30 minutos, verifica en:
- https://search.maven.org/artifact/com.pragma/archetype-generator/1.0.0-PRERELEASE/jar
- https://repo1.maven.org/maven2/com/pragma/archetype-generator/

## 🤖 Publicación Automática con GitHub Actions

### Configurar Secrets en GitHub

1. Ve a: `Settings` → `Secrets and variables` → `Actions`
2. Agrega los siguientes secrets:
   
   **Para Gradle Plugin Portal:**
   - `GRADLE_PUBLISH_KEY`: Tu API Key de Gradle Plugin Portal
   - `GRADLE_PUBLISH_SECRET`: Tu API Secret de Gradle Plugin Portal
   
   **Para Maven Central (opcional):**
   - `OSSRH_USERNAME`: Tu username de Sonatype
   - `OSSRH_PASSWORD`: Tu password de Sonatype
   - `GPG_PRIVATE_KEY`: Tu GPG private key (exportada en base64)
   - `GPG_PASSPHRASE`: Tu GPG passphrase

### Exportar GPG Key para GitHub Actions

```bash
# Exportar private key
gpg --export-secret-keys -a KEY_ID > private-key.asc

# Convertir a base64
cat private-key.asc | base64 > private-key-base64.txt

# Copiar el contenido de private-key-base64.txt a GitHub Secret GPG_PRIVATE_KEY

# Eliminar archivos temporales
rm private-key.asc private-key-base64.txt
```

### Workflow de Publicación

El workflow `.github/workflows/publish.yml` se ejecutará automáticamente cuando:
- Crees un tag con formato `v*` (ej: `v1.0.0-PRERELEASE`)

```bash
# Crear tag y publicar
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

## 📦 Versionado

Seguimos [Semantic Versioning](https://semver.org/):

### Formato de Versiones

- **MAJOR.MINOR.PATCH** (ej: `1.0.0`)
- **MAJOR.MINOR.PATCH-PRERELEASE** (ej: `1.0.0-PRERELEASE`, `1.0.0-RC1`)
- **MAJOR.MINOR.PATCH-SNAPSHOT** (ej: `1.0.0-SNAPSHOT`) - Solo para desarrollo

### Ejemplos

```
0.1.0-SNAPSHOT    → Desarrollo inicial
1.0.0-PRERELEASE  → Primera pre-release
1.0.0-RC1         → Release Candidate 1
1.0.0             → Release estable
1.0.1             → Patch (bug fixes)
1.1.0             → Minor (nuevas features)
2.0.0             → Major (breaking changes)
```

## 🔍 Verificar Publicación

### Gradle Plugin Portal

```bash
# Buscar el plugin
curl https://plugins.gradle.org/api/gradle/8.0/plugin/use/com.pragma.archetype-generator/1.0.0-PRERELEASE
```

### Maven Central

```bash
# Buscar en Maven Central (después de 30 minutos)
curl https://search.maven.org/solrsearch/select?q=g:com.pragma+AND+a:archetype-generator
```

### Usar en un Proyecto

**Opción 1: Como Gradle Plugin (desde Plugin Portal)**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

// build.gradle.kts
plugins {
    id("com.pragma.archetype-generator") version "1.0.0-PRERELEASE"
}
```

**Opción 2: Como Dependencia Maven (desde Maven Central)**

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.pragma:archetype-generator:1.0.0-PRERELEASE")
}
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.pragma</groupId>
    <artifactId>archetype-generator</artifactId>
    <version>1.0.0-PRERELEASE</version>
</dependency>
```

## 📝 Checklist Pre-Publicación

Antes de publicar, verifica:

- [ ] Versión actualizada en `build.gradle.kts`
- [ ] Tests pasando: `./gradlew test`
- [ ] Build exitoso: `./gradlew build`
- [ ] Cobertura de tests > 80%: `./gradlew jacocoTestReport`
- [ ] README actualizado con nueva versión
- [ ] CHANGELOG actualizado con cambios
- [ ] Documentación actualizada
- [ ] LICENSE y NOTICE presentes
- [ ] No hay TODOs o FIXMEs críticos

## 🐛 Troubleshooting

### Error: "Invalid credentials" (Gradle Plugin Portal)

```
> Task :publishPlugins FAILED
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':publishPlugins'.
> Failed to publish plugin. Invalid credentials.
```

**Solución:**
1. Verifica que `gradle.publish.key` y `gradle.publish.secret` estén en `~/.gradle/gradle.properties`
2. Verifica que las credenciales sean correctas
3. Regenera las API Keys si es necesario

### Error: "Invalid credentials" (Maven Central)

```
> Could not publish to 'https://s01.oss.sonatype.org/...'
> Received status code 401 from server: Unauthorized
```

**Solución:**
1. Verifica que `ossrhUsername` y `ossrhPassword` estén en `~/.gradle/gradle.properties`
2. Verifica que tu cuenta Sonatype esté aprobada
3. Verifica que el namespace `com.pragma` esté reclamado

### Error: "GPG signing failed"

```
> Execution failed for task ':signPluginMavenPublication'.
> Unable to retrieve secret key
```

**Solución:**
1. Verifica que GPG esté instalado: `gpg --version`
2. Verifica que tengas una key: `gpg --list-keys`
3. Verifica configuración en `~/.gradle/gradle.properties`:
   ```properties
   signing.keyId=LAST_8_CHARS
   signing.password=YOUR_PASSPHRASE
   signing.secretKeyRingFile=/Users/[usuario]/.gnupg/secring.gpg
   ```
4. Si usas GPG 2.1+, exporta el keyring:
   ```bash
   gpg --export-secret-keys > ~/.gnupg/secring.gpg
   ```

### Error: "Plugin already exists"

```
> A plugin with ID 'com.pragma.archetype-generator' and version '1.0.0-PRERELEASE' already exists
```

**Solución:**
- No puedes republicar la misma versión
- Incrementa la versión (ej: `1.0.0-PRERELEASE` → `1.0.1-PRERELEASE`)

### Error: "Validation failed"

```
> Plugin validation failed
```

**Solución:**
1. Verifica que `gradlePlugin` esté configurado correctamente
2. Verifica que `implementationClass` exista
3. Ejecuta `./gradlew validatePlugins` para ver detalles

### Error: "Repository not found" (Maven Central)

```
> Repository 'com.pragma-XXXX' not found in staging repositories
```

**Solución:**
1. Espera 5-10 minutos después de publicar
2. Refresca la página de Sonatype
3. Verifica que la publicación fue exitosa: `./gradlew publishPluginMavenPublicationToOSSRHRepository --info`

## 📚 Recursos

- [Gradle Plugin Portal](https://plugins.gradle.org/)
- [Publishing Plugins](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)
- [Maven Central (Sonatype)](https://central.sonatype.org/publish/publish-guide/)
- [OSSRH Guide](https://central.sonatype.org/publish/publish-gradle/)
- [GPG Signing](https://central.sonatype.org/publish/requirements/gpg/)
- [Semantic Versioning](https://semver.org/)

## 🔄 Proceso Completo

### Primera Publicación (1.0.0-PRERELEASE)

**Solo Gradle Plugin Portal (Recomendado para empezar):**

```bash
# 1. Actualizar versión
# Editar build.gradle.kts: version = "1.0.0-PRERELEASE"

# 2. Build y test
./gradlew clean build test

# 3. Publicar
./gradlew publishPlugins

# 4. Commit y tag
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.0-PRERELEASE"
git push origin main
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE

# 5. Verificar en https://plugins.gradle.org/
```

**Gradle Plugin Portal + Maven Central (Distribución completa):**

```bash
# 1. Actualizar versión
# Editar build.gradle.kts: version = "1.0.0-PRERELEASE"

# 2. Build y test
./gradlew clean build test

# 3. Publicar a Gradle Plugin Portal
./gradlew publishPlugins

# 4. Publicar a Maven Central
./gradlew publishPluginMavenPublicationToOSSRHRepository

# 5. Cerrar y liberar en Sonatype
# Ve a https://s01.oss.sonatype.org/
# Staging Repositories → Close → Release

# 6. Commit y tag
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.0-PRERELEASE"
git push origin main
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE

# 7. Verificar
# Gradle Plugin Portal: https://plugins.gradle.org/plugin/com.pragma.archetype-generator
# Maven Central: https://search.maven.org/artifact/com.pragma/archetype-generator
```

### Publicaciones Subsecuentes

```bash
# 1. Actualizar versión en build.gradle.kts
version = "1.0.1-PRERELEASE"

# 2. Actualizar CHANGELOG.md

# 3. Commit, tag, push
git add .
git commit -m "chore: bump version to 1.0.1-PRERELEASE"
git push origin main
git tag v1.0.1-PRERELEASE
git push origin v1.0.1-PRERELEASE

# 4. Publicar
./gradlew publishPlugins
```

## 🎯 Release Estable (1.0.0)

Cuando estés listo para la release estable:

```bash
# 1. Actualizar versión
version = "1.0.0"

# 2. Actualizar documentación
# - README.md
# - CHANGELOG.md
# - Docs en site-docs

# 3. Commit y tag
git add .
git commit -m "chore: release v1.0.0"
git push origin main
git tag v1.0.0
git push origin v1.0.0

# 4. Publicar
./gradlew publishPlugins

# 5. Crear GitHub Release
# Ve a: https://github.com/somospragma/backend-architecture-design-archetype-generator-core/releases/new
# - Tag: v1.0.0
# - Title: Release 1.0.0
# - Description: Changelog y features
```

## 📄 Licencia

Recuerda que el plugin está bajo Apache License 2.0. Todos los usuarios deben cumplir con los términos de la licencia.
