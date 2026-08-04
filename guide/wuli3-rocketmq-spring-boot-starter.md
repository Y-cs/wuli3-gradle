# wuli3-rocketmq-spring-boot-starter 使用指南

该 starter 在存在 `RocketMQTemplate` 时，为事件模块提供 `RemoteEventTransport` 发送实现，并提供可手动使用的消息上下文恢复支持。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter")
}
```

模块会传递 `wuli3-event-spring-boot-starter`、`wuli3-context-propagation` 和 RocketMQ Spring Boot starter。

## RocketMQ 配置

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: order-service
```

具体连接、认证和 producer 参数由 RocketMQ Spring Boot starter 管理。没有 `RocketMQTemplate` 时，本模块自动配置不生效。

## 发布远程事件

```java
final EventEnvelope<OrderPaid> envelope =
        EventEnvelopeTemplate.of("orders", "order.paid.v1").wrap(payload);

final PublishOptions options =
        new PublishOptions(PublishOptions.Channel.REMOTE).afterCommit();

eventPublisher.publish(envelope, options);
```

支持同步、异步、顺序和精确延迟发送，但精确延迟不能与 `async` 或 order key 组合。编码器要求：

- channel 必须是 `REMOTE`。
- topic 长度不超过 127 字节。
- topic 只能包含字母、数字、`%`、`-` 和 `_`。
- 非正数 delay 会被拒绝。

## 上下文传播

Encoder 读取可选的 `ContextReader`，并通过 `ContextEncoder` 按显式白名单把传播字段写入 `RocketMessageWrapper.headers`。`EventEnvelope` 不承载传输 header，其 JSON body 只包含事件语义字段。

默认自动配置使用 `ContextEncoder.standardContextEncoder()`，当前会传播 `X-Request-Id`、`X-Origin-Ip`、`X-User-Id` 和 `X-Username`。因此该 starter 应只用于允许传播认证信息的可信消息边界。

若边界只允许传播调用标识，应显式覆盖该 Bean：

```java
@Bean
ContextEncoder rocketMqContextEncoder() {
    return new ContextEncoder(List.of(new InvocationContextEncoder()));
}
```

同一个 `ContextEncoder` Bean 同时决定 `RocketMessageWrapperEncoder` 的出站字段和 `RocketContextSupport` 的入站字段。缩小白名单后，入站恢复也只会接受对应字段。

消费适配器需要明确控制上下文作用域：

```java
try (ContextScope ignored = rocketMqContextSupport.restoreFrom(messageExt.getProperties())) {
    listener.handle(envelope);
}
```

`restoreFrom` 只恢复字段编码器识别出的上下文，不会自动注册或包裹 RocketMQ Listener。实际 Listener 仍应根据消息来源、线程模型、重试和死信策略决定调用时机。非法认证字段由 `AuthContextEncoder` 忽略，避免消费适配器承担解析细节。

## 投递边界

- `afterCommit` 是提交后尽力发送，不是 Outbox 或可靠消息。
- 异步发送失败由 transport 记录日志，不会回滚已经提交的业务事务。
- 传播 header 属于 RocketMQ 消息属性，不进入 `EventEnvelope` 的 JSON body。
- 消费端的自动 Listener、幂等、重试和死信不在当前模块范围内；上下文恢复通过 `RocketContextSupport` 显式完成。

## 验证

```bash
./gradlew :wuli3-rocketmq-spring-boot-starter:test
./gradlew :wuli3-rocketmq-spring-boot-starter:check
```
