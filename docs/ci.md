# CI y publicación del plugin

## Workflows

| Workflow | Cuándo corre | Qué hace |
|---|---|---|
| `ci.yml` | PR a `main` o `release/**`, push a `main` | tests y cobertura, código muerto (PMD), Sonar si hay `SONAR_TOKEN`, dependency review, gitleaks, lint de workflows y una generación de humo con los templates del mayor del plugin |
| `codeql.yml` | PR, push a `main` y cada lunes | análisis estático de seguridad (SAST) |
| `security.yml` | lo llama `ci.yml` | gitleaks sobre todo el historial, actionlint y zizmor |
| `release.yml` | push de un tag `vX.Y.Z` | verifica y, con aprobación, publica |

El job `ci-ok` resume el CI: es el único check que hay que marcar como obligatorio.

## Ramas

- `main` es trunk: todo entra por PR desde `feat/*` o `fix/*`.
- Una versión se publica con un tag `vX.Y.Z` sobre `main`. La versión sale del tag; `gradle.properties` guarda el siguiente `-SNAPSHOT`.
- `release/X.x` solo existe cuando hay que parchear un mayor viejo después de sacar el siguiente.

## Publicar una versión

1. La rama `vX` de templates ya existe con el `schemaVersion` que lee el plugin. `release.yml` lo verifica y se detiene si no es así, porque un plugin que no encuentra sus templates falla para todos los usuarios.
2. `git tag vX.Y.Z && git push origin vX.Y.Z` sobre un commit de `main`.
3. `verify` corre el `check` completo con la versión del tag.
4. `publish` espera la aprobación en el environment `release`. Luego publica en el Gradle Plugin Portal, crea el release de GitHub con los jars y su atestación de procedencia.
5. `maven-central` (opcional) sube el bundle firmado al Central Portal.

## Configuración en GitHub (una sola vez)

**Environment `release`**
- Revisores obligatorios y "Prevent self-review".
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
- Secret scanning con push protection, Dependabot alerts y code scanning.
- Actions: permisos del `GITHUB_TOKEN` en solo lectura y "Require approval for all outside collaborators".

## Herramientas

Las actions están fijadas por SHA con su versión en un comentario. Dependabot propone actualizaciones cada semana, con 14 días de espera desde que sale cada versión. Las herramientas que no son actions (gitleaks, actionlint) se descargan con versión fija y se verifican contra su checksum.
