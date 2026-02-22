# Requirements Document

## Introduction

This document specifies requirements for completing the Clean Architecture Gradle Plugin's architecture support and template system. The plugin generates clean architecture projects and currently supports Hexagonal architecture with Spring Reactive. This feature adds Onion architecture support, improves the template system for contributors, enhances adapter generation across architectures, implements intelligent configuration merging, and restructures documentation for production readiness.

## Glossary

- **Plugin**: The Gradle plugin that generates clean architecture projects
- **Architecture**: A structural pattern for organizing code (e.g., Hexagonal, Onion)
- **Template_System**: The FreeMarker-based system that generates project files from templates
- **Adapter**: A component that connects the application core to external systems (databases, APIs, etc.)
- **Template_Repository**: The Git repository containing architecture and adapter templates
- **Local_Mode**: Development mode where templates are loaded from local filesystem
- **Remote_Mode**: Production mode where templates are downloaded from Git repository
- **Configuration_File**: The .cleanarch.yml file containing project configuration
- **Metadata_File**: The metadata.yml file describing an adapter's properties and dependencies
- **Structure_File**: The structure.yml file defining an architecture's folder organization
- **Application_Properties**: The application.yml file containing Spring Boot configuration
- **Layer**: A logical grouping of components in clean architecture (domain, application, infrastructure)
- **Driven_Adapter**: An outbound adapter that the application uses (database, external API)
- **Driving_Adapter**: An inbound adapter that uses the application (REST controller, message consumer)

## Requirements

### Requirement 1: Onion Architecture Single Module Support

**User Story:** As a developer, I want to generate projects using Onion architecture, so that I can use an alternative clean architecture pattern for my applications.

#### Acceptance Criteria

1. WHEN the user selects Onion architecture, THE Plugin SHALL generate a single-module project with the structure: core/domain, core/application/service, core/application/port, infrastructure/adapter/in, infrastructure/adapter/out
2. THE Plugin SHALL place domain entities and value objects in core/domain
3. THE Plugin SHALL place use cases and application services in core/application/service
4. THE Plugin SHALL place port interfaces in core/application/port
5. THE Plugin SHALL place driving adapters in infrastructure/adapter/in
6. THE Plugin SHALL place driven adapters in infrastructure/adapter/out
7. THE Plugin SHALL generate a README.md file explaining Onion architecture principles with the project name
8. WHEN generating adapters for Onion architecture, THE Plugin SHALL place them in the correct infrastructure/adapter subdirectory based on adapter type
9. THE Onion_Architecture SHALL enforce dependency rules where core/domain depends on nothing, core/application depends only on core/domain, and infrastructure depends on core layers
10. FOR ALL valid adapter types, generating an adapter in Onion architecture SHALL produce functionally equivalent output to Hexagonal architecture with different folder placement

### Requirement 2: Local Template Development Mode

**User Story:** As a contributor, I want to develop templates locally without pushing to Git, so that I can iterate quickly during template development.

#### Acceptance Criteria

1. WHERE localPath is configured in .cleanarch.yml, THE Template_System SHALL load templates from the specified local filesystem path
2. WHEN localPath is specified, THE Template_System SHALL validate the path exists before attempting to load templates
3. IF localPath does not exist, THEN THE Plugin SHALL display an error message indicating the path was not found and suggest checking the configuration
4. THE Template_System SHALL auto-detect the path ../backend-architecture-design-archetype-generator-templates when no localPath is configured and that path exists
5. WHEN templates are loaded from localPath, THE Plugin SHALL log a message indicating local mode is active
6. THE Configuration_File SHALL support the localPath property under a templates section
7. THE Plugin SHALL NOT accept localPath as a command-line parameter, only from .cleanarch.yml
8. FOR ALL template operations in local mode, the behavior SHALL be identical to remote mode except for the template source location

### Requirement 3: Remote Branch Template Mode

**User Story:** As a contributor, I want to test templates from a feature branch, so that I can validate changes before merging to main.

#### Acceptance Criteria

1. WHERE branch is configured in .cleanarch.yml, THE Template_System SHALL download templates from the specified Git branch
2. WHEN branch is not specified, THE Template_System SHALL default to the main branch
3. THE Template_System SHALL validate the specified branch exists in the remote repository
4. IF the specified branch does not exist, THEN THE Plugin SHALL display an error message with the branch name and repository URL
5. WHEN downloading from a remote branch, THE Plugin SHALL cache templates locally to avoid repeated downloads
6. THE Plugin SHALL support the feature/init-templates branch for testing purposes
7. FOR ALL template operations in remote mode, templates SHALL be downloaded before generation begins

