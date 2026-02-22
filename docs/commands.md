# Commands Reference

This document provides detailed information about all available Gradle tasks provided by the Clean Architecture Generator plugin.

## initCleanArch

Initializes a new clean architecture project with the selected architecture type.

**Usage:**
```bash
./gradlew initCleanArch
```

**Interactive Prompts:**
- Architecture type selection
- Framework selection (Spring/Quarkus)
- Paradigm selection (Imperative/Reactive)
- Project name
- Base package

**Generated Files:**
- Project structure folders
- `build.gradle` or `build.gradle.kts`
- `settings.gradle` or `settings.gradle.kts`
- `.cleanarch.yml` configuration file
- `README.md` with architecture documentation

**Example:**
```bash
$ ./gradlew initCleanArch
Select architecture type:
1. Hexagonal Single Module
2. Hexagonal Multi Module
3. Hexagonal Multi Module Granular
4. Onion Single Module
Enter choice: 1

Select framework:
1. Spring Boot
2. Quarkus
Enter choice: 1

Select paradigm:
1. Imperative
2. Reactive
Enter choice: 2

Enter project name: user-service
Enter base package: com.example.userservice

✓ Project initialized successfully!
```

## generateOutputAdapter

Generates a driven adapter (output adapter) for external dependencies.

**Usage:**
```bash
./gradlew generateOutputAdapter
```

**Interactive Prompts:**
- Adapter type (MongoDB, PostgreSQL, REST Client, etc.)
- Adapter name
- Entity name
- Package name (optional, auto-generated if not provided)

**Generated Files:**
- Adapter implementation class
- Port interface (if not exists)
- Entity/Model classes
- Configuration classes (if needed)
- Test files
- Updated `build.gradle` with dependencies
- Updated `application.yml` with configuration

**Example:**
```bash
$ ./gradlew generateOutputAdapter
Select adapter type:
1. MongoDB
2. PostgreSQL
3. REST Client
4. Redis
Enter choice: 1

Enter adapter name: UserPersistence
Enter entity name: User

✓ Adapter generated successfully!
  - UserPersistenceAdapter.java
  - UserRepository.java
  - MongoConfig.java
  - application.yml updated
  - build.gradle updated
```

## generateInputAdapter

Generates a driving adapter (input adapter) for external interfaces.

**Usage:**
```bash
./gradlew generateInputAdapter
```

**Interactive Prompts:**
- Adapter type (REST Controller, GraphQL, gRPC, etc.)
- Adapter name
- Operations/endpoints

**Generated Files:**
- Controller/Handler class
- Request/Response DTOs
- Mapper classes
- Configuration classes (if needed)
- Test files
- Updated `build.gradle` with dependencies

**Example:**
```bash
$ ./gradlew generateInputAdapter
Select adapter type:
1. REST Controller
2. GraphQL
3. gRPC
Enter choice: 1

Enter controller name: UserController
Enter operations (comma-separated): create,findById,update,delete

✓ Adapter generated successfully!
  - UserController.java
  - UserRequest.java
  - UserResponse.java
  - UserMapper.java
```

## generateUseCase

Generates a use case (application service) with its port interfaces.

**Usage:**
```bash
./gradlew generateUseCase
```

**Interactive Prompts:**
- Use case name
- Input/output types
- Required ports

**Generated Files:**
- Use case implementation class
- Input port interface
- Output port interfaces (if needed)
- Test files

**Example:**
```bash
$ ./gradlew generateUseCase
Enter use case name: CreateUser
Enter input type: CreateUserCommand
Enter output type: User

✓ Use case generated successfully!
  - CreateUserUseCase.java
  - CreateUserPort.java
  - CreateUserUseCaseTest.java
```

## validateTemplates

Validates all templates for syntax errors and missing variables.

**Usage:**
```bash
./gradlew validateTemplates
```

**Options:**
- Validate all templates (default)
- Validate specific architecture
- Validate specific adapter

**Example:**
```bash
$ ./gradlew validateTemplates
Validating templates...
✓ Architecture templates: 4 valid
✓ Adapter templates: 12 valid
✓ All templates validated successfully!
```

## Common Options

All commands support these Gradle options:

- `--info`: Show detailed logging
- `--debug`: Show debug logging
- `--stacktrace`: Show full stack traces on errors
- `--scan`: Generate a build scan

**Example:**
```bash
./gradlew generateOutputAdapter --info
```

## Error Handling

If a command fails, the plugin will:
1. Display clear error messages
2. Provide suggestions for fixing the issue
3. Rollback any partial changes (for generation commands)
4. Show the backup location if rollback fails

## See Also

- [Getting Started Guide](getting-started.md)
- [Configuration Guide](configuration.md)
- [Architecture Guide](architectures.md)
