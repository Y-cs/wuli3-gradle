package com.kjs.wuli3

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsTask
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import java.net.URL

class QualityConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("checkstyle")
            pluginManager.apply("com.github.spotbugs")
            pluginManager.apply("de.thetaphi.forbiddenapis")
            pluginManager.apply("net.ltgt.errorprone")

            val forbiddenApisEnabled = booleanProperty(ConventionProperties.FORBIDDEN_APIS_ENABLED, true)
            val forbiddenApisTestEnabled = booleanProperty(ConventionProperties.FORBIDDEN_APIS_TEST_ENABLED, false)
            val nullAwayEnabled = booleanProperty(ConventionProperties.NULL_AWAY_ENABLED, true)
            val nullAwayJSpecify = booleanProperty(ConventionProperties.NULL_AWAY_JSPECIFY, true)
            val spotBugsEnabled = booleanProperty(ConventionProperties.SPOTBUGS_ENABLED, true)
            val nullAwayAnnotatedPackages = stringProperty(
                ConventionProperties.NULL_AWAY_ANNOTATED_PACKAGES,
                ConventionProperties.DEFAULT_NULL_AWAY_ANNOTATED_PACKAGES,
            )

            extensions.configure<CheckstyleExtension> {
                toolVersion = "13.6.0"
                config = resources.text.fromString(pluginResource("checkstyle/checkstyle.xml").readText())
                isShowViolations = true
            }

            extensions.configure<SpotBugsExtension> {
                toolVersion.set("4.10.2")
                effort.set(Effort.MAX)
                reportLevel.set(Confidence.HIGH)
                ignoreFailures.set(false)
                showStackTraces.set(true)
            }

            dependencies {
                "errorprone"("com.google.errorprone:error_prone_core:2.50.0")
                "errorprone"("com.uber.nullaway:nullaway:0.13.7")
                "compileOnly"("org.jspecify:jspecify")
            }

            tasks.withType<Checkstyle>().configureEach {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                }
            }

            tasks.withType<SpotBugsTask>().configureEach {
                enabled = spotBugsEnabled
                reports.create("xml") {
                    required.set(true)
                }
                reports.create("html") {
                    required.set(true)
                }
            }

            tasks.withType<CheckForbiddenApis>().configureEach {
                enabled = forbiddenApisEnabled
                bundledSignatures.addAll(listOf("jdk-unsafe"))
                signaturesURLs.add(pluginResource("forbidden-apis/wuli3-signatures.txt"))
                ignoreFailures = false
            }

            tasks.matching { it.name == "forbiddenApisTest" }.configureEach {
                enabled = forbiddenApisEnabled && forbiddenApisTestEnabled
            }

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone {
                    disableWarningsInGeneratedCode.set(true)
                    check(
                        "NullAway",
                        if (nullAwayEnabled) CheckSeverity.ERROR else CheckSeverity.OFF,
                    )
                    if (nullAwayEnabled) {
                        option("NullAway:AnnotatedPackages", nullAwayAnnotatedPackages)
                        option("NullAway:JSpecifyMode", nullAwayJSpecify.toString())
                    }
                }
            }
        }
    }

    private fun pluginResource(path: String): URL =
        requireNotNull(javaClass.classLoader.getResource(path)) {
            "Missing build-logic resource: $path"
        }
}
