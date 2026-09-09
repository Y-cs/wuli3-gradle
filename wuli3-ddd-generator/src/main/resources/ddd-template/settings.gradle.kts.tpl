pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "{{service}}"

include(
    "shared-kernel",
    "domain",
    "api",
    "app",
    "infra",
    "adapter",
    "bootstrap",
)
