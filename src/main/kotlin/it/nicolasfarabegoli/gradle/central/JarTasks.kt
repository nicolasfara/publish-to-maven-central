package it.nicolasfarabegoli.gradle.central

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar

/**
 * Generic class to generate Jar with classifier.
 * This generic task use the [Jar] class.
 */
open class JarClassifier(classifier: String) : Jar() {
    init {
        archiveClassifier.set(classifier)
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}

/**
 * Class for generating javadoc jar.
 */
open class JavadocJar : JarClassifier("javadoc")

/**
 * Class for generating sources jar.
 */
open class SourcesJar : JarClassifier("sources") {
    init {
        val sourceSets = project.properties["sourceSets"] as? SourceSetContainer
            ?: throw IllegalStateException("Project has no property 'sourceSets' of type 'SourceSetContainer'")

        val sourceSet = sourceSets.getByName("main")
            ?: throw IllegalStateException("Project has no source set named 'main'")

        addSourceSet(sourceSet)
    }

    private fun addSourceSet(source: SourceSet) {
        from(source.allSource)
    }
}
