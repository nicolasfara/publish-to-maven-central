package it.nicolasfarabegoli.gradle.central

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

/**
 * Plugin managing the project's configuration for publishing to Maven Central with no-configuration.
 */
class PublishToMavenCentral : Plugin<Project> {
    companion object {
        private const val publicationName = "Maven"

        private inline fun <reified T> Project.createExtension(name: String, vararg args: Any?): T =
            project.extensions.create(name, T::class.java, *args)

        private inline fun <reified T : Any> Project.configure(crossinline body: T.() -> Unit): Unit =
            extensions.configure(T::class.java) { it.body() }

        private inline fun <reified T : Task> Project.registerTaskIfNeeded(name: String): Task =
            tasks.findByName(name) ?: project.tasks.register(name, T::class.java).get()
    }
    override fun apply(project: Project) {
        project.plugins.apply(MavenPublishPlugin::class.java)
        project.plugins.apply(SigningPlugin::class.java)
        val extension = project.createExtension<PublishToMavenCentralExtension>("publishOnMavenCentral", project)

        project.configure<PublishingExtension> {
            val javadocJar = project.registerTaskIfNeeded<JavadocJar>("javadocJar")
            val sourcesJar = project.registerTaskIfNeeded<SourcesJar>("sourcesJar")

            project.tasks.findByName("assemble")?.dependsOn(javadocJar, sourcesJar)
                ?: throw IllegalArgumentException("Unable to find 'assemble' task")

            fun createPub(component: SoftwareComponent) {
                project.logger.debug("Creating the component ${component.name}")
                publications { publications ->
                    val name = "${component.name}$publicationName"
                    if (publications.none { it.name == name }) {
                        val publication = publications.create(name, MavenPublication::class.java) {
                            it.from(component)
                        }
                        project.logger.debug("Created new publication $name")
                        publication.artifact(sourcesJar)
                        publication.artifact(javadocJar)

                        project.configure<SigningExtension> {
                            val signingKey: String? by project
                            val signingPassword: String? by project
                            useInMemoryPgpKeys(
                                System.getenv("GPG_KEY")
                                    ?: signingKey,
                                System.getenv("GPG_PASSPHRASE")
                                    ?: signingPassword
                            )
                            sign(publication)
                        }
                    }
                }
            }
            project.components.forEach(::createPub)
            project.components.whenObjectAdded(::createPub)
        }

        project.afterEvaluate {
            project.the<PublishingExtension>().publications.withType<MavenPublication>().forEach {
                it.configurePom(extension)
            }
        }

        project.afterEvaluate {
            project.configureRepository(extension.mavenCentral)
        }

        project.plugins.withType<JavaPlugin> {
            project.tasks.withType<JavadocJar> {
                val javadocTask = project.tasks.findByName("javadoc") as? Javadoc
                    ?: throw IllegalStateException("Java plugin applied but no Javadoc task existing")
                dependsOn(javadocTask)
                from(javadocTask.destinationDir)
            }
            project.tasks.withType(SourcesJar::class)
        }
        val dokkaPluginClass = kotlin.runCatching { Class.forName("org.jetbrains.dokka.gradle.DokkaPlugin") }
        if (dokkaPluginClass.isSuccess) {
            @Suppress("UNCHECKED_CAST")
            project.plugins.withType(dokkaPluginClass.getOrThrow() as Class<Plugin<*>>) {
                project.tasks.withType(JavadocJar::class.java) { javadocJar ->
                    val dokkaJavadoc = project.tasks.findByName("dokkaJavadoc")
                        ?: throw IllegalStateException("Dokka plugin applied but no dokkaJavadoc task existing!")
                    val outputDirectory = dokkaJavadoc.property("outputDirectory")
                        ?: throw IllegalStateException(
                            "dokkaJavadoc has no property 'outputDirectory' - " +
                                "maybe this version is incompatible with publish-on-central?"
                        )
                    javadocJar.dependsOn(dokkaJavadoc)
                    javadocJar.from(outputDirectory)
                }
            }
        }
    }
}
