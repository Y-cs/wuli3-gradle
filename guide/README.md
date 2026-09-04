# Wuli3 模块使用指南

本目录集中维护仓库各模块的使用文档。模块目录只保留源码、资源和构建文件，不再放置独立 README 或使用手册。

## 构建与依赖管理

| 文档 | 用途 |
| --- | --- |
| [build-logic](build-logic.md) | Gradle Java、Spring、质量和发布约定插件。 |
| [wuli3-dependencies](wuli3-dependencies.md) | Gradle Platform 与 Maven BOM。 |

## 基础模块

| 文档 | 用途 |
| --- | --- |
| [wuli3-core](wuli3-core.md) | 错误模型、断言、ID、时间和 Stream 工具。 |
| [wuli3-json](wuli3-json.md) | Jackson facade、Mapper 装配、资源路径和脱敏。 |
| [wuli3-context-propagation](wuli3-context-propagation.md) | 固定上下文、线程存储、异步快照和出站编码。 |
| [wuli3-event-core](wuli3-event-core.md) | 事件信封、发布选项和传输端口。 |

## Spring Boot Starter

| 文档 | 用途 |
| --- | --- |
| [wuli3-event-spring-boot-starter](wuli3-event-spring-boot-starter.md) | LOCAL/REMOTE 事件路由和事务提交后发布。 |
| [wuli3-core-spring-boot-starter](wuli3-core-spring-boot-starter.md) | 日志、配置加密与分阶段优雅关闭基础设施。 |
| [wuli3-aliyun-spring-boot-starter](wuli3-aliyun-spring-boot-starter.md) | AliYun OSS SDK V2 多套命名客户端配置。 |
| [wuli3-audit-log-spring-boot-starter](wuli3-audit-log-spring-boot-starter.md) | 审计协议、上下文补全、事件写入与存储端口。 |
| [wuli3-opentelemetry-spring-boot-starter](wuli3-opentelemetry-spring-boot-starter.md) | OpenTelemetry Java Agent 适配、Trace 上下文访问与业务指标记录。 |
| [wuli3-web-spring-boot-starter](wuli3-web-spring-boot-starter.md) | Spring MVC 上下文、JSON、统一响应与异常处理。 |
| [wuli3-dubbo-spring-boot-starter](wuli3-dubbo-spring-boot-starter.md) | Dubbo 调用上下文与协议无关错误传播。 |
| [wuli3-mysql-spring-boot-starter](wuli3-mysql-spring-boot-starter.md) | MyBatis-Plus 依赖聚合。 |
| [wuli3-redis-spring-boot-starter](wuli3-redis-spring-boot-starter.md) | Redis String/JSON/Hash/Set 统一操作与分布式锁。 |
| [wuli3-rocketmq-spring-boot-starter](wuli3-rocketmq-spring-boot-starter.md) | RocketMQ 远程事件发送适配。 |
| [wuli3-rabbitmq-spring-boot-starter](wuli3-rabbitmq-spring-boot-starter.md) | RabbitMQ 远程事件发送适配。 |
| [wuli3-elasticsearch-spring-boot-starter](wuli3-elasticsearch-spring-boot-starter.md) | Spring Data Elasticsearch 依赖聚合。 |
| [wuli3-mongodb-spring-boot-starter](wuli3-mongodb-spring-boot-starter.md) | Spring Data MongoDB 依赖聚合。 |

## 发布验证

| 文档 | 用途 |
| --- | --- |
| [integration-tests](integration-tests.md) | 使用独立 Gradle/Maven 工程验证 BOM 和发布元数据。 |

## 外部项目引入约定

Gradle 项目推荐先导入 BOM，再声明无版本模块依赖：

```kotlin
dependencies {
    implementation(platform("com.kjs.wuli3:wuli3-dependencies:0.1.0-SNAPSHOT"))
    implementation("com.kjs.wuli3:wuli3-core")
}
```

Maven 项目在 `dependencyManagement` 中导入 `wuli3-dependencies`，然后在 `dependencies` 中声明模块。完整示例见
[wuli3-dependencies](wuli3-dependencies.md)。

文档中的版本用于当前仓库快照。升级版本时，应同时更新 BOM、消费验证 fixture 和受影响的指南。
