package it.nicolasfarabegoli.gradle.central

import org.gradle.api.provider.Property
import java.time.Duration

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
        const val mavenCentralName = "MavenCentral"
        const val mavenCentralUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        const val mavenCentralNexusUrl = "https://s01.oss.sonatype.org/service/local/"
    }
}
