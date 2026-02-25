# 🚀 Quick Start - Publicación 1.0.0-PRERELEASE

Tienes dos opciones para publicar el plugin:

## ⚡ Opción 1: Solo Gradle Plugin Portal (RECOMENDADO)

Más rápido y simple. Ideal para empezar.

### Requisitos:
1. Cuenta en https://plugins.gradle.org/
2. API Keys generadas

### Pasos:

```bash
# 1. Configurar credenciales en ~/.gradle/gradle.properties
gradle.publish.key=YOUR_API_KEY
gradle.publish.secret=YOUR_API_SECRET

# 2. Build y test
cd backend-architecture-design-archetype-generator-core
./gradlew clean build test

# 3. Publicar
./gradlew publishPlugins

# 4. Commit y tag
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.0-PRERELEASE"
git push origin main
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

### Uso:
```kotlin
plugins {
    id("com.pragma.archetype-generator") version "1.0.0-PRERELEASE"
}
```

**Tiempo estimado:** 15-30 minutos

---

## 🌐 Opción 2: Gradle Plugin Portal + Maven Central

Distribución más amplia. Requiere más configuración inicial.

### Requisitos adicionales:
1. Cuenta en https://issues.sonatype.org/
2. Reclamar namespace `com.pragma` (toma 1-2 días)
3. GPG key para firmar artefactos

### Pasos adicionales:

```bash
# 1. Generar GPG key
gpg --gen-key
gpg --list-keys
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID

# 2. Configurar credenciales adicionales en ~/.gradle/gradle.properties
ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD
signing.keyId=LAST_8_CHARS_OF_GPG_KEY
signing.password=YOUR_GPG_PASSPHRASE
signing.secretKeyRingFile=/Users/[usuario]/.gnupg/secring.gpg

# 3. Publicar a Gradle Plugin Portal (igual que Opción 1)
./gradlew publishPlugins

# 4. Publicar a Maven Central
./gradlew publishPluginMavenPublicationToOSSRHRepository

# 5. Cerrar y liberar en Sonatype
# Ve a https://s01.oss.sonatype.org/
# Staging Repositories → Selecciona tu repo → Close → Release
```

### Uso adicional:
```kotlin
// Como dependencia Maven
dependencies {
    implementation("com.pragma:archetype-generator:1.0.0-PRERELEASE")
}
```

**Tiempo estimado:** 2-3 días (incluyendo aprobación de Sonatype)

---

## 🤔 ¿Cuál elegir?

### Elige Opción 1 si:
- ✅ Quieres publicar rápido (hoy mismo)
- ✅ Tus usuarios usan Gradle
- ✅ Es tu primera publicación
- ✅ Quieres simplicidad

### Elige Opción 2 si:
- ✅ Necesitas soporte para Maven
- ✅ Quieres máxima distribución
- ✅ Puedes esperar 2-3 días
- ✅ Tienes experiencia con GPG y Sonatype

---

## 📋 Estado Actual

- ✅ Versión actualizada a `1.0.0-PRERELEASE` en `build.gradle.kts`
- ✅ Configuración de Maven Central agregada
- ✅ Workflow de GitHub Actions creado
- ⏳ Pendiente: Configurar credenciales
- ⏳ Pendiente: Ejecutar publicación

---

## 🆘 Ayuda

Ver guía completa en: [PUBLISHING.md](./PUBLISHING.md)

### Links útiles:
- Gradle Plugin Portal: https://plugins.gradle.org/
- Sonatype OSSRH: https://issues.sonatype.org/
- Maven Central: https://search.maven.org/
- Guía Sonatype: https://central.sonatype.org/publish/publish-guide/
