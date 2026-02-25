# SonarQube ClassCastException - Análisis y Solución

## 🔴 Problema

```
Execution failed for task ':sonar'.
> class java.lang.String cannot be cast to class java.util.Collection
```

## 🔍 Análisis del Problema

### Stack Trace Completo

```
Caused by: java.lang.ClassCastException: class java.lang.String cannot be cast to class java.util.Collection
    at org.sonarqube.gradle.SonarUtils.appendProps(SonarUtils.java:125)
    at org.sonarqube.gradle.SonarUtils.appendSourcesProp(SonarUtils.java:130)
    at org.sonarqube.gradle.SonarPropertyComputer.addKotlinBuildScriptsToSources(SonarPropertyComputer.java:490)
```

### Causa Raíz

El plugin de SonarQube para Gradle tiene un comportamiento especial:

1. **Detecta automáticamente** que el proyecto usa Kotlin (por el archivo `build.gradle.kts`)
2. **Intenta agregar** los scripts de Kotlin build a la propiedad `sonar.sources`
3. **Espera que `sonar.sources` sea una Collection** para poder agregar elementos
4. **Falla** cuando encuentra que `sonar.sources` es un String

### Código Problemático en el Plugin

```java
// SonarUtils.java:125
private static void appendProps(Map<String, Object> properties, String key, Collection<String> valuesToAppend) {
    // El plugin espera que properties.get(key) retorne una Collection
    Collection<String> existingValues = (Collection<String>) properties.get(key);
    // ❌ FALLA si properties.get(key) retorna un String
}
```

## ❌ Intentos Fallidos

### Intento 1: Usar `listOf()` en Kotlin

```kotlin
sonar {
    properties {
        property("sonar.sources", listOf("src/main/java"))  // ❌ No funciona
    }
}
```

**Resultado**: Mismo error. El plugin no reconoce las listas de Kotlin.

### Intento 2: Concatenación de Strings

```kotlin
sonar {
    properties {
        property("sonar.sources", "src/main/java")  // ❌ No funciona
    }
}
```

**Resultado**: El plugin intenta convertir el String a Collection y falla.

### Intento 3: Usar Arrays de Java

```kotlin
sonar {
    properties {
        property("sonar.sources", arrayOf("src/main/java"))  // ❌ No funciona
    }
}
```

**Resultado**: Mismo error de casting.

## ✅ Solución Final

### Enfoque: Configuración Híbrida

**Principio**: Dejar que el plugin auto-detecte las fuentes y configurar todo lo demás en `sonar-project.properties`.

### 1. build.gradle.kts (Mínimo)

```kotlin
sonar {
    properties {
        // Solo propiedades dinámicas que necesitan valores de Gradle
        property("sonar.projectVersion", version.toString())
        property("sonar.gradle.skipCompile", "true")
    }
}
```

**Por qué funciona**:
- No definimos `sonar.sources` → El plugin lo auto-detecta correctamente
- No definimos `sonar.tests` → El plugin lo auto-detecta correctamente
- Solo configuramos valores que cambian dinámicamente

### 2. sonar-project.properties (Completo)

```properties
# Project identification
sonar.projectKey=com.pragma:archetype-generator-core
sonar.organization=somospragma
sonar.projectName=Clean Architecture Generator Core

# Source and test directories (auto-detected by plugin, but can be overridden)
sonar.sources=src/main/java
sonar.tests=src/test/java

# Java version
sonar.java.source=21
sonar.java.target=21

# Coverage
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml

# Binaries
sonar.java.binaries=build/classes/java/main
sonar.java.test.binaries=build/classes/java/test

# Exclusions
sonar.exclusions=\
  **/*Test.java,\
  **/*Tests.java,\
  **/test/**,\
  **/build/**

sonar.coverage.exclusions=\
  **/config/**,\
  **/infrastructure/config/**,\
  **/*Plugin.java,\
  **/*Task.java,\
  **/domain/model/**,\
  **/domain/port/**
```

**Por qué funciona**:
- El archivo `.properties` usa el formato nativo de SonarQube
- No hay conversiones de tipos
- El plugin lee estos valores directamente sin procesamiento adicional

### 3. GitHub Actions Workflow

