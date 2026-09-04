# wuli3-core-spring-boot-starter 使用指南

`wuli3-core-spring-boot-starter` 是 Wuli3 Spring Boot 模块共享的基础设施，统一提供日志策略、Jasypt 配置加密依赖和应用关闭协调能力。

## 1. 引入模块

应用直接引入 starter：

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-core-spring-boot-starter")
}
```

其他 Wuli3 Spring Boot starter 通过 `api` 依赖它，使应用只需声明一个基础 starter：

```kotlin
dependencies {
    api(project(":wuli3-core-spring-boot-starter"))
}
```

模块本身依赖 `wuli3-core`、`wuli3-context-propagation`、Spring Boot starter 和
`jasypt-spring-boot-starter`。它不负责生成请求 ID、采集日志，也不提供消息可靠投递或崩溃恢复。

## 2. 自动配置边界

模块通过 Spring Boot 自动配置导入以下组件：

| 自动配置 | 生效条件 | 职责 |
| --- | --- | --- |
| `CoreSpringBootAutoConfiguration` | Spring Boot 应用 | 标记公共基础设施边界。 |
| `LoggingAutoConfiguration` | classpath 存在 Logback，且 `wuli3.logging.enabled` 未关闭 | 注册日志属性。 |
| `GracefulShutdownAutoConfiguration` | `wuli3.spring.shutdown.enabled` 未关闭 | 注册关闭钩子注册表、阶段执行器、协调器、注解钩子自动注册器、日志增强和进程内度量。 |

如果应用显式设置 `logging.config`，日志环境处理器不会注入 Wuli3 默认日志属性；应用自己的 `logging.*` 配置始终优先。

## 3. 日志策略

### 3.1 默认行为

日志配置前缀为 `wuli3.logging`。默认使用文本格式、只输出控制台，不启用本地日志文件。默认文本格式包含：

- 时间、级别和线程名；
- `spring.application.name`（未设置时为 `application`）；
- MDC 中的 `requestId`、`trace_id` 和 `span_id`；
- Logger、消息和异常堆栈。

`requestId` 由 Web/上下文模块负责写入，OpenTelemetry Agent 可写入 `trace_id` 和 `span_id`；当前没有对应上下文时字段为空，日志模块不会自行生成这些标识。

### 3.2 常用配置

```yaml
wuli3:
  logging:
    enabled: true
    # text、ecs、gelf、logstash
    format: text
    pattern:
      console: "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] app=${spring.application.name:application} requestId=%X{requestId} traceId=%X{trace_id} spanId=%X{span_id} %logger{36} - %msg%n%wEx"
      file: "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] app=${spring.application.name:application} requestId=%X{requestId} traceId=%X{trace_id} spanId=%X{span_id} %logger{36} - %msg%n%wEx"
    file:
      enabled: false
      name: logs/${spring.application.name:application}.log
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 5GB
      clean-history-on-start: false
      file-name-pattern: "${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz"
```

支持的 `format` 值：

- `text`：可读文本格式，默认值；
- `ecs`：Elastic Common Schema；
- `gelf`：Graylog Extended Log Format；
- `logstash`：Logstash 兼容格式。

结构化格式由 Spring Boot 3.5 的标准属性驱动，不需要额外 JSON 编码器。文件输出开启后，Wuli3 会设置 Spring Boot 的滚动归档默认值；也可以直接使用 `logging.file.*` 和 `logging.logback.rollingpolicy.*` 覆盖它们。

### 3.3 使用边界

- `wuli3.logging.enabled=false` 会关闭 Wuli3 日志默认属性注入。
- `logging.config` 指向自定义 Logback 配置时，应用完全接管日志配置。
- 日志模块只负责输出策略和本地滚动归档，不负责采集、传输、审计存储或敏感信息脱敏。
- 密码、令牌和密钥不得写入日志；文件归档容量和保留周期应结合部署环境设置。

## 4. 配置加密

starter 聚合 `jasypt-spring-boot-starter`，保留 Jasypt 的标准配置前缀和自动配置行为。将密文用 `ENC(...)` 包裹：

```yaml
spring:
  datasource:
    username: app
    password: ENC(cipher-text)

jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
```

主密码必须从环境变量、容器 Secret 或其他外部密钥管理系统注入，不要提交到源码、配置文件或镜像层。只有在确认已有密文格式兼容时，才覆盖 Jasypt 的算法、盐和输出格式；修改这些参数会使已有密文无法解密。

该模块不保存主密码，不提供运行时密钥轮换，也不提供业务密文生成 API。

## 5. 分阶段优雅关闭

### 5.1 启用方式

Wuli3 关闭协调器是 Spring `SmartLifecycle` 组件。Web 应用应同时启用 Spring Boot 的 Web Server 优雅关闭：

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 45s

wuli3:
  spring:
    shutdown:
      enabled: true
      phase-timeout: 30s
      phases:
        drain-async-tasks:
          timeout: 30s
        await-remote-ack:
          timeout: 10s
        close-clients:
          timeout: 5s
```

`enabled` 默认为 `true`。`phase-timeout` 是所有阶段的默认预算；`phases.<阶段>.timeout` 可覆盖单个阶段。所有时长必须为正数，支持 Spring Boot 的 `ms`、`s`、`m` 和 `h` 单位。

