package com.kjs.wuli3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
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

            val javaVersion = intProperty(
                ConventionProperties.JAVA_VERSION,
                ConventionProperties.DEFAULT_JAVA_VERSION,
            )

            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(javaVersion))
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
                val bomDependency = conventionBomDependency()
                "implementation"(platform(bomDependency))
                "testImplementation"(platform(bomDependency))
                "annotationProcessor"(platform(bomDependency))
                "testAnnotationProcessor"(platform(bomDependency))
                "testImplementation"("org.junit.jupiter:junit-jupiter")
                "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
                "testImplementation"("org.assertj:assertj-core")
            }
        }
    }

    private fun Project.conventionBomDependency(): Any {
        val useProjectBom = booleanProperty(ConventionProperties.USE_PROJECT_BOM, false)
        if (useProjectBom) {
            val projectBomPath = stringProperty(
                ConventionProperties.PROJECT_BOM_PATH,
                ConventionProperties.DEFAULT_PROJECT_BOM_PATH,
            )
            return project(projectBomPath)
        }

        return stringProperty(
            ConventionProperties.BOM_COORDINATES,
            ConventionProperties.DEFAULT_BOM_COORDINATES,
        )
    }
}