```yaml
- name: SonarCloud Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: |
    ./gradlew sonar \
      -Dsonar.host.url=https://sonarcloud.io \
      ${{ secrets.SONAR_PROJECT_KEY && format('-Dsonar.projectKey={0}', secrets.SONAR_PROJECT_KEY) || '' }} \
      ${{ secrets.SONAR_ORGANIZATION && format('-Dsonar.organization={0}', secrets.SONAR_ORGANIZATION) || '' }}
```

**Características**:
- Usa valores de `sonar-project.properties` por defecto
- Permite override con secretos de GitHub si están definidos
- Formato condicional para evitar parámetros vacíos

## 📊 Comparación de Enfoques

| Enfoque | Ventajas | Desventajas | Resultado |
|---------|----------|-------------|-----------|
| Todo en `build.gradle.kts` | Centralizado en Gradle | ClassCastException | ❌ Falla |
| Todo en `sonar-project.properties` | Sin conversiones de tipos | Valores estáticos | ✅ Funciona |
| **Híbrido (Solución)** | Dinámico + Sin errores | Configuración en 2 lugares | ✅ Funciona |

## 🎯 Lecciones Aprendidas

### 1. El Plugin de SonarQube es Especial

- No es un plugin de Gradle estándar
- Tiene lógica interna compleja para auto-detección
- Modifica propiedades después de que las defines

### 2. Kotlin DSL vs Properties File

- **Kotlin DSL**: Bueno para valores dinámicos (version, paths calculados)
- **Properties File**: Mejor para configuración estática (exclusiones, paths fijos)

### 3. Auto-detección del Plugin

El plugin auto-detecta:
- ✅ Directorios de fuentes (`src/main/java`)
- ✅ Directorios de tests (`src/test/java`)
- ✅ Binarios compilados (`build/classes`)
- ✅ Scripts de Kotlin build (`.gradle.kts`)

**Recomendación**: Dejar que el plugin auto-detecte cuando sea posible.

## 🔧 Troubleshooting

### Si el error persiste

1. **Verificar que no hay configuración duplicada**:
   ```bash
   # Buscar configuraciones de sonar.sources
   grep -r "sonar.sources" .
   ```

2. **Limpiar caché de Gradle**:
   ```bash
   ./gradlew clean --no-build-cache
   rm -rf ~/.gradle/caches/
   ```

3. **Verificar versión del plugin**:
   ```kotlin
   // build.gradle.kts
   id("org.sonarqube") version "4.4.1.3373"  // Usar versión estable
   ```

4. **Ejecutar con debug**:
   ```bash
   ./gradlew sonar --debug 2>&1 | grep -i "sonar.sources"
   ```

### Si necesitas configurar sources manualmente

**Opción 1**: Solo en `sonar-project.properties`
```properties
sonar.sources=src/main/java,src/main/kotlin
```

**Opción 2**: No configurar nada (auto-detección)
```kotlin
sonar {
    properties {
        // No definir sonar.sources
    }
}
```

## 📚 Referencias

- [SonarQube Gradle Plugin Documentation](https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/)
- [SonarQube Analysis Parameters](https://docs.sonarqube.org/latest/analyzing-source-code/analysis-parameters/)
- [Gradle Plugin Issue Tracker](https://github.com/SonarSource/sonar-scanner-gradle/issues)

## ✅ Checklist de Verificación

Antes de ejecutar `./gradlew sonar`:

- [ ] `build.gradle.kts` tiene configuración mínima (solo valores dinámicos)
- [ ] `sonar-project.properties` tiene toda la configuración estática
- [ ] No hay definición de `sonar.sources` en `build.gradle.kts`
- [ ] El proyecto compila correctamente: `./gradlew build`
- [ ] El reporte de coverage existe: `ls build/reports/jacoco/test/jacocoTestReport.xml`
- [ ] La versión del plugin es estable: `4.4.1.3373`

## 🎉 Resultado Esperado

```bash
./gradlew sonar -Dsonar.host.url=https://sonarcloud.io

> Task :sonar
SonarQube analysis completed successfully

BUILD SUCCESSFUL in 15s
```

---

**Última actualización**: 2026-02-25  
**Estado**: ✅ Resuelto  
**Versión del plugin**: 4.4.1.3373
