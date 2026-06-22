# wuli3-gradle-build-logic

Gradle 构建逻辑模块，不作为业务依赖发布。

根工程通过 `settings.gradle.kts` 引入：

```kotlin
pluginManagement {
    includeBuild("build-logic")
}
```

`rootProject.name = "wuli3-gradle-build-logic"` 只用于标识这个 included build；业务模块不需要直接引用该名称。

提供约定插件：

- `com.kjs.wuli3.java-conventions`：JDK 21、测试、基础依赖约定。
- `com.kjs.wuli3.quality-conventions`：Checkstyle、Forbidden APIs、Error Prone、NullAway。
- `com.kjs.wuli3.spring-conventions`：Spring Boot starter 模块约定。

普通 Java 模块使用：

```kotlin
plugins {
    id("com.kjs.wuli3.java-conventions")
}
```

Spring Boot starter 模块使用：

```kotlin
plugins {
    id("com.kjs.wuli3.spring-conventions")
}
```

业务模块只应用约定插件，不重复配置质量规则。
