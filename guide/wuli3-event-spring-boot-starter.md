# wuli3-event-spring-boot-starter 使用指南

该 starter 为 `wuli3-event-core` 提供 Spring Boot 自动配置：默认 LOCAL 事件使用 Spring
`ApplicationEventPublisher`，REMOTE 事件委托给 `RemoteEventMessageTransport`。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-event-spring-boot-starter")
}
```

自动配置会在缺少用户实现时创建：

- `SpringLocalEventMessageTransport`
- 远程占位 transport
- `EventPublisher`

所有 Bean 都允许应用通过同类型 Bean 替换。

## 本地发布

```java
final EventEnvelope<OrderPaid> envelope =
        EventEnvelopeTemplate.of("orders", "order.paid.v1").wrap(payload);

eventPublisher.publish(envelope);
```

Spring 发布的是整个 `EventEnvelope`，监听端应读取其 `eventType` 和 `payload`：

```java
@EventListener
void onEvent(final EventEnvelope<?> envelope) {
    if ("order.paid.v1".equals(envelope.eventType())) {
        final OrderPaid event = (OrderPaid) envelope.payload();
        // 处理事件
    }
}
```

LOCAL 不支持 `async`、delay 或 order key。需要 Spring 异步监听时，由应用明确配置 Spring 事件监听器的执行模型。

## 事务提交后发布

```java
final PublishOptions options = PublishOptions.defaults().afterCommit();
eventPublisher.publish(envelope, options);
```

当 Spring 事务实际存在且同步已启用时，发送动作注册到 `afterCommit`；没有活动事务时立即发送。LOCAL 和 REMOTE 都遵守调用方显式设置的 `afterCommit`。

## 远程发布

```java
final PublishOptions options =
        new PublishOptions(PublishOptions.Channel.REMOTE).afterCommit();

eventPublisher.publish(envelope, options);
```

应用必须提供 `RemoteEventMessageTransport` Bean。未配置时，REMOTE 发布会抛出
`IllegalStateException("No RemoteEventMessageTransport is configured")`。RocketMQ 可使用
[wuli3-rocketmq-spring-boot-starter](wuli3-rocketmq-spring-boot-starter.md)。

## 保证边界

- `afterCommit` 只避免事务回滚后发送，不保证进程崩溃后的消息恢复。
- 默认实现没有 Outbox、持久重试、幂等或死信处理。
- LOCAL 监听器异常会沿当前调用链传播；REMOTE 的失败语义由具体 transport 决定。
- 批量发布只是对集合执行 transport 的批量入口，不代表中间件事务。

## 验证

```bash
./gradlew :wuli3-event-spring-boot-starter:test
./gradlew :wuli3-event-spring-boot-starter:check
```
