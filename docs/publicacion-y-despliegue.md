# Publicación y despliegue del plugin

El plugin y las plantillas se publican por separado y con tags. El plugin se publica en el Gradle Plugin Portal con un tag `vX.Y.Z` del core. Las plantillas las lee del repositorio de templates, en su último tag `v<mayor>.x.y`.

## Qué templates lee el plugin

| Plugin | Lee | Ejemplo |
|---|---|---|
| 1.x publicado | el último tag `v1.x.y` de templates | si existen `v1.0.0`, `v1.2.0` y `v1.2.1`, lee `v1.2.1` |
| 2.x publicado | el último tag `v2.x.y` | |
| Build local sin mayor (`0.x` o sin versión) | la rama `develop` de templates | |
| Con `templates.version: v1.2.0` en `.cleanarch.yml` | ese tag fijo | para congelar un proyecto |
| Con `templates.branch: develop` | esa rama | para probar plantillas sin publicar |

- El plugin consulta los tags en la API de GitHub una vez por build. Guarda el resultado unos minutos, para que un daemon de Gradle vea las versiones nuevas sin reiniciarse.
- Si no puede leer los tags (sin red o por límite de la API), falla con un mensaje que explica cómo fijar `templates.version`.
- Cada tag de templates es inmutable, así que la caché de plantillas por tag nunca queda desactualizada.
- Un cambio en templates llega a todos los usuarios de ese mayor con el siguiente tag, sin publicar plugin.

## Ramas y tags del core

| Rama o tag | Para qué |
|---|---|
| `main` | trunk: todo entra por PR desde `feat/*` o `fix/*`, con `ci-ok` en verde |
| `vX.Y.Z` (tag) | publica la versión X.Y.Z; solo se crea sobre `main` (o `release/X.x`) |
| `release/X.x` | solo si hay que parchear un mayor viejo después de sacar el siguiente |
| `develop` | sobra con este modelo: se puede archivar |

`gradle.properties` guarda la siguiente versión con `-SNAPSHOT`; la versión publicada sale siempre del tag.

## Publicar una versión

1. **Templates primero.** Tiene que existir un tag de templates del mismo mayor (por ejemplo `v1.0.0`) con el `schemaVersion` que lee el plugin. El release lo verifica y se detiene si falta.
2. **Crear el tag** sobre un commit de `main` con el CI en verde:
   ```bash
   git tag v1.4.0 && git push origin v1.4.0
   ```
3. **`release.yml` corre por etapas:**
   - **Quality (CI):** el tag está en `main`, existe el último tag de templates con su `schemaVersion`, y `check` pasa contra ese tag.
   - **Security (CI):** dependencias vulnerables con OSV (falla con CVSS ≥ 7.0) y secretos con gitleaks.
   - **Build:** los jars y el SBOM, con su atestación de procedencia.
   - **Approval:** el job Deploy espera a un revisor del environment `release`.
   - **Deploy (CD):** publica en el Gradle Plugin Portal los mismos jars atestados, sin recompilar. Crea el release de GitHub con los jars y el SBOM y, si `PUBLISH_MAVEN_CENTRAL=true`, publica en Maven Central.
4. **Subir la versión siguiente** en `gradle.properties` (por ejemplo `1.4.1-SNAPSHOT`) con un PR.

## Cambio incompatible de plantillas (nuevo mayor)

1. Subir `SCHEMA_VERSION` en `TemplatesContract` y el mayor del plugin (`2.0.0-SNAPSHOT`).
2. En templates, subir `schemaVersion` y publicar `v2.0.0` (ver `docs/publicacion-y-despliegue.md` de templates).
3. Publicar el plugin `v2.0.0`. Los usuarios de 1.x siguen leyendo `v1.x.y` sin cambios.

## Configuración en GitHub

Está en [ci.md](ci.md): el environment `release` con sus secretos y revisores, los rulesets de `main` y de los tags `v*`, y las opciones de seguridad.

Para Maven Central, la clave GPG se genera como explica [publish/GPG_SETUP_GUIDE.md](publish/GPG_SETUP_GUIDE.md). Va en base64 en el secreto `GPG_PRIVATE_KEY` del environment `release`, no del repositorio.
