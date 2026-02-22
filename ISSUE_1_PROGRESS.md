# Issue #1: Template Organization - Framework-Aware Adapter Loading

## Estado: ✅ COMPLETADO (Fase 1)

### Cambios Realizados

#### 1. AdapterMetadataLoader.java
- ✅ Agregado nuevo método `loadAdapterMetadata(adapterName, framework, paradigm, adapterType)`
- ✅ Método legacy `loadAdapterMetadata(adapterName)` marcado como @Deprecated
- ✅ Implementado fallback: intenta framework-aware primero, luego legacy
- ✅ Refactorizado código común en `loadMetadataFromPath()`
- ✅ Actualizado `validateReferencedTemplates()` para usar basePath dinámico

**Ruta framework-aware:**
```
frameworks/{framework}/{paradigm}/adapters/{adapterType}/{adapterName}/metadata.yml
```

**Ejemplo:**
```
frameworks/spring/reactive/adapters/driven-adapters/mongodb/metadata.yml
```

#### 2. TemplateRepository.java (Interface)
- ✅ Agregado nuevo método con firma completa
- ✅ Método legacy marcado como @Deprecated
- ✅ Documentación actualizada

#### 3. FreemarkerTemplateRepository.java
- ✅ Implementado nuevo método que delega a AdapterMetadataLoader
- ✅ Mantiene compatibilidad con método legacy

### Próximos Pasos

#### Fase 2: Actualizar Llamadas (PENDIENTE)
Necesitamos actualizar los lugares donde se llama `loadAdapterMetadata()` para usar el nuevo método:

**Archivos a actualizar:**
1. `GenerateAdapterUseCaseImpl.java` - Múltiples llamadas
2. `TemplateValidator.java` - 1 llamada
3. Tests que usan el método

**Estrategia:**
- Obtener framework y paradigm del ProjectConfig
- Determinar adapterType basado en si es driven o driving adapter
- Pasar los 4 parámetros al nuevo método

#### Fase 3: Tests (PENDIENTE)
- Actualizar tests existentes
- Agregar tests para framework-aware loading
- Agregar tests para fallback a legacy

### Compatibilidad

✅ **Backward Compatible:** El código legacy sigue funcionando
✅ **Forward Compatible:** Nuevo código puede usar framework-aware paths
✅ **Fallback:** Si framework-aware falla, intenta legacy automáticamente

### Ejemplo de Uso

**Antes (Legacy):**
```java
AdapterMetadata metadata = templateRepository.loadAdapterMetadata("mongodb");
```

**Ahora (Framework-Aware):**
```java
AdapterMetadata metadata = templateRepository.loadAdapterMetadata(
    "mongodb",           // adapterName
    "spring",            // framework
    "reactive",          // paradigm
    "driven-adapters"    // adapterType
);
```

### Notas

- El método legacy está deprecated pero funcional
- El fallback asegura que proyectos existentes sigan funcionando
- La migración puede ser gradual

---

**Última actualización:** 2024-02-22
**Siguiente:** Actualizar llamadas en GenerateAdapterUseCaseImpl
