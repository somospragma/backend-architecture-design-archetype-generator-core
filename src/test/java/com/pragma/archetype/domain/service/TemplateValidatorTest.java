package com.pragma.archetype.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.project.ArchitectureType;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.model.validation.ValidationResult;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateValidator Tests")
class TemplateValidatorTest {

    @Nested
    @DisplayName("validateArchitectureTemplates Tests")
    class ValidateArchitectureTemplatesTests {

        @Test
        @DisplayName("Should succeed when all architecture templates are valid")
        void shouldSucceedWhenAllArchitectureTemplatesAreValid() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
            StructureMetadata metadata = createValidStructureMetadata();

            when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);
            when(templateRepository.validateTemplate("architectures/hexagonal-single/README.md.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("architectures/hexagonal-single/build.gradle.ftl"))
                    .thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertTrue(result.valid());
            assertTrue(result.errors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when structure metadata is invalid")
        void shouldFailWhenStructureMetadataIsInvalid() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;
            StructureMetadata metadata = mock(StructureMetadata.class);
            when(metadata.validate()).thenReturn(ValidationResult.failure("Invalid structure metadata"));

            when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Structure metadata validation failed")));
        }

        @Test
        @DisplayName("Should warn when README template is missing")
        void shouldWarnWhenReadmeTemplateIsMissing() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.ONION_SINGLE;
            StructureMetadata metadata = createValidStructureMetadata();

            when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);
            when(templateRepository.validateTemplate("architectures/onion-single/README.md.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));
            when(templateRepository.validateTemplate("architectures/onion-single/build.gradle.ftl"))
                    .thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertTrue(result.valid());
            assertTrue(result.warnings().stream()
                    .anyMatch(w -> w.contains("README template not found")));
        }

        @Test
        @DisplayName("Should warn when build template is missing")
        void shouldWarnWhenBuildTemplateIsMissing() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.HEXAGONAL_MULTI;
            StructureMetadata metadata = createValidStructureMetadata();

            when(templateRepository.loadStructureMetadata(architecture)).thenReturn(metadata);
            when(templateRepository.validateTemplate("architectures/hexagonal-multi/README.md.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("architectures/hexagonal-multi/build.gradle.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertTrue(result.valid());
            assertTrue(result.warnings().stream()
                    .anyMatch(w -> w.contains("Build template not found")));
        }

        @Test
        @DisplayName("Should fail when architecture templates are not found")
        void shouldFailWhenArchitectureTemplatesNotFound() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;

            when(templateRepository.loadStructureMetadata(architecture))
                    .thenThrow(new TemplateRepository.TemplateNotFoundException("Architecture not found"));

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Architecture templates not found")));
        }

        @Test
        @DisplayName("Should fail when unexpected error occurs")
        void shouldFailWhenUnexpectedErrorOccurs() throws Exception {
            // Given
            ArchitectureType architecture = ArchitectureType.HEXAGONAL_SINGLE;

            when(templateRepository.loadStructureMetadata(architecture))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            ValidationResult result = validator.validateArchitectureTemplates(architecture);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Failed to validate architecture templates")));
        }
    }

    @Nested
    @DisplayName("validateAdapterTemplates Tests")
    class ValidateAdapterTemplatesTests {

        @Test
        @DisplayName("Should succeed when all adapter templates are valid")
        void shouldSucceedWhenAllAdapterTemplatesAreValid() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createValidAdapterMetadata(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertTrue(result.valid());
            assertTrue(result.errors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when adapter metadata is invalid")
        void shouldFailWhenAdapterMetadataIsInvalid() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = mock(AdapterMetadata.class);
            when(metadata.validate()).thenReturn(ValidationResult.failure("Invalid adapter metadata"));
            when(metadata.hasApplicationProperties()).thenReturn(false);
            when(metadata.hasConfigurationClasses()).thenReturn(false);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate(anyString())).thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Adapter metadata validation failed")));
        }

        @Test
        @DisplayName("Should fail when main adapter template is missing")
        void shouldFailWhenMainAdapterTemplateIsMissing() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createValidAdapterMetadata(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Adapter template not found")));
        }

        @Test
        @DisplayName("Should warn when data entity template is missing")
        void shouldWarnWhenDataEntityTemplateIsMissing() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createValidAdapterMetadata(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.success());

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertTrue(result.valid());
            assertTrue(result.warnings().stream()
                    .anyMatch(w -> w.contains("Data entity template not found")));
        }

        @Test
        @DisplayName("Should warn when mapper template is missing")
        void shouldWarnWhenMapperTemplateIsMissing() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createValidAdapterMetadata(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertTrue(result.valid());
            assertTrue(result.warnings().stream()
                    .anyMatch(w -> w.contains("Mapper template not found")));
        }

        @Test
        @DisplayName("Should fail when application properties template is missing")
        void shouldFailWhenApplicationPropertiesTemplateIsMissing() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createAdapterMetadataWithApplicationProperties(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/application-properties.yml.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Application properties template not found")));
        }

        @Test
        @DisplayName("Should fail when configuration class template is missing")
        void shouldFailWhenConfigurationClassTemplateIsMissing() throws Exception {
            // Given
            String adapterName = "mongodb";
            AdapterMetadata metadata = createAdapterMetadataWithConfigurationClasses(adapterName);

            when(templateRepository.loadAdapterMetadata(adapterName)).thenReturn(metadata);
            when(templateRepository.validateTemplate("adapters/mongodb/Adapter.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/DataEntity.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/Mapper.java.ftl"))
                    .thenReturn(ValidationResult.success());
            when(templateRepository.validateTemplate("adapters/mongodb/MongoConfig.java.ftl"))
                    .thenReturn(ValidationResult.failure("Template not found"));

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Configuration class template not found")));
        }

        @Test
        @DisplayName("Should fail when adapter templates are not found")
        void shouldFailWhenAdapterTemplatesNotFound() throws Exception {
            // Given
            String adapterName = "mongodb";

            when(templateRepository.loadAdapterMetadata(adapterName))
                    .thenThrow(new TemplateRepository.TemplateNotFoundException("Adapter not found"));

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Adapter templates not found")));
        }

        @Test
        @DisplayName("Should fail when unexpected error occurs")
        void shouldFailWhenUnexpectedErrorOccurs() throws Exception {
            // Given
            String adapterName = "mongodb";

            when(templateRepository.loadAdapterMetadata(adapterName))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            ValidationResult result = validator.validateAdapterTemplates(adapterName);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Failed to validate adapter templates")));
        }
    }

