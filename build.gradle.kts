@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST")

import groovy.json.JsonSlurper
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import coffee.cypher.gradleutil.filters.FlatteningJsonFilter

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `maven-publish`
    signing
    alias(libs.plugins.kotlin)
    alias(libs.plugins.quilt.loom)
    alias(libs.plugins.dokka)
    alias(libs.plugins.nexus)
}

base {
    @Suppress("DEPRECATION")
    archivesBaseName = "aptitude"
}

val modProps =
    JsonSlurper().parseText(File("mod.json").readText()) as Map<String, Any>

version = (modProps.getValue("core") as Map<String, Any>).getValue("version")
group = "coffee.cypher.aptitude"

repositories {
    mavenCentral()
    maven("https://maven.gegy.dev/")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:${libs.versions.quilt.mappings.get()}:v2"))
    })
    modImplementation(libs.quilt.loader)

    implementation(libs.bundles.kotlin)

    // TODO consider this
    // QSL is not a complete API; You will need Quilted Fabric API to fill in the gaps.
    // Quilted Fabric API will automatically pull in the correct QSL version.
    modImplementation(libs.bundles.qsl)

    modImplementation(libs.kettle)
}

val javaVersion = JavaVersion.VERSION_17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    }
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to version))
        }

        filesMatching("**/*.flatten.json") {
            filter(FlatteningJsonFilter::class.java)
            path = path.replace("\\.flatten\\.json$".toRegex(), ".json")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            useK2 = false
            jvmTarget = javaVersion.toString()
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(javaVersion.toString().toInt())
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }

    dokkaJekyll.configure {
        description = "Generates GitHub Pages reference for the project"

        outputDirectory.set(project.buildDir.resolve("docs/reference"))

        doFirst {
            delete(project.buildDir.resolve("docs/reference"))
        }
    }

    dokkaJavadoc.configure {
        description = "Generates Javadoc for the project"

        outputDirectory.set(this@tasks.javadoc.map { it.destinationDir!! })
    }

    withType<DokkaTask> {
        group = "documentation"

        dokkaSourceSets {
            configureEach {
                sourceLink {
                    localDirectory.set(projectDir.resolve("src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                    remoteUrl.set(URL("https://github.com/Cypher121/aptitude/blob/main/src/main/kotlin"))
                }

                jdkVersion.set(java.toolchain.languageVersion.get().asInt())

                reportUndocumented.set(true)
            }
        }
    }

    javadoc {
        dependsOn(dokkaJavadoc)

        taskActions.clear()
    }

    register("buildUserGuide", Copy::class) {
        group = "documentation"
        dependsOn(dokkaJekyll)

        from(projectDir.resolve("docs"))
        into(project.buildDir.resolve("docs"))
    }
}

//region publishing

tasks {
    dokkaJavadoc {
        onlyIf { !project.hasProperty("publishOnly") }
    }

    javadoc {
        onlyIf { !project.hasProperty("publishOnly") }
    }

    remapJar {
        onlyIf { !project.hasProperty("publishOnly") }
    }

    remapSourcesJar {
        onlyIf { !project.hasProperty("publishOnly") }
    }
}

class Keystore(project: Project) {
    val pgpKey: String? by project
    val pgpPassword: String? by project

    val sonatypeUsername: String? by project
    val sonatypePassword: String? by project
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = project.name
            version = version.toString()

            from(components["java"])

            pom {
                name.set("Aptitude")
                description.set("Villager profession expansion")
                url.set("https://www.curseforge.com/minecraft/mc-mods/aptitude/")

                scm {
                    connection.set("scm:git:git://github.com/Cypher121/aptitude.git")
                    developerConnection.set("scm:git:ssh://github.com/Cypher121/aptitude.git")
                    url.set("https://github.com/Cypher121/aptitude/")
                }

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("cypher121")
                        name.set("Cypher121")
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(Keystore(project).sonatypeUsername.orEmpty())
            password.set(Keystore(project).sonatypePassword.orEmpty())
        }
    }
}

signing {
    sign(publishing.publications)

    val keystore = Keystore(project)

    if (keystore.pgpKey != null) {
        useInMemoryPgpKeys(
            keystore.pgpKey,
            keystore.pgpPassword
        )
    }
}

tasks.register("prepareArtifacts", Copy::class) {
    group = "publishing"

    val artifactDir: String? by project

    dependsOn(tasks.remapJar, tasks.javadoc, tasks.remapSourcesJar)

    from(project.buildDir.resolve("libs")) {
        include("**/*.jar")
    }

    val destination = artifactDir?.let(::File)
        ?: project.buildDir.resolve("release")

    into(destination)
}

//endregion publishing
