# Análisis de Gaps: KB vs Generador de Arquetipos

> Fecha: 2026-04-26
> Comparación entre los lineamientos del KB del Backend Chapter y lo que actualmente genera el plugin.

---

## Resumen Ejecutivo

Se identificaron **32 gaps** organizados en 8 categorías. Los más críticos están en naming conventions, estructura de módulos, quality tools, y paradigma reactivo. El generador tiene una base sólida pero necesita alinearse con las decisiones y límites del KB.

---

## 🔴 GAPS CRÍTICOS (Bloquean compliance)

### GAP-01: Prefijo `I` en interfaces — NO GENERADO
- **KB exige:** Todas las interfaces deben tener prefijo `I` (ej: `IAccountGateway`). NON-NEGOTIABLE.
- **Generador produce:** `${useCaseName}UseCase` (interfaz sin prefijo `I`), `${entityName}Repository` (sin prefijo `I`).
- **Templates afectados:** `InputPort.java.ftl`, todos los port templates.
- **Impacto:** Limit 006 violado. Todo output sería rechazado.

### GAP-02: Sufijo `*Gateway` en ports — NO GENERADO
- **KB exige:** Interfaces de dominio siempre con sufijo `Gateway`: `IAccountGateway`.
- **Generador produce:** `${entityName}Repository` en PostgreSQL adapter, `${useCaseName}UseCase` como port name.
- **Templates afectados:** `InputPort.java.ftl`, driven-adapter templates.
- **Impacto:** Naming conventions violadas.

### GAP-03: DTOs como Java Records — NO GENERADO
- **KB exige:** Todos los DTOs (request/response) deben ser Java Records. Limit 004.
- **Generador produce:** Controllers con `Object` como tipo genérico, sin DTOs tipados. Entity.java.ftl genera clases con getters/setters manuales.
- **Templates afectados:** `Controller.java.ftl` (ambos paradigmas), `Entity.java.ftl`.
- **Impacto:** Limit 004 violado.

### GAP-04: MapStruct obligatorio — PARCIALMENTE GENERADO
- **KB exige:** Todos los mappers deben ser interfaces MapStruct `@Mapper(componentModel = "spring")`. Decision 010, Limit 007.
- **Generador produce:** Dependencia MapStruct incluida en build.gradle.kts, pero los templates de adaptadores NO generan interfaces MapStruct. Los adapters usan `Object` como tipo genérico sin mappers.
- **Templates afectados:** Todos los Adapter.java.ftl.
- **Impacto:** Decision 010 y Limit 007 violados.

### GAP-05: REST reactivo usa `@RestController` — PROHIBIDO por KB
- **KB exige:** En WebFlux, usar `RouterFunction<ServerResponse>` + Handler. `@RestController` PROHIBIDO. Limit 002.
- **Generador produce:** `Controller.java.ftl` reactivo con `@RestController`, `@GetMapping`, `@PostMapping`.
- **Templates afectados:** `frameworks/spring/reactive/adapters/entry-points/rest/Controller.java.ftl`.
- **Impacto:** Limit 002 violado. Incompatible con Netty event-loop model.

### GAP-06: HTTP Client imperativo usa `RestTemplate` — DEPRECADO
- **KB exige:** Usar `RestClient` (Spring 6.1+) para imperativo. `RestTemplate` está deprecado.
- **Generador produce:** `${adapterName}HttpClientAdapter` con `RestTemplate`.
- **Templates afectados:** `frameworks/spring/imperative/adapters/driven-adapters/http-client/Adapter.java.ftl`.
- **Impacto:** Archetype springboot-imperative violado.

### GAP-07: `UseCasesConfig` con regex scan — NO GENERADO
- **KB exige:** `UseCasesConfig` con `@ComponentScan` y regex `^.+UseCase$`, body VACÍO. Decision 028.
- **Generador produce:** `BeanConfiguration.java` con `@ComponentScan` genérico sobre paquetes, no regex scan.
- **Templates afectados:** `BeanConfiguration.java.ftl`.
- **Impacto:** Decision 028 violado.

### GAP-08: `GlobalErrorWebExceptionHandler` para WebFlux — NO GENERADO
- **KB exige:** Todo proyecto WebFlux debe incluir `GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler`. Limit 005.
- **Generador produce:** Ningún error handler para WebFlux.
- **Impacto:** Limit 005 violado.

