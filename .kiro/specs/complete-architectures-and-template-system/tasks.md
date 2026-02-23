# Tasks: Complete Architectures and Template System

## Overview

This task list implements the complete architectures and template system feature for the Clean Architecture Gradle Plugin. The implementation adds Onion architecture support, enhances the template development workflow with local mode and remote branch testing, implements intelligent YAML configuration merging, and restructures documentation for production readiness.

## Task List

### Phase 1: Core Infrastructure and Configuration

- [x] **Task 1: Enhance Domain Models**
  - [x] 1.1 Add ONION_SINGLE to ArchitectureType enum
  - [x] 1.2 Create StructureMetadata model with adapterPaths, namingConventions, layerDependencies
  - [x] 1.3 Enhance TemplateConfig model with mode, branch, localPath, cache fields
  - [x] 1.4 Enhance AdapterMetadata model with testDependencies, applicationPropertiesTemplate, configurationClasses
  - [x] 1.5 Create ValidationResult model with valid, errors, warnings fields
  - [x] 1.6 Create NamingConventions model with suffixes and prefixes maps
  - [x] 1.7 Create LayerDependencies model with allowedDependencies map
  - [x] 1.8 Create ConfigurationClass model with name, packagePath, templatePath
  - [x] 1.9 Add validation methods to StructureMetadata (validate, resolveAdapterPath)
  - [x] 1.10 Add helper methods to AdapterMetadata (hasApplicationProperties, hasConfigurationClasses, hasTestDependencies)

- [x] **Task 2: Implement Configuration Validation**
  - [x] 2.1 Create ConfigurationValidator service
  - [x] 2.2 Implement validateProjectConfig method checking required fields
  - [x] 2.3 Implement validateTemplateConfig method validating localPath and branch
  - [x] 2.4 Implement validateStructureMetadata method checking adapterPaths and dependencies
  - [x] 2.5 Implement validateAdapterMetadata method validating template references
  - [x] 2.6 Add package name validation following Java conventions (lowercase, no special chars)
  - [x] 2.7 Add helpful error messages with examples for each validation failure
  - [x] 2.8 Write unit tests for all validation scenarios including edge cases

- [x] **Task 3: Implement Template Source Resolution**
  - [x] 3.1 Create TemplateSourceResolver service
  - [x] 3.2 Implement resolveSource method checking localPath, auto-detect, remote in order
  - [x] 3.3 Implement isLocalMode method returning true for local sources
  - [x] 3.4 Implement getLocalPath method resolving configured or auto-detected path
  - [x] 3.5 Implement validateSource method checking path exists or repository accessible
  - [x] 3.6 Add auto-detection for ../backend-architecture-design-archetype-generator-templates
  - [x] 3.7 Add logging for template source mode (local vs remote)
  - [x] 3.8 Write unit tests for all resolution scenarios

- [x] **Task 4: Enhance YAML Configuration Adapter**
  - [x] 4.1 Add SnakeYAML dependency to build.gradle
  - [x] 4.2 Implement readYaml method parsing YAML files to Map
  - [x] 4.3 Implement writeYaml method serializing Map to YAML with 2-space indentation
  - [x] 4.4 Implement readTemplateConfiguration method parsing templates section
  - [x] 4.5 Configure SnakeYAML to preserve property order
  - [x] 4.6 Add error handling for YAML syntax errors with line numbers
  - [x] 4.7 Write unit tests for YAML parsing and serialization
  - [x] 4.8 Write property test for YAML round-trip equivalence (Property 6)

- [x] **Task 5: Implement YAML Merger**
  - [x] 5.1 Create YamlMerger service
  - [x] 5.2 Implement merge method combining base and overlay maps
  - [x] 5.3 Implement deepMerge method recursively merging nested maps
  - [x] 5.4 Implement hasConflict method detecting value conflicts for same key
  - [x] 5.5 Add logic to preserve existing values (no overwrites)
  - [x] 5.6 Add logic to maintain YAML structure and grouping
  - [x] 5.7 Return MergeResult with merged map, conflicts list, addedKeys list
  - [x] 5.8 Write unit tests for various merge scenarios
  - [x] 5.9 Write property test for merge preserves existing values (Property 5)
  - [x] 5.10 Write property test for structure preservation (Property 7)

### Phase 2: Path Resolution and Structure Metadata

