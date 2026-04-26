---
inclusion: auto
---
# KB Architecture Rules — Steering for Generator Development

When modifying templates or core logic of the archetype generator, these rules from the KB MUST be followed:

## Naming Conventions (NON-NEGOTIABLE)
- All Java interfaces MUST have `I` prefix: `IAccountGateway`, NOT `AccountGateway`
- Domain ports use suffix `*Gateway`: `IAccountGateway`, `ITransactionGateway`
- Use cases use suffix `*UseCase`: `CreateAccountUseCase`
- Adapters use suffix `*Adapter`: `AccountJpaAdapter`
- DTOs MUST be Java Records with suffix `*Request`/`*Response`. NO `@Data` classes, NO `*DTO` suffix
- Framework entities: SQL=`*Entity`, MongoDB=`*Document`, DynamoDB=`*Item`
- Mappers: `*EntityMapper` (persistence), `*RestMapper` (REST), `*AdapterMapper` (external API)
- Driven adapter modules for REST: `{system}-client-api` (NOT `http-client` or `rest-consumer`)

## Hexagonal Multi-Module Structure (Default)
- Minimum 8+ Gradle modules: model, ports, use-case, entry-point, driven-adapter, helpers, app-service
- Note: `{client}-lib-mocks` is NOT the generator's responsibility — it's created by agents generating client-specific microservices
- `domain/ports/` is FLAT — NO `in/` or `out/` sub-packages
- `domain/model/` contains entities only — NO interfaces
- `infrastructure/helpers/` is mandatory shared module
- `{client}-lib-mocks/` is mandatory root-level module
- `application/app-service/` contains ONLY MainApplication + UseCasesConfig

## UseCasesConfig
- Uses `@ComponentScan` with regex `^.+UseCase$`
- Body is ALWAYS empty — NO `@Bean` methods
- Use cases have NO Spring annotations (`@Service`, `@Component`)

## Reactive (WebFlux) Rules
- Entry-points: `RouterFunction<ServerResponse>` + Handler. `@RestController` is FORBIDDEN
- Error handling: `AbstractErrorWebExceptionHandler`, NOT `@RestControllerAdvice`
- Persistence: R2DBC, NOT JPA. HTTP client: `WebClient`, NOT `RestTemplate`
- Server: Netty. Filters: `WebFilter`, NOT `OncePerRequestFilter`
- NO `.block()`, `.blockFirst()`, `.blockLast()` outside tests

## Imperative (Spring MVC) Rules
- Entry-points: `@RestController` + `@GetMapping`/`@PostMapping`
- Error handling: `@RestControllerAdvice` with `ProblemDetail` (RFC 7807)
- HTTP client: `RestClient` (Spring 6.1+), NOT `RestTemplate` (deprecated)
- Persistence: JPA/Hibernate

## MapStruct (MANDATORY)
- ALL mappers MUST be `@Mapper(componentModel = "spring")` interfaces
- NO manual/static mappers. NO utility classes with static mapping methods
- Inject mappers via constructor injection

## Build System
- `gradle.properties`: plugin versions, performance settings, credential placeholders
- `gradle/libs.versions.toml`: library versions and aliases. NEVER hardcode versions in build.gradle
- `main.gradle`: JaCoCo (85%), PIT (20%), SonarQube, OWASP, MapStruct config. NEVER empty
- `lombok.config`: `config.stopBubbling = true`, `lombok.addLombokGeneratedAnnotation = true`

## Quality Tools (ALL mandatory)
- JaCoCo: 85% minimum line coverage
- PIT/pitest: 20% minimum mutation kill
- SonarQube: static analysis with env-var properties
- OWASP Dependency Check: vulnerability scanning (failBuildOnCVSS=11)
- ArchUnit: 10 mandatory rules (domain isolation, naming, layer separation)

## Tech Stack
- Java 21, Spring Boot 4.x, Gradle with version catalog, Jakarta EE 11, Lombok, MapStruct

## Gap Analysis Reference
See #[[file:.kiro/docs/gap-analysis-kb-vs-generator.md]] for the complete gap analysis between KB and generator.
