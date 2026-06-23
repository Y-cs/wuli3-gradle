package com.kjs.wuli3

import org.gradle.api.Project

internal object ConventionProperties {
    const val DEFAULT_BOM_COORDINATES = "com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"
    const val DEFAULT_JAVA_VERSION = 21
    const val DEFAULT_NULL_AWAY_ANNOTATED_PACKAGES = "com.kjs.wuli3"
    const val DEFAULT_PROJECT_BOM_PATH = ":wuli3-dependencies"

    const val BOM_COORDINATES = "wuli3.conventions.bom-coordinates"
    const val FORBIDDEN_APIS_ENABLED = "wuli3.conventions.forbidden-apis.enabled"
    const val FORBIDDEN_APIS_TEST_ENABLED = "wuli3.conventions.forbidden-apis.test-enabled"
    const val JAVA_VERSION = "wuli3.conventions.java-version"
    const val NULL_AWAY_ANNOTATED_PACKAGES = "wuli3.conventions.nullaway.annotated-packages"
    const val NULL_AWAY_ENABLED = "wuli3.conventions.nullaway.enabled"
    const val NULL_AWAY_JSPECIFY = "wuli3.conventions.nullaway.jspecify"
    const val PROJECT_BOM_PATH = "wuli3.conventions.project-bom-path"
    const val SPOTBUGS_ENABLED = "wuli3.conventions.spotbugs.enabled"
    const val USE_PROJECT_BOM = "wuli3.conventions.use-project-bom"
}

internal fun Project.stringProperty(name: String, defaultValue: String): String =
    providers.gradleProperty(name).orElse(defaultValue).get()

internal fun Project.booleanProperty(name: String, defaultValue: Boolean): Boolean =
    providers.gradleProperty(name).map(String::toBooleanStrict).orElse(defaultValue).get()

internal fun Project.intProperty(name: String, defaultValue: Int): Int =
    providers.gradleProperty(name).map(String::toInt).orElse(defaultValue).get()