- [x] **Task 6: Implement Structure Metadata Loader**
  - [x] 6.1 Create StructureMetadataLoader service
  - [x] 6.2 Implement loadStructureMetadata method reading structure.yml for architecture
  - [x] 6.3 Parse adapterPaths section into Map<String, String>
  - [x] 6.4 Parse namingConventions section into NamingConventions model
  - [x] 6.5 Parse layerDependencies section into LayerDependencies model
  - [x] 6.6 Validate required fields exist in structure.yml
  - [x] 6.7 Add error handling for missing or malformed structure.yml
  - [x] 6.8 Write unit tests for loading various structure.yml files

- [x] **Task 7: Implement Path Resolver**
  - [x] 7.1 Create PathResolverImpl implementing PathResolver interface
  - [x] 7.2 Implement resolveAdapterPath using architecture's adapterPaths configuration
  - [x] 7.3 Implement placeholder substitution for {type}, {name}, {module}, {basePackage}
  - [x] 7.4 Implement resolveComponentPath for non-adapter components
  - [x] 7.5 Implement validatePath checking layer dependencies
  - [x] 7.6 Add support for both single-module and multi-module path resolution
  - [x] 7.7 Write unit tests for path resolution across different architectures
  - [x] 7.8 Write property test for placeholder substitution (Property 2)
  - [x] 7.9 Write property test for path resolution consistency (Property 13)

- [x] **Task 8: Implement Template Validator**
  - [x] 8.1 Create TemplateValidator service
  - [x] 8.2 Implement validateArchitectureTemplates checking structure.yml and required files
  - [x] 8.3 Implement validateAdapterTemplates checking metadata.yml and template files
  - [x] 8.4 Implement validateTemplateVariables checking for undefined variables
  - [x] 8.5 Implement extractRequiredVariables parsing FreeMarker templates
  - [x] 8.6 Add FreeMarker syntax validation
  - [x] 8.7 Return ValidationResult with detailed errors and warnings
  - [x] 8.8 Write unit tests for template validation scenarios

### Phase 3: Template Repository Enhancement

- [x] **Task 9: Enhance FreemarkerTemplateRepository**
  - [x] 9.1 Inject TemplateSourceResolver into FreemarkerTemplateRepository
  - [x] 9.2 Update template loading to use TemplateSourceResolver
  - [x] 9.3 Disable template caching when in local mode
  - [x] 9.4 Implement loadStructureMetadata using StructureMetadataLoader
  - [x] 9.5 Implement loadAdapterMetadata parsing metadata.yml
  - [x] 9.6 Implement validateTemplate using TemplateValidator
  - [x] 9.7 Add error handling for missing template files
  - [x] 9.8 Write unit tests for template loading in local and remote modes

- [x] **Task 10: Enhance GitHub Template Downloader**
  - [x] 10.1 Add branch parameter to downloadTemplate method
  - [x] 10.2 Implement downloadBranch method downloading entire branch
  - [x] 10.3 Implement validateRemoteRepository checking branch exists
  - [x] 10.4 Update cache key to include branch name
  - [x] 10.5 Add error handling for network failures with retry logic
  - [x] 10.6 Add error handling for invalid branch names
  - [x] 10.7 Write unit tests for branch downloading and validation

### Phase 4: Onion Architecture Implementation

- [x] **Task 11: Create Onion Architecture Templates**
  - [x] 11.1 Create structure.yml for onion-single architecture
  - [x] 11.2 Define adapterPaths: driven -> infrastructure/adapter/out/{name}, driving -> infrastructure/adapter/in/{name}
  - [x] 11.3 Define namingConventions with appropriate suffixes
  - [x] 11.4 Define layerDependencies: domain -> [], application -> [domain], infrastructure -> [domain, application]
  - [x] 11.5 Define folder structure: core/domain, core/application/service, core/application/port, infrastructure/adapter/in, infrastructure/adapter/out
  - [x] 11.6 Create README.md.ftl template explaining Onion architecture principles
  - [x] 11.7 Include ASCII diagram showing Onion architecture layers
  - [x] 11.8 Create build.gradle.ftl for Onion single-module project
  - [x] 11.9 Create settings.gradle.ftl for Onion project

- [x] **Task 12: Implement Onion Structure Resolver**
  - [x] 12.1 Create OnionStructureResolver implementing architecture-specific logic
  - [x] 12.2 Implement adapter path mapping to infrastructure/adapter/in or out
  - [x] 12.3 Implement naming convention application
  - [x] 12.4 Implement layer dependency validation
  - [x] 12.5 Write unit tests for Onion path resolution
  - [x] 12.6 Write property test for adapter placement (Property 1)

- [x] **Task 13: Update InitCleanArchTask for Onion**
  - [x] 13.1 Add onion-single to architecture selection options
  - [x] 13.2 Load structure metadata for selected architecture
  - [x] 13.3 Generate folder structure from structure.yml
  - [x] 13.4 Process README.md.ftl with project name parameter
  - [x] 13.5 Create .cleanarch.yml with architecture type
  - [x] 13.6 Write integration test generating Onion project

