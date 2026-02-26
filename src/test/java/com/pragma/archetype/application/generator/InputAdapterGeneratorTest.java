package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.application.port.out.AdapterDiscoveryPort;
import com.pragma.archetype.domain.model.adapter.Endpoint;
import com.pragma.archetype.domain.model.adapter.EndpointParameter;
import com.pragma.archetype.domain.model.adapter.HttpMethod;
import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.adapter.ParameterType;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class InputAdapterGeneratorTest {

        @Mock
        private TemplateRepository templateRepository;

        @Mock
        private FileSystemPort fileSystemPort;

        @Mock
        private AdapterDiscoveryPort adapterDiscoveryPort;

        private InputAdapterGenerator generator;

        @BeforeEach
        void setUp() {
                generator = new InputAdapterGenerator(templateRepository, fileSystemPort, adapterDiscoveryPort);

                // Configure adapterDiscoveryPort mock to return true for all adapter types
                lenient().when(adapterDiscoveryPort.adapterExists(anyString(), any())).thenReturn(true);
                lenient().when(adapterDiscoveryPort.listAdapterNames(any())).thenReturn(
                                List.of("rest", "graphql", "grpc", "websocket", "sqs-consumer", "scheduled"));

                // Configure findAdapter to return a dummy metadata
                lenient().when(adapterDiscoveryPort.findAdapter(anyString(), any())).thenReturn(
                                java.util.Optional.of(com.pragma.archetype.domain.model.adapter.AdapterMetadata
                                                .builder()
                                                .name("test")
                                                .direction(com.pragma.archetype.domain.model.adapter.AdapterDirection.INPUT)
                                                .build()));
        }

        @Test
        void shouldGenerateRestController() {
                // Given
                InputAdapterConfig config = InputAdapterConfig.builder()
                                .name("User")
                                .useCaseName("CreateUserUseCase")
                                .type("rest")
                                .packageName("com.test.infrastructure.rest")
                                .endpoints(List.of(
                                                new Endpoint(
                                                                "/users",
                                                                HttpMethod.POST,
                                                                "execute",
                                                                "User",
                                                                List.of(new EndpointParameter(
                                                                                "userData",
                                                                                "UserData",
                                                                                ParameterType.BODY)))))
                                .build();

                when(templateRepository.processTemplate(anyString(), anyMap()))
                                .thenReturn("public class UserController {}");

                // When
                List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

                // Then
                assertNotNull(files);
                assertEquals(1, files.size());
                assertTrue(files.get(0).path().toString().contains("UserController.java"));
                verify(templateRepository).processTemplate(anyString(), anyMap());
        }

        @Test
        void shouldGenerateGraphQLResolver() {
                // Given
                InputAdapterConfig config = InputAdapterConfig.builder()
                                .name("Product")
                                .useCaseName("GetProductUseCase")
                                .type("graphql")
                                .packageName("com.test.infrastructure.graphql")
                                .endpoints(List.of(
                                                new Endpoint(
                                                                "/products",
                                                                HttpMethod.GET,
                                                                "execute",
                                                                "Product",
                                                                List.of())))
                                .build();

                when(templateRepository.processTemplate(anyString(), anyMap()))
                                .thenReturn("public class ProductResolver {}");

                // When
                List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

                // Then
                assertNotNull(files);
                assertEquals(1, files.size());
                assertTrue(files.get(0).path().toString().contains("ProductResolver.java"));
        }

        @Test
        void shouldGenerateGrpcService() {
                // Given
                InputAdapterConfig config = InputAdapterConfig.builder()
                                .name("Order")
                                .useCaseName("CreateOrderUseCase")
                                .type("grpc")
                                .packageName("com.test.infrastructure.grpc")
                                .endpoints(List.of(
                                                new Endpoint(
                                                                "/orders",
                                                                HttpMethod.POST,
                                                                "execute",
                                                                "Order",
                                                                List.of())))
                                .build();

                when(templateRepository.processTemplate(anyString(), anyMap()))
                                .thenReturn("public class OrderService {}");

                // When
                List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

                // Then
                assertNotNull(files);
                assertEquals(1, files.size());
                assertTrue(files.get(0).path().toString().contains("OrderService.java"));
        }

        @Test
        void shouldGenerateWebSocketHandler() {
                // Given
                InputAdapterConfig config = InputAdapterConfig.builder()
                                .name("Chat")
                                .useCaseName("SendMessageUseCase")
                                .type("websocket")
                                .packageName("com.test.infrastructure.websocket")
                                .endpoints(List.of(
                                                new Endpoint(
                                                                "/chat",
                                                                HttpMethod.POST,
                                                                "execute",
                                                                "Message",
                                                                List.of())))
                                .build();

                when(templateRepository.processTemplate(anyString(), anyMap()))
                                .thenReturn("public class ChatHandler {}");

                // When
                List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

                // Then
                assertNotNull(files);
                assertEquals(1, files.size());
                assertTrue(files.get(0).path().toString().contains("ChatHandler.java"));
        }
}
