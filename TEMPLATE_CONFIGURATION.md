# Template Configuration Guide

The plugin uses templates to generate code. Templates are organized in a specific structure that separates architecture definitions from framework-specific implementations.

## Template Structure

```
templates/
├── architectures/           # Architecture-specific templates
│   └── hexagonal-single/
│       ├── structure.yml    # Package structure definition
│       └── project/         # Project initialization files
│           ├── build.gradle.kts.ftl
│           ├── settings.gradle.kts.ftl
│           ├── BeanConfiguration.java.ftl
│           ├── Application.java.ftl
│           ├── application.yml.ftl
│           ├── .gitignore.ftl
│           └── README.md.ftl
│
└── frameworks/              # Framework-specific templates
    └── spring/
        └── reactive/
            ├── metadata.yml
            ├── project/     # Framework project files
            │   ├── Application.java.ftl
            │   └── application.yml.ftl
            ├── domain/      # Domain layer templates
            │   ├── Entity.java.ftl
            │   └── metadata.yml
            ├── usecase/     # Use case templates
            │   ├── InputPort.java.ftl
            │   ├── UseCase.java.ftl
            │   ├── Test.java.ftl
            │   └── metadata.yml
            └── adapters/    # Adapter templates
                ├── driven-adapters/  # Output adapters
                │   ├── index.json
                │   ├── redis/
                │   │   ├── Adapter.java.ftl
                │   │   ├── Config.java.ftl
                │   │   ├── Entity.java.ftl
                │   │   ├── Test.java.ftl
                │   │   └── metadata.yml
                │   └── generic/
                │       ├── Entity.java.ftl
                │       └── Mapper.java.ftl
                └── entry-points/     # Input adapters
                    ├── index.json
                    └── rest/
                        ├── Controller.java.ftl
                        ├── RequestDTO.java.ftl
                        ├── ResponseDTO.java.ftl
                        ├── DTOMapper.java.ftl
                        ├── Test.java.ftl
                        └── metadata.yml
```

## Configuration Priority

1. **`.cleanarch.yml`** configuration file
2. **Embedded templates** (production default)

---

## 1. .cleanarch.yml Configuration

Configure templates in your project's `.cleanarch.yml` file.

### Option A: Local Path (Development)

Use this when developing templates locally:

```yaml
project:
  name: my-service
  basePackage: com.example.myservice
  pluginVersion: 0.1.15-SNAPSHOT
  createdAt: 2026-01-31T10:00:00

architecture:
  type: hexagonal-single
  paradigm: reactive
  framework: spring

templates:
  mode: developer
  localPath: /absolute/path/to/backend-architecture-design-archetype-generator-templates/templates
  cache: false
```

**Important:** 
- Use absolute paths, not relative paths
- The path should point to the `templates/` directory (not the repository root)
- Example: `/Users/david/repos/backend-architecture-design-archetype-generator-templates/templates`

### Option B: Custom GitHub Repository (Development/Testing)

Use this to test templates from a fork or feature branch:

```yaml
templates:
  mode: developer
  repository: https://github.com/myorg/my-templates-fork
  branch: feature/my-changes
  cache: false
```

### Option C: Production (Default)

For production use, you can omit the `templates` section entirely, or explicitly configure it:

```yaml
templates:
  mode: production
  repository: https://github.com/somospragma/backend-architecture-design-archetype-generator-templates
  branch: main
  cache: true
```

---

## 2. Embedded Templates (Production Default)

When the plugin is published and used in production, templates are embedded in the JAR file. No configuration needed.

---

## Template Configuration Fields

| Field | Type | Description | Default |
|-------|------|-------------|---------|
| `mode` | String | `production` or `developer` | `production` |
| `repository` | String | GitHub repository URL | pragma's official repo |
| `branch` | String | Git branch to use | `main` |
| `version` | String | Specific version/tag (overrides branch) | `null` |
| `localPath` | String | Absolute path to `templates/` directory | `null` |
| `cache` | Boolean | Cache downloaded templates | `true` |

---

## How Templates Are Used

### Project Initialization (`initCleanArch`)

1. Reads `architectures/{architecture-type}/structure.yml`
2. Generates project files from `architectures/{architecture-type}/project/*.ftl`
3. Generates framework files from `frameworks/{framework}/{paradigm}/project/*.ftl`
4. Creates directory structure based on `structure.yml`

### Component Generation

- **Entities**: `frameworks/{framework}/{paradigm}/domain/Entity.java.ftl`
- **Use Cases**: `frameworks/{framework}/{paradigm}/usecase/*.ftl`
- **Driven Adapters**: `frameworks/{framework}/{paradigm}/adapters/driven-adapters/{type}/*.ftl`
- **Entry Points**: `frameworks/{framework}/{paradigm}/adapters/entry-points/{type}/*.ftl`

---

## Examples

### Development with Local Templates

Create `.cleanarch.yml` in your project:

```yaml
project:
  name: my-service
  basePackage: com.example.myservice
  pluginVersion: 0.1.15-SNAPSHOT
  createdAt: 2026-01-31T10:00:00

architecture:
  type: hexagonal-single
  paradigm: reactive
  framework: spring

templates:
  mode: developer
  localPath: /Users/david/repos/backend-architecture-design-archetype-generator-templates/templates
  cache: false
```

Then run:

```bash
./gradlew initCleanArch --packageName=com.example.service
```

Output:
```
✓ Using local templates from .cleanarch.yml: /Users/david/repos/.../templates
✓ Project initialized successfully!
```

### Testing a Feature Branch

```yaml
templates:
  mode: developer
  repository: https://github.com/myusername/templates-fork
  branch: feature/new-adapter-type
  cache: false
```

### Production Use

No configuration needed! The plugin uses embedded templates by default.

Or explicitly:

```yaml
templates:
  mode: production
  cache: true
```

---

## Troubleshooting

### Templates not found

If you see "Using embedded templates" but expected local templates:

1. **Check the path exists and points to `templates/` directory:**
   ```bash
   ls /path/to/templates/architectures
   ls /path/to/templates/frameworks
   ```

2. **Verify `.cleanarch.yml` syntax is correct**
3. **Use absolute paths, not relative paths**
4. **Check file permissions**

### Wrong templates being used

Check the Gradle output to see which source is being used:

```
✓ Using local templates from .cleanarch.yml: /path/to/templates
```

or

```
✓ Using embedded templates from plugin JAR (production mode)
```

### Template not found error

If you get an error like "Template not found: frameworks/spring/reactive/domain/Entity.java.ftl":

1. Verify the template file exists in your local templates directory
2. Check the file name matches exactly (case-sensitive)
3. Ensure you're pointing to the `templates/` directory, not the repository root

---

## Best Practices

1. **Development**: Use `.cleanarch.yml` with `mode: developer` and `localPath`
2. **Testing**: Use `.cleanarch.yml` with custom repository and branch
3. **Production**: Don't configure anything, use embedded templates
4. **Version Control**: Add `.cleanarch.yml` to `.gitignore` if it contains local paths
5. **Team Development**: Share a template configuration in documentation, not in version control
6. **Path Verification**: Always verify your `localPath` points to the `templates/` directory that contains `architectures/` and `frameworks/` subdirectories