### Phase 5: Enhanced Adapter Generation

- [x] **Task 14: Enhance GenerateAdapterUseCase**
  - [x] 14.1 Load adapter metadata using TemplateRepository
  - [x] 14.2 Validate adapter metadata using ConfigurationValidator
  - [x] 14.3 Resolve adapter path using PathResolver
  - [x] 14.4 Process main adapter templates
  - [x] 14.5 Process configuration class templates if specified
  - [x] 14.6 Process application properties template if specified
  - [x] 14.7 Merge application properties using YamlMerger
  - [x] 14.8 Add dependencies to build file
  - [x] 14.9 Add test dependencies to build file with test scope
  - [x] 14.10 Return GenerationResult with files, errors, warnings

- [x] **Task 15: Checkpoint - Validate error handling and incremental generation**
  - [x] 15.1 Verify error handling works correctly for all failure scenarios
  - [x] 15.2 Verify incremental generation preserves existing files
  - [x] 15.3 Verify configuration merging doesn't overwrite existing values
  - [x] 15.4 Run all unit tests and verify they pass
  - [x] 15.5 Run integration tests for adapter generation

- [x] **Task 16: Implement Backup Service**
  - [x] 16.1 Create BackupService for file backup and restore
  - [x] 16.2 Implement createBackup method copying files to backup directory
  - [x] 16.3 Implement restoreBackup method restoring files from backup
  - [x] 16.4 Implement deleteBackup method cleaning up successful backups
  - [x] 16.5 Use timestamp-based backup directories
  - [x] 16.6 Add error handling for backup failures
  - [x] 16.7 Write unit tests for backup and restore operations
  - [x] 16.8 Write property test for rollback on failure (Property 22)

- [x] **Task 17: Implement Atomic Configuration Updates**
  - [x] 17.1 Wrap configuration updates in transaction-like logic
  - [x] 17.2 Create backup before any modifications
  - [x] 17.3 Apply all modifications
  - [x] 17.4 On success, delete backup
  - [x] 17.5 On failure, restore from backup
  - [x] 17.6 Log all operations for troubleshooting
  - [x] 17.7 Write unit tests for atomic updates
  - [x] 17.8 Write property test for atomic configuration updates (Property 21)

- [x] **Task 18: Update GenerateOutputAdapterTask**
  - [x] 18.1 Add validation that .cleanarch.yml exists
  - [x] 18.2 Load project configuration
  - [x] 18.3 Call GenerateAdapterUseCase with command
  - [x] 18.4 Display generation result with files created
  - [x] 18.5 Display warnings for conflicts
  - [x] 18.6 Display errors if generation fails
  - [x] 18.7 Write integration test for task execution

- [x] **Task 19: Update GenerateInputAdapterTask**
  - [x] 19.1 Add validation that .cleanarch.yml exists
  - [x] 19.2 Load project configuration
  - [x] 19.3 Call GenerateAdapterUseCase with driving adapter type
  - [x] 19.4 Display generation result
  - [x] 19.5 Write integration test for task execution

### Phase 6: Property-Based Testing

- [x] **Task 20: Set Up Property-Based Testing Infrastructure**
  - [x] 20.1 Add Kotest property testing dependency to build.gradle
  - [x] 20.2 Create Arb generators for domain models (architecture, adapterType, adapterName)
  - [x] 20.3 Create Arb generator for valid package names
  - [x] 20.4 Create Arb generator for YAML maps
  - [x] 20.5 Create Arb generator for template contexts
  - [x] 20.6 Configure property test iterations (minimum 100)
  - [x] 20.7 Set up property test base class with common configuration

- [x] **Task 21: Implement Core Property Tests**
  - [x] 21.1 Implement Property 1: Adapter Placement by Type
  - [x] 21.2 Implement Property 2: Placeholder Substitution in Paths
  - [x] 21.3 Implement Property 3: Architecture-Independent Adapter Generation
  - [x] 21.4 Implement Property 5: YAML Merge Preserves Existing Values
  - [x] 21.5 Implement Property 6: YAML Round-Trip Equivalence
  - [x] 21.6 Implement Property 7: YAML Structure Preservation
  - [x] 21.7 Implement Property 15: Package Name Validation
  - [x] 21.8 Implement Property 16: Package-Folder Alignment
  - [x] 21.9 Implement Property 17: Base Package Consistency
  - [x] 21.10 Verify all property tests pass with 100+ iterations

