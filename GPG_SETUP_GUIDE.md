# Guía Rápida: Generar Clave GPG para Maven Central

## ¿Qué es GPG?

GPG (GNU Privacy Guard) es una herramienta que **firma digitalmente** tus artefactos. Maven Central requiere que todos los artefactos estén firmados para garantizar que:
- Fueron publicados por ti
- No fueron modificados por terceros

**Es como un sello de autenticidad digital.**

---

## Paso 1: Generar la Clave GPG

Abre tu terminal y ejecuta:

```bash
gpg --full-generate-key
```

Te hará varias preguntas:

### Pregunta 1: Tipo de clave
```
Please select what kind of key you want:
   (1) RSA and RSA (default)
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
Your selection?
```
**Respuesta**: Presiona `1` y Enter (RSA and RSA)

### Pregunta 2: Tamaño de la clave
```
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (3072)
```
**Respuesta**: Escribe `4096` y Enter (más seguro)

### Pregunta 3: Validez de la clave
```
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0)
```
**Respuesta**: Presiona Enter (0 = no expira)

Confirma con `y` (yes)

### Pregunta 4: Información personal
```
Real name:
```
**Respuesta**: `Pragma S.A.` (o tu nombre/empresa)

```
Email address:
```
**Respuesta**: `info@pragma.com.co` (o tu email)

```
Comment:
```
**Respuesta**: Presiona Enter (opcional, puedes dejarlo vacío)

### Pregunta 5: Passphrase (Contraseña)
```
Enter passphrase:
```
**Importante**: 
- Crea una contraseña **segura** (mínimo 12 caracteres)
- **Guárdala en un lugar seguro** (la necesitarás después)
- Ejemplo: `MySecurePassphrase2024!`

---

## Paso 2: Verificar que se Creó

```bash
gpg --list-secret-keys --keyid-format=long
```

Verás algo como:

```
sec   rsa4096/ABCD1234EFGH5678 2024-01-01 [SC]
      1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234
uid                 [ultimate] Pragma S.A. <info@pragma.com.co>
ssb   rsa4096/WXYZ9876HGFE4321 2024-01-01 [E]
```

**Importante**: Copia el ID de la clave (en este ejemplo: `ABCD1234EFGH5678`)

---

## Paso 3: Publicar la Clave Pública

Maven Central necesita verificar tu firma, así que debes publicar tu clave pública en servidores de claves:

```bash
# Reemplaza ABCD1234EFGH5678 con tu KEY_ID real
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
```

**Nota**: Puede tardar unos minutos en propagarse.

---

## Paso 4: Exportar la Clave Privada (para GitHub)

GitHub Actions necesita tu clave privada para firmar los artefactos. La exportamos en formato base64:

```bash
# Reemplaza ABCD1234EFGH5678 con tu KEY_ID real
gpg --export-secret-keys ABCD1234EFGH5678 | base64 > gpg-private-key.txt
```

Esto crea un archivo `gpg-private-key.txt` con tu clave privada codificada.

**⚠️ IMPORTANTE**: 
- Este archivo contiene tu clave privada
- **NO lo compartas con nadie**
- **NO lo subas a Git**
- Solo lo usarás para copiarlo a GitHub Secrets

---

## Paso 5: Configurar en GitHub

Ve a tu repositorio en GitHub:

```
Settings → Secrets and variables → Actions → New repository secret
```

### Secreto 1: GPG_PRIVATE_KEY

```
Nombre: GPG_PRIVATE_KEY
Valor: [copia TODO el contenido del archivo gpg-private-key.txt]
```

**Cómo copiar**:
```bash
# macOS
cat gpg-private-key.txt | pbcopy

# Linux
cat gpg-private-key.txt | xclip -selection clipboard

# O simplemente abre el archivo y copia todo
```

### Secreto 2: GPG_PASSPHRASE

```
Nombre: GPG_PASSPHRASE
Valor: [la contraseña que usaste al crear la clave]
```

Ejemplo: `MySecurePassphrase2024!`

---

## Paso 6: Limpiar (Seguridad)

Después de configurar los secretos en GitHub, **elimina** el archivo local:

```bash
rm gpg-private-key.txt
```

---

## Verificación

Para verificar que todo está correcto:

```bash
# 1. Verificar que la clave existe
gpg --list-secret-keys

# 2. Verificar que está publicada (puede tardar unos minutos)
gpg --keyserver keyserver.ubuntu.com --recv-keys ABCD1234EFGH5678

# 3. Probar firma (opcional)
echo "test" | gpg --clearsign
```

---

## Resumen de lo que Necesitas para GitHub

Después de seguir estos pasos, tendrás:

1. ✅ **GPG_PRIVATE_KEY**: Contenido del archivo `gpg-private-key.txt`
2. ✅ **GPG_PASSPHRASE**: La contraseña que creaste

Estos 2 secretos + los 4 de Maven Central y Gradle = **6 secretos totales**

---

## Troubleshooting

### "gpg: agent_genkey failed: No such file or directory"

**Solución**:
```bash
# Reinicia el agente GPG
gpgconf --kill gpg-agent
gpg-agent --daemon
```

### "gpg: keyserver send failed: No route to host"

**Solución**: Intenta con otro servidor de claves:
```bash
gpg --keyserver hkp://keyserver.ubuntu.com:80 --send-keys ABCD1234EFGH5678
```

### Olvidé mi passphrase

**Solución**: Tendrás que generar una nueva clave GPG desde el principio.

---

## Comandos de Referencia Rápida

```bash
# Generar clave
gpg --full-generate-key

# Listar claves
gpg --list-secret-keys --keyid-format=long

# Publicar clave
gpg --keyserver keyserver.ubuntu.com --send-keys [KEY_ID]

# Exportar clave privada en base64
gpg --export-secret-keys [KEY_ID] | base64 > gpg-private-key.txt

# Eliminar archivo temporal
rm gpg-private-key.txt
```

---

## ¿Por qué Maven Central Requiere GPG?

Maven Central es un repositorio público usado por millones de desarrolladores. La firma GPG garantiza:

1. **Autenticidad**: El artefacto fue publicado por ti
2. **Integridad**: El artefacto no fue modificado después de la publicación
3. **No repudio**: No puedes negar que publicaste ese artefacto

Es un requisito de seguridad estándar en el ecosistema Java.

---

**Tiempo estimado**: 5-10 minutos  
**Dificultad**: Fácil  
**Frecuencia**: Solo una vez (la clave no expira)
