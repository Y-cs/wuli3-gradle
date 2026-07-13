import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "com.kjs.wuli3"
version = providers.gradleProperty("wuli3.build-logic.version").get()

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.5.8")
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.8.0")
    implementation("de.thetaphi:forbiddenapis:3.10")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("javaConventions") {
            id = "com.kjs.wuli3.java-conventions"
            implementationClass = "com.kjs.wuli3.JavaConventionsPlugin"
        }
        register("qualityConventions") {
            id = "com.kjs.wuli3.quality-conventions"
            implementationClass = "com.kjs.wuli3.QualityConventionsPlugin"
        }
        register("springConventions") {
            id = "com.kjs.wuli3.spring-conventions"
            implementationClass = "com.kjs.wuli3.SpringConventionsPlugin"
        }
        register("publishingConventions") {
            id = "com.kjs.wuli3.publishing-conventions"
            implementationClass = "com.kjs.wuli3.PublishingConventionsPlugin"
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "build-logic"
        }
    }

    repositories {
        val releaseRepository = providers.gradleProperty("companyMavenReleasesUrl")
        val snapshotRepository = providers.gradleProperty("companyMavenSnapshotsUrl")
        val fallbackRepository = providers.gradleProperty("companyMavenUrl")
        val repositoryUrl = when {
            version.toString().endsWith("-SNAPSHOT") -> snapshotRepository.orElse(fallbackRepository)
            else -> releaseRepository.orElse(fallbackRepository)
        }

        if (repositoryUrl.isPresent) {
            maven {
                name = "companyMaven"
                url = uri(repositoryUrl.get())
                credentials {
                    username = providers.gradleProperty("companyMavenUsername")
                        .orElse(providers.environmentVariable("COMPANY_MAVEN_USERNAME"))
                        .orNull
                    password = providers.gradleProperty("companyMavenPassword")
                        .orElse(providers.environmentVariable("COMPANY_MAVEN_PASSWORD"))
                        .orNull
                }
            }
        }
    }
}
