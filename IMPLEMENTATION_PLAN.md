# Plan de Implementación - Corrección de Issues

## Estado: EN PROGRESO
**Fecha inicio:** 2024-02-22

---

## ✅ COMPLETADO

### Fase 0: Análisis y Planificación
- [x] Identificar todos los issues encontrados en testing
- [x] Priorizar issues por severidad
- [x] Crear plan de implementación

---

## 🔄 EN PROGRESO

### Fase 1: Fixes Críticos (Desbloquear funcionalidad)

#### Issue #1: Template Organization - Framework-Aware Adapter Loading
**Prioridad:** CRÍTICA  
**Estimación:** 4-6 horas

**Tareas:**
- [ ] 1.1 Actualizar `AdapterMetadataLoader` para soportar rutas por framework
- [ ] 1.2 Agregar método `resolveAdapterPath(framework, paradigm, adapterType, adapterName)`
- [ ] 1.3 Actualizar `TemplateValidator` para usar nuevas rutas
- [ ] 1.4 Actualizar tests de `AdapterMetadataLoader`
- [ ] 1.5 Verificar con test de integración

**Archivos a modificar:**
- `src/main/java/com/pragma/archetype/infrastructure/adapter/out/template/AdapterMetadataLoader.java`
- `src/main/java/com/pragma/archetype/domain/service/TemplateValidator.java`
- `src/test/java/com/pragma/archetype/infrastructure/adapter/out/template/AdapterMetadataLoaderTest.java`

---

#### Issue #2: Metadata Format Standardization
**Prioridad:** CRÍTICA  
**Estimación:** 2-3 horas

**Tareas:**
- [ ] 2.1 Actualizar `AdapterMetadataLoader.extractDependencies()` para soportar ambos formatos
- [ ] 2.2 Agregar tests para formato anidado `dependencies.gradle`
- [ ] 2.3 Agregar tests para formato plano `dependencies`
- [ ] 2.4 Documentar formato estándar en CONTRIBUTING.md

**Archivos a modificar:**
- `src/main/java/com/pragma/archetype/infrastructure/adapter/out/template/AdapterMetadataLoader.java`
- `src/test/java/com/pragma/archetype/infrastructure/adapter/out/template/AdapterMetadataLoaderTest.java`

---

#### Issue #3: Template Variable Context
**Prioridad:** CRÍTICA  
**Estimación:** 3-4 horas

**Tareas:**
- [ ] 3.1 Revisar `AdapterGenerator.generateDataEntity()` y agregar variables faltantes
- [ ] 3.2 Revisar `ProjectGenerator` y agregar variables faltantes
- [ ] 3.3 Crear método helper `buildTemplateContext()` centralizado
- [ ] 3.4 Agregar tests para verificar contexto completo
- [ ] 3.5 Documentar variables disponibles en cada tipo de template

**Archivos a modificar:**
- `src/main/java/com/pragma/archetype/application/generator/AdapterGenerator.java`
- `src/main/java/com/pragma/archetype/application/generator/ProjectGenerator.java`
- `src/test/java/com/pragma/archetype/application/generator/AdapterGeneratorTest.java`

---

### Fase 2: Completar Templates Faltantes

#### Issue #4: Entity Templates para Adaptadores
**Prioridad:** ALTA  
**Estimación:** 2-3 horas

**Decisión:** Las entidades son necesarias para adaptadores de persistencia (MongoDB, Redis, PostgreSQL).
Son las clases de datos (DTOs) que mapean entre el dominio y la capa de persistencia.

**Tareas:**
- [ ] 4.1 Crear `Entity.java.ftl` para MongoDB
- [ ] 4.2 Crear `Entity.java.ftl` para Redis
- [ ] 4.3 Crear `Entity.java.ftl` para PostgreSQL
- [ ] 4.4 Crear `Entity.java.ftl` genérico para otros adaptadores
- [ ] 4.5 Agregar tests de generación de entidades

**Ubicación:**
- `backend-architecture-design-archetype-generator-templates/frameworks/spring/reactive/adapters/driven-adapters/mongodb/Entity.java.ftl`
- `backend-architecture-design-archetype-generator-templates/frameworks/spring/reactive/adapters/driven-adapters/redis/Entity.java.ftl`
- `backend-architecture-design-archetype-generator-templates/frameworks/spring/reactive/adapters/driven-adapters/postgresql/Entity.java.ftl`

---

#### Issue #5: Onion Architecture Templates
**Prioridad:** ALTA  
**Estimación:** 2 horas

