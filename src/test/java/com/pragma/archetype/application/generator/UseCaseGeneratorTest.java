package com.pragma.archetype.application.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pragma.archetype.domain.model.file.GeneratedFile;
import com.pragma.archetype.domain.model.usecase.UseCaseConfig;
import com.pragma.archetype.domain.port.out.FileSystemPort;
import com.pragma.archetype.domain.port.out.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class UseCaseGeneratorTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private FileSystemPort fileSystemPort;

  private UseCaseGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new UseCaseGenerator(templateRepository, fileSystemPort);
  }

  @Test
  void shouldGeneratePortAndImplementation() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("CreateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of(
                new UseCaseConfig.MethodParameter("userId", "String")))))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public interface CreateUserUseCase {}")
        .thenReturn("public class CreateUserUseCaseImpl {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

    // Then
    assertNotNull(files);
    assertEquals(2, files.size());
    verify(templateRepository, times(2)).processTemplate(anyString(), anyMap());
  }

  @Test
  void shouldGenerateOnlyPort() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("GetUser")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(false)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public interface GetUserUseCase {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
    verify(templateRepository, times(1)).processTemplate(anyString(), anyMap());
  }

  @Test
  void shouldGenerateOnlyImplementation() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("UpdateUser")
        .packageName("com.test.domain.port.in")
        .generatePort(false)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("execute", "User", List.of())))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public class UpdateUserUseCaseImpl {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

    // Then
    assertNotNull(files);
    assertEquals(1, files.size());
    verify(templateRepository, times(1)).processTemplate(anyString(), anyMap());
  }

  @Test
  void shouldHandleMultipleMethods() {
    // Given
    UseCaseConfig config = UseCaseConfig.builder()
        .name("UserManagement")
        .packageName("com.test.domain.port.in")
        .generatePort(true)
        .generateImpl(true)
        .methods(List.of(
            new UseCaseConfig.UseCaseMethod("create", "User", List.of(
                new UseCaseConfig.MethodParameter("userData", "UserData"))),
            new UseCaseConfig.UseCaseMethod("update", "User", List.of(
                new UseCaseConfig.MethodParameter("userId", "String"),
                new UseCaseConfig.MethodParameter("userData", "UserData")))))
        .build();

    when(templateRepository.processTemplate(anyString(), anyMap()))
        .thenReturn("public interface UserManagementUseCase {}")
        .thenReturn("public class UserManagementUseCaseImpl {}");

    // When
    List<GeneratedFile> files = generator.generate(Path.of("/test"), config);

    // Then
    assertNotNull(files);
    assertEquals(2, files.size());
  }
}
