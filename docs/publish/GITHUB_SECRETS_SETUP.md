# 🔐 Configuración de GitHub Secrets para Publicación Automática

Esta guía explica cómo configurar los secrets en GitHub para que el workflow publique automáticamente en Gradle Plugin Portal y Maven Central.

## 📋 Secrets Necesarios

### Para Gradle Plugin Portal (OBLIGATORIO)
- `GRADLE_PUBLISH_KEY`
- `GRADLE_PUBLISH_SECRET`

### Para Maven Central (OPCIONAL)
- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`

---

## 🚀 Paso a Paso

### 1. Ir a GitHub Secrets

```
1. Ve a tu repositorio en GitHub
2. Click en "Settings" (arriba)
3. En el menú izquierdo: "Secrets and variables" → "Actions"
4. Click en "New repository secret"
```

---

### 2. Configurar Gradle Plugin Portal

#### Secret 1: GRADLE_PUBLISH_KEY

```
Name: GRADLE_PUBLISH_KEY
Secret: [Tu API Key de Gradle Plugin Portal]

Dónde obtenerlo:
1. Ve a: https://plugins.gradle.org/
2. Login con tu cuenta
3. Click en tu nombre → "API Keys"
4. Copia el "Key"
```

#### Secret 2: GRADLE_PUBLISH_SECRET

```
Name: GRADLE_PUBLISH_SECRET
Secret: [Tu API Secret de Gradle Plugin Portal]

Dónde obtenerlo:
- Mismo lugar que el Key (arriba)
- Copia el "Secret"
```

---

### 3. Configurar Maven Central (Opcional)

#### Secret 3: OSSRH_USERNAME

```
Name: OSSRH_USERNAME
Secret: [Tu username token de Sonatype]

Dónde obtenerlo:
1. Ve a: https://s01.oss.sonatype.org/
2. Login con tu cuenta
3. Click en tu nombre → "Profile"
4. Dropdown → "User Token"
5. Click "Access User Token"
6. Copia el "Username Code"
```

#### Secret 4: OSSRH_PASSWORD

```
Name: OSSRH_PASSWORD
Secret: [Tu password token de Sonatype]

Dónde obtenerlo:
- Mismo lugar que el username (arriba)
- Copia el "Password Code"
```

#### Secret 5: GPG_PRIVATE_KEY

```
Name: GPG_PRIVATE_KEY
Secret: [Tu GPG private key en base64]

Cómo generarlo:
```bash
# 1. Listar tus keys
gpg --list-secret-keys

# 2. Exportar la key (reemplaza KEY_ID con tu key)
gpg --export-secret-keys -a KEY_ID > private-key.asc

# 3. Convertir a base64
cat private-key.asc | base64 > private-key-base64.txt

# 4. Copiar el contenido de private-key-base64.txt
cat private-key-base64.txt

# 5. Pegar en GitHub Secret

# 6. IMPORTANTE: Eliminar archivos temporales
rm private-key.asc private-key-base64.txt
```

#### Secret 6: GPG_PASSPHRASE

```
Name: GPG_PASSPHRASE
Secret: [La passphrase que usaste al crear la GPG key]

Es la contraseña que elegiste cuando ejecutaste:
gpg --gen-key
```

---

## ✅ Verificar Configuración

Después de agregar todos los secrets, deberías ver:

```
Repository secrets (6):
├─ GRADLE_PUBLISH_KEY
├─ GRADLE_PUBLISH_SECRET
├─ OSSRH_USERNAME
├─ OSSRH_PASSWORD
├─ GPG_PRIVATE_KEY
└─ GPG_PASSPHRASE
```

---

## 🎯 Cómo Funciona el Workflow

### Cuando creas un tag:

```bash
git tag v1.0.0-PRERELEASE
git push origin v1.0.0-PRERELEASE
```

El workflow automáticamente:
1. ✅ Compila el proyecto
2. ✅ Ejecuta tests
3. ✅ Publica en Gradle Plugin Portal
4. ✅ Publica en Maven Central (si los secrets están configurados)
5. ✅ Crea un GitHub Release

### Publicación Manual:

```
1. Ve a: Actions → Publish Plugin
2. Click "Run workflow"
3. Selecciona rama: main
4. Marca/desmarca "Publish to Maven Central"
5. Click "Run workflow"
```

---

## 🔒 Seguridad

### Buenas Prácticas:

1. **Nunca commitees secrets al repositorio**
   ```bash
   # ❌ NUNCA hagas esto:
   git add ~/.gradle/gradle.properties
   
   # ✅ Verifica que esté en .gitignore:
   echo "gradle.properties" >> .gitignore
   ```