**Tareas:**
- [ ] 5.1 Copiar `.gitignore.ftl` desde hexagonal-single
- [ ] 5.2 Copiar `Application.java.ftl` y adaptar para Onion
- [ ] 5.3 Copiar `application.yml.ftl` y adaptar para Onion
- [ ] 5.4 Copiar `BeanConfiguration.java.ftl` y adaptar para Onion
- [ ] 5.5 Verificar con test de generación de proyecto Onion

**Ubicación:**
- `backend-architecture-design-archetype-generator-templates/architectures/onion-single/templates/`

---

### Fase 3: Mejorar Calidad de Tests

#### Issue #6: Error Message Assertions
**Prioridad:** MEDIA  
**Estimación:** 1-2 horas

**Tareas:**
- [ ] 6.1 Ejecutar tests y capturar mensajes de error reales
- [ ] 6.2 Actualizar assertions en `ErrorScenarioIntegrationTest`
- [ ] 6.3 Verificar que todos los tests pasen
- [ ] 6.4 Documentar formato de mensajes de error

**Archivos a modificar:**
- `src/test/java/com/pragma/archetype/integration/ErrorScenarioIntegrationTest.java`

---

#### Issue #7: Property-Based Tests con Kotest
**Prioridad:** MEDIA  
**Estimación:** 1 día

**Tareas:**
- [ ] 7.1 Agregar dependencia Kotest a `build.gradle.kts`
- [ ] 7.2 Crear `PropertyTestBase.kt` con configuración común
- [ ] 7.3 Crear generadores Arb para domain models
- [ ] 7.4 Implementar Property 1: Adapter Placement by Type
- [ ] 7.5 Implementar Property 2: Placeholder Substitution
- [ ] 7.6 Implementar Property 5: YAML Merge Preserves Values
- [ ] 7.7 Implementar Property 6: YAML Round-Trip
- [ ] 7.8 Implementar Property 7: YAML Structure Preservation
- [ ] 7.9 Ejecutar property tests con 100+ iteraciones

**Archivos a crear:**
- `src/test/kotlin/com/pragma/archetype/property/PropertyTestBase.kt`
- `src/test/kotlin/com/pragma/archetype/property/DomainModelGenerators.kt`
- `src/test/kotlin/com/pragma/archetype/property/PathResolutionPropertyTest.kt`
- `src/test/kotlin/com/pragma/archetype/property/YamlMergePropertyTest.kt`

---

#### Issue #9: Cache Verification Tests
**Prioridad:** MEDIA  
**Estimación:** 3-4 horas

**Tareas:**
- [ ] 9.1 Crear `CacheIntegrationTest.java`
- [ ] 9.2 Test: Verificar que templates se cachean en primera descarga
- [ ] 9.3 Test: Verificar que segunda generación usa caché (más rápida)
- [ ] 9.4 Test: Verificar estructura del directorio de caché
- [ ] 9.5 Test: Verificar que caché se invalida al cambiar branch
- [ ] 9.6 Agregar logging para operaciones de caché

**Archivos a crear:**
- `src/test/java/com/pragma/archetype/integration/CacheIntegrationTest.java`

**Archivos a modificar:**
- `src/main/java/com/pragma/archetype/infrastructure/adapter/out/http/GitHubTemplateDownloader.java`

---

#### Issue #10: Network Error Handling Tests
**Prioridad:** BAJA  
**Estimación:** 3-4 horas

**Tareas:**
- [ ] 10.1 Crear `NetworkErrorHandlingTest.java` con WireMock
- [ ] 10.2 Test: Connection timeout
- [ ] 10.3 Test: HTTP 404 (branch no existe)
- [ ] 10.4 Test: HTTP 500 (server error)
- [ ] 10.5 Test: DNS resolution failure
- [ ] 10.6 Verificar mensajes de error claros

**Archivos a crear:**
- `src/test/java/com/pragma/archetype/integration/NetworkErrorHandlingTest.java`

---

### Fase 4: Limpieza de Documentación

#### Limpieza de Archivos Markdown en Core
**Prioridad:** ALTA  
**Estimación:** 2-3 horas