### GAP-09: Estructura hexagonal-multi-granular no alinea con KB
- **KB exige:** Mínimo 8+ módulos: model, ports, use-case, entry-point, driven-adapter, helpers, app-service, {client}-lib-mocks.
- **Generador produce:** `hexagonal-multi-granular` con model, ports, usecase, app-service. Falta `helpers/` y `{client}-lib-mocks/`.
- **Templates afectados:** `hexagonal-multi-granular/structure.yml`.
- **Impacto:** Hexagonal-layers reference violada.

### ~~GAP-10: Módulo `{client}-lib-mocks` — N/A~~
- **KB exige:** Módulo `{client}-lib-mocks` OBLIGATORIO en todo proyecto. Decision 015.
- **No aplica al generador:** El generador es una herramienta genérica que no conoce el cliente destino. El módulo `{client}-lib-mocks` es responsabilidad del agente que genera microservicios para un cliente específico, no del plugin de arquetipos.

---

## 🟠 GAPS IMPORTANTES (Afectan calidad del output)

### GAP-11: `gradle.properties` — NO GENERADO
- **KB exige:** `gradle.properties` obligatorio con versiones de plugins, performance settings, credenciales placeholder. Decision 022.
- **Generador produce:** Solo `build.gradle.kts`. No genera `gradle.properties`.

### GAP-12: `gradle/libs.versions.toml` — NO GENERADO
- **KB exige:** Version catalog obligatorio. Nunca hardcodear versiones en build.gradle.
- **Generador produce:** Versiones hardcodeadas en `build.gradle.kts.ftl` (ej: `version "3.2.1"`, `version "1.5.5.Final"`).

### GAP-13: `main.gradle` — NO GENERADO
- **KB exige:** `main.gradle` obligatorio con JaCoCo (85%), PIT (20%), SonarQube, OWASP, MapStruct config.
- **Generador produce:** PIT configurado inline en build.gradle.kts pero sin main.gradle separado. JaCoCo no configurado. OWASP no configurado. SonarQube no configurado en output.

### GAP-14: `lombok.config` — NO GENERADO
- **KB exige:** `lombok.config` en raíz con `config.stopBubbling = true` y `lombok.addLombokGeneratedAnnotation = true`.
- **Generador produce:** No genera este archivo.

### GAP-15: Spring Boot 4.x / Spring Framework 7 — NO ALINEADO
- **KB exige:** Spring Boot 4.0.3, Java 21, Jakarta EE 11, JSpecify null-safety, Gradle 9.
- **Generador produce:** Spring Boot 3.2.x en templates, Gradle 8.5.

### GAP-16: Health Check / Actuator — PARCIALMENTE GENERADO
- **KB exige:** `spring-boot-starter-actuator` obligatorio con probes habilitados. Decision 017.
- **Generador produce:** Dependencia actuator en build.gradle.kts pero sin configuración de probes en application.yml.

### GAP-17: Graceful Shutdown — NO GENERADO
- **KB exige:** `server.shutdown: graceful` obligatorio. Decision 018.
- **Generador produce:** No incluye esta configuración en application.yml.

### GAP-18: Structured Logging — NO GENERADO
- **KB exige:** Logging JSON en producción, correlation ID. Decision 019.
- **Generador produce:** Logging básico sin formato JSON ni correlation ID.

### GAP-19: RFC 7807 Error Handling — NO GENERADO
- **KB exige:** `ProblemDetail` + `GlobalExceptionHandler` + `ErrorType` enum para errores consistentes.
- **Generador produce:** Controllers con TODO comments, sin error handling.

### GAP-20: OpenAPI/SpringDoc — NO GENERADO
- **KB exige:** springdoc-openapi obligatorio con anotaciones `@Operation`, `@Schema`. Decision 012.
- **Generador produce:** Dependencia springdoc en metadata.yml de REST imperative, pero sin anotaciones en controllers generados.

---

## 🟡 GAPS MODERADOS (Mejoran alineamiento)

### GAP-21: Módulo `helpers/` — NO GENERADO
- **KB exige:** `infrastructure/helpers/` como módulo Gradle compartido obligatorio.
- **Generador produce:** No genera este módulo en ninguna arquitectura.

### GAP-22: Naming de módulos driven-adapter REST
- **KB exige:** `{system}-client-api` (ej: `transactions-client-api`). No usar `rest-consumer` ni `http-client`.
- **Generador produce:** `http-client` como nombre genérico.

