# wuli3-gradle

wuli3 分布式项目脚手架底座。项目使用 JDK 21、Gradle 9.6.0、多模块构建，面向后续 Spring Boot / DDD 业务项目复用。

## 模块

| 模块 | 说明 |
| --- | --- |
| `build-logic` | Gradle 约定插件，统一 Java、Spring、质量检测规则。 |
| `wuli3-dependencies` | 统一依赖版本平台。 |
| `wuli3-core` | 无 Spring 依赖的基础能力：错误模型、分页、函数式增强、时间工具。 |
| `wuli3-json` | Jackson 对象提供和 JSON 操作支持。 |
| `wuli3-event-core` | DDD 事件抽象，区分领域事件和集成事件。 |
| `wuli3-event-inmemory` | 单服务内存异步事件总线。 |
| `wuli3-web-spring-boot-starter` | Spring MVC 增强：统一响应、异常处理、MDC、请求 ID。 |
| `wuli3-*-spring-boot-starter` | MySQL、Redis、RocketMQ、Elasticsearch、MongoDB starter 壳。 |

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
./gradlew :wuli3-dependencies:publishToMavenLocal
```

## Maven 项目使用 BOM

先发布到本地 Maven 仓库：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

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
