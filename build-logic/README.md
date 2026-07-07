# wuli3-gradle-build-logic

Gradle 构建逻辑模块，提供可复用、可发布的约定插件。

根工程通过 `settings.gradle.kts` 引入：

```kotlin
pluginManagement {
    includeBuild("build-logic")
}
```

included build 名称只用于本仓库识别；业务模块不需要直接引用该名称。

提供约定插件：

- `com.kjs.wuli3.java-conventions`：JDK 21、测试、基础依赖约定。
- `com.kjs.wuli3.quality-conventions`：Spotless、Palantir Java Format、Checkstyle、SpotBugs、Forbidden APIs、Error Prone、NullAway。
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

## 业务项目使用

发布到 Maven 仓库后，业务 Gradle 项目可以通过插件版本使用：

```kotlin
pluginManagement {
    repositories {
        maven("https://maven.example.com/repository/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}
```

```kotlin
plugins {
    id("com.kjs.wuli3.java-conventions") version "0.1.0-SNAPSHOT"
}
```

默认会导入 `com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT` 作为 BOM，并启用 Spotless、Palantir Java Format、Checkstyle、SpotBugs、Forbidden APIs、Error Prone、NullAway 和 JaCoCo 覆盖率门禁。
业务项目可在 `gradle.properties` 覆盖默认值：

```properties
wuli3.conventions.bom-coordinates=com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT
wuli3.conventions.java-version=21
wuli3.conventions.spotless.enabled=true
wuli3.conventions.palantir-java-format.version=2.94.0
wuli3.conventions.nullaway.annotated-packages=com.example.service
wuli3.conventions.nullaway.enabled=true
wuli3.conventions.spotbugs.enabled=true
wuli3.conventions.forbidden-apis.enabled=true
wuli3.conventions.forbidden-apis.test-enabled=false
wuli3.conventions.jacoco.verification.enabled=true
wuli3.conventions.jacoco.line.minimum=0.45
```

Spotless 提供 `spotlessCheck` 和 `spotlessApply`，Java 格式化规则使用 Palantir Java Format。JaCoCo 默认按模块整体 `LINE`
覆盖率做门禁，并生成 HTML 和 XML 报告；XML 报告可供 CI、SonarQube 或 diff coverage 工具使用，但本约定插件本身不计算
提交级覆盖率。

本仓库通过以下属性继续使用本地 BOM 项目：

```properties
wuli3.conventions.use-project-bom=true
wuli3.conventions.project-bom-path=:wuli3-dependencies
```

## 发布到 Maven 仓库

本模块可以作为 Gradle 插件发布。默认坐标：

```text
com.kjs.wuli3:build-logic:0.1.0-SNAPSHOT
```

默认版本配置在 `build-logic/gradle.properties`：

```properties
wuli3.build-logic.version=0.1.0-SNAPSHOT
```

发布到本地 Maven 仓库：

```bash
./gradlew -p build-logic publishToMavenLocal
```

发布到公司 Maven 仓库：

```bash
./gradlew -p build-logic publish \
  -PcompanyMavenSnapshotsUrl=https://maven.example.com/repository/snapshots \
  -PcompanyMavenReleasesUrl=https://maven.example.com/repository/releases \
  -PcompanyMavenUsername=your-username \
  -PcompanyMavenPassword=your-password
```

也可以使用环境变量提供凭据：

```bash
COMPANY_MAVEN_USERNAME=your-username COMPANY_MAVEN_PASSWORD=your-password ./gradlew -p build-logic publish
```

Maven 项目不能直接使用 Gradle 插件。后续如需统一 Maven 构建规则，应单独提供 parent POM。
