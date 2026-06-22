package com.kjs.wuli3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

class JavaConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("java-library")
            pluginManager.apply("jacoco")
            pluginManager.apply("com.kjs.wuli3.quality-conventions")

            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
                }
                withSourcesJar()
            }

            extensions.configure<JacocoPluginExtension> {
                toolVersion = "0.8.13"
            }

            tasks.withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
                options.compilerArgs.addAll(
                    listOf(
                        "-parameters",
                        "-Xlint:all",
                        "-Xlint:-processing",
                    ),
                )
            }

            tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
                useJUnitPlatform()
            }

            dependencies {
                "implementation"(platform(project(":wuli3-dependencies")))
                "testImplementation"(platform(project(":wuli3-dependencies")))
                "annotationProcessor"(platform(project(":wuli3-dependencies")))
                "testAnnotationProcessor"(platform(project(":wuli3-dependencies")))
                "compileOnly"("org.jspecify:jspecify")
                "testImplementation"("org.junit.jupiter:junit-jupiter")
                "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
                "testImplementation"("org.assertj:assertj-core")
            }
        }
    }
}
