# Changelog

Cambios del plugin `co.com.pragma.archetype-generator`. Las plantillas tienen sus propias versiones en el [repositorio de templates](https://github.com/somospragma/backend-architecture-design-archetype-generator-templates/tags).

## 1.1.0 — 2026-11-11

### Cambiado

- **Las plantillas salen del último tag `v1.x.y` de templates**, no de la rama `main`. Una plantilla nueva o corregida llega con el siguiente tag de templates, sin actualizar el plugin.
- El plugin rechaza plantillas de un `schemaVersion` que no entiende, con un mensaje que dice qué hacer, en vez de generar código roto.
- Dependencias actualizadas: FreeMarker 2.3.35, jackson-databind 2.22.4, logback-classic 1.5.38, snakeyaml 2.7 y slf4j 2.0.20.

### Agregado

- `templates.version` en `.cleanarch.yml` fija un tag de templates, por ejemplo `v1.0.1`, para congelar un proyecto.
- `templates.branch` lee una rama de templates, para probar plantillas que aún no tienen tag.

### Corregido

- Los números de versión de los tags de templates se leen sin errores aunque un tag tenga un número fuera de rango.

### Al actualizar desde 1.0.x

- Cambia la versión del plugin a `1.1.0`. El plugin 1.0.x leía la rama `main` de templates, que ya no es compatible con él.
- El plugin consulta los tags de templates en la API de GitHub. Sin red, o si se alcanza el límite de la API, fija `templates.version`.