### GAP-23: `domain/ports/` FLAT — NO ALINEADO
- **KB exige:** `domain/ports/` plano, sin sub-paquetes `in/` ni `out/`.
- **Generador produce:** `hexagonal-single` y `hexagonal-multi` usan `port/in` y `port/out`.

### GAP-24: Use cases sin anotaciones Spring
- **KB exige:** Use cases sin `@Service`, `@Component`. Registrados por regex scan.
- **Generador produce:** `UseCase.java.ftl` sin anotaciones Spring (correcto), pero `BeanConfiguration` no usa regex scan.

### GAP-25: Entidades de dominio con Lombok
- **KB exige:** POJOs con `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Generador produce:** `Entity.java.ftl` con getters/setters manuales, sin Lombok.

### GAP-26: ArchUnit rules incompletas
- **KB exige:** 10 reglas obligatorias incluyendo naming (`I*Gateway`, `*UseCase`, `*Adapter`), separación de capas.
- **Generador produce:** `ArchitectureTest.java.ftl` con 5 reglas básicas de capas, sin reglas de naming.

### GAP-27: Adapter suffix inconsistente
- **KB exige:** Adapters siempre con sufijo `*Adapter`.
- **Generador produce:** PostgreSQL imperativo usa suffix `Repository` en metadata.yml.

### GAP-28: `settings.gradle` con flat module names
- **KB exige:** Flat module names con `projectDir` mappings (ej: `include 'model'`, `project(':model').projectDir = file('domain/model')`).
- **Generador produce:** `settings.gradle.kts` básico sin projectDir mappings.

### GAP-29: Dependencias de adaptadores usan versiones hardcodeadas
- **KB exige:** Versiones en `libs.versions.toml`, referenciadas como `libs.<alias>`.
- **Generador produce:** metadata.yml con versiones hardcodeadas (ej: `version: 3.2.0`).

### GAP-30: No genera `Dockerfile` con non-root user correctamente
- **KB exige:** Dockerfile multi-stage con non-root user.
- **Generador produce:** Dockerfile.ftl con non-root user (correcto), pero usa Gradle 8.5 y Spring Boot 3.x.

### GAP-31: No genera `RequestParameterValidator` para WebFlux
- **KB exige:** Clase abstracta en `infrastructure/helpers/` para validación de parámetros.
- **Generador produce:** No genera esta clase.

### GAP-32: Onion architecture usa `adapter.in`/`adapter.out`
- **KB exige:** No usar `adapter.in`, `adapter.out`. Usar `entry-points` y `driven-adapters`.
- **Generador produce:** `onion-single/structure.yml` usa `infrastructure/adapter/in` y `infrastructure/adapter/out`.

---

## Matriz de Priorización

| Prioridad | Gaps | Esfuerzo estimado |
|-----------|------|-------------------|
| P0 (Blocker) | GAP-01, GAP-02, GAP-03, GAP-04, GAP-05, GAP-07, GAP-08, GAP-09 | Alto |
| P1 (Critical) | GAP-06, GAP-11, GAP-12, GAP-13, GAP-14, GAP-15, GAP-19, GAP-23 | Alto |
| P2 (Important) | GAP-16, GAP-17, GAP-18, GAP-20, GAP-21, GAP-22, GAP-25, GAP-26 | Medio |
| P3 (Nice-to-have) | GAP-24, GAP-27, GAP-28, GAP-29, GAP-30, GAP-31, GAP-32 | Bajo-Medio |

---

## Recomendación de Abordaje

1. **Sprint 1 — Naming & Structure:** GAP-01, GAP-02, GAP-03, GAP-23, GAP-25, GAP-27 (templates)
2. **Sprint 2 — Reactive Compliance:** GAP-05, GAP-06, GAP-08, GAP-19 (templates reactivos)
3. **Sprint 3 — Build System:** GAP-11, GAP-12, GAP-13, GAP-14, GAP-15, GAP-29 (gradle config)
4. **Sprint 4 — Modules & Quality:** GAP-04, GAP-07, GAP-09, GAP-10, GAP-21, GAP-26 (estructura)
5. **Sprint 5 — Observability & API:** GAP-16, GAP-17, GAP-18, GAP-20, GAP-31 (cross-cutting)