**Archivos a ELIMINAR del core:**
- [ ] ARCHITECTURE_VARIANTS.md
- [ ] CLEAN_ARCH_GENERATOR_SPEC.md
- [ ] COMMANDS_AND_RESPONSIBILITIES.md
- [ ] ESTADO_ACTUAL.md
- [ ] GENERATOR_FLOW.md
- [ ] GITHUB_DOWNLOAD_IMPLEMENTATION.md
- [ ] IMPLEMENTATION_ROADMAP.md
- [ ] MEJORAS.md
- [ ] MIGRATION_SUMMARY.md
- [ ] MULTI_MODULE_STRUCTURE.md
- [ ] PLUGIN_USAGE_FLOW.md
- [ ] PROGRESS.md
- [ ] PROJECT_STRUCTURE.md
- [ ] RESUMEN_COMPLETO.md
- [ ] TASK_12.3_IMPLEMENTATION.md
- [ ] TASK_25.1_TEST_REPORT.md
- [ ] TASK_25.2_TEST_REPORT.md
- [ ] TASK_25.3_TEST_REPORT.md
- [ ] TASK_25.4_TEST_REPORT.md
- [ ] TASK_25.5_TEST_REPORT.md
- [ ] TASK_25.6_TEST_REPORT.md
- [ ] TASK_25.7-25.10_COMPREHENSIVE_TEST_REPORT.md
- [ ] TEMPLATE_CONFIGURATION.md
- [ ] TEMPLATES_EXPLAINED.md
- [ ] test-*.sh (scripts de test)
- [ ] test-*.log (logs de test)

**Archivos a MANTENER:**
- [x] README.md (actualizar)
- [x] CONTRIBUTING.md (actualizar)

**Estructura docs/ a crear:**
```
docs/
├── architecture.md          # Arquitectura del plugin
├── development.md           # Guía de desarrollo
├── testing.md              # Estrategia de testing
└── templates.md            # Sistema de templates
```

---

### Fase 5: Actualización de Documentación Docusaurus

#### Sincronizar Documentación de Usuario con Estado Actual
**Prioridad:** ALTA  
**Estimación:** 2-3 horas

**IMPORTANTE:** Docusaurus es documentación de USUARIO (cómo usar el plugin), NO documentación interna.

**Tareas:**
- [ ] Revisar y actualizar listado de adaptadores disponibles en `docs/clean-arch/adapters/`
- [ ] Actualizar comandos si hay cambios en `docs/clean-arch/commands/`
- [ ] Revisar getting-started guides para asegurar que reflejan el flujo actual
- [ ] Actualizar for-contributors con información de templates por framework
- [ ] Actualizar ejemplos de metadata.yml con formato correcto
- [ ] Verificar que todos los links funcionen
- [ ] Agregar/actualizar ejemplos de uso reales

---

### Fase 6: Tests Finales

#### Issue #8: Ejecutar Suite Completa de Tests
**Prioridad:** ALTA  
**Estimación:** 1 hora

**Tareas:**
- [ ] 8.1 Ejecutar `./gradlew clean test`
- [ ] 8.2 Verificar que todos los tests pasen
- [ ] 8.3 Ejecutar `./gradlew jacocoTestReport`
- [ ] 8.4 Verificar cobertura (objetivo: 60%+)
- [ ] 8.5 Ejecutar property tests
- [ ] 8.6 Ejecutar integration tests
- [ ] 8.7 Ejecutar manual workflow tests
- [ ] 8.8 Generar reporte final

---

## 📊 Métricas de Progreso

### Tests
- Unit Tests: 220/226 passing (97.3%) → Objetivo: 226/226 (100%)
- Property Tests: 0/24 implemented → Objetivo: 8/24 (core properties)
- Integration Tests: 24/30 passing (80%) → Objetivo: 30/30 (100%)
- Coverage: 42% → Objetivo: 60%+

### Documentación
- Archivos MD en core: 30+ → Objetivo: 2 (README + CONTRIBUTING)
- Docs estructurados: 0 → Objetivo: 4 archivos en docs/
- Docusaurus actualizado: Parcial → Objetivo: 100%

---

## 🎯 Orden de Ejecución Recomendado

1. **Día 1 (6-8 horas):**
   - Issue #1: Template Organization (4-6h)
   - Issue #2: Metadata Format (2-3h)

2. **Día 2 (6-8 horas):**
   - Issue #3: Template Variables (3-4h)
   - Issue #4: Entity Templates (2-3h)
   - Issue #5: Onion Templates (2h)

3. **Día 3 (6-8 horas):**
   - Issue #6: Error Messages (1-2h)
   - Limpieza de Documentación (2-3h)
   - Actualización Docusaurus (2-3h)

4. **Día 4 (6-8 horas):**
   - Issue #7: Property Tests (full day)

5. **Día 5 (4-6 horas):**
   - Issue #9: Cache Tests (3-4h)
   - Issue #8: Tests Finales (1h)
   - Reporte final (1h)

**Total estimado:** 4-5 días de trabajo

---

## 📝 Notas

- Los issues #1, #2, #3 son bloqueantes y deben hacerse primero
- La limpieza de documentación puede hacerse en paralelo
- Los property tests pueden posponerse si hay presión de tiempo
- El issue #10 (Network tests) es opcional y puede ir al backlog

---

**Última actualización:** 2024-02-22
