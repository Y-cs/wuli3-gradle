# wuli3-event-spring-boot-starter 使用指南

该 starter 为 `wuli3-event-core` 提供 Spring Boot 自动配置：本地事件使用 Spring
`ApplicationEventPublisher`，应用提供的远程 transport 按其具体选项类型注册。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-event-spring-boot-starter")
}
```

自动配置会在缺少用户实现时创建：

- `SpringLocalEventTransport`
- `EventPublisher`

上述 Bean 允许应用通过同类型 Bean 替换。不存在默认远程占位 transport；未注册的选项类型由路由器直接拒绝。

## 本地发布

```java
final EventEnvelope<OrderPaid> envelope =
        EventEnvelopeTemplate.of("orders", "order.paid.v1").wrap(payload);

final SpringLocalPublishOptions options = new SpringLocalPublishOptions(false, false);
eventPublisher.publish(options, envelope);
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

`SpringLocalPublishOptions` 的两个字段依次为 `async` 和 `afterCommit`。异步请求使用名为
`applicationTaskExecutor` 的 `Executor` Bean；没有该 Bean 时会抛出 `UnsupportedCapabilityException`，
不会绕过应用的线程池策略。delay 和 order key 不属于本地选项。

## 事务提交后发布

```java
final SpringLocalPublishOptions options = new SpringLocalPublishOptions(false, true);
eventPublisher.publish(options, envelope);
```

当 Spring 事务实际存在且同步已启用时，发送动作注册到 `afterCommit`；没有活动事务时立即发送。
实现 `TransactionalPublishOptions` 的远程选项也遵循相同规则。

## 远程发布

远程 starter 或应用代码通过 `RemoteEventTransport<PO>` 提供具体选项类型。没有对应 transport 时，
以该 options 发布会抛出 `UnsupportedCapabilityException`。RocketMQ 可使用
[wuli3-rocketmq-spring-boot-starter](wuli3-rocketmq-spring-boot-starter.md)。

## 保证边界

- `afterCommit` 只避免事务回滚后发送，不保证进程崩溃后的消息恢复。
- 默认实现没有 Outbox、持久重试、幂等或死信处理。
- 同步本地监听器异常会沿当前调用链传播；远程失败语义由具体 transport 决定。
- 一次发布多个信封不代表中间件事务；transport 可以逐条发送。

## 验证

```bash
./gradlew :wuli3-event-spring-boot-starter:test
./gradlew :wuli3-event-spring-boot-starter:check
```