### Requirement 4: Multi-Architecture Adapter Path Resolution

**User Story:** As a plugin user, I want adapters to work across all supported architectures, so that I can generate the same adapter types regardless of architecture choice.

#### Acceptance Criteria

1. THE Structure_File SHALL define an adapterPaths section specifying where to place each adapter type
2. WHEN generating a driven adapter, THE Plugin SHALL resolve the output path using adapterPaths.driven from the architecture's structure.yml
3. WHEN generating a driving adapter, THE Plugin SHALL resolve the output path using adapterPaths.driving from the architecture's structure.yml
4. THE adapterPaths SHALL support placeholders like {type} and {name} for dynamic path construction
5. FOR ALL supported architectures, each adapter type SHALL be placeable using the adapterPaths configuration
6. THE Plugin SHALL validate that adapterPaths exists in structure.yml before generating adapters
7. IF adapterPaths is missing, THEN THE Plugin SHALL display an error indicating the architecture configuration is incomplete
8. FOR ALL adapter generation operations, the same adapter metadata SHALL produce correct output in different architectures with only path differences

### Requirement 5: Intelligent Application Properties Merge

**User Story:** As a developer, I want adapter-specific configuration merged into my existing application.yml, so that I don't have to manually copy properties for each adapter.

#### Acceptance Criteria

1. WHEN generating an adapter with application-properties.yml.ftl, THE Plugin SHALL merge the properties into the existing application.yml file
2. THE Plugin SHALL use SnakeYAML library for parsing and merging YAML files
3. WHEN merging properties, THE Plugin SHALL preserve existing property values and only add new properties
4. IF a property key already exists with a different value, THEN THE Plugin SHALL keep the existing value and log a warning
5. THE Plugin SHALL maintain YAML structure and indentation when merging
6. THE Plugin SHALL add a comment warning about not storing secrets in production above sensitive property sections
7. WHEN no application.yml exists, THE Plugin SHALL create a new file with the adapter properties
8. THE Plugin SHALL organize merged properties by keeping adapter-specific properties grouped together
9. FOR ALL adapters with application-properties.yml.ftl, merging then parsing the result SHALL produce valid YAML with all expected properties present

### Requirement 6: Enhanced Adapter Metadata System

**User Story:** As a contributor, I want to specify additional adapter metadata, so that adapters can include configuration classes, test dependencies, and property templates.

#### Acceptance Criteria

1. THE Metadata_File SHALL support an applicationProperties field specifying the path to a properties template file
2. THE Metadata_File SHALL support a configurationClasses array listing additional Spring configuration classes to generate
3. THE Metadata_File SHALL support a testDependencies array listing dependencies needed only for testing
4. WHEN applicationProperties is specified, THE Plugin SHALL process the template and merge it into application.yml
5. WHEN configurationClasses are specified, THE Plugin SHALL generate the configuration class files in the appropriate package
6. WHEN testDependencies are specified, THE Plugin SHALL add them to the build file with test scope
7. THE Plugin SHALL validate that referenced template files exist before processing
8. IF a referenced template file is missing, THEN THE Plugin SHALL display an error with the file path and adapter name

### Requirement 7: Architecture Structure Metadata Enhancement

**User Story:** As a contributor, I want to define architecture-specific conventions in structure.yml, so that generated code follows consistent patterns for each architecture.

#### Acceptance Criteria

1. THE Structure_File SHALL support an adapterPaths section defining where adapters are placed
2. THE Structure_File SHALL support a namingConventions section defining suffixes and prefixes for components
3. THE Structure_File SHALL support a layerDependencies section defining which layers can depend on which other layers
4. WHEN namingConventions are defined, THE Plugin SHALL apply them when generating component names
5. WHEN layerDependencies are defined, THE Plugin SHALL validate generated code respects dependency rules
6. THE Plugin SHALL use adapterPaths to resolve adapter placement for all adapter generation commands
7. THE Structure_File SHALL be validated for required fields when an architecture is selected
8. IF required structure metadata is missing, THEN THE Plugin SHALL display an error listing the missing fields

### Requirement 8: Architecture-Specific README Generation

**User Story:** As a developer, I want a README explaining my chosen architecture, so that team members understand the project structure and principles.

