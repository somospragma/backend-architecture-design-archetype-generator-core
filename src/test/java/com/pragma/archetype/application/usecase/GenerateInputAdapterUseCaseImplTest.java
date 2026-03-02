package com.pragma.archetype.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.generator.UnifiedAdapterGenerator;
import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.AdapterDirection;
import com.pragma.archetype.domain.model.adapter.AdapterMetadata;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.HttpMethod;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.config.ProjectConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.structure.StructureMetadata;
import com.pragma.archetype.domain.port.in.GenerateInputAdapteResult;
import com.pragma.archetype.domain.port.out.ConfigurationPort;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;
import com.pragma.archetype.infrastructure.adapter.out.config.YamlConfigurationAdapter;

@ExtendWith(MockitoExtension.class)
class GenerateInputAdapterUseCaseImplTest {

    @Mock
    private UnifiedAdapterGenerator generator;

    @Mock
    private ConfigurationPort configurationPort;

    @Mock
    private FileSystemPort fileSystemPort;

    @Mock
    private AdapterDiscoveryPort adapterDiscoveryPort;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private YamlConfigurationAdapter yamlConfigurationAdapter;

    @Mock
    private ProjectConfig projectConfig;

    @Mock
te StructureMetadata structureMetadata;

    @Mock
    private AdapterMetadata adapterMetadata;

    private GenerateInputAdapterUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateInputAdapterUseCaseImpl(
                generator,
                configurationPort,
                fileSystemPort,
                adapterDiscoveryPort,
                templateRepository,
                yamlConfigurationAdapter);

        // Setup common mocks (lenient for optional usage in some tests)
        lenient().when(configurationPort.readConfiguration(any())).thenReturn(Optional.of(projectConfig));
        lenient().when(templateRepository.loadStructureMetadata(any())).thenReturn(structureMetadata);
        lenient().when(adapterDiscoveryPort.findAdapter(any(), any(), any(), any()))
                .thenReturn(Optional.of(adapterMetadata));
        lenient().when(adapterMetadata.getName()).thenReturn("rest");
        lenient().when(adapterMetadata.getDirection()).thenReturn(AdapterDirection.INPUT);

        // Mock discovered templates with a main template
        var mainTemplate = com.pragma.archetype.domain.model.adapter.TemplateFile.builder()
                .name("RestController")
                .type("main")
                .destination("{name}Controller.java")
                .build();
        lenient().when(adapterMetadata.getDiscoveredTemplates()).thenReturn(List.of(mainTemplate));

        lenient().when(adapterMetadata.getDependencies()).thenReturn(List.of());
        lenient().when(adapterMetadata.getApplicationPropertiesTemplate()).thenReturn(null);
        lenient().when(adapterMetadata.getConfigurationClasses()).thenReturn(List.of());
        lenient().when(adapterMetadata.getTestDependencies()).thenReturn(List.of());
        lenient().when(adapterMetadata.getAdditionalResources()).thenReturn(List.of());
        lenient().when(adapterMetadata.getBuildPlugins()).thenReturn(List.of());
        lenient().when(adapterMetadata.getBuildConfiguration()).thenReturn(null);
        lenient().when(adapterMetadata.getSuffix()).thenReturn(null);
        l).thenReturn(false);
    }

    @Test
    void shouldGenerateInputAdapterSuccessfully() {
        // Given
        InputAdapterConfig config = InputAdapterConfig.builder()
                .name("UserController")
                .useCaseName("CreateUserUseCase")
                .type("rest")
                .packageName("com.test.infrastructure.rest")
                .framework("spring")
                .paradigm("imperative")
                .endpoints(List.of(
                        new Endpoint(
                                "/users",
                                HttpMethod.POST,
                                "execute",
                                "User",
                                List.of())))
                .build();

        GeneratedFile file = GeneratedFile.javaSource(
                Path.of("src/main/java/com/test/infrastructure/rest/UserController.java"),
                "public class UserController {}");

n(List.of(file));

        // When
        GenerationResult result = useCase.execute(Path.of("/test"), config);

        // Then
        assertTrue(result.success());
        assertEquals(1, result.generatedFiles().size());
    }

    @Test
    void shouldFailWhenProjectConfigNotFound() {
        // Given
        InputAdapterConfig config = InputAdapterConfig.builder()
                .name("UserController")
                .useCaseName("CreateUserUseCase")
                .type("rest")
                .packageName("com.test.infrastructure.rest")
                .framework("spring")
                .paradigm("imperative")
                .endpoints(List.of())
                .build();

        when(configurationPort.readConfiguration(any())).thenReturn(Optional.empty());

        // When
        GenerationResult result = useCase.execute(Path.of("/test"), config);

        // Then
        assertFalse(result.success());
        a)));
    }

    @Test
    void shouldHandleGeneratorException() {
        // Given
        InputAdapterConfig config = InputAdapterConfig.builder()
                .name("UserController")
                .useCaseName("CreateUserUseCase")
                .type("rest")
                .packageName("com.test.infrastructure.rest")
                .framework("spring")
                .paradigm("imperative")
                .endpoints(List.of(
                        new Endpoint(
                                "/users",
                                HttpMethod.POST,
                                "execute",
                                "User",
                                List.of())))
                .build();

        when(generator.generate(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Generator error"));

        // When
        GenerationResult result = useCase.execute(Path.of("/test"), config);

        // Then
        assertFalse(result.success());
        aertTrue(result.errors().stream().anyMatch(e -> e.contains("Failed to generate adapter")));
    }
}
