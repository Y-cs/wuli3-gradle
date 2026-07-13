package com.kjs.wuli3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

/** Maven publication conventions for public Wuli3 Java components. */
class PublishingConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("maven-publish")

            extensions.configure<JavaPluginExtension> {
                withJavadocJar()
            }

            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                        pom {
                            name.set(project.name)
                            description.set(project.description ?: "Wuli3 component ${project.name}")
                            url.set("https://github.com/kjs/wuli3")
                            licenses {
                                license {
                                    name.set("Apache License, Version 2.0")
                                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                                }
                            }
                            scm {
                                connection.set("scm:git:https://github.com/kjs/wuli3.git")
                                developerConnection.set("scm:git:ssh://git@github.com/kjs/wuli3.git")
                                url.set("https://github.com/kjs/wuli3")
                            }
                            developers {
                                developer {
                                    id.set("wuli3-maintainers")
                                    name.set("Wuli3 Maintainers")
                                }
                            }
                        }
                    }
                }
                repositories {
                    maven {
                        name = "temporary"
                        url = rootProject.layout.buildDirectory.dir("temporary-maven-repository").get().asFile.toURI()
                    }
                }
            }
        }
    }
}
