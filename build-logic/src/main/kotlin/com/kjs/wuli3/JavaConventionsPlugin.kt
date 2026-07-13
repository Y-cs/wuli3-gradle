package com.kjs.wuli3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.math.BigDecimal

class JavaConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("java-library")
            pluginManager.apply("jacoco")
            pluginManager.apply("com.kjs.wuli3.quality-conventions")
            pluginManager.apply("com.kjs.wuli3.publishing-conventions")

            val javaVersion = intProperty(
                ConventionProperties.JAVA_VERSION,
                ConventionProperties.DEFAULT_JAVA_VERSION,
            )
            val lombokEnabled = booleanProperty(ConventionProperties.LOMBOK_ENABLED, true)
            val jacocoVerificationEnabled = booleanProperty(
                ConventionProperties.JACOCO_VERIFICATION_ENABLED,
                true,
            )
            val jacocoLineMinimum = stringProperty(
                ConventionProperties.JACOCO_LINE_MINIMUM,
                ConventionProperties.DEFAULT_JACOCO_LINE_MINIMUM,
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

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }

            tasks.withType<JacocoReport>().configureEach {
                dependsOn(tasks.withType<Test>())
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    csv.required.set(false)
                }
            }

            tasks.withType<JacocoCoverageVerification>().configureEach {
                enabled = jacocoVerificationEnabled
                dependsOn(tasks.withType<Test>())
                violationRules {
                    rule {
                        limit {
                            counter = "LINE"
                            value = "COVEREDRATIO"
                            minimum = BigDecimal(jacocoLineMinimum)
                        }
                    }
                }
            }

            tasks.named("check").configure {
                dependsOn(tasks.withType<JacocoReport>())
                dependsOn(tasks.withType<JacocoCoverageVerification>())
            }

            dependencies {
                val bomDependency = conventionBomDependency()
                "implementation"(platform(bomDependency))
                "testImplementation"(platform(bomDependency))
                "annotationProcessor"(platform(bomDependency))
                "testAnnotationProcessor"(platform(bomDependency))
                if (lombokEnabled) {
                    lombok()
                }
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
            return dependencies.project(mapOf("path" to projectBomPath))
        }

        return stringProperty(
            ConventionProperties.BOM_COORDINATES,
            ConventionProperties.DEFAULT_BOM_COORDINATES,
        )
    }

    private fun DependencyHandler.lombok() {
        add("compileOnly", "org.projectlombok:lombok")
        add("annotationProcessor", "org.projectlombok:lombok")
        add("testCompileOnly", "org.projectlombok:lombok")
        add("testAnnotationProcessor", "org.projectlombok:lombok")
    }
}
