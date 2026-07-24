# wuli3-gradle

wuli3 分布式项目脚手架底座。项目使用 JDK 21、Gradle 9.6.0、多模块构建，面向后续 Spring Boot / DDD 业务项目复用。

## 模块

| 模块 | 说明 |
| --- | --- |
| `build-logic` | Gradle 约定插件，统一 Java、Spring、质量检测规则。 |
| `wuli3-dependencies` | 统一依赖版本平台。 |
| `wuli3-core` | 无 Spring 依赖的基础能力：错误模型、分页、函数式增强、时间工具。 |
| `wuli3-json` | Jackson 对象提供和 JSON 操作支持。 |
| `wuli3-event-core` | 纯 Java 事件信封、发布选项和消息传输契约。 |
| `wuli3-event-spring-boot-starter` | Spring 本地事件发布与 REMOTE 提交后尽力投递编排。 |
| `wuli3-web-spring-boot-starter` | Spring MVC 增强：统一响应、异常处理、MDC、请求 ID。 |
| `wuli3-rocketmq-spring-boot-starter` | 默认 `RocketMQTemplate` 远程事件传输适配与条件自动配置。 |
| 其他数据 starter | MySQL、Redis、Elasticsearch、MongoDB 依赖聚合。 |

`integration-tests/` 不是 Gradle 业务模块，而是用于验证发布产物能否被外部 Gradle/Maven 项目正确消费的独立测试工程。

事件发布的完整边界、事务语义和 RocketMQ 配置见 [`docs/event-publication.md`](docs/event-publication.md)。

## 质量门禁

执行 `check` 会统一运行编译、测试和静态检测：

```bash
./gradlew clean check
```

当前规则：

- 主代码禁止 `java.util.Date`、`Calendar`、`java.sql.Date/Time/Timestamp`。
- 使用 JSpecify 注解表达空安全语义。
- 使用 Error Prone + NullAway 做空指针静态检测。
- 使用 Checkstyle 做基础代码风格检查。

## 常用命令

```bash
./gradlew test
./gradlew check
./gradlew clean check
./gradlew verifyBomConsumers
./gradlew apiCompatibilityCheck
```

## 发布与消费验证

模块内部测试直接使用项目依赖，不能发现 POM、BOM、Gradle Module Metadata 或发布依赖声明错误。根任务
`verifyBomConsumers` 通过两套隔离消费者验证真实发布结果：

1. 将 BOM 和所有公共组件发布到 `build/temporary-maven-repository/`。
2. 使用独立 Gradle user home 运行 `integration-tests/gradle-consumer`。
3. 使用项目内 Maven settings 和独立本地仓库运行 `integration-tests/maven-consumer`。
4. 验证消费者可以通过 BOM 无版本引入组件，并完成编译和最小运行测试。

本地执行：

```bash
./gradlew verifyBomConsumers --warning-mode fail
```

CI 推荐执行顺序：

```bash
./gradlew --no-daemon clean check --continue
./gradlew --no-daemon verifyBomConsumers apiCompatibilityCheck --warning-mode fail
```

CI 环境需要 JDK 21、Maven 和 Maven Central 网络访问。消费验证只使用构建目录中的临时 Maven 仓库，不需要正式仓库凭据。
正式发布必须在消费验证成功后执行，发布凭据只从 CI secret 或环境变量注入。目录结构与扩展规则见
[`integration-tests/README.md`](integration-tests/README.md)。

## Maven 项目使用 BOM

仅需在本机调试 BOM 时，可以发布到 Maven Local：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

该命令只发布 BOM，不代表所有组件均可被外部项目消费；完整发布链路应使用 `verifyBomConsumers` 验证。

Maven 项目引入：

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

## 约定

- 根包名：`com.kjs.wuli3`。
- Java 版本：21。
- Spring Boot 基线：3.5.x。
- 核心模块不依赖 Spring。
- 公共时间类型使用 `java.time`。