### Phase 7: Enhanced Adapter Metadata

- [x] **Task 22: Create Enhanced Adapter Examples**
  - [x] 22.1 Create MongoDB adapter with enhanced metadata (applicationPropertiesTemplate, configurationClasses, testDependencies)
  - [x] 22.2 Create REST controller adapter with enhanced metadata
  - [x] 22.3 Create application-properties.yml.ftl for MongoDB with security warning comment
  - [x] 22.4 Create MongoConfig.java.ftl configuration class template
  - [x] 22.5 Add test dependencies (e.g., embedded MongoDB) to metadata.yml
  - [x] 22.6 Test generating MongoDB adapter and verify all files created
  - [x] 22.7 Test generating REST controller adapter and verify all files created
  - [x] 22.8 Verify application.yml merge works correctly

- [x] **Task 23: Implement Configuration Class Generation**
  - [x] 23.1 Parse configurationClasses from adapter metadata
  - [x] 23.2 Resolve package path for each configuration class
  - [x] 23.3 Process configuration class templates
  - [x] 23.4 Write configuration class files to correct package
  - [x] 23.5 Add imports and package declarations
  - [x] 23.6 Write unit tests for configuration class generation
  - [x] 23.7 Write property test for configuration class generation (Property 9)

### Phase 8: Documentation

- [ ] **Task 24: Create Docusaurus Documentation Structure**
  - [x] 24.1 Set up Docusaurus project structure
  - [x] 24.2 Create docs/clean-arch/getting-started/ with installation.md, first-project.md, adding-adapters.md
  - [x] 24.3 Create docs/clean-arch/commands/ with pages for each command
  - [x] 24.4 Create docs/clean-arch/adapters/ with index.md and detail pages
  - [x] 24.5 Create docs/clean-arch/architectures/ with overview.md and pages for each architecture
  - [x] 24.6 Create docs/clean-arch/for-contributors/ with developer-mode.md, adding-adapters.md, adding-architectures.md, testing-templates.md
  - [x] 24.7 Create docs/clean-arch/reference/ with cleanarch-yml.md, metadata-yml.md, structure-yml.md
  - [x] 24.8 Create docs/clean-arch/intro.md explaining clean architecture principles
  - [x] 24.9 Add navigation configuration to docusaurus.config.js

- [ ] **Task 25: Final Integration Testing and Validation**
  - [x] 25.1 Test complete workflow: init Onion project, generate multiple adapters, verify build succeeds
  - [x] 25.2 Test local template mode: modify template locally, generate adapter, verify changes applied
  - [x] 25.3 Test remote branch mode: use feature/init-templates branch, generate project, verify correct templates used
  - [x] 25.4 Test YAML merging: generate multiple adapters with properties, verify application.yml correct
  - [x] 25.5 Test error scenarios: invalid config, missing templates, network failures
  - [x] 25.6 Test rollback: force generation failure, verify files restored
  - [x] 25.7 Run all unit tests and verify 80%+ coverage
  - [x] 25.8 Run all property tests and verify all pass
  - [x] 25.9 Run all integration tests and verify all pass
  - [x] 25.10 Perform manual testing of all major workflows

## Notes

### Implementation Order Rationale

The tasks are ordered to build foundational components first, then layer higher-level features on top:

1. **Phase 1**: Core infrastructure (models, validation, configuration) provides the foundation
2. **Phase 2**: Path resolution and structure metadata enable architecture-agnostic adapter generation
3. **Phase 3**: Template repository enhancements enable local development mode
4. **Phase 4**: Onion architecture implementation validates the architecture-agnostic design
5. **Phase 5**: Enhanced adapter generation brings together all previous components
6. **Phase 6**: Property-based testing validates correctness properties across all components
7. **Phase 7**: Enhanced metadata demonstrates the full power of the new system
8. **Phase 8**: Documentation makes the feature production-ready

### Testing Strategy

- **Unit tests**: Test each component in isolation with mocks
- **Property tests**: Verify universal properties across randomized inputs (minimum 100 iterations)
- **Integration tests**: Test complete workflows end-to-end
- **Manual tests**: Verify user experience and error messages

### Checkpoints

Checkpoints are placed at critical integration points to validate that components work together correctly before proceeding:

- **Task 15**: After implementing enhanced adapter generation, validate error handling and incremental generation work correctly

### Dependencies Between Tasks

