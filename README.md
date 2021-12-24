# publish-to-maven-central

Configuring the `maven-central` and `publish-plugin` could be difficult and sometimes boring.  
The goal of this plugin is to apply a default configuration that is convenient and easy to change.  
Finally, this plugin has been designed to work in CI environment by default, so you don't need to make ad-hoc configuration for those environments.

## Available tasks

By default, two `Jar` tasks are provided:
  - `javadocJar`: if a javadoc task exist, depends on it. If `dokkaJavadoc` tasks exists, depends on them.
  - `sourcesJar`: collect and pack all sources from the `main` source set.

For each *SoftwareComponent* and *repository* a task is generated.  
By default, the `MavenCentral` repository is created.
In short: if you want to publish all artifacts in one shot, the task `publish(Kotlin|Java)` do it for you.

## Use the plugin

```kotlin
plugins {
    id("it.nicolasfarabegoli.publish-to-maven-central") version "<last version>"
}
```

## Configuration

| Property             | Default value                                                      | Description                                                                     |
|----------------------|--------------------------------------------------------------------|---------------------------------------------------------------------------------|
| `group`              | None                                                               | The group name is **MANDATORY**, without it, some configurations will be wrong. |
| `projectName`        | `project.name`                                                     | The name of the project.                                                        |
| `projectDescription` | `project.description`                                              | The description of the project.                                                 |
| `licenseName`        | Apache-2.0                                                         | By default Apache-2.0 license is used. Please modify it based on you need.      |
| `licenseUrl`         | https://www.apache.org/licenses/LICENSE-2.0                        | The url of the chosen license.                                                  |
| `scmConnection`      | git:git@github.com:nicolasfara/{project.name}                      | The default value of the SCM connection **MUST** be changed based on your need. |
| `projectUrl`         | https://github.com/nicolasfara/{project.name}                      | The default value of the project URL **MUST** be changed based on your need.    |

### Repository configuration

By default, the **Maven Central Repository** is pre-configured with the following parameters:

| Property            | DefaultValue                                                                                                                                                                                             | Description                         |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `name`              | MavenCentral                                                                                                                                                                                             | The repository's name               |
| `url`               | https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/                                                                                                                                        | The repository's URL                |
| `username`          | The username is evaluated in the following order:<ul><li> env `OSSRH_USERNAME`</li><li> property `mavenCentralUsername`</li><li> property `sonatypeUsername`</li><li> property `ossrhUsername`</li></ul> | The username for Maven Central      |
| `password`          | The password is evaluated in the following order:<ul><li> env `OSSRH_PASSWORD`</li><li> property `mavenCentralPassword`</li><li> property `sonatypePassword`</li><li> property `ossrhPassword`</li></ul> | The username for Maven Central      |
| `nexusUrl`          | `null`                                                                                                                                                                                                   | The nexus repository's URL          |
| `timeout`           | `Duration.ofMinutest(2)`                                                                                                                                                                                 | The duration for timeout connection |
| `connectionTimeout` | `Duration.ofMinutest(2)`                                                                                                                                                                                 | The duration for the connection     |