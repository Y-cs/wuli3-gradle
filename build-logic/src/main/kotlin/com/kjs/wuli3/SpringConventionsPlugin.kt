package com.kjs.wuli3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SpringConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("com.kjs.wuli3.java-conventions")

            dependencies {
                "implementation"("org.springframework.boot:spring-boot-autoconfigure")
                "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
                "testImplementation"("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}
