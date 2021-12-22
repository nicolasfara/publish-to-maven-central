package it.nicolasfarabegoli.gradle.central

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class PublishToMavenCentralExtension(private val project: Project) {
    val projectName: Property<String> = project.propertyWithDefault(project.name)
    val projectDescription: Property<String> = project.propertyWithDefault(project.description)
    val licenseName: Property<String> = project.propertyWithDefault("Apache-2.0")
    val licenseUrl: Property<String> = project.propertyWithDefault("https://www.apache.org/licenses/LICENSE-2.0")
    val scmConnection: Property<String> = project.propertyWithDefault("git:git@github.com:nicolasfara/${project.name}")
    val projectUrl: Property<String> = project.propertyWithDefault("https://github.com/nicolasfara/${project.name}")
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
