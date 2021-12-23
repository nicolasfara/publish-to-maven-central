import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
//    `maven-publish`
//    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.jacoco)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.taskTree)
//    alias(libs.plugins.publish.nexus)
    alias(libs.plugins.publish.central)
}

group = "it.nicolasfarabegoli"
val projectId = "$group.$name"
val fullName = "Publish on Maven Central Gradle Plugin"
val websiteUrl = "https://github.com/nicolasfara/publish-to-maven-central"
val projectDetail = "A plugin that allows you to publish to Maven Central with (almost) zero-config"
val pluginImplementationClass = "it.nicolasfarabegoli.gradle.central.PublishToMavenCentral"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(kotlin("stdlib"))
    api(gradleApi())
    api(gradleKotlinDsl())
    api(libs.nexus.publish)
    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotest)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

pluginBundle {
    website = websiteUrl
    vcsUrl = websiteUrl
    tags = listOf("maven-central", "nexus", "ossrh")
}

gradlePlugin {
    plugins {
        create("PublishToMavenCentral") {
            id = projectId
            displayName = fullName
            description = projectDetail
            implementationClass = pluginImplementationClass
        }
    }
}

publishOnMavenCentral {
    projectDescription.set("A plugin that allows you to publish to Maven Central with (almost) zero-config")
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    developers {
                        developer {
                            name.set("Nicolas Farabegoli")
                            email.set("nicolas.farabegoli@gmail.com")
                            url.set("https://github.com/nicolasfara")
                        }
                    }
                }
            }
        }
    }
}

// publishing {
//    publications {
//        create<MavenPublication>("plugin") {
//            version = "${project.properties["version"]}"
//            from(components["java"])
//            artifact(project.property("sourcesJar"))
//            artifact(project.property("javadocJar"))
//
//            pom {
//                name.set("publish-to-maven-central")
//                description.set("A simple plugin configuring the process of publication to sonatype (Maven Central)")
//                url.set("https://github.com/nicolasfara/${project.name}")
//                licenses {
//                    license {
//                        name.set("Apache-2.0")
//                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
//                    }
//                }
//                developers {
//                    developer {
//                        name.set("Nicolas Farabegoli")
//                        email.set("nicolas.farabegoli@gmail.com")
//                        url.set("https://github.com/nicolasfara")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/nicolasfara/${project.name}")
//                }
//            }
//        }
//    }
// }
//
// nexusPublishing {
//    repositories {
//        create("central") {
//            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
//            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots"))
//            username.set(System.getenv("OSSRH_USERNAME"))
//            password.set(System.getenv("OSSRH_PASSWORD"))
//        }
//    }
// }