#### Acceptance Criteria

1. WHEN generating a project, THE Plugin SHALL create a README.md file from the architecture's README.md.ftl template
2. THE README SHALL include the project name as a parameter
3. THE README SHALL explain the chosen architecture's principles and layer responsibilities
4. THE README SHALL include ASCII diagrams showing the architecture structure
5. THE README SHALL provide examples of data flow through the layers
6. THE README SHALL document where to add each component type (entities, use cases, adapters)
7. THE README SHALL include links to online documentation for the architecture pattern
8. FOR ALL supported architectures, the generated README SHALL accurately describe that architecture's structure and principles

### Requirement 9: Configuration File Validation

**User Story:** As a plugin user, I want clear errors when .cleanarch.yml is missing, so that I understand what configuration is required.

#### Acceptance Criteria

1. WHEN executing any command except initCleanArch, THE Plugin SHALL validate that .cleanarch.yml exists in the current directory
2. IF .cleanarch.yml does not exist, THEN THE Plugin SHALL display an error message explaining the file is required
3. THE error message SHALL include a link to documentation showing how to create .cleanarch.yml
4. THE Plugin SHALL validate .cleanarch.yml contains required fields before executing commands
5. IF required fields are missing, THEN THE Plugin SHALL display an error listing the missing fields with examples
6. THE Plugin SHALL provide helpful error messages for malformed YAML syntax
7. THE initCleanArch command SHALL NOT require .cleanarch.yml to exist since it creates the initial project structure

### Requirement 10: Core Repository Documentation Cleanup

**User Story:** As a developer, I want consolidated documentation in the core repository, so that I can find information easily without navigating through many scattered files.

#### Acceptance Criteria

1. THE Plugin repository SHALL contain only README.md, CONTRIBUTING.md, and a docs/ folder for markdown documentation
2. THE Plugin SHALL consolidate valuable content from existing markdown files into the docs/ folder
3. THE Plugin SHALL delete or archive outdated and duplicate markdown files
4. THE README.md SHALL provide a basic overview and link to comprehensive documentation
5. THE CONTRIBUTING.md SHALL guide contributors on how to add adapters and architectures
6. THE docs/ folder SHALL organize content by topic with clear navigation
7. WHEN documentation cleanup is complete, THE repository SHALL contain no more than 5 markdown files in the root directory

### Requirement 11: Docusaurus Documentation Structure

**User Story:** As a plugin user, I want comprehensive online documentation, so that I can learn how to use all plugin features effectively.

#### Acceptance Criteria

1. THE Documentation_Site SHALL have the structure: docs/clean-arch/intro.md, getting-started/, commands/, adapters/, for-contributors/, reference/
2. THE commands/ section SHALL contain one page per command with usage examples and parameter descriptions
3. THE adapters/ section SHALL have an index page listing all available adapters and detail pages for each adapter
4. THE for-contributors/ section SHALL include developer-mode.md, adding-adapters.md with metadata.yml guide, and adding-architectures.md with structure.yml guide
5. THE reference/cleanarch-yml.md page SHALL provide a complete example showing all configuration options with descriptions
6. THE getting-started/ section SHALL provide step-by-step tutorials for common use cases
7. THE intro.md page SHALL explain clean architecture principles and when to use each architecture pattern
8. WHEN a new feature is implemented, THE corresponding documentation SHALL be updated before the feature is considered complete
9. THE Documentation_Site SHALL be written in Spanish for user-facing content and English for contributor content

### Requirement 12: Template Repository Validation

**User Story:** As a contributor, I want to validate my template changes work correctly, so that I can ensure quality before submitting pull requests.

#### Acceptance Criteria

1. WHEN using remote branch mode with feature/init-templates, THE Plugin SHALL download templates from that branch successfully
2. THE Plugin SHALL validate downloaded templates have the required structure before using them
3. IF template structure is invalid, THEN THE Plugin SHALL display an error describing what is missing or incorrect
4. THE Plugin SHALL verify that all referenced template files in metadata.yml exist in the downloaded templates
5. THE Plugin SHALL provide a validation command that checks template structure without generating a project
6. WHEN validation succeeds, THE Plugin SHALL display a success message listing validated architectures and adapters
7. THE Plugin SHALL document the validation process in for-contributors/testing-templates.md

### Requirement 13: Multi-Module Architecture Support Preparation

**User Story:** As a developer, I want the plugin architecture to support multi-module projects, so that future architectures can use multiple Gradle modules.

