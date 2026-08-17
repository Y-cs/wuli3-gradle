@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "wuli3-gradle"

include(
    "wuli3-dependencies",
    "wuli3-core",
    "wuli3-json",
    "wuli3-event-core",
    "wuli3-event-spring-boot-starter",
    "wuli3-context-propagation",
    "wuli3-aliyun-spring-boot-starter",
    "wuli3-configuration-spring-boot-starter",
    "wuli3-logging-spring-boot-starter",
    "wuli3-web-spring-boot-starter",
    "wuli3-mysql-spring-boot-starter",
    "wuli3-redis-spring-boot-starter",
    "wuli3-rocketmq-spring-boot-starter",
    "wuli3-rabbitmq-spring-boot-starter",
    "wuli3-elasticsearch-spring-boot-starter",
    "wuli3-mongodb-spring-boot-starter",
)
