package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.adapter.InputAdapterConfig;
import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class InputAdapterGeneratorTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  private InputAdapterGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new InputAdapterGenerator(templateRepository, fileSystemPort);
  }

  @Test
  void shouldGenerateRestController() {
    // Given
    InputAdapterConfig config = InputAdapterConfig.builder()
        .name("User")
        .useCaseName("CreateUserUseCase")
        .type(InputAdapterConfig.InputAdapterType.REST)
        .packageName("com.test.infrastructure.rest")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/users",
                InputAdapterConfig.HttpMethod.POST,
                "execute",
                "User",
                List.of(new InputAdapterConfig.EndpointParameter(
                    "userData",
                    "UserData",
                    InputAdapterConfig.ParameterType.BODY)))))
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
        .type(InputAdapterConfig.InputAdapterType.GRAPHQL)
        .packageName("com.test.infrastructure.graphql")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/products",
                InputAdapterConfig.HttpMethod.GET,
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
        .type(InputAdapterConfig.InputAdapterType.GRPC)
        .packageName("com.test.infrastructure.grpc")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/orders",
                InputAdapterConfig.HttpMethod.POST,
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
        .type(InputAdapterConfig.InputAdapterType.WEBSOCKET)
        .packageName("com.test.infrastructure.websocket")
        .endpoints(List.of(
            new InputAdapterConfig.Endpoint(
                "/chat",
                InputAdapterConfig.HttpMethod.POST,
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
