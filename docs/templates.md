# Template System

## Overview

Templates use FreeMarker and are organized by framework, paradigm, and architecture.

## Template Structure

```
templates/
├── frameworks/
│   └── spring/
│       └── reactive/
│           ├── adapters/
│           │   ├── driven-adapters/
│           │   │   ├── mongodb/
│           │   │   │   ├── Adapter.java.ftl
│           │   │   │   ├── Entity.java.ftl
│           │   │   │   ├── Repository.java.ftl
│           │   │   │   ├── metadata.yml
│           │   │   │   └── application-properties.yml.ftl
│           │   │   ├── redis/
│           │   │   ├── postgresql/
│           │   │   └── generic/
│           │   └── entry-points/
│           │       └── rest/
│           ├── domain/
│           ├── usecase/
│           └── project/
└── architectures/
    ├── hexagonal-single/
    │   ├── structure.yml
    │   └── project/
    │       ├── build.gradle.kts.ftl
    │       ├── Application.java.ftl
    │       └── .gitignore.ftl
    └── onion-single/
```

## Template Loading

### Framework-Aware (New)
```
frameworks/{framework}/{paradigm}/adapters/{type}/{name}/
```
Example: `frameworks/spring/reactive/adapters/driven-adapters/mongodb/`

### Legacy (Fallback)
```
adapters/{name}/
```

### Architecture Templates
```
architectures/{type}/project/
```

## Metadata Format

### metadata.yml
```yaml
name: mongodb
type: driven
description: MongoDB adapter using Spring Data Reactive

dependencies:
  - group: org.springframework.boot
    artifact: spring-boot-starter-data-mongodb-reactive
    version: 3.2.0

testDependencies:
  - group: de.flapdoodle.embed
    artifact: de.flapdoodle.embed.mongo
    version: 4.11.0
    scope: test

applicationPropertiesTemplate: application-properties.yml.ftl

configurationClasses:
  - name: MongoConfig
    packagePath: config
    templatePath: MongoConfig.java.ftl
```

## Template Variables

### Available in All Templates
- `basePackage`: Project base package
- `projectName`: Project name
- `framework`: Framework (spring, quarkus)
- `paradigm`: Paradigm (reactive, imperative)
- `architecture`: Architecture type

### Adapter-Specific
- `adapterName`: Adapter name
- `packageName`: Adapter package
- `entityName`: Entity name
- `adapterType`: Adapter type
- `methods`: List of methods

### Example Template
```java
package ${basePackage}.infrastructure.adapter.out.${adapterName?lower_case};

import ${basePackage}.domain.model.${entityName};
import org.springframework.stereotype.Repository;

/**
 * ${adapterName} adapter for ${projectName}
 * Framework: ${framework}
 */
@Repository
public class ${adapterName}Adapter {
    // Implementation
}
```

## Creating New Templates

1. Create directory structure
2. Add metadata.yml
3. Create .ftl templates
4. Test with real generation
5. Update documentation

## Template Caching

Templates are cached locally in `.cleanarch/cache/` after first download from remote repository.
