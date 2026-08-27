# wuli3-logging-spring-boot-starter 使用指南

该模块为 Spring Boot 3.5 应用提供统一的 Logback 日志策略。它通过 Spring Boot 的标准日志属性设置低优先级默认值，应用已有的 `logging.*` 配置和 `logging.config` 始终优先。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-logging-spring-boot-starter")
}
```

模块显式使用 Spring Boot 默认的 Logback 实现；不应与 Log4j2 替换方案同时使用。

## 默认文本格式

默认只输出控制台日志，格式包含时间、级别、线程、应用名、`requestId`、可选的 `traceId/spanId`、Logger、消息和异常堆栈：

```text
2026-08-14T10:20:30.123+08:00 INFO  [http-nio-8080-exec-1] app=orders requestId=rid-1 traceId=abc spanId=def c.k.example.OrderService - created
```

Web starter 的请求过滤器会将 `requestId` 放入 MDC；OpenTelemetry Java Agent 的 Logback MDC instrumentation
写入 `trace_id` 和 `span_id`，默认日志格式将它们输出为 `traceId` 和 `spanId`。未安装 Agent 或当前没有有效
Span 时字段为空，日志 starter 不会自行生成这些标识。

## 结构化日志

Spring Boot 3.5 自带 ECS、GELF 和 Logstash 格式，不需要额外 JSON 编码器：

```yaml
wuli3:
  logging:
    format: ecs # text、ecs、gelf 或 logstash
```

结构化格式会同时应用于控制台和文件输出，并保留 MDC 上下文。

## 文件归档

容器环境默认不创建本地日志文件。需要归档时显式开启：

```yaml
wuli3:
  logging:
    file:
      enabled: true
      name: logs/${spring.application.name:application}.log
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 5GB
      clean-history-on-start: false
```

归档文件按日期和大小滚动，并以 gzip 压缩。也可以直接使用 Spring Boot 的 `logging.file.*` 和 `logging.logback.rollingpolicy.*` 属性；同名应用配置会覆盖 Wuli3 默认值。

## 自定义文本格式

```yaml
wuli3:
  logging:
    pattern:
      console: "%d{ISO8601} %-5level requestId=%X{requestId} traceId=%X{trace_id} spanId=%X{span_id} %logger{36} - %msg%n%wEx"
      file: "%d{ISO8601} %-5level requestId=%X{requestId} traceId=%X{trace_id} spanId=%X{span_id} %logger{36} - %msg%n%wEx"
```

如果设置了 `logging.config` 指向自定义 Logback 配置文件，starter 不再注入任何默认日志属性。

## 使用边界

- 只负责日志输出策略、格式和本地滚动归档，不负责日志采集、传输或审计存储。
- 不负责生成或传播 `requestId`；请求上下文由 `wuli3-web-spring-boot-starter` 和 `wuli3-context-propagation` 管理。
- 文件归档容量、保留期限和敏感字段脱敏应结合部署环境另行评估，业务代码不得把密码、令牌等秘密写入日志。

## 验证

```bash
./gradlew :wuli3-logging-spring-boot-starter:check
```