    @Nested
    @DisplayName("validateTemplateVariables Tests")
    class ValidateTemplateVariablesTests {

        @Test
        @DisplayName("Should succeed when all required variables are provided")
        void shouldSucceedWhenAllRequiredVariablesAreProvided() throws Exception {
            // Given
            String templatePath = "adapters/mongodb/Adapter.java.ftl";
            Set<String> requiredVariables = Set.of("name", "package", "type");
            Set<String> providedVariables = Set.of("name", "package", "type", "extra");

            when(templateRepository.extractRequiredVariables(templatePath)).thenReturn(requiredVariables);

            // When
            ValidationResult result = validator.validateTemplateVariables(templatePath, providedVariables);

            // Then
            assertTrue(result.valid());
            assertTrue(result.errors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when required variables are missing")
        void shouldFailWhenRequiredVariablesAreMissing() throws Exception {
            // Given
            String templatePath = "adapters/mongodb/Adapter.java.ftl";
            Set<String> requiredVariables = Set.of("name", "package", "type");
            Set<String> providedVariables = Set.of("name", "package");

            when(templateRepository.extractRequiredVariables(templatePath)).thenReturn(requiredVariables);

            // When
            ValidationResult result = validator.validateTemplateVariables(templatePath, providedVariables);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("requires undefined variable: type")));
        }

        @Test
        @DisplayName("Should succeed when no variables are required")
        void shouldSucceedWhenNoVariablesAreRequired() throws Exception {
            // Given
            String templatePath = "adapters/mongodb/Adapter.java.ftl";
            Set<String> requiredVariables = Set.of();
            Set<String> providedVariables = Set.of("name", "package");

            when(templateRepository.extractRequiredVariables(templatePath)).thenReturn(requiredVariables);

            // When
            ValidationResult result = validator.validateTemplateVariables(templatePath, providedVariables);

            // Then
            assertTrue(result.valid());
            assertTrue(result.errors().isEmpty());
        }

        @Test
        @DisplayName("Should fail when unexpected error occurs")
        void shouldFailWhenUnexpectedErrorOccurs() throws Exception {
            // Given
            String templatePath = "adapters/mongodb/Adapter.java.ftl";
            Set<String> providedVariables = Set.of("name", "package");

            when(templateRepository.extractRequiredVariables(templatePath))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            ValidationResult result = validator.validateTemplateVariables(templatePath, providedVariables);

            // Then
            assertFalse(result.valid());
            assertTrue(result.errors().stream()
                    .anyMatch(e -> e.contains("Failed to validate template variables")));
        }
    }

    @Mock
    private TemplateRepository templateRepository;

    private TemplateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateValidator(templateRepository);
    }

    // Helper methods to create test data

    private StructureMetadata createValidStructureMetadata() {
        return new StructureMetadata(
                "hexagonal-single",
                java.util.Map.of(
                        "driven", "infrastructure/adapters/{name}",
                        "driving", "infrastructure/entry-points/{name}"),
                null,
                null,
                List.of(),
                null);
    }

    private AdapterMetadata createValidAdapterMetadata(String name) {
        return new AdapterMetadata(
                name,
                "driven",
                "Test adapter",
                List.of(),
                List.of(),
                null,
                List.of());
    }

    private AdapterMetadata createAdapterMetadataWithApplicationProperties(String name) {
        return new AdapterMetadata(
                name,
                "driven",
                "Test adapter",
                List.of(),
                List.of(),
                "application-properties.yml.ftl",
                List.of());
    }

    private AdapterMetadata createAdapterMetadataWithConfigurationClasses(String name) {
        AdapterMetadata.ConfigurationClass configClass = new AdapterMetadata.ConfigurationClass(
                "MongoConfig",
                "config",
                "MongoConfig.java.ftl");

        return new AdapterMetadata(
                name,
                "driven",
                "Test adapter",
                List.of(),
                List.of(),
                null,
                List.of(configClass));
    }
}
