# CI y publicación del plugin

Los pipelines van por etapas: **Quality (CI) → Security (CI) → Build → Deploy (CD)**. Cada job lleva la etapa en su nombre, así que el grafo de GitHub Actions las muestra en columnas.

## Workflows

| Workflow | Cuándo corre | Etapas |
|---|---|---|
| `ci.yml` | PR a `main` o `release/**`, push a `main` | Quality: tests con cobertura ≥ 60 % (y Sonar si hay `SONAR_TOKEN`), código muerto (PMD). Security: gitleaks, dependency review, dependencias vulnerables (OSV, árbol completo), CodeQL, actionlint y zizmor. Build: genera y compila 2 proyectos con el plugin del PR |
| `release.yml` | push de un tag `vX.Y.Z` | Quality: tag sobre `main`, último tag de templates `vX.y.z` con su `schemaVersion`, `check`. Security: OSV (con SBOM) y gitleaks. Build: jars y su atestación de procedencia. Deploy: espera aprobación en el environment `release` y publica esos mismos jars |
| `security-weekly.yml` | cada lunes a las 07:00 UTC | CodeQL, OSV y gitleaks sobre `main`. CodeQL y OSV publican en la pestaña Security; si algo falla, abre o actualiza un issue `security-weekly` y una corrida limpia lo cierra |
| `codeql.yml` | lo llaman `ci.yml` y `security-weekly.yml` | SAST con `security-and-quality`; las alertas van a la pestaña Security |

El job `ci-ok` resume el CI: es el único check que hay que marcar como obligatorio.

Los proyectos generados se escanean en el repo de templates (`security-weekly.yml` de templates, también los lunes).

## Escaneo de dependencias

`scripts/ci/scan-dependencies.sh` vive en templates y lo usan los dos repos:
1. Gradle bloquea todas las configuraciones (`lock-dependencies.init.gradle`), así que se escanean también las transitivas.
2. OSV-Scanner las cruza con OSV, que incluye los avisos de GitHub y de Maven.
3. Falla si alguna vulnerabilidad llega a CVSS 7.0 (`FAIL_ON_CVSS`). Las menores salen en el resumen del job, sin bloquear.
4. Publica todos los hallazgos en la pestaña Security (code scanning, categoría `osv-dependencies`), en cada PR y cada lunes.
5. Deja un SBOM CycloneDX; el release lo adjunta y lo incluye en la atestación.

Las herramientas (OSV-Scanner, gitleaks, actionlint) se descargan con versión fija y se verifican contra su checksum.

## Ramas

- `main` es trunk: todo entra por PR desde `feat/*` o `fix/*`.
- Una versión se publica con un tag `vX.Y.Z` sobre `main`. La versión sale del tag; `gradle.properties` guarda el siguiente `-SNAPSHOT`.
- `release/X.x` solo existe cuando hay que parchear un mayor viejo después de sacar el siguiente.

## Publicar una versión

1. Ya existe un tag de templates `vX.y.z` con el `schemaVersion` que lee el plugin. El release lo verifica y se detiene si no es así, porque un plugin que no encuentra sus templates falla para todos los usuarios. El detalle está en [publicacion-y-despliegue.md](publicacion-y-despliegue.md).
2. `git tag vX.Y.Z && git push origin vX.Y.Z` sobre un commit de `main`.
3. Corren Quality, Security y Build. Build deja los jars atestados como artefacto.
4. Deploy espera la aprobación en el environment `release`. Al aprobar, publica en el Gradle Plugin Portal esos mismos jars (no los recompila), crea el release de GitHub con los jars y el SBOM y, si `PUBLISH_MAVEN_CENTRAL=true`, sube el bundle firmado a Maven Central.

## Configuración en GitHub (una sola vez)

**Environment `release`**
- Revisores obligatorios y "Prevent self-review". La aprobación es el paso Approval del pipeline: el job Deploy queda en "Waiting for review" hasta que alguien aprueba.
- Deployment tags: solo `v*`.
- Secretos: `GRADLE_PUBLISH_KEY` y `GRADLE_PUBLISH_SECRET`. Para Maven Central también `GPG_PRIVATE_KEY` (en base64), `GPG_PASSPHRASE`, `OSSRH_USERNAME` y `OSSRH_PASSWORD` (el token de usuario del Central Portal).
- Los secretos de publicación viven solo aquí, no a nivel de repositorio.

**Secretos y variables del repositorio**
- `SONAR_TOKEN` (opcional; sin él, el paso de Sonar se salta).
- `PUBLISH_MAVEN_CENTRAL=true` para activar la publicación en Maven Central.

**Ruleset de `main`**
- PR obligatorio con 1 aprobación, descartar aprobaciones viejas al haber nuevos commits y resolver conversaciones.
- Check obligatorio: `ci-ok`.
- Sin force push ni borrado.

**Ruleset de tags `v*`**
- Solo los maintainers pueden crearlos; nadie puede moverlos ni borrarlos.

**Seguridad del repositorio**
- Dependabot alerts y Dependabot security updates (activas desde el 2026-11-10).
- Secret scanning con push protection y code scanning.
- Actions: permisos del `GITHUB_TOKEN` en solo lectura y "Require approval for all outside collaborators".

## Herramientas

Las actions están fijadas por SHA con su versión en un comentario. Dependabot propone actualizaciones cada semana, con 14 días de espera desde que sale cada versión.