#### Acceptance Criteria

1. THE Structure_File SHALL support defining multiple modules with separate folder structures
2. THE adapterPaths configuration SHALL support module-specific paths using {module} placeholder
3. THE Plugin SHALL resolve paths correctly for both single-module and multi-module architectures
4. WHEN generating adapters in multi-module architectures, THE Plugin SHALL place files in the correct module
5. THE Plugin architecture SHALL separate path resolution logic from generation logic to support future multi-module architectures
6. FOR ALL path resolution operations, the same logic SHALL work for single-module and multi-module structures

### Requirement 14: Configuration Properties Parser and Printer

**User Story:** As a developer, I want reliable YAML configuration handling, so that my application.yml files are never corrupted during adapter generation.

#### Acceptance Criteria

1. THE Plugin SHALL use SnakeYAML library for parsing application.yml files
2. THE Plugin SHALL preserve comments in application.yml when merging properties
3. THE Plugin SHALL maintain property order when merging new properties
4. THE Plugin SHALL format output YAML with consistent indentation (2 spaces)
5. WHEN parsing application.yml, THE Plugin SHALL validate YAML syntax and report errors with line numbers
6. THE Plugin SHALL support all YAML features used in Spring Boot configuration files (anchors, references, multi-line strings)
7. FOR ALL valid application.yml files, parsing then printing then parsing SHALL produce an equivalent configuration object (round-trip property)
8. IF YAML parsing fails, THEN THE Plugin SHALL display the error message from SnakeYAML with the file path and line number

### Requirement 15: Template Variable Validation

**User Story:** As a contributor, I want validation of template variables, so that I catch errors before templates are used in production.

#### Acceptance Criteria

1. THE Plugin SHALL validate that all variables used in FreeMarker templates are defined in the template context
2. WHEN processing a template with undefined variables, THE Plugin SHALL display an error listing the undefined variables
3. THE Plugin SHALL provide a list of available variables for each template type in contributor documentation
4. THE Plugin SHALL validate template syntax before processing
5. IF template syntax is invalid, THEN THE Plugin SHALL display the FreeMarker error with template name and line number
6. THE validation command SHALL check all templates in an architecture or adapter for undefined variables

### Requirement 16: Adapter Dependency Conflict Detection

**User Story:** As a developer, I want to be warned about dependency conflicts, so that I can resolve them before they cause build failures.

#### Acceptance Criteria

1. WHEN adding an adapter with dependencies, THE Plugin SHALL check for version conflicts with existing dependencies
2. IF a dependency version conflict is detected, THEN THE Plugin SHALL display a warning with the conflicting versions
3. THE Plugin SHALL suggest using dependency management to resolve conflicts
4. THE Plugin SHALL detect conflicts between adapter dependencies and framework dependencies
5. WHEN multiple adapters are generated, THE Plugin SHALL check for conflicts between adapter dependencies
6. THE Plugin SHALL allow users to override dependency versions in .cleanarch.yml

### Requirement 17: Generated Code Package Structure Validation

**User Story:** As a developer, I want generated code to follow Java package conventions, so that my project structure is clean and maintainable.

#### Acceptance Criteria

1. THE Plugin SHALL validate that package names follow Java naming conventions (lowercase, no special characters except dots)
2. THE Plugin SHALL ensure generated classes are placed in packages matching their folder structure
3. THE Plugin SHALL validate that import statements in generated code reference existing classes
4. WHEN generating code, THE Plugin SHALL use the base package from .cleanarch.yml consistently
5. THE Plugin SHALL prevent generating classes with duplicate fully-qualified names
6. IF package structure validation fails, THEN THE Plugin SHALL display an error with the invalid package name and reason

### Requirement 18: Incremental Adapter Generation

**User Story:** As a developer, I want to add adapters to existing projects, so that I can extend functionality without regenerating the entire project.

#### Acceptance Criteria

1. WHEN generating an adapter in an existing project, THE Plugin SHALL preserve all existing code
2. THE Plugin SHALL merge new dependencies into the existing build file without removing existing dependencies
3. THE Plugin SHALL merge adapter properties into existing application.yml without overwriting existing properties
4. THE Plugin SHALL detect if an adapter with the same name already exists and prompt for confirmation before overwriting
5. WHEN adding multiple adapters, THE Plugin SHALL handle each adapter independently
6. THE Plugin SHALL update configuration files (build.gradle, application.yml) atomically to prevent partial updates on failure
7. FOR ALL adapter generation operations, existing project functionality SHALL remain unchanged except for the added adapter

