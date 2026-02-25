# Quick Start: Publicación Automática

Guía rápida para configurar la publicación en Maven Central y Gradle Plugin Portal.

## 🚀 Configuración Rápida (5 minutos)

### 1. Secretos de GitHub

Ve a: `Settings → Secrets and variables → Actions`

Agrega estos 6 secretos:

```
OSSRH_USERNAME          = [usuario de sonatype]
OSSRH_PASSWORD          = [password/token de sonatype]
GPG_PRIVATE_KEY         = [clave gpg en base64]
GPG_PASSPHRASE          = [passphrase de gpg]
GRADLE_PUBLISH_KEY      = [api key de gradle]
GRADLE_PUBLISH_SECRET   = [api secret de gradle]
```

### 2. Verificar Configuración

```bash
# El build.gradle.kts ya está configurado con:
group = "co.com.pragma"
version = "1.0.0"
```

### 3. Publicar

**Opción A - Automático** (cada push a main):
```bash
git push origin main
# Se publica automáticamente
```

**Opción B - Manual**:
```
GitHub → Actions → "Publish to Gradle Plugin Portal Only" → Run workflow
```

**Opción C - Release con Tag**:
```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
# Crea release en GitHub + publica
```

## 📦 Dónde Obtener las Credenciales

### Maven Central (OSSRH)
```
URL: https://s01.oss.sonatype.org/
→ Profile → User Token
→ Copia username y password
```

### Gradle Plugin Portal
```
URL: https://plugins.gradle.org/
→ Login con GitHub
→ API Keys
→ Copia Key y Secret
```

### GPG Key
```bash
# Generar
gpg --full-generate-key

# Exportar en base64
gpg --export-secret-keys [KEY_ID] | base64 > gpg-key.txt

# Publicar
gpg --keyserver keyserver.ubuntu.com --send-keys [KEY_ID]
```

## ✅ Verificación

### Gradle Plugin Portal (inmediato)
```
https://plugins.gradle.org/plugin/co.com.pragma.archetype-generator
```

### Maven Central (2 horas)
```
https://central.sonatype.com/artifact/co.com.pragma/archetype-generator-core
```

## 🔧 Workflows Disponibles

1. **`publish-on-main.yml`** - Automático en cada push a main
2. **`publish-gradle-only.yml`** - Manual, solo Gradle
3. **`publish-maven-only.yml`** - Manual, solo Maven
4. **`publish.yml`** - Automático con tags, crea GitHub Release

## 📋 Checklist

- [ ] 6 secretos configurados en GitHub
- [ ] Namespace `co.com.pragma` verificado en Maven Central
- [ ] Clave GPG publicada en keyservers
- [ ] Tests pasando: `./gradlew test`
- [ ] Listo para publicar

## 🆘 Problemas Comunes

**"Unauthorized" en Gradle**: Verifica `GRADLE_PUBLISH_KEY` y `GRADLE_PUBLISH_SECRET`

**"401" en Maven**: Verifica `OSSRH_USERNAME` y `OSSRH_PASSWORD`

**"gpg: signing failed"**: Verifica `GPG_PRIVATE_KEY` (debe estar en base64) y `GPG_PASSPHRASE`

---

Para más detalles, consulta [PUBLISHING_SETUP_GUIDE.md](./PUBLISHING_SETUP_GUIDE.md)