- Tasks 1-5 can be implemented in parallel (core infrastructure)
- Tasks 6-8 depend on Task 1 (domain models)
- Tasks 9-10 depend on Tasks 3 and 8 (template source resolution and validation)
- Tasks 11-13 depend on Tasks 6-7 (structure metadata and path resolution)
- Tasks 14-19 depend on all previous tasks (enhanced adapter generation uses all components)
- Task 20-21 can be implemented in parallel with other tasks (property tests)
- Tasks 22-23 depend on Task 14 (enhanced adapter generation)
- Task 24 can be implemented in parallel with other tasks (documentation)
- Task 25 depends on all previous tasks (final integration testing)

### Optional Tasks

Tasks marked with * are optional enhancements that can be implemented later:

- None in this spec (all tasks are required for production readiness)

### Requirement Coverage

Each task references the requirements it implements:

- **Requirement 1** (Onion Architecture): Tasks 11-13
- **Requirement 2** (Local Template Mode): Tasks 3, 9
- **Requirement 3** (Remote Branch Mode): Task 10
- **Requirement 4** (Multi-Architecture Adapter Paths): Tasks 6-7, 12
- **Requirement 5** (YAML Merging): Tasks 4-5, 14
- **Requirement 6** (Enhanced Metadata): Tasks 14, 22-23
- **Requirement 7** (Structure Metadata): Tasks 6-7, 11
- **Requirement 8** (Architecture README): Tasks 11, 13
- **Requirement 9** (Configuration Validation): Tasks 2, 18-19
- **Requirement 10** (Documentation Cleanup): Task 24
- **Requirement 11** (Docusaurus Documentation): Task 24
- **Requirement 12** (Template Validation): Task 8
- **Requirement 13** (Multi-Module Preparation): Task 7
- **Requirement 14** (YAML Parser): Tasks 4-5
- **Requirement 15** (Template Variable Validation): Task 8
- **Requirement 16** (Dependency Conflict Detection): Task 14 (partial, full implementation deferred)
- **Requirement 17** (Package Structure Validation): Task 2
- **Requirement 18** (Incremental Generation): Tasks 14-19
- **Requirement 19** (Template Hot Reload): Task 9
- **Requirement 20** (Error Recovery): Tasks 16-17
- **Requirement 21** (Architecture Documentation): Task 24

### Property Test Coverage

Each correctness property from the design document is tested:

- **Property 1**: Task 12.6 (Adapter Placement by Type)
- **Property 2**: Task 7.8 (Placeholder Substitution)
- **Property 3**: Task 21.3 (Architecture-Independent Generation)
- **Property 5**: Task 5.9 (YAML Merge Preserves Values)
- **Property 6**: Task 4.8 (YAML Round-Trip)
- **Property 7**: Task 5.10 (YAML Structure Preservation)
- **Property 9**: Task 23.7 (Configuration Class Generation)
- **Property 13**: Task 7.9 (Path Resolution Consistency)
- **Property 15**: Task 21.7 (Package Name Validation)
- **Property 16**: Task 21.8 (Package-Folder Alignment)
- **Property 17**: Task 21.9 (Base Package Consistency)
- **Property 21**: Task 17.8 (Atomic Configuration Updates)
- **Property 22**: Task 16.8 (Rollback on Failure)

Properties 4, 8, 10, 11, 12, 14, 18, 19, 20, 23, 24 are validated through unit tests and integration tests rather than property tests.

### Risk Mitigation

- **Risk**: YAML merging corrupts existing configuration
  - **Mitigation**: Extensive unit tests, property tests, backup/restore mechanism
  
- **Risk**: Path resolution breaks for complex architectures
  - **Mitigation**: Property tests across all architectures, comprehensive unit tests
  
- **Risk**: Template validation misses errors
  - **Mitigation**: Multiple validation layers, integration tests with real templates
  
- **Risk**: Rollback fails leaving project in broken state
  - **Mitigation**: Backup service with error handling, manual recovery instructions

### Success Criteria

The feature is complete when:

1. All tasks are marked complete
2. All unit tests pass with 80%+ coverage
3. All property tests pass with 100+ iterations
4. All integration tests pass
5. Documentation is complete and published
6. Manual testing validates user experience
7. Code review is complete and approved

### Estimated Effort

- **Phase 1**: 3-4 days (core infrastructure)
- **Phase 2**: 2-3 days (path resolution)
- **Phase 3**: 2 days (template repository)
- **Phase 4**: 2-3 days (Onion architecture)
- **Phase 5**: 4-5 days (enhanced adapter generation)
- **Phase 6**: 2-3 days (property testing)
- **Phase 7**: 2 days (enhanced metadata)
- **Phase 8**: 3-4 days (documentation)

**Total**: 20-27 days (4-5 weeks)

This estimate assumes one developer working full-time. Actual time may vary based on unforeseen issues and code review iterations.
