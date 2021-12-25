package it.nicolasfarabegoli.gradle.central

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Extension used to configure the plugin for the [project].
 */
open class PublishToMavenCentralExtension(private val project: Project) {
    /**
     * Project name.
     */
    val projectName: Property<String> = project.propertyWithDefault(project.name)

    /**
     * Project description.
     */
    val projectDescription: Property<String> = project.propertyWithDefault(project.description)

    /**
     * Name of the license. Use the SPDX short identifier: https://opensource.org/licenses.
     */
    val licenseName: Property<String> = project.propertyWithDefault("Apache-2.0")

    /**
     * The URL of the chose license.
     */
    val licenseUrl: Property<String> = project.propertyWithDefault("https://www.apache.org/licenses/LICENSE-2.0")

    /**
     * SCM connection URL.
     */
    val scmConnection: Property<String> = project.propertyWithDefault("git:git@github.com:nicolasfara/${project.name}")

    /**
     * Project's URL, typically a GitHub URL repo.
     */
    val projectUrl: Property<String> = project.propertyWithDefault("https://github.com/nicolasfara/${project.name}")

    /**
     * Configuration for the Maven Central repository.
     */
    val mavenCentral: Repository = Repository(
        Repository.mavenCentralName,
        url = Repository.mavenCentralUrl,
        username = project.propertyWithDefaultProvider {
            System.getenv("OSSRH_USERNAME")
                ?: project.properties["mavenCentralUsername"]?.toString()
                ?: project.properties["sonatypeUsername"]?.toString()
                ?: project.properties["ossrhUsername"]?.toString()
        },
        password = project.propertyWithDefaultProvider {
            System.getenv("OSSRH_PASSWORD")
                ?: project.properties["mavenCentralPassword"]?.toString()
                ?: project.properties["sonatypePassword"]?.toString()
                ?: project.properties["ossrhPassword"]?.toString()
        },
        nexusUrl = Repository.mavenCentralNexusUrl
    )
}
