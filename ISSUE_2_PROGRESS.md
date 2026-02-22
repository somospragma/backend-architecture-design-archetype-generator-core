# Issue #2: Metadata Format Standardization

## Estado: ✅ COMPLETADO

### Problema
Los archivos `metadata.yml` en el repositorio de plantillas usan formato anidado (`dependencies.gradle`) pero el código esperaba formato plano (`dependencies`).

**Formato Anidado (en plantillas):**
```yaml
dependencies:
  gradle:
    - groupId: org.springframework.boot
      artifactId: spring-boot-starter-data-mongodb-reactive
      version: 3.2.0
```

**Formato Plano (esperado por código):**
```yaml
dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-data-mongodb-reactive
    version: 3.2.0
```

### Solución Implementada

Actualizado `AdapterMetadataLoader.java` para **soportar ambos formatos** automáticamente.

### Cambios Realizados

#### 1. extractDependencies()
- ✅ Detecta si es formato anidado (Map con key `gradle`)
- ✅ Detecta si es formato plano (List directa)
- ✅ Delega a `parseDependencyList()` con flag de formato

#### 2. extractTestDependencies()
- ✅ Detecta si es formato anidado (Map con key `gradle`)
- ✅ Detecta si es formato plano (List directa)
- ✅ Delega a `parseDependencyList()` con flag de formato

#### 3. parseDependencyList() (NUEVO)
- ✅ Método helper que procesa lista de dependencias
- ✅ Recibe flag `isNestedFormat` para saber qué campos buscar
- ✅ Llama a `parseDependency()` con el formato correcto

#### 4. parseDependency()
- ✅ Actualizado para aceptar parámetro `isNestedFormat`
- ✅ Busca `groupId/artifactId` si es nested format
- ✅ Busca `group/artifact` si es flat format
- ✅ **Fallback:** Si no encuentra en un formato, intenta el otro
- ✅ Método legacy deprecated para compatibilidad

### Formatos Soportados

#### Formato Anidado (Nested)
```yaml
dependencies:
  gradle:
    - groupId: org.springframework.boot
      artifactId: spring-boot-starter-webflux
      version: 3.2.0

testDependencies:
  gradle:
    - groupId: io.projectreactor
      artifactId: reactor-test
      version: 3.6.0
      scope: test
```

#### Formato Plano (Flat)
```yaml
dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-webflux
    version: 3.2.0

testDependencies:
  - group: io.projectreactor
    artifact: reactor-test
    version: 3.6.0
    scope: test
```

#### Formato Mixto (Fallback)
```yaml
dependencies:
  gradle:
    - group: org.springframework.boot  # Usa 'group' en vez de 'groupId'
      artifact: spring-boot-starter-webflux
      version: 3.2.0
```

### Compatibilidad

✅ **Backward Compatible:** Formato plano sigue funcionando  
✅ **Forward Compatible:** Formato anidado ahora funciona  
✅ **Flexible:** Acepta mezcla de formatos (fallback automático)  
✅ **Sin Breaking Changes:** No requiere actualizar templates existentes

### Beneficios

1. **No requiere migración:** Templates existentes siguen funcionando
2. **Flexibilidad:** Acepta ambos formatos
3. **Robustez:** Fallback automático si falta un campo
4. **Claridad:** Mensajes de error claros si faltan ambos formatos

### Tests Pendientes

- [ ] Test para formato anidado (dependencies.gradle)
- [ ] Test para formato plano (dependencies)
- [ ] Test para formato mixto (fallback)
- [ ] Test para testDependencies en ambos formatos

---

**Estado:** ✅ COMPLETADO  
**Siguiente:** Issue #3 - Template Variable Context
