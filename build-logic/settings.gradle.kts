pluginManagement {
    repositories {
        // build-logic 自身需要解析 Gradle 插件，例如 kotlin-dsl 依赖的插件。
        gradlePluginPortal()
        mavenCentral()
    }
}

// 这里只影响 included build 的显示名称，不是业务模块引用的插件 ID。
rootProject.name = "build-logic"
