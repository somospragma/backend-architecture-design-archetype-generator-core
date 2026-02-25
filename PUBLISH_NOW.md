# 🚀 Publicar AHORA - Guía Ejecutiva

Ya tienes todo configurado. Sigue estos pasos para publicar `1.0.0-PRERELEASE`.

## ✅ Pre-requisitos (Ya los tienes)

- ✅ Cuenta en Gradle Plugin Portal
- ✅ Cuenta en Maven Central (Sonatype)
- ✅ Versión actualizada a `1.0.0-PRERELEASE` en `build.gradle.kts`
- ✅ Workflow de GitHub Actions configurado

---

## 📦 Opción 1: Publicación Local (Manual)

### Paso 1: Configurar Credenciales Locales

Edita `~/.gradle/gradle.properties` (ya deberías tenerlo abierto):

```properties
# Gradle Plugin Portal (OBLIGATORIO)
gradle.publish.key=TU_KEY_DE_GRADLE_PLUGIN_PORTAL
gradle.publish.secret=TU_SECRET_DE_GRADLE_PLUGIN_PORTAL

# Maven Central (OPCIONAL - solo si ya tienes namespace aprobado)
ossrhUsername=TU_USERNAME_TOKEN_SONATYPE
ossrhPassword=TU_PASSWORD_TOKEN_SONATYPE
signing.keyId=ULTIMOS_8_CHARS_GPG_KEY
signing.password=TU_GPG_PASSPHRASE
signing.secretKeyRingFile=/Users/david.yepes/.gnupg/secring.gpg
```

### Paso 2: Publicar

```bash
cd backend-architecture-design-archetype-generator-core

# Build y test
./gradlew clean build test

# Publicar a Gradle Plugin Portal
./gradlew publishPlugins

# Publicar a Maven Central (solo si namespace está aprobado)
./gradlew publishPluginMavenPublicationToOSSRHRepository
```

### Paso 3: Si publicaste en Maven Central

```
1. Ve a: https://s01.oss.sonatype.org/
2. Login
3. Click "Staging Repositories"
4. Busca: compragma-XXXX
5. Selecciona → Click "Close" → Espera 5 min
6. Click "Release"
```

### Paso 4: Commit y Tag

```bash
git add .
git commit -m "chore: release 1.0.0-PRERELEASE"
git push origin main

git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

---

## 🤖 Opción 2: Publicación Automática (GitHub Actions)

### Paso 1: Configurar GitHub Secrets

Ve a: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

**Obligatorios (Gradle Plugin Portal):**
```
GRADLE_PUBLISH_KEY = [Tu key de plugins.gradle.org]
GRADLE_PUBLISH_SECRET = [Tu secret de plugins.gradle.org]
```

**Opcionales (Maven Central):**
```
OSSRH_USERNAME = [Tu username token de s01.oss.sonatype.org]
OSSRH_PASSWORD = [Tu password token de s01.oss.sonatype.org]
GPG_PRIVATE_KEY = [Tu GPG key en base64]
GPG_PASSPHRASE = [Tu GPG passphrase]
```

Ver guía completa: [GITHUB_SECRETS_SETUP.md](GITHUB_SECRETS_SETUP.md)

### Paso 2: Crear Tag y Push

```bash
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

### Paso 3: Verificar Workflow

```
1. Ve a: Actions → Publish Plugin
2. Verás el workflow ejecutándose
3. Espera a que termine (✅ verde)
```

### Paso 4: Si publicaste en Maven Central

```
1. Ve a: https://s01.oss.sonatype.org/
2. Login
3. Staging Repositories → Close → Release
```

---

## 🎯 Recomendación

### Para HOY (más rápido):

**Solo Gradle Plugin Portal:**
```bash
# 1. Configurar solo estos en ~/.gradle/gradle.properties:
gradle.publish.key=...
gradle.publish.secret=...

# 2. Publicar
./gradlew publishPlugins

# 3. Tag
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

**Tiempo:** 5 minutos

### Para DESPUÉS (cuando namespace esté aprobado):

**Agregar Maven Central:**
```bash
# 1. Esperar aprobación de namespace com.pragma (1-2 días)
# 2. Configurar credenciales Maven Central
# 3. Publicar con: ./gradlew publishPluginMavenPublicationToOSSRHRepository
```

---

## ✅ Verificar Publicación

### Gradle Plugin Portal (10-30 minutos):
```
https://plugins.gradle.org/plugin/com.pragma.archetype-generator
```

### Maven Central (30 minutos - 4 horas):
```
https://search.maven.org/artifact/com.pragma/archetype-generator/1.0.0-PRERELEASE/jar
```

### Probar en un Proyecto:

```kotlin
// build.gradle.kts
plugins {
    id("com.pragma.archetype-generator") version "1.0.0-PRERELEASE"
}
```

---

## 🐛 Si Algo Falla

### Error en Gradle Plugin Portal:
```bash
# Verificar credenciales
cat ~/.gradle/gradle.properties | grep gradle.publish

# Regenerar API Keys en plugins.gradle.org
```

### Error en Maven Central:
```bash
# Verificar que namespace esté aprobado
# Ve a: https://issues.sonatype.org/

# Verificar GPG key publicada
gpg --keyserver keyserver.ubuntu.com --recv-keys TU_KEY_ID
```

### Ver logs detallados:
```bash
./gradlew publishPlugins --info --stacktrace
./gradlew publishPluginMavenPublicationToOSSRHRepository --info --stacktrace
```

---

## 📚 Documentación Completa

- [PUBLISHING.md](PUBLISHING.md) - Guía completa de publicación
- [PUBLISHING_QUICKSTART.md](PUBLISHING_QUICKSTART.md) - Comparación de opciones
- [GITHUB_SECRETS_SETUP.md](GITHUB_SECRETS_SETUP.md) - Configurar GitHub Actions

---

## 🎉 Después de Publicar

1. ✅ Verifica que aparezca en plugins.gradle.org
2. ✅ Prueba instalarlo en un proyecto nuevo
3. ✅ Actualiza la documentación con la nueva versión
4. ✅ Anuncia en Slack/Teams
5. ✅ Crea GitHub Release con changelog

---

## 🔄 Próximas Publicaciones

Para versiones futuras:

```bash
# 1. Actualizar versión en build.gradle.kts
version = "1.0.1-PRERELEASE"

# 2. Commit
git add build.gradle.kts
git commit -m "chore: bump version to 1.0.1-PRERELEASE"
git push origin main

# 3. Tag (esto dispara publicación automática si configuraste GitHub Actions)
git tag v1.0.1-PRERELEASE
git push origin v1.0.1-PRERELEASE
```

---

## 💡 Tips

- Publica primero en Gradle Plugin Portal (es más rápido)
- Maven Central puede esperar hasta que el namespace esté aprobado
- Usa GitHub Actions para automatizar futuras publicaciones
- Guarda las credenciales en un lugar seguro (1Password, Vault)
- Rota las API Keys cada 6-12 meses

---

¡Listo para publicar! 🚀
