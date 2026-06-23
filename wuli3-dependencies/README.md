# wuli3-dependencies

`wuli3-dependencies` 是 wuli3 项目的统一依赖版本平台，基于 Gradle `java-platform` 插件构建，并通过 `maven-publish` 发布为 Maven BOM。

它可以作为全项目的依赖管理项目使用。推荐定位是：统一管理第三方库版本、导入外部 BOM、为 Gradle 和 Maven 项目提供同一套依赖版本基线。

## 适用边界

适合放在这里：

- Spring Boot、MyBatis Plus 等外部 BOM。
- 各模块共同使用的第三方依赖版本。
- 编译、测试、注解处理、静态分析工具等库版本。
- starter 模块和业务模块需要共享的依赖版本约束。

不建议放在这里：

- Gradle 插件版本。插件版本应继续放在 `settings.gradle.kts` 或约定插件里管理。
- 仓库地址。仓库地址应放在 `settings.gradle.kts` 的 `dependencyResolutionManagement` 中。
- 模块自己的私有依赖选择。BOM 管版本，具体是否依赖某个库仍由模块自己声明。
- 业务配置、运行时参数、Spring 配置项。

## 当前坐标

当前项目根构建统一设置了 group 和 version，因此发布坐标为：

```text
com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT
```

如果根项目版本变化，BOM 版本也会一起变化。

## 当前管理内容

当前导入的外部 BOM：

- `org.springframework.boot:spring-boot-dependencies:3.5.15`
- `com.baomidou:mybatis-plus-bom:3.5.16`

当前约束的依赖版本：

- `org.jspecify:jspecify:1.0.0`
- `com.google.errorprone:error_prone_core:2.50.0`
- `com.uber.nullaway:nullaway:0.13.7`
- `com.puppycrawl.tools:checkstyle:13.6.0`
- `org.redisson:redisson-spring-boot-starter:4.6.1`
- `com.google.guava:guava:33.6.0-jre`
- `cn.hutool:hutool-all:5.8.46`
- `org.apache.commons:commons-lang3:3.20.0`
- `org.apache.commons:commons-collections4:4.5.0`
- `commons-io:commons-io:2.22.0`
- `org.apache.commons:commons-text:1.15.0`
- `it.unimi.dsi:fastutil:8.5.18`
- `org.projectlombok:lombok:1.18.46`
- `org.junit.jupiter:junit-jupiter:5.12.2`
- `org.assertj:assertj-core:3.27.3`
- `org.apache.rocketmq:rocketmq-spring-boot-starter:2.3.4`

## Gradle 项目使用

### 本仓库子模块

本仓库的 Java 子模块已经通过 `build-logic` 中的 `com.kjs.wuli3` Java 约定插件自动接入：

```kotlin
dependencies {
    implementation(platform(project(":wuli3-dependencies")))
    testImplementation(platform(project(":wuli3-dependencies")))
    annotationProcessor(platform(project(":wuli3-dependencies")))
    testAnnotationProcessor(platform(project(":wuli3-dependencies")))
}
```

所以本仓库普通子模块一般不需要重复声明 platform，只需要直接声明依赖且省略版本：

```kotlin
dependencies {
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.redisson:redisson-spring-boot-starter")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
}
```

### 本仓库内手动接入

如果某个 Gradle 子项目没有使用现有 Java 约定插件，可以手动接入项目 platform：

```kotlin
dependencies {
    implementation(platform(project(":wuli3-dependencies")))

    implementation("cn.hutool:hutool-all")
    implementation("org.redisson:redisson-spring-boot-starter")
}
```

需要更严格地强制使用 BOM 版本时，可以使用 `enforcedPlatform`：

```kotlin
dependencies {
    implementation(enforcedPlatform(project(":wuli3-dependencies")))
}
```

`enforcedPlatform` 会提高版本约束强度，适合全公司或全项目强制统一版本时使用。普通模块优先使用 `platform`。

### 外部 Gradle 项目接入

先发布 BOM 到本地 Maven 仓库：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

外部 Gradle 项目接入：

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"))

    implementation("com.google.guava:guava")
    implementation("it.unimi.dsi:fastutil")
    implementation("org.redisson:redisson-spring-boot-starter")
    testImplementation("org.junit.jupiter:junit-jupiter")
}
```

使用 Lombok 时，编译期依赖和注解处理器都可以省略版本：

```kotlin
dependencies {
    implementation(platform("com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"))
    annotationProcessor(platform("com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
```

如果发布到了公司 Maven 仓库，把 `mavenLocal()` 换成公司仓库地址即可：

```kotlin
repositories {
    maven("https://maven.example.com/repository/releases")
    mavenCentral()
}
```

## Maven 项目使用

先发布 BOM 到本地 Maven 仓库：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

Maven 项目在 `dependencyManagement` 中导入 BOM：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.kjs.wuli3</groupId>
      <artifactId>wuli3-dependencies</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

然后业务依赖可以省略版本：

```xml
<dependencies>
  <dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
  </dependency>

  <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
  </dependency>

  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
  </dependency>

  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

如果 Maven 项目从本地仓库读取，不需要额外配置。Maven 默认会读取 `~/.m2/repository`。

如果 BOM 发布到了公司 Maven 仓库，需要在 `pom.xml` 或 Maven `settings.xml` 中配置仓库：

```xml
<repositories>
  <repository>
    <id>company-maven</id>
    <url>https://maven.example.com/repository/releases</url>
  </repository>
</repositories>
```

## 维护方式

新增统一版本时，在 `build.gradle.kts` 的 `constraints` 中增加约束：

```kotlin
dependencies {
    constraints {
        api("group:name:version")
    }
}
```

导入外部 BOM 时，在 `dependencies` 中增加 `api(platform(...))`：

```kotlin
dependencies {
    api(platform("group:bom-name:version"))
}
```

建议规则：

- 优先通过外部成熟 BOM 管理大生态版本，例如 Spring Boot BOM。
- 只有多个模块共用、或希望全项目统一的依赖，才加入 `constraints`。
- 模块内只使用一次的依赖，可以先在模块内声明版本，确认复用后再沉淀到 BOM。
- 升级版本时优先单独提交，并运行全量测试。

## 验证命令

发布到本地 Maven 仓库：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

检查本模块：

```bash
./gradlew :wuli3-dependencies:check
```

检查全项目：

```bash
./gradlew clean check
```

查看某个 Gradle 子项目的依赖解析结果：

```bash
./gradlew :wuli3-redis-spring-boot-starter:dependencyInsight --dependency redisson --configuration runtimeClasspath
```
