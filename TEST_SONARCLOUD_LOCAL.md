# Prueba Local de SonarCloud

Este documento explica cómo probar la configuración de SonarCloud localmente antes de hacer merge.

## ✅ Problema Resuelto

El error que estabas viendo:
```
class java.lang.String cannot be cast to class java.util.Collection
```

**Causa**: Las propiedades `sonar.exclusions` y `sonar.coverage.exclusions` esperaban una `Collection` pero recibían un `String` con valores separados por comas.

**Solución**: Cambiamos a usar `listOf()` en Kotlin para pasar listas nativas en lugar de strings.

## 🧪 Prueba Local (Sin Token de SonarCloud)

Puedes verificar que la configuración es correcta sin necesitar un token de SonarCloud:

### 1. Generar el Reporte de Coverage

```bash
./gradlew clean build test jacocoTestReport
```

**Resultado esperado**:
- ✅ Tests pasan exitosamente
- ✅ Se genera el archivo XML: `build/reports/jacoco/test/jacocoTestReport.xml`

### 2. Verificar el Archivo XML

```bash
# Ver que el archivo existe y su tamaño
ls -lh build/reports/jacoco/test/jacocoTestReport.xml

# Ver las primeras líneas del XML
head -30 build/reports/jacoco/test/jacocoTestReport.xml
```

**Resultado esperado**:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
<report name="archetype-generator">
    <sessioninfo id="..." start="..." dump="..."/>
    <package name="com/pragma/archetype/application/generator">
        <class name="com/pragma/archetype/application/generator/AdapterGenerator">
            <method name="generate" desc="..." line="...">
                <counter type="INSTRUCTION" missed="..." covered="..."/>
                <counter type="LINE" missed="..." covered="..."/>
                ...
```

### 3. Ver el Reporte HTML Localmente

```bash
# macOS
open build/reports/jacoco/test/html/index.html

# Linux
xdg-open build/reports/jacoco/test/html/index.html

# Windows
start build/reports/jacoco/test/html/index.html
```

**Resultado esperado**:
- Se abre el navegador con el reporte de coverage
- Puedes navegar por paquetes y clases
- Ves líneas cubiertas (verde) y no cubiertas (rojo)

## 🔐 Prueba con SonarCloud (Requiere Token)

Si tienes acceso a SonarCloud y quieres probar el análisis completo:

### 1. Obtener Token de SonarCloud

1. Ve a https://sonarcloud.io
2. My Account → Security → Generate Tokens
3. Nombre: `Local Testing`
4. Tipo: User Token
5. Copia el token generado

### 2. Ejecutar Análisis Local

```bash
./gradlew clean build test jacocoTestReport

./gradlew sonar \
  -Dsonar.projectKey=com.pragma:archetype-generator-core \
  -Dsonar.organization=somospragma \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=TU_TOKEN_AQUI \
  -Dsonar.gradle.skipCompile=true
```

**Nota**: Reemplaza `somospragma` con tu organización real en SonarCloud.

### 3. Verificar en SonarCloud

1. Ve a https://sonarcloud.io
2. Navega a tu proyecto
3. Verifica que aparezca el coverage en la sección "Coverage"

## 📋 Checklist Antes de Hacer Merge

- [ ] Los tests pasan: `./gradlew test`
- [ ] El reporte XML se genera: `ls build/reports/jacoco/test/jacocoTestReport.xml`
- [ ] El reporte HTML se ve correctamente
- [ ] No hay errores de compilación
- [ ] El coverage es ≥ 80% (verificar en HTML)

## 🚀 Después del Merge

Una vez que hagas merge a `main` o `develop`:

1. **El workflow de GitHub Actions se ejecutará automáticamente**
   - Compila el proyecto
   - Ejecuta los tests
   - Genera el reporte de coverage
   - Envía los resultados a SonarCloud

2. **Verifica el workflow**
   - Ve a: Repository → Actions
   - Busca el workflow "SonarCloud Analysis"
   - Verifica que complete exitosamente

3. **Verifica en SonarCloud**
   - Ve a https://sonarcloud.io
   - Navega a tu proyecto
   - Verifica que el coverage aparezca correctamente

## 🔧 Configuración de Secretos en GitHub

Para que el workflow funcione, necesitas configurar estos secretos:

1. Ve a: Repository → Settings → Secrets and variables → Actions
2. Agrega estos secretos:

| Secreto | Valor | Descripción |
|---------|-------|-------------|
| `SONAR_TOKEN` | `squ_...` | Token de SonarCloud |
| `SONAR_PROJECT_KEY` | `com.pragma:archetype-generator-core` | Clave del proyecto |
| `SONAR_ORGANIZATION` | `somospragma` | Tu organización |

## 📊 Métricas Esperadas

Con la configuración actual:

- **Coverage**: ~80-85%
- **Lines to Cover**: ~5,000-6,000
- **Uncovered Lines**: ~1,000-1,500
- **Quality Gate**: Should pass

## ❓ Troubleshooting

### El archivo XML no se genera

```bash
# Limpiar y regenerar
./gradlew clean
./gradlew test jacocoTestReport --rerun-tasks
```

### Error: "sonar.token is required"

Esto es normal si intentas ejecutar `./gradlew sonar` sin token. Opciones:

1. **Opción 1**: Solo genera el reporte sin enviar a SonarCloud
   ```bash
   ./gradlew test jacocoTestReport
   ```

2. **Opción 2**: Usa un token de SonarCloud (ver sección anterior)

### El coverage es muy bajo

Verifica las exclusiones en `build.gradle.kts`:

```kotlin
sonar {
    properties {
        property("sonar.coverage.exclusions", listOf(
            "**/config/**",
            "**/infrastructure/config/**",
            "**/*Plugin.java",
            "**/*Task.java",
            "**/domain/model/**",
            "**/domain/port/**"
        ))
    }
}
```

Estas exclusiones son intencionales para:
- Clases de configuración (no tienen lógica de negocio)
- Plugins y Tasks de Gradle (difíciles de testear)
- Modelos de dominio (POJOs con Lombok)
- Ports (interfaces sin implementación)

## 📚 Referencias

- [SONARCLOUD_SETUP.md](./SONARCLOUD_SETUP.md) - Guía completa de configuración
- [SONARQUBE_COVERAGE_SETUP.md](./SONARQUBE_COVERAGE_SETUP.md) - Troubleshooting detallado
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)

## ✅ Resumen

1. **Localmente**: Ejecuta `./gradlew clean build test jacocoTestReport`
2. **Verifica**: Abre `build/reports/jacoco/test/html/index.html`
3. **Merge**: Haz merge a `main` o `develop`
4. **Configura**: Agrega los secretos en GitHub
5. **Verifica**: Revisa el workflow en Actions y el resultado en SonarCloud

¡Listo! El coverage debería aparecer correctamente en SonarCloud después del merge.
