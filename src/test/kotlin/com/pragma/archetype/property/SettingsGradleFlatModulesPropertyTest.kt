package com.pragma.archetype.property

import com.pragma.archetype.application.generator.ProjectGenerator
import com.pragma.archetype.domain.model.file.GeneratedFile
import com.pragma.archetype.domain.port.out.FileSystemPort
import com.pragma.archetype.domain.port.out.TemplateRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.az
import org.mockito.Mockito
import java.nio.file.Path

/**
 * Property-based tests for Settings.gradle Flat Module Names (Property 19).
 *
 * Tag: Feature: kb-alignment-hexagonal-architectures, Property 19: Settings.gradle Flat Module Names
 *
 * For any multi-module architecture, verify flat includes with projectDir mappings.
 * Instead of `include("domain:model")`, the output should be:
 *   include("model")
 *   project(":model").projectDir = file("domain/model")
 *
 * **Validates: Requirements 19.1, 19.2, 19.3**
 */
class SettingsGradleFlatModulesPropertyTest : PropertyTestBase() {

    /**
     * Generates valid multi-module paths like "domain:model", "infrastructure:entry-points:rest-api".
     */
    private fun modulePath(): Arb<String> {
        val segment = Arb.string(3..10, Codepoint.az())
        return Arb.element(
            listOf(2, 3) // 2 or 3 segments
        ).flatMap { segmentCount ->
            if (segmentCount == 2) {
                segment.flatMap { first ->
                    segment.map { second -> "$first:$second" }
                }
            } else {
                segment.flatMap { first ->
                    segment.flatMap { second ->
                        segment.map { third -> "$first:$second:$third" }
                    }
                }
            }
        }
    }

    init {
        "Property 19.1: addModuleToSettings should use flat include with leaf module name" {
            checkAll(modulePath()) { modPath ->
                // Arrange
                val templateRepository = Mockito.mock(TemplateRepository::class.java)
                val fileSystemPort = Mockito.mock(FileSystemPort::class.java)
                val projectGenerator = ProjectGenerator(templateRepository, fileSystemPort)

                val projectPath = Path.of("/tmp/test-project")
                val settingsFile = projectPath.resolve("settings.gradle.kts")
                val initialContent = "rootProject.name = \"test-project\"\n"

                Mockito.`when`(fileSystemPort.exists(settingsFile)).thenReturn(true)
                Mockito.`when`(fileSystemPort.readFile(settingsFile)).thenReturn(initialContent)

                // Capture written content
                val writtenContent = mutableListOf<String>()
                Mockito.doAnswer { invocation ->
                    val file = invocation.getArgument<GeneratedFile>(0)
                    writtenContent.add(file.content())
                    null
                }.`when`(fileSystemPort).writeFile(Mockito.any(GeneratedFile::class.java))

                // Act
                projectGenerator.addModuleToSettings(projectPath, modPath)

                // Assert - should use flat include with leaf module name
                val segments = modPath.split(":")
                val leafName = segments.last()
                writtenContent.size shouldBe 1
                writtenContent[0] shouldContain "include(\"$leafName\")"
                // Should NOT contain the nested colon-separated include
                if (segments.size > 1) {
                    writtenContent[0] shouldNotContain "include(\"$modPath\")"
                }
            }
        }

        "Property 19.2: addModuleToSettings should include projectDir mapping" {
            checkAll(modulePath()) { modPath ->
                // Arrange
                val templateRepository = Mockito.mock(TemplateRepository::class.java)
                val fileSystemPort = Mockito.mock(FileSystemPort::class.java)
                val projectGenerator = ProjectGenerator(templateRepository, fileSystemPort)

                val projectPath = Path.of("/tmp/test-project")
                val settingsFile = projectPath.resolve("settings.gradle.kts")
                val initialContent = "rootProject.name = \"test-project\"\n"

                Mockito.`when`(fileSystemPort.exists(settingsFile)).thenReturn(true)
                Mockito.`when`(fileSystemPort.readFile(settingsFile)).thenReturn(initialContent)

                val writtenContent = mutableListOf<String>()
                Mockito.doAnswer { invocation ->
                    val file = invocation.getArgument<GeneratedFile>(0)
                    writtenContent.add(file.content())
                    null
                }.`when`(fileSystemPort).writeFile(Mockito.any(GeneratedFile::class.java))

                // Act
                projectGenerator.addModuleToSettings(projectPath, modPath)

                // Assert - should have projectDir mapping with slash-separated path
                val segments = modPath.split(":")
                val leafName = segments.last()
                val directoryPath = modPath.replace(':', '/')

                writtenContent.size shouldBe 1
                writtenContent[0] shouldContain "project(\":$leafName\").projectDir = file(\"$directoryPath\")"
            }
        }

        "Property 19.3: addModuleToSettings should not duplicate existing includes" {
            checkAll(modulePath()) { modPath ->
                // Arrange
                val templateRepository = Mockito.mock(TemplateRepository::class.java)
                val fileSystemPort = Mockito.mock(FileSystemPort::class.java)
                val projectGenerator = ProjectGenerator(templateRepository, fileSystemPort)

                val projectPath = Path.of("/tmp/test-project")
                val settingsFile = projectPath.resolve("settings.gradle.kts")

                val segments = modPath.split(":")
                val leafName = segments.last()

                // Content already has the flat include
                val initialContent = "rootProject.name = \"test-project\"\ninclude(\"$leafName\")\n"

                Mockito.`when`(fileSystemPort.exists(settingsFile)).thenReturn(true)
                Mockito.`when`(fileSystemPort.readFile(settingsFile)).thenReturn(initialContent)

                // Act
                projectGenerator.addModuleToSettings(projectPath, modPath)

                // Assert - writeFile should NOT be called since module already included
                Mockito.verify(fileSystemPort, Mockito.never()).writeFile(Mockito.any(GeneratedFile::class.java))
            }
        }
    }
}
