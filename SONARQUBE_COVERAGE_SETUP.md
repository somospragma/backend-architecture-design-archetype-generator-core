# SonarQube Coverage Setup

## Problema: Cobertura no aparece en SonarQube

Si la cobertura de JaCoCo no aparece en SonarQube, puede deberse a varias razones:

### 1. Ruta del Reporte XML Incorrecta

**Problema**: SonarQube no encuentra el archivo XML de JaCoCo.

**Solución**: Verificar que la ruta en `sonar-project.properties` coincida con la ubicación real del archivo:

```properties
# En sonar-project.properties
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
```

**Verificar ubicación real**:
```bash
./gradlew clean test jacocoTestReport
ls -la build/reports/jacoco/test/
```

### 2. Reporte XML No Generado

**Problema**: JaCoCo no está generando el reporte XML.

**Solución**: Asegurar que `build.gradle.kts` tenga la configuración correcta:

```kotlin
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)  // ← IMPORTANTE: debe estar en true
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/test/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/reports/jacoco/test/html"))
    }
}
```

### 3. Tests No Ejecutados Antes del Análisis

**Problema**: SonarQube analiza el código antes de que se ejecuten los tests.

**Solución**: Ejecutar tests y generar reporte antes del análisis:

```bash
# Orden correcto
./gradlew clean test jacocoTestReport
./gradlew sonar
```

### 4. Configuración de Binarios Incorrecta

**Problema**: SonarQube no encuentra las clases compiladas.

**Solución**: Verificar rutas en `sonar-project.properties`:

```properties
sonar.java.binaries=build/classes/java/main
sonar.java.test.binaries=build/classes/java/test
```

### 5. Plugin de SonarQube No Configurado

**Problema**: El plugin de SonarQube no está en el proyecto.

**Solución**: Agregar plugin al `build.gradle.kts`:

```kotlin
plugins {
    id("org.sonarqube") version "4.4.1.3373"
    jacoco
}
```

## Configuración Completa

### build.gradle.kts

```kotlin
plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.sonarqube") version "4.4.1.3373"  // ← Agregar
    jacoco
    kotlin("jvm") version "1.9.21"
}

tasks.test {
    useJUnitPlatform()
    ignoreFailures = true
    finalizedBy(tasks.jacocoTestReport)  // ← Genera reporte automáticamente
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/test/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/reports/jacoco/test/html"))
        csv.required.set(false)
    }
    
    // Excluir código generado por Lombok
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/infrastructure/config/**",
                    "**/*Plugin.class",
                    "**/*Task.class"
                )
            }
        })
    )
}

// Configuración de SonarQube (opcional, también puede ir en sonar-project.properties)
sonar {
    properties {
        property("sonar.projectKey", "com.pragma:archetype-generator-core")
        property("sonar.projectName", "Clean Architecture Generator Core")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
```

### sonar-project.properties

```properties
# Project identification
sonar.projectKey=com.pragma:archetype-generator-core
sonar.projectName=Clean Architecture Generator Core
sonar.projectVersion=1.0.0-PRERELEASE

# Source and test directories
sonar.sources=src/main/java
sonar.tests=src/test/java

# Java version
sonar.java.source=21
sonar.java.target=21

# Coverage report paths
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml

# Binary directories
sonar.java.binaries=build/classes/java/main
sonar.java.test.binaries=build/classes/java/test

# Exclusions
sonar.exclusions=\
  **/*Test.java,\
  **/*Tests.java,\
  **/test/**,\
  **/build/**

# Coverage exclusions
sonar.coverage.exclusions=\
  **/config/**,\
  **/infrastructure/config/**,\
  **/*Plugin.java,\
  **/*Task.java,\
  **/domain/model/**,\
  **/domain/port/**
```

## Comandos de Verificación

### 1. Generar Reporte de Cobertura
```bash
./gradlew clean test jacocoTestReport
```

### 2. Verificar que el XML existe
```bash
ls -la build/reports/jacoco/test/jacocoTestReport.xml
```

### 3. Ver contenido del XML (primeras líneas)
```bash
head -20 build/reports/jacoco/test/jacocoTestReport.xml
```

Debe verse algo como:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
<report name="backend-architecture-design-archetype-generator-core">
    <sessioninfo id="..." start="..." dump="..."/>
    <package name="com/pragma/archetype/application/generator">
        <class name="com/pragma/archetype/application/generator/AdapterGenerator">
            ...
```

### 4. Ejecutar análisis de SonarQube
```bash
./gradlew sonar \
  -Dsonar.host.url=https://sonarqube.example.com \
  -Dsonar.login=YOUR_TOKEN
```

### 5. Verificar en SonarQube
- Ir a tu proyecto en SonarQube
- Navegar a "Measures" → "Coverage"
- Debería mostrar el porcentaje de cobertura

## Troubleshooting

### El reporte XML está vacío o corrupto
```bash
# Limpiar y regenerar
./gradlew clean
./gradlew test jacocoTestReport --rerun-tasks
```

### SonarQube muestra 0% de cobertura
1. Verificar que el archivo XML existe y no está vacío
2. Verificar que la ruta en sonar-project.properties es correcta
3. Verificar que las clases compiladas existen en build/classes/java/main
4. Revisar logs de SonarQube para errores

### Cobertura muy baja debido a Lombok
Lombok genera código (getters, setters, builders) que no se ejecuta en tests.

**Solución**: Excluir clases con mucho código Lombok:
```properties
sonar.coverage.exclusions=\
  **/domain/model/**,\
  **/domain/port/**
```

## CI/CD Integration

### GitHub Actions
```yaml
name: SonarQube Analysis

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Shallow clones should be disabled
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      
      - name: Run tests and generate coverage
        run: ./gradlew clean test jacocoTestReport
      
      - name: SonarQube Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        run: ./gradlew sonar
```

## Métricas Esperadas

Con la configuración correcta, deberías ver:

- **Coverage**: ~80-85% (considerando exclusiones de Lombok)
- **Lines to Cover**: ~5000-6000 líneas
- **Uncovered Lines**: ~1000-1500 líneas
- **Conditions to Cover**: ~500-800
- **Uncovered Conditions**: ~100-200

## Referencias

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SonarQube Java Coverage](https://docs.sonarqube.org/latest/analysis/coverage/)
- [Gradle JaCoCo Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