### 5.2 阶段和执行保证

阶段按以下顺序执行：

| 阶段 | 典型职责 |
| --- | --- |
| `DRAIN_ASYNC_TASKS` | 停止接收新异步任务，排空线程池和异步事件。 |
| `AWAIT_REMOTE_ACK` | 等待 MQ confirm、callback、Future 或其他远程确认。 |
| `CLOSE_CLIENTS` | 关闭由业务钩子管理的数据库、缓存、HTTP、MQ 等外部客户端。 |

服务摘除、入口流量治理和 Web Server graceful shutdown 由服务发现、Web 模块和 Spring Boot
自身负责，不建模为 Wuli3 的内部阶段。Wuli3 只编排自身能够观察并在预算内等待的异步工作和客户端资源。

协调器在容器关闭时关闭注册表，然后按枚举顺序执行全部阶段。阶段和钩子在同一关闭线程中顺序执行；钩子必须自行使用 `ShutdownContext.remaining()` 约束等待时间，协调器不会强制中断忽略预算的阻塞调用。单个钩子抛出运行时异常会被记录，不会阻止同阶段后续钩子。线程中断则停止该阶段剩余钩子，但仍会继续后续阶段。

### 5.3 注册关闭钩子

推荐使用 `@RegisterShutdownHook` 声明阶段和优先级。组件只需是 Spring Bean 并实现 `ShutdownHook`，启动时会由自动注册器加入注册表：

```java
@Component
@RegisterShutdownHook(phase = ShutdownPhase.CLOSE_CLIENTS, priority = 100)
final class ClientShutdownHook implements ShutdownHook {

    private final MyClient client;

    ClientShutdownHook(final MyClient client) {
        this.client = client;
    }

    @Override
    public void shutdown(final ShutdownContext context) throws InterruptedException {
        // 使用 context.remaining() 限制等待时间，并响应线程中断。
        client.close(context.remaining());
    }

    @Override
    public String name() {
        return "myClient";
    }
}
```

注册规则如下：

- 同一阶段内，优先级数值越小越先执行；
- 相同优先级按注册顺序执行；
- 关闭开始后注册表会关闭，继续注册会抛出 `IllegalStateException`；
- `ShutdownHook#name()` 用于输出稳定的钩子日志，匿名实现建议显式覆盖。

需要动态决定阶段或优先级时，才直接使用 `ShutdownHookRegistry#register`；普通模块不应在
构造函数、`@PostConstruct` 或自动配置方法中自行注册。

钩子应尽量幂等、使用带超时的 IO、定期检查 `ShutdownContext.remaining()`，并正确处理 `InterruptedException`。阶段在同一关闭线程中顺序执行；钩子应自行遵守上下文剩余时间，避免阻塞后续阶段。

### 5.4 已接入的模块

- 其他 starter 暂未接入 Wuli3 shutdown hook；需要接入时由模块自行提供 `ShutdownHook` Bean；
- `wuli3-web-spring-boot-starter` 配合 Spring Boot 的 `server.shutdown=graceful` 完成 Web Server 关闭。

### 5.5 日志和度量

关闭基础设施默认记录阶段开始/完成、钩子耗时和失败。当前不内置指标存储或 Actuator endpoint；需要外部监控时，应由应用或监控适配层自行记录和导出数据。

## 6. 部署边界与故障排查

优雅关闭只覆盖 JVM 能响应正常关闭信号（通常为 `SIGTERM`）的场景，不覆盖 `kill -9`、节点宕机、掉电、OOM 或网络隔离。关键消息不能只依赖关闭窗口保证不丢失，仍需使用持久化 Outbox、事务性消息或远端持久化确认。

Kubernetes 的 `terminationGracePeriodSeconds` 应覆盖各阶段预算之和，并额外预留服务摘除传播时间：

```yaml
spec:
  terminationGracePeriodSeconds: 60
  containers:
    - name: app
      lifecycle:
        preStop:
          exec:
            command: ["/bin/sh", "-c", "sleep 5"]
```

出现关闭超时时，按以下顺序排查：

1. 查看阶段和钩子日志，定位耗时或失败的组件；
2. 检查钩子是否在所有完成、异常和中断分支中及时返回；
3. 检查钩子是否存在无界等待、未处理的中断或关闭期间重新提交任务；
4. 根据实际工作量调整对应阶段预算，并同步调整容器终止宽限期。

## 7. 从旧 starter 迁移

日志和配置加密能力已经并入 `wuli3-core-spring-boot-starter`。新项目直接依赖该模块；原有配置通常无需修改：

- `wuli3.logging.*` 继续有效；
- `jasypt.*` 继续由 Jasypt 自动配置处理；
- 使用旧 logging/configuration artifact 的项目应改为依赖 core starter，并移除对已删除模块的直接声明。

## 8. 验证

```bash
./gradlew :wuli3-core-spring-boot-starter:check
```

相关模块指南：

- [wuli3-event-spring-boot-starter](wuli3-event-spring-boot-starter.md)
- [wuli3-rabbitmq-spring-boot-starter](wuli3-rabbitmq-spring-boot-starter.md)
- [wuli3-rocketmq-spring-boot-starter](wuli3-rocketmq-spring-boot-starter.md)
- [wuli3-web-spring-boot-starter](wuli3-web-spring-boot-starter.md)