2. **Rota secrets periódicamente**
   - Cada 6-12 meses
   - Cuando alguien deja el equipo
   - Si sospechas compromiso

3. **Usa tokens en lugar de contraseñas**
   - Sonatype: User Token (no tu contraseña real)
   - Gradle: API Keys (no tu contraseña de login)

4. **Limita permisos**
   - Solo agrega secrets necesarios
   - Revisa quién tiene acceso al repositorio

---

## 🐛 Troubleshooting

### Error: "Invalid credentials" en Gradle Plugin Portal

```
Solución:
1. Verifica que GRADLE_PUBLISH_KEY y GRADLE_PUBLISH_SECRET estén correctos
2. Regenera las API Keys en plugins.gradle.org
3. Actualiza los secrets en GitHub
```

### Error: "Unauthorized" en Maven Central

```
Solución:
1. Verifica que OSSRH_USERNAME y OSSRH_PASSWORD sean los User Tokens
2. NO uses tu contraseña de login, usa los tokens
3. Regenera los tokens en s01.oss.sonatype.org
```

### Error: "GPG signing failed"

```
Solución:
1. Verifica que GPG_PRIVATE_KEY esté en base64
2. Verifica que GPG_PASSPHRASE sea correcta
3. Asegúrate de haber publicado la key pública:
   gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### Error: "Secret not found"

```
Solución:
1. Verifica que el nombre del secret sea EXACTO (case-sensitive)
2. Verifica que esté en "Repository secrets" (no "Environment secrets")
3. Espera 1-2 minutos después de crear el secret
```

---

## 📝 Checklist de Configuración

### Mínimo (Solo Gradle Plugin Portal):
- [ ] GRADLE_PUBLISH_KEY configurado
- [ ] GRADLE_PUBLISH_SECRET configurado
- [ ] Workflow ejecutado exitosamente
- [ ] Plugin visible en plugins.gradle.org

### Completo (Gradle + Maven Central):
- [ ] GRADLE_PUBLISH_KEY configurado
- [ ] GRADLE_PUBLISH_SECRET configurado
- [ ] OSSRH_USERNAME configurado
- [ ] OSSRH_PASSWORD configurado
- [ ] GPG_PRIVATE_KEY configurado
- [ ] GPG_PASSPHRASE configurado
- [ ] GPG key publicada en keyservers
- [ ] Namespace com.pragma aprobado en Sonatype
- [ ] Workflow ejecutado exitosamente
- [ ] Plugin visible en plugins.gradle.org
- [ ] Artefacto visible en search.maven.org

---

## 🔄 Migrar de Cuenta Personal a Empresarial

Cuando tengas la cuenta empresarial lista:

### 1. Generar nuevas credenciales con cuenta empresarial

```bash
# Gradle Plugin Portal:
1. Login con cuenta empresarial
2. Generar nuevas API Keys

# Maven Central:
1. Login con cuenta empresarial
2. Generar nuevos User Tokens
3. Usar la misma GPG key o generar nueva
```

### 2. Actualizar GitHub Secrets

```bash
1. Ve a Settings → Secrets and variables → Actions
2. Para cada secret, click en "Update"
3. Pega las nuevas credenciales
4. Save
```

### 3. Probar

```bash
# Crear tag de prueba
git tag v1.0.1-TEST
git push origin v1.0.1-TEST

# Verificar que el workflow funcione
# Si falla, revisar logs en Actions
```

---

## 📚 Referencias

- [GitHub Encrypted Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Gradle Plugin Portal API Keys](https://plugins.gradle.org/docs/submit)
- [Sonatype User Tokens](https://central.sonatype.org/publish/generate-token/)
- [GPG Signing](https://central.sonatype.org/publish/requirements/gpg/)

---

## 💡 Tips

### Probar localmente antes de GitHub Actions:

```bash
# Simular lo que hace el workflow:
export GRADLE_PUBLISH_KEY="tu_key"
export GRADLE_PUBLISH_SECRET="tu_secret"
export OSSRH_USERNAME="tu_username"
export OSSRH_PASSWORD="tu_password"

./gradlew clean build test
./gradlew publishPlugins
./gradlew publishPluginMavenPublicationToOSSRHRepository
```

### Ver logs detallados en GitHub Actions:

```
1. Ve a Actions → Click en el workflow fallido
2. Click en el job "publish"
3. Expande cada step para ver logs
4. Busca líneas con "ERROR" o "FAILED"
```

### Publicar solo en Gradle Plugin Portal (más rápido):

```bash
# Opción 1: Remover secrets de Maven Central
# El workflow los saltará automáticamente

# Opción 2: Usar workflow_dispatch y desmarcar Maven Central
Actions → Publish Plugin → Run workflow → Uncheck "Publish to Maven Central"
```
