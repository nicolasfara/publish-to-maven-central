package it.nicolasfarabegoli.gradle.central

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

/**
 * Extension useful to configure the POM file in the plugin.
 */
fun MavenPublication.configurePom(extension: PublishToMavenCentralExtension) {
    pom { pom ->
        with(pom) {
            name.set(extension.projectName)
            description.set(extension.projectDescription)
            packaging = "jar"
            url.set(extension.projectUrl)
            scm { scm ->
                scm.url.set(extension.projectUrl)
                scm.connection.set(extension.scmConnection)
                scm.developerConnection.set(extension.scmConnection)
            }
            licenses {
                it.license { license ->
                    license.name.set(extension.licenseName)
                    license.url.set(extension.licenseUrl)
                }
            }
        }
    }
}

/**
 * Extension useful to configure a new repository [repoToConfigure] in the project.
 */
fun Project.configureRepository(repoToConfigure: Repository) {
    extensions.configure(PublishingExtension::class) { publishing ->
        publishing.repositories { repository ->
            repository.maven { mar ->
                mar.name = repoToConfigure.name
                mar.url = uri(repoToConfigure.url)
                mar.credentials { credentials ->
                    credentials.username = repoToConfigure.username.orNull
                    credentials.password = repoToConfigure.password.orNull
                }
                tasks.withType<PublishToMavenRepository> {
                    if (this.repository == mar) {
                        this.doFirst {
                            warnIfCredentialsAreMissing(repoToConfigure)
                        }
                    }
                }
            }
        }
    }
    repoToConfigure.nexusUrl?.let { configureNexusRepository(repoToConfigure, it) }
}

/**
 * Extension useful to configure a new Nexus [repo] with [nexusUrl].
 */
fun Project.configureNexusRepository(repo: Repository, nexusUrl: String) {
    the<PublishingExtension>().publications.withType<MavenPublication>().forEach { pub ->
        val nexus = NexusOperation(
            project = project,
            nexusUrl = nexusUrl,
            group = project.group.toString(),
            username = repo.username,
            password = repo.password,
            timeOut = repo.timeout,
            connectionTimeOut = repo.connectionTimeout
        )
        val publicationName = pub.name.replaceFirstChar(Char::titlecase)
        val uploadArtifacts = project.tasks.create<PublishToMavenRepository>(
            "upload${publicationName}To${repo.name}Nexus"
        ) {
            this.repository = project.repositories.maven { artifactRepo ->
                artifactRepo.name = repo.name
                artifactRepo.url = project.uri(repo.url)
                artifactRepo.credentials {
                    it.username = repo.username.orNull
                    it.password = repo.password.orNull
                }
            }

            this.doFirst {
                warnIfCredentialsAreMissing(repo)
                this.repository.url = nexus.repoUrl
            }
            this.publication = pub
            this.group = PublishingPlugin.PUBLISH_TASK_GROUP
            this.description = "Initialize a new Nexus repository on ${repo.name}" +
                "and uploads the $publicationName publication."
        }
        val closeRepository = tasks.create("close${publicationName}On${repo.name}Nexus") {
            it.doLast { nexus.close() }
            it.dependsOn(uploadArtifacts)
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Closes the Nexus repository on ${repo.name} with the $publicationName publication"
        }
        tasks.create("release${publicationName}On${repo.name}Nexus") {
            it.doLast { nexus.release() }
            it.dependsOn(closeRepository)
            it.group = PublishingPlugin.PUBLISH_TASK_GROUP
            it.description = "Releases the Nexus repo on ${repo.name} with the $publicationName publication."
        }
    }
}

/**
 * Extension used to verify if credentials for repository are correctly setup, otherwise a warning is raised.
 */
fun Project.warnIfCredentialsAreMissing(repository: Repository) {
    repository.username.orNull ?: logger.warn(
        "No username configured for repository {} at {}.",
        repository.name,
        repository.url,
    )

    repository.password.orNull ?: logger.warn(
        "No password configured for user {} on repository {} at {}.",
        repository.username.orNull,
        repository.name,
        repository.url,
    )
}
