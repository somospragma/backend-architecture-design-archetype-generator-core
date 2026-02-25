# Guía Completa de Configuración para Publicación

Esta guía te llevará paso a paso para configurar la publicación automática en Maven Central y Gradle Plugin Portal.

## 📋 Tabla de Contenidos

1. [Prerequisitos](#prerequisitos)
2. [Configuración de Maven Central (Sonatype OSSRH)](#configuración-de-maven-central)
3. [Configuración de Gradle Plugin Portal](#configuración-de-gradle-plugin-portal)
4. [Generación de Claves GPG](#generación-de-claves-gpg)
5. [Configuración de Secretos en GitHub](#configuración-de-secretos-en-github)
6. [Workflows Disponibles](#workflows-disponibles)
7. [Proceso de Publicación](#proceso-de-publicación)
8. [Verificación](#verificación)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisitos

- ✅ Namespace verificado en Maven Central: `co.com.pragma`
- ✅ Cuenta en Sonatype OSSRH
- ✅ Cuenta en Gradle Plugin Portal
- ✅ Acceso de admin al repositorio de GitHub

---

## Configuración de Maven Central

### 1. Verificar Namespace en Sonatype

Ya tienes verificado `co.com.pragma`. Para confirmar:

1. Ve a: https://s01.oss.sonatype.org/
2. Inicia sesión con tu cuenta
3. Ve a "Staging Repositories"
4. Verifica que puedes crear repositorios con el namespace `co.com.pragma`

### 2. Obtener Credenciales de Sonatype

1. **Username**: Tu usuario de Sonatype OSSRH
2. **Password**: Tu contraseña de Sonatype OSSRH (o token)

**Recomendación**: Usa un User Token en lugar de tu contraseña:

```
1. Ve a: https://s01.oss.sonatype.org/
2. Profile → User Token → Access User Token
3. Copia el username y password generados
```

---

## Configuración de Gradle Plugin Portal

### 1. Crear Cuenta

1. Ve a: https://plugins.gradle.org/
2. Inicia sesión con GitHub
3. Autoriza la aplicación

### 2. Obtener API Keys

1. Ve a: https://plugins.gradle.org/u/[tu-usuario]
2. Click en "API Keys" en el menú lateral
3. Si no tienes keys, click en "Create API Key"
4. Copia:
   - **Key**: Tu API Key
   - **Secret**: Tu API Secret

**⚠️ IMPORTANTE**: Guarda estas credenciales de forma segura, no las podrás ver de nuevo.

---

## Generación de Claves GPG

Maven Central requiere que firmes tus artefactos con GPG.

### Opción 1: Generar Nueva Clave GPG

```bash
# 1. Generar clave
gpg --full-generate-key

# Selecciona:
# - Tipo: RSA and RSA
# - Tamaño: 4096 bits
# - Validez: 0 (no expira) o el tiempo que prefieras
# - Nombre: Pragma S.A.
# - Email: info@pragma.com.co
# - Passphrase: [crea una contraseña segura]

# 2. Listar claves
gpg --list-secret-keys --keyid-format=long

# Salida:
# sec   rsa4096/ABCD1234EFGH5678 2024-01-01 [SC]
#       FULL_KEY_ID_HERE
# uid   Pragma S.A. <info@pragma.com.co>

# 3. Exportar clave privada (en base64)
gpg --export-secret-keys ABCD1234EFGH5678 | base64 > gpg-private-key.txt

# 4. Publicar clave pública
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
```

### Opción 2: Usar Clave Existente

Si ya tienes una clave GPG:

```bash
# 1. Listar claves
gpg --list-secret-keys --keyid-format=long

# 2. Exportar clave privada
gpg --export-secret-keys [KEY_ID] | base64 > gpg-private-key.txt

# 3. Verificar que esté publicada
gpg --keyserver keyserver.ubuntu.com --recv-keys [KEY_ID]
```

---

## Configuración de Secretos en GitHub

Ve a tu repositorio en GitHub:

```
Settings → Secrets and variables → Actions → New repository secret
```

### Secretos Requeridos

| Nombre del Secreto | Descripción | Cómo Obtenerlo |
|-------------------|-------------|----------------|
| `OSSRH_USERNAME` | Usuario de Sonatype OSSRH | Sonatype → Profile → User Token |
| `OSSRH_PASSWORD` | Contraseña/Token de Sonatype | Sonatype → Profile → User Token |
| `GPG_PRIVATE_KEY` | Clave privada GPG en base64 | `gpg --export-secret-keys [KEY_ID] \| base64` |
| `GPG_PASSPHRASE` | Passphrase de la clave GPG | La contraseña que usaste al crear la clave |
| `GRADLE_PUBLISH_KEY` | API Key de Gradle Plugin Portal | https://plugins.gradle.org/u/[usuario] |
| `GRADLE_PUBLISH_SECRET` | API Secret de Gradle Plugin Portal | https://plugins.gradle.org/u/[usuario] |

### Paso a Paso para Agregar Secretos

```bash
# 1. OSSRH_USERNAME
Nombre: OSSRH_USERNAME
Valor: [tu-username-de-sonatype]

# 2. OSSRH_PASSWORD
Nombre: OSSRH_PASSWORD
Valor: [tu-password-o-token-de-sonatype]

# 3. GPG_PRIVATE_KEY
Nombre: GPG_PRIVATE_KEY
Valor: [contenido del archivo gpg-private-key.txt]

# 4. GPG_PASSPHRASE
Nombre: GPG_PASSPHRASE
Valor: [la-passphrase-de-tu-clave-gpg]

# 5. GRADLE_PUBLISH_KEY
Nombre: GRADLE_PUBLISH_KEY
Valor: [tu-gradle-api-key]

# 6. GRADLE_PUBLISH_SECRET
Nombre: GRADLE_PUBLISH_SECRET
Valor: [tu-gradle-api-secret]
```

---

## Workflows Disponibles

### 1. Publicación Automática en Main (`publish-on-main.yml`)

**Trigger**: Cada push a la rama `main` (excepto cambios en docs)

**Qué hace**:
- ✅ Ejecuta tests
- ✅ Publica en Gradle Plugin Portal
- ✅ Publica en Maven Central

**Cuándo usar**: Para publicación continua en cada commit a main

### 2. Publicación Manual en Gradle (`publish-gradle-only.yml`)

**Trigger**: Manual (workflow_dispatch)

**Qué hace**:
- ✅ Ejecuta tests
- ✅ Publica SOLO en Gradle Plugin Portal

**Cuándo usar**: Cuando solo quieres actualizar el plugin en Gradle

### 3. Publicación Manual en Maven (`publish-maven-only.yml`)

**Trigger**: Manual (workflow_dispatch)

**Qué hace**:
- ✅ Ejecuta tests
- ✅ Publica SOLO en Maven Central

**Cuándo usar**: Cuando solo quieres actualizar en Maven Central

### 4. Publicación por Tags (`publish.yml` - existente)

**Trigger**: Cuando creas un tag `v*` (ej: `v1.0.0`)

**Qué hace**:
- ✅ Ejecuta tests
- ✅ Publica en Gradle Plugin Portal
- ✅ Publica en Maven Central
- ✅ Crea un GitHub Release

**Cuándo usar**: Para releases oficiales versionadas

---

## Proceso de Publicación

### Opción 1: Publicación Automática (Recomendada)

```bash
# 1. Asegúrate de estar en main
git checkout main
git pull

# 2. Haz tus cambios
# ... edita archivos ...

# 3. Commit y push
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main

# 4. El workflow se ejecutará automáticamente
# Ve a: Actions → Publish on Main Push
```

### Opción 2: Publicación Manual

```bash
# 1. Ve a GitHub Actions
# 2. Selecciona el workflow que quieras:
#    - "Publish to Gradle Plugin Portal Only"
#    - "Publish to Maven Central Only"
# 3. Click en "Run workflow"
# 4. Selecciona la rama (main)
# 5. Opcionalmente, agrega una razón
# 6. Click en "Run workflow"
```

### Opción 3: Publicación por Tag (Release)

```bash
# 1. Actualiza la versión en build.gradle.kts
# version = "1.0.0"  # Quita -PRERELEASE

# 2. Commit
git add build.gradle.kts
git commit -m "chore: release version 1.0.0"
git push

# 3. Crea y push el tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 4. El workflow se ejecutará automáticamente
# 5. Se creará un GitHub Release
```

---

## Verificación

### Verificar Publicación en Gradle Plugin Portal

1. **Inmediatamente después**:
   ```
   https://plugins.gradle.org/plugin/co.com.pragma.archetype-generator
   ```

2. **Probar el plugin**:
   ```kotlin
   // En un proyecto de prueba
   plugins {
       id("co.com.pragma.archetype-generator") version "1.0.0"
   }
   ```

### Verificar Publicación en Maven Central

1. **Buscar en Maven Central** (puede tardar hasta 2 horas):
   ```
   https://central.sonatype.com/artifact/co.com.pragma/archetype-generator-core
   ```

2. **Verificar en Sonatype**:
   ```
   https://s01.oss.sonatype.org/
   → Staging Repositories
   → Busca tu repositorio
   → Verifica que esté "Released"
   ```

3. **Probar la dependencia**:
   ```kotlin
   dependencies {
       implementation("co.com.pragma:archetype-generator-core:1.0.0")
   }
   ```

---

## Troubleshooting

### Error: "Unauthorized" en Gradle Plugin Portal

**Causa**: API Keys incorrectas

**Solución**:
1. Verifica que `GRADLE_PUBLISH_KEY` y `GRADLE_PUBLISH_SECRET` estén correctos
2. Regenera las keys en https://plugins.gradle.org/
3. Actualiza los secretos en GitHub

### Error: "401 Unauthorized" en Maven Central

**Causa**: Credenciales de OSSRH incorrectas

**Solución**:
1. Verifica `OSSRH_USERNAME` y `OSSRH_PASSWORD`
2. Prueba iniciar sesión en https://s01.oss.sonatype.org/
3. Considera usar User Token en lugar de contraseña

### Error: "gpg: signing failed"

**Causa**: Clave GPG o passphrase incorrecta

**Solución**:
1. Verifica que `GPG_PRIVATE_KEY` esté en base64
2. Verifica que `GPG_PASSPHRASE` sea correcta
3. Prueba localmente:
   ```bash
   echo "$GPG_PRIVATE_KEY" | base64 --decode | gpg --import
   ```

### Error: "Namespace not verified"

**Causa**: El namespace `co.com.pragma` no está verificado en tu cuenta

**Solución**:
1. Ve a https://issues.sonatype.org/
2. Busca tu ticket de verificación de namespace
3. Asegúrate de que esté aprobado y cerrado

### El artefacto no aparece en Maven Central

**Causa**: Puede tardar hasta 2 horas en sincronizarse

**Solución**:
1. Espera 2 horas
2. Verifica en Sonatype que el repositorio esté "Released"
3. Si después de 4 horas no aparece, contacta a Sonatype Support

---

## Checklist de Configuración

Antes de publicar, verifica:

- [ ] Namespace `co.com.pragma` verificado en Maven Central
- [ ] Cuenta creada en Gradle Plugin Portal
- [ ] Clave GPG generada y publicada
- [ ] Todos los secretos configurados en GitHub:
  - [ ] `OSSRH_USERNAME`
  - [ ] `OSSRH_PASSWORD`
  - [ ] `GPG_PRIVATE_KEY`
  - [ ] `GPG_PASSPHRASE`
  - [ ] `GRADLE_PUBLISH_KEY`
  - [ ] `GRADLE_PUBLISH_SECRET`
- [ ] `build.gradle.kts` actualizado con `group = "co.com.pragma"`
- [ ] `version` actualizada (sin `-PRERELEASE` para releases)
- [ ] Tests pasando: `./gradlew test`
- [ ] Build exitoso: `./gradlew build`

---

## Recursos Adicionales

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Gradle Plugin Portal Publishing](https://plugins.gradle.org/docs/publish-plugin)
- [GPG Documentation](https://www.gnupg.org/documentation/)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

---

## Soporte

Si encuentras problemas:

1. Revisa los logs del workflow en GitHub Actions
2. Consulta esta guía de troubleshooting
3. Revisa la documentación oficial de Maven Central y Gradle
4. Contacta al equipo de desarrollo

---

**Última actualización**: 2026-02-25  
**Versión del plugin**: 1.0.0  
**Namespace**: co.com.pragma
