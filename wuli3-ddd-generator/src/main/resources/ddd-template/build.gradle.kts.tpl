import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
    id("com.kjs.wuli3.quality-conventions") version "{{buildLogicVersion}}" apply false
    id("org.springframework.boot") version "3.5.15" apply false
}

allprojects {
    group = "{{basePackage}}"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    pluginManager.apply("java-library")
    pluginManager.apply("com.kjs.wuli3.quality-conventions")
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
    dependencies {
        add("implementation", platform("com.kjs.wuli3:wuli3-dependencies:${providers.gradleProperty("wuli3.version").get()}"))
        add("testImplementation", platform("com.kjs.wuli3:wuli3-dependencies:${providers.gradleProperty("wuli3.version").get()}"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", "org.assertj:assertj-core")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing"))
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
