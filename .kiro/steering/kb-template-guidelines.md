---
inclusion: fileMatch
fileMatchPattern: "**/*.ftl"
---
# KB Template Guidelines — Steering for FreeMarker Templates

When editing or creating FreeMarker templates (.ftl), follow these KB rules:

## Domain Entities (domain/model/)
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- NO framework annotations (`@Entity`, `@Table`, `@Document`)
- NO interfaces in model — interfaces go in `domain/ports/`

## Domain Ports (domain/ports/)
- Interface naming: `I{Concept}Gateway` (prefix `I` + suffix `Gateway`)
- Reactive: return `Mono<T>` / `Flux<T>`
- Imperative: return `T`, `Optional<T>`, `List<T>`
- Flat package — no `in/` or `out/` sub-packages

## Use Cases (domain/usecases/)
- Class naming: `{Action}{Concept}UseCase`
- Use `@RequiredArgsConstructor` from Lombok
- NO `@Service`, `@Component`, or any Spring annotation
- Depend only on `model` and `ports`
- Use cases MUST NOT depend on other use cases (use Domain Services for shared logic)

## DTOs (entry-points/*/dto/)
- MUST be Java Records: `public record CreateAccountRequest(@NotBlank String name) {}`
- Jakarta Validation annotations on record components
- NO `@Data` classes, NO `*DTO` suffix
- Use `*Request` / `*Response` suffixes

## Adapters (driven-adapters/)
- Class naming: `{Concept}{Technology}Adapter` (suffix `*Adapter`)
- Implement `I*Gateway` interface from domain/ports
- Use `@Repository` or `@Component`
- Internal entities: `*Entity` (JPA), `*Document` (MongoDB), `*Item` (DynamoDB)

## Mappers
- MUST be MapStruct interfaces: `@Mapper(componentModel = "spring")`
- Naming: `*EntityMapper` (persistence), `*RestMapper` (REST), `*AdapterMapper` (external)
- NO static methods, NO utility classes

## REST Controllers (Imperative)
- `@RestController` + `@RequestMapping("/api/v1/{resource}")`
- Use `@Valid` for request validation
- Return `ResponseEntity<T>` with appropriate HTTP status
- Include `@Tag`, `@Operation`, `@ApiResponse` for OpenAPI

## REST Entry-Points (Reactive/WebFlux)
- Use `RouterFunction<ServerResponse>` + Handler pattern
- `@RestController` is FORBIDDEN in reactive projects
- Error handling: `AbstractErrorWebExceptionHandler`
- Use `ServerRequest` / `ServerResponse`, NOT `HttpServletRequest`

## Error Handling
- Imperative: `@RestControllerAdvice` with `ProblemDetail` (RFC 7807)
- Reactive: `GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler`
- Include `ErrorType` enum with standard error types

## HTTP Clients
- Imperative: `RestClient` (Spring 6.1+). NOT `RestTemplate`
- Reactive: `WebClient`. NOT `RestTemplate`

## Application Configuration
- `application.yml` must include:
  - `server.shutdown: graceful`
  - `spring.lifecycle.timeout-per-shutdown-phase: 30s`
  - Actuator health probes enabled
  - Secrets as `${PLACEHOLDER}` — never hardcoded
