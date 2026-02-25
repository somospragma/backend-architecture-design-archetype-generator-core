# Migration Guide: Reactive to Imperative (and vice versa)

This guide helps you understand the differences between reactive and imperative paradigms in Spring and how to migrate between them.

## Key Differences

| Aspect | Reactive | Imperative |
|--------|----------|------------|
| **I/O Model** | Non-blocking, asynchronous | Blocking, synchronous |
| **Return Types** | `Mono<T>`, `Flux<T>` | `T`, `List<T>`, `Optional<T>` |
| **Web Framework** | Spring WebFlux | Spring MVC |
| **Database** | R2DBC, Reactive MongoDB | JPA/Hibernate, MongoDB Driver |
| **HTTP Client** | WebClient | RestTemplate |
| **Threading** | Event loop (few threads) | Thread-per-request |
| **Backpressure** | Built-in | Manual (queues, throttling) |
| **Learning Curve** | Steeper | Gentler |

## When to Use Each

### Use Imperative When:
- Building traditional CRUD applications
- Team is familiar with blocking I/O
- Integrating with blocking libraries (JDBC, legacy systems)
- Simpler debugging and testing requirements
- Lower throughput requirements (< 1000 req/s)

### Use Reactive When:
- Building high-throughput microservices
- Handling streaming data or SSE
- Need efficient resource utilization
- Working with reactive data sources
- Building event-driven architectures

## Code Migration Examples

### 1. Repository Layer

**Reactive (R2DBC):**
```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Flux<User> findByStatus(String status);
}

// Usage
public Mono<User> getUser(Long id) {
    return userRepository.findById(id);
}
```

**Imperative (JPA):**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(String status);
}

// Usage
public Optional<User> getUser(Long id) {
    return userRepository.findById(id);
}
```

### 2. Service Layer

**Reactive:**
```java
@Service
public class UserService {
    private final UserRepository repository;
    
    public Mono<User> createUser(User user) {
        return repository.save(user)
            .doOnSuccess(u -> log.info("User created: {}", u.getId()));
    }
    
    public Flux<User> getAllUsers() {
        return repository.findAll();
    }
    
    public Mono<User> updateUser(Long id, User updates) {
        return repository.findById(id)
            .flatMap(existing -> {
                existing.setName(updates.getName());
                return repository.save(existing);
            })
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }
}
```

**Imperative:**
```java
@Service
public class UserService {
    private final UserRepository repository;
    
    public User createUser(User user) {
        User saved = repository.save(user);
        log.info("User created: {}", saved.getId());
        return saved;
    }
    
    public List<User> getAllUsers() {
        return repository.findAll();
    }
    
    public User updateUser(Long id, User updates) {
        User existing = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        existing.setName(updates.getName());
        return repository.save(existing);
    }
}
```

### 3. REST Controller

**Reactive (WebFlux):**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long id) {
        return service.getUser(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public Flux<User> getAllUsers() {
        return service.getAllUsers();
    }
    
    @PostMapping
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody User user) {
        return service.createUser(user)
            .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created));
    }
}
```

**Imperative (Spring MVC):**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return service.getUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = service.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### 4. HTTP Client

**Reactive (WebClient):**
```java
@Component
public class ExternalApiClient {
    private final WebClient webClient;
    
    public Mono<ApiResponse> callExternalApi(String endpoint) {
        return webClient.get()
            .uri(endpoint)
            .retrieve()
            .bodyToMono(ApiResponse.class)
            .timeout(Duration.ofSeconds(5))
            .retry(3);
    }
}
```

**Imperative (RestTemplate):**
```java
@Component
public class ExternalApiClient {
    private final RestTemplate restTemplate;
    
    public ApiResponse callExternalApi(String endpoint) {
        return restTemplate.getForObject(endpoint, ApiResponse.class);
    }
}
```

### 5. Error Handling

**Reactive:**
```java
public Mono<User> getUser(Long id) {
    return repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .onErrorMap(DataAccessException.class, 
            e -> new DatabaseException("Failed to fetch user", e))
        .doOnError(e -> log.error("Error fetching user {}", id, e));
}
```

**Imperative:**
```java
public User getUser(Long id) {
    try {
        return repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    } catch (DataAccessException e) {
        log.error("Error fetching user {}", id, e);
        throw new DatabaseException("Failed to fetch user", e);
    }
}
```

## Testing Differences

### Reactive Tests

```java
@Test
void shouldFindUserById() {
    // Given
    User user = new User(1L, "John");
    when(repository.findById(1L)).thenReturn(Mono.just(user));
    
    // When & Then
    StepVerifier.create(service.getUser(1L))
        .expectNext(user)
        .verifyComplete();
}

@Test
void shouldHandleUserNotFound() {
    when(repository.findById(1L)).thenReturn(Mono.empty());
    
    StepVerifier.create(service.getUser(1L))
        .expectError(UserNotFoundException.class)
        .verify();
}
```

### Imperative Tests

```java
@Test
void shouldFindUserById() {
    // Given
    User user = new User(1L, "John");
    when(repository.findById(1L)).thenReturn(Optional.of(user));
    
    // When
    User result = service.getUser(1L);
    
    // Then
    assertEquals(user, result);
}

@Test
void shouldHandleUserNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());
    
    assertThrows(UserNotFoundException.class, 
        () -> service.getUser(1L));
}
```

## Dependencies Migration

### Reactive Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation 'org.postgresql:r2dbc-postgresql'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
    
    testImplementation 'io.projectreactor:reactor-test'
}
```

### Imperative Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Configuration Migration

### Reactive (application.yml)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
  
  webflux:
    base-path: /api
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/mydb
```

### Imperative (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
    hikari:
      maximum-pool-size: 10
  
  jpa:
    hibernate:
      ddl-auto: update
  
  mvc:
    servlet:
      path: /api
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/mydb
```

## Common Pitfalls

### 1. Blocking in Reactive Code

**❌ Bad:**
```java
public Mono<User> getUser(Long id) {
    User user = repository.findById(id).block(); // NEVER DO THIS!
    return Mono.just(user);
}
```

**✅ Good:**
```java
public Mono<User> getUser(Long id) {
    return repository.findById(id);
}
```

### 2. Not Subscribing to Reactive Streams

**❌ Bad:**
```java
public void processUsers() {
    repository.findAll()
        .map(this::transform); // Nothing happens - no subscription!
}
```

**✅ Good:**
```java
public Flux<User> processUsers() {
    return repository.findAll()
        .map(this::transform); // Caller will subscribe
}
```

### 3. Mixing Paradigms

Avoid mixing reactive and blocking code in the same application. Choose one paradigm and stick with it throughout your stack.

## Performance Considerations

### Reactive Advantages:
- Better resource utilization (fewer threads)
- Higher throughput under load
- Natural backpressure handling
- Efficient for I/O-bound operations

### Imperative Advantages:
- Simpler debugging (stack traces)
- Easier to understand and maintain
- Better for CPU-bound operations
- Wider ecosystem support

## Migration Strategy

1. **Start with the data layer**: Migrate repositories first
2. **Update service layer**: Change return types and error handling
3. **Modify controllers**: Update endpoints to match new paradigm
4. **Update tests**: Rewrite tests for new paradigm
5. **Update dependencies**: Switch Spring starters
6. **Test thoroughly**: Ensure all functionality works

## Further Reading

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/)
- [Reactive Programming Guide](https://www.reactivemanifesto.org/)
