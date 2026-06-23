# Repository Guidelines

## 项目结构与模块组织

本仓库是 JDK 21 + Gradle 9.6.0 的多模块 Java 项目。根目录的 `settings.gradle.kts` 声明所有模块，`build-logic/` 提供统一 Gradle 约定插件。业务源码位于各模块的 `src/main/java`，测试位于 `src/test/java`，Spring Boot 自动配置文件位于 `src/main/resources/META-INF/spring/`。

主要模块包括：

- `wuli3-dependencies`：统一依赖版本平台，发布为 Maven BOM。
- `wuli3-core`、`wuli3-json`：基础能力与 JSON 支持。
- `wuli3-event-*`：事件抽象与内存实现。
- `wuli3-*-spring-boot-starter`：Web、MySQL、Redis、RocketMQ、Elasticsearch、MongoDB starter。

## 构建、测试与开发命令

常用命令：

```bash
./gradlew test
./gradlew check
./gradlew clean check
./gradlew :wuli3-dependencies:publishToMavenLocal
```

`test` 运行 JUnit 测试；`check` 会执行编译、测试、Checkstyle、Forbidden APIs、Error Prone 和 NullAway；`clean check` 用于提交前全量验证；`publishToMavenLocal` 将 BOM 发布到本地 Maven 仓库，便于 Maven 或外部 Gradle 项目验证。

## 编码风格与命名约定

Java 包名使用 `com.kjs.wuli3` 前缀。模块名使用 `wuli3-*`，Spring starter 模块使用 `wuli3-*-spring-boot-starter`。生产代码避免使用 `java.util.Date`、`Calendar`、`java.sql.Date/Time/Timestamp`，优先使用 `java.time`。使用 JSpecify 表达空安全语义，NullAway 会对 `com.kjs.wuli3` 包执行静态检查。

普通 Java 模块使用 `com.kjs.wuli3.java-conventions`，Spring starter 模块使用 `com.kjs.wuli3.spring-conventions`，不要在模块中重复配置公共质量规则。

## 测试指南

测试框架为 JUnit Jupiter，断言库为 AssertJ。测试类放在对应模块的 `src/test/java` 下，命名建议使用 `*Test`，例如 `TimeFormattersTest`、`RedisAutoConfigurationTest`。新增公共 API、自动配置、错误处理和事件行为时，应补充对应单元测试或自动配置加载测试。

## 提交与 Pull Request 规范

提交历史使用简短 Conventional Commit 风格，例如 `feat: 增加时间格式化支持`、`chore: 修正依赖 BOM 名称`。建议继续使用 `feat:`、`fix:`、`chore:`、`test:`、`docs:` 等前缀。

PR 应说明变更目的、影响模块、验证命令和结果。涉及依赖升级时，说明升级原因与兼容性影响；涉及 starter 行为时，说明自动配置触发条件和测试覆盖。

## 配置与安全提示

仓库地址统一配置在 `settings.gradle.kts` 的 `dependencyResolutionManagement` 中。依赖版本优先沉淀到 `wuli3-dependencies`，插件版本和构建约定放在 `build-logic`。不要提交本地路径、私有仓库凭据或 IDE 私有配置。

## Agent 工作约定

AI/Agent 创建或修改并确认需要纳入版本管理的文件，应执行 `git add <path>` 暂存。暂存时只处理本次 Agent 产生的文件，不要顺手暂存用户已有的无关改动。
