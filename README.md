# wuli3-gradle

wuli3 分布式项目脚手架底座。项目使用 JDK 21、Gradle 9.6.0、多模块构建，面向后续 Spring Boot / DDD 业务项目复用。

## 模块

| 模块 | 说明 |
| --- | --- |
| [`build-logic`](guide/build-logic.md) | Gradle 约定插件，统一 Java、Spring、质量检测和发布规则。 |
| [`wuli3-dependencies`](guide/wuli3-dependencies.md) | 统一依赖版本平台。 |
| [`wuli3-core`](guide/wuli3-core.md) | 无 Spring 依赖的错误模型、断言、ID、时间和 Stream 工具。 |
| [`wuli3-json`](guide/wuli3-json.md) | Jackson facade、Mapper 装配、资源路径和脱敏。 |
| [`wuli3-event-core`](guide/wuli3-event-core.md) | 纯 Java 事件信封、发布选项和消息传输契约。 |
| [`wuli3-event-spring-boot-starter`](guide/wuli3-event-spring-boot-starter.md) | Spring LOCAL/REMOTE 事件路由。 |
| [`wuli3-context-propagation`](guide/wuli3-context-propagation.md) | 固定上下文、线程存储、异步快照和出站编码。 |
| [`wuli3-aliyun-spring-boot-starter`](guide/wuli3-aliyun-spring-boot-starter.md) | AliYun OSS SDK V2 多套命名客户端配置。 |
| [`wuli3-configuration-spring-boot-starter`](guide/wuli3-configuration-spring-boot-starter.md) | Jasypt 配置属性加密与解密。 |
| [`wuli3-logging-spring-boot-starter`](guide/wuli3-logging-spring-boot-starter.md) | Spring Boot + Logback 日志格式、结构化输出与滚动归档。 |
| [`wuli3-audit-log-spring-boot-starter`](guide/wuli3-audit-log-spring-boot-starter.md) | 审计协议、上下文补全、写入/查询 Transport 与存储端口。 |
| [`wuli3-opentelemetry-spring-boot-starter`](guide/wuli3-opentelemetry-spring-boot-starter.md) | OpenTelemetry Java Agent 适配、Trace 上下文访问与业务指标记录。 |
| [`wuli3-web-spring-boot-starter`](guide/wuli3-web-spring-boot-starter.md) | Spring MVC 上下文、JSON、统一响应和异常处理。 |
| [`wuli3-mysql-spring-boot-starter`](guide/wuli3-mysql-spring-boot-starter.md) | MyBatis-Plus 依赖聚合。 |
| [`wuli3-redis-spring-boot-starter`](guide/wuli3-redis-spring-boot-starter.md) | Redis String/JSON/Hash/Set 统一操作与分布式锁。 |
| [`wuli3-rocketmq-spring-boot-starter`](guide/wuli3-rocketmq-spring-boot-starter.md) | RocketMQ v4/v5 远程事件发送适配。 |
| [`wuli3-rabbitmq-spring-boot-starter`](guide/wuli3-rabbitmq-spring-boot-starter.md) | RabbitMQ 远程事件发送适配。 |
| [`wuli3-elasticsearch-spring-boot-starter`](guide/wuli3-elasticsearch-spring-boot-starter.md) | Spring Data Elasticsearch 依赖聚合。 |
| [`wuli3-mongodb-spring-boot-starter`](guide/wuli3-mongodb-spring-boot-starter.md) | Spring Data MongoDB 依赖聚合。 |

`integration-tests/` 不是 Gradle 业务模块，而是用于验证发布产物能否被外部 Gradle/Maven 项目正确消费的独立测试工程。

所有模块文档统一从 [`guide/README.md`](guide/README.md) 进入。事件发布边界见
[`wuli3-event-spring-boot-starter`](guide/wuli3-event-spring-boot-starter.md)，RocketMQ 配置见
[`wuli3-rocketmq-spring-boot-starter`](guide/wuli3-rocketmq-spring-boot-starter.md)。

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
[`integration-tests` 使用指南](guide/integration-tests.md)。

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
- 注释使用中文。

## 注释使用说明

注释应说明代码无法直接表达的意图、边界或使用约束，避免重复名称、类型或实现已清晰表达的事实。公共 API、跨模块约定、关键扩展点和容易误用的逻辑应提供必要说明。

### Java 文件头部注释

**必填项：**

- 用一句话说明文件职责或用途。
- 标明作者的 Git 用户名和创建时间；时间格式与现有代码保持一致。

**按需补充：**

- 使用 `注意：` 说明限制、前置条件、兼容性或其他关键边界。
- 存在固定或特殊用法时，说明调用方式和适用场景。

```java
/**
 * {文件职责或用途}。
 *
 * 注意：{限制、前置条件或兼容性说明}。
 * 使用方式：{固定或特殊用法}。
 *
 * @author {git username} create on {yyyy/M/d HH:mm}
 */
```

### 方法注释

**必填项：**

- 用简洁语句说明方法的职责、可观察行为或业务语义。

**按需补充：**

- 使用 `注意：` 说明前置条件、边界、幂等性、线程安全或其他调用约束。
- 当参数名称和类型不足以表达语义时，通过 `@param` 说明关键参数。
- 当返回值存在特殊含义、边界值或空值语义时，通过 `@return` 说明。
- 当调用方需要处理特定异常时，通过 `@throws` 说明触发条件。

```java
/**
 * {方法职责或行为}。
 *
 * 注意：{前置条件、边界或调用约束}。
 *
 * @param parameterName {参数语义}
 * @return {返回值语义}
 * @throws ExceptionType {异常触发条件}
 */
```

### 字段注释

字段名称和类型无法充分表达语义时，必须补充简洁注释；显而易见的普通字段不添加重复说明。

**按需补充：**

- 使用 `注意：` 说明取值范围、单位、生命周期、并发约束或其他关键边界。
- 对配置字段说明默认值、生效条件或与其他配置的关系。

```java
/** 最大重试次数；取值范围为 0 至 3。 */
private final int maxRetryTimes;
```