### Requirement 19: Template Hot Reload for Development

**User Story:** As a contributor, I want templates to reload automatically during development, so that I can see changes immediately without restarting.

#### Acceptance Criteria

1. WHERE localPath is configured, THE Plugin SHALL reload templates from disk on each generation command
2. THE Plugin SHALL NOT cache templates when running in local mode
3. WHEN a template file is modified, THE next generation command SHALL use the updated template
4. THE Plugin SHALL log when templates are reloaded in local mode
5. WHERE remote mode is active, THE Plugin SHALL cache templates and only reload when explicitly requested

### Requirement 20: Error Recovery and Rollback

**User Story:** As a developer, I want failed generations to be rolled back, so that my project is not left in a broken state.

#### Acceptance Criteria

1. WHEN a generation command fails, THE Plugin SHALL rollback all file changes made during that command
2. THE Plugin SHALL create a backup of modified files before making changes
3. IF rollback fails, THEN THE Plugin SHALL display instructions for manual recovery with backup file locations
4. THE Plugin SHALL validate all templates and configuration before making any file changes
5. WHEN validation fails, THE Plugin SHALL display all errors before attempting generation
6. THE Plugin SHALL provide a --dry-run option that validates and shows what would be generated without making changes
7. FOR ALL generation operations, either all changes SHALL be applied successfully or no changes SHALL be persisted (atomicity property)

### Requirement 21: Architecture Documentation in Docusaurus

**User Story:** As a developer, I want documentation explaining all available architectures, so that I can choose the right architecture pattern for my project needs.

#### Acceptance Criteria

1. THE Documentation_Site SHALL include pages in docs/clean-arch/ explaining each architecture type (Hexagonal Single Module, Hexagonal Multi Module, Hexagonal Multi Module Granular, Onion Single Module)
2. THE architecture documentation SHALL describe when to use each architecture based on project size, team size, and complexity requirements
3. THE architecture documentation SHALL explain the differences between architectures including structure, complexity, and modularity characteristics
4. THE architecture documentation SHALL include structure diagrams showing the folder organization for each architecture
5. THE architecture documentation SHALL provide examples of component placement (entities, use cases, adapters, ports) for each architecture
6. THE architecture documentation SHALL include a comparison table showing key differences between all architectures
7. THE getting-started guides SHALL link to the architecture documentation pages
8. THE architecture documentation SHALL be organized either in docs/clean-arch/architectures/ folder or within docs/clean-arch/guides/ with clear navigation
9. FOR ALL supported architectures, the documentation SHALL accurately represent the structure defined in that architecture's structure.yml file

## Non-Functional Requirements

### Performance

1. THE Plugin SHALL generate a complete project structure in less than 10 seconds on standard hardware
2. THE Plugin SHALL download and cache remote templates in less than 30 seconds on standard internet connections
3. THE Plugin SHALL validate .cleanarch.yml in less than 1 second
4. WHEN using local mode, THE Plugin SHALL load templates in less than 2 seconds

### Usability

1. THE Plugin SHALL provide clear, actionable error messages for all failure scenarios
2. THE Plugin SHALL use consistent terminology across all commands and documentation
3. THE Plugin SHALL provide progress indicators for long-running operations (template download, project generation)
4. THE Plugin SHALL support both Spanish and English error messages based on system locale

### Maintainability

1. THE Plugin SHALL separate template processing logic from architecture-specific logic
2. THE Plugin SHALL use dependency injection for all major components
3. THE Plugin SHALL include unit tests achieving at least 80% code coverage
4. THE Plugin SHALL document all public APIs with KDoc comments
5. THE Plugin SHALL follow Kotlin coding conventions and pass ktlint checks

### Extensibility

1. THE Plugin SHALL support adding new architectures without modifying core plugin code
2. THE Plugin SHALL support adding new adapters without modifying core plugin code
3. THE Plugin SHALL provide clear interfaces for template processing that can be extended
4. THE Plugin SHALL document the process for adding new architectures and adapters in contributor guides

### Reliability

1. THE Plugin SHALL handle network failures gracefully when downloading remote templates
2. THE Plugin SHALL validate all user input before processing
3. THE Plugin SHALL never corrupt existing project files during adapter generation
4. THE Plugin SHALL provide detailed logs for troubleshooting in verbose mode
