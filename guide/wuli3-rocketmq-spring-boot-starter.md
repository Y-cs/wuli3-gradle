# wuli3-rocketmq-spring-boot-starter 使用指南

该 starter 在存在 `RocketMQTemplate` 时，为事件模块提供 `RemoteEventMessageTransport` 发送实现。当前只实现生产者链路。

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

Encoder 直接读取可选的 `ContextReader`，并使用固定上下文编码器写入消息 header；不复用 Web 层策略，也不创建通用传播器 Bean。

默认只传播调用链信息：

```properties
wuli3.rocketmq.event.context-mode=INVOCATION_ONLY
```

仅当生产者和消费者之间存在可信内部身份边界时，才能显式传播认证信息：

```properties
wuli3.rocketmq.event.context-mode=TRUSTED_INTERNAL
```

`TRUSTED_INTERNAL` 会额外传播 `X-User-Id` 和 `X-Username`。消息中的保留传播字段不能由业务 Header
覆盖，Encoder 会使用当前上下文重新生成这些字段。

当前 starter 只实现发送端，没有通用的消息入站上下文恢复 API。未来实现 Listener 时，应基于实际消息消费、认证和线程模型，在 Listener 适配器内明确定义字段校验与上下文生命周期。

## 投递边界

- `afterCommit` 是提交后尽力发送，不是 Outbox 或可靠消息。
- 异步发送失败由 transport 记录日志，不会回滚已经提交的业务事务。
- 业务 header 不能覆盖四个保留上下文字段；编码器会先删除再按当前上下文重建。
- 消费端的幂等、重试、死信和上下文恢复不在当前模块范围内。

## 验证

```bash
./gradlew :wuli3-rocketmq-spring-boot-starter:test
./gradlew :wuli3-rocketmq-spring-boot-starter:check
```
