pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("../../build/temporary-maven-repository") }
        mavenCentral()
    }
}

rootProject.name = "wuli3-gradle-consumer"
