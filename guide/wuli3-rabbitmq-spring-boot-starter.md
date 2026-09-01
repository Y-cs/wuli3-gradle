# wuli3-rabbitmq-spring-boot-starter 使用指南

该 starter 为事件模块提供基于 RabbitMQ 的 `RemoteEventTransport` 发送实现，并提供可手动使用的消息上下文恢复支持。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-rabbitmq-spring-boot-starter")
}
```

模块会传递 `wuli3-event-spring-boot-starter`、`wuli3-context-propagation` 和 Spring Boot AMQP starter。

## RabbitMQ 配置

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

应用或基础设施需要自行声明与每个 `EventEnvelope.topic` 同名的 exchange，以及对应的 queue 和 binding；本 starter 不创建或修改 RabbitMQ 拓扑。

## 发布远程事件

```java
final EventEnvelope<OrderPaid> envelope =
        EventEnvelopeTemplate.of("orders", "order.paid.v1").wrap(payload);

final RabbitPublishOptions options = new RabbitPublishOptions()
        .withAfterCommit();

eventPublisher.publish(options, envelope);
```

传输使用 `EventEnvelope.topic` 作为 exchange，`EventEnvelope.eventType` 作为 routing key。事件信封仍以 JSON 作为 body，`eventId` 和 `eventType` 分别写入 AMQP 的 `messageId` 和 `type` 属性。

`RabbitPublishOptions` 支持 `withAsync()` 和 `withAfterCommit()`：

- `withAsync()` 在调用线程编码事件和上下文后，使用事件模块的 `applicationTaskExecutor` 发送。
- `withAfterCommit()` 在当前 Spring 事务存在时于提交后发送。
异步任务启动失败会抛出 `SendFailedException`；已调度的后台发送失败会记录日志。该模块不将异步返回等同于 publisher confirm，也不实现 Outbox、重试、死信、延迟或顺序消息语义。

## 上下文传播

编码器读取可选的 `ContextReader`，并通过 `ContextPropagator` 按显式白名单将传播字段写入 AMQP headers。`EventEnvelope` 不承载传输 header，其 JSON body 只包含事件语义字段。

默认自动配置使用 `ContextEncoder.standardContextEncoder()`，当前会传播 `X-Request-Id`、`X-Origin-Ip`、
`X-Principal-Type`、`X-Principal-Id` 和 `X-Principal-Name`。因此该 starter 应只用于允许传播认证信息的可信消息边界。

若边界只允许传播调用标识，可显式覆盖 `ContextPropagator` Bean。同一个 `ContextPropagator` Bean 同时决定出站字段和入站可恢复字段。

消费适配器需要先得到已解码的 `ContextProxy`，再显式恢复作用域：

```java
final ContextPropagator propagator =
        rabbitContextSupport.restoreFrom(message.getMessageProperties().getHeaders());
try (ContextScope ignored = propagator.restore(propagator.capture())) {
    listener.handle(message);
}
```

`restoreFrom` 不会自动注册或包装 RabbitMQ Listener。实际 Listener 仍应根据消息来源、线程模型、重试和死信策略决定调用时机。

## 验证

```bash
./gradlew :wuli3-rabbitmq-spring-boot-starter:test
./gradlew :wuli3-rabbitmq-spring-boot-starter:check
```
