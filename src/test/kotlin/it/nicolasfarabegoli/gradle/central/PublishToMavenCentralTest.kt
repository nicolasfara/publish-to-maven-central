package it.nicolasfarabegoli.gradle.central

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

class PublishToMavenCentralTest : WordSpec({
    val projectDir = File("build/gradleTest")
    fun setupTest() {
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
                `java-gradle-plugin`
                id("it.nicolasfarabegoli.publish-to-maven-central")
            }
            
            publishOnMavenCentral {
                projectDescription.set("foo bar")
            }
            """.trimIndent()
        )
    }
    val taskName = "generatePomFileForJavaMavenPublication"
    "The plugin" should {
        "create the correct tasks" {
            setupTest()
            val tasks = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments("tasks")
                .build()
            tasks.output shouldContain "publishJavaMavenPublication"
            tasks.output shouldContain "publishPluginMavenPublicationToMavenCentralRepository"
            tasks.output shouldContain "releaseJavaMavenOnMavenCentralNexus"
            tasks.output shouldNotContain "releaseJavaMavenOnGithubNexus"
        }
        "generate the pom correctly" {
            setupTest()
            val result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments(taskName, "sourcesJar", "javadocJar", "--stacktrace")
                .withProjectDir(projectDir)
                .build()

            result.task(":$taskName")?.outcome shouldBe TaskOutcome.SUCCESS
            with(File("$projectDir/build/publications/javaMaven/pom-default.xml")) {
                shouldExist()
                shouldBeAFile()
                val content = readText(Charsets.UTF_8)
                content shouldContain "artifactId"
                content shouldContain "groupId"
                content shouldContain "name"
                content shouldContain "url"
                content shouldContain "license"
            }
        }
    }
})
