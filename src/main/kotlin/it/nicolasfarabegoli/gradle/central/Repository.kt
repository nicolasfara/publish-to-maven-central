package it.nicolasfarabegoli.gradle.central

import org.gradle.api.provider.Property
import java.time.Duration

/**
 * Model class representing the concept of repository.
 * [name] the name of the repository (symbolic name).
 * [url] the url of the repository.
 * [username] username to access to the repository.
 * [password] password to access to the repository.
 * [nexusUrl] if present, the repository is a Nexus one.
 * [timeout] nexus timeout.
 * [connectionTimeout] nexus connection timeout.
 */
data class Repository(
    val name: String,
    val url: String,
    val username: Property<String>,
    val password: Property<String>,
    val nexusUrl: String? = null,
    val timeout: Duration = Duration.ofMinutes(2),
    val connectionTimeout: Duration = Duration.ofMinutes(2)
) {
    companion object {
        /**
         * Default name of the Maven Central repository.
         */
        const val mavenCentralName = "MavenCentral"

        /**
         * The new URL for the maven central.
         */
        const val mavenCentralUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

        /**
         * Sonatype Nexus instance of the Maven Central.
         */
        const val mavenCentralNexusUrl = "https://s01.oss.sonatype.org/service/local/"
    }
}
