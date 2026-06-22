package com.kjs.wuli3

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

class QualityConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("checkstyle")
            pluginManager.apply("de.thetaphi.forbiddenapis")
            pluginManager.apply("net.ltgt.errorprone")

            extensions.configure<CheckstyleExtension> {
                toolVersion = "13.6.0"
                config = resources.text.fromUri(
                    rootProject.layout.projectDirectory.file("build-logic/src/main/resources/checkstyle/checkstyle.xml"),
                )
                isShowViolations = true
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

            tasks.withType<CheckForbiddenApis>().configureEach {
                bundledSignatures.addAll(listOf("jdk-unsafe"))
                signaturesFiles = files(
                    rootProject.layout.projectDirectory.file("build-logic/src/main/resources/forbidden-apis/wuli3-signatures.txt"),
                )
                ignoreFailures = false
            }

            tasks.matching { it.name == "forbiddenApisTest" }.configureEach {
                enabled = false
            }

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone {
                    disableWarningsInGeneratedCode.set(true)
                    check("NullAway", CheckSeverity.ERROR)
                    option("NullAway:AnnotatedPackages", "com.kjs.wuli3")
                    option("NullAway:JSpecifyMode", "true")
                }
            }
        }
    }
}
