# wuli3-event-core 使用指南

`wuli3-event-core` 是纯 Java 事件契约模块，定义事件信封、发布选项、发布端口和消息传输端口，不依赖 Spring 或具体消息中间件。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-event-core")
}
```

## 创建事件信封

固定主题和事件类型时，优先复用模板：

```java
private static final EventEnvelopeTemplate ORDER_PAID =
        EventEnvelopeTemplate.of("orders", "order.paid.v1");

final EventEnvelope<OrderPaid> envelope = ORDER_PAID.wrap(payload);
final EventEnvelope<OrderPaid> withHeader = envelope.withHeader("tenant", "tenant-1");
```

`EventEnvelope` 包含：

| 字段 | 说明 |
| --- | --- |
| `headers` | 浅不可变的传输元数据。 |
| `topic` | 逻辑远程主题。 |
| `eventType` | 稳定事件契约名称。 |
| `eventId` | 唯一事件标识。 |
| `occurredOn` | 事件创建时间。 |
| `payload` | 事件载荷。 |

## 发布选项

`PublishOptions.defaults()` 表示同步 `LOCAL` 发布。远程发布从明确的通道开始：

```java
final PublishOptions options = new PublishOptions(PublishOptions.Channel.REMOTE)
        .afterCommit();

eventPublisher.publish(envelope, options);
```

可选能力：

| API | 语义 |
| --- | --- |
| `async()` | 请求异步发送。 |
| `afterCommit()` | 有活动事务时，在提交成功后发送。 |
| `delaySeconds(long)` / `setDelayTime(Duration)` | 请求延迟发送。 |
| `setOrderKey(String)` | 请求按业务键有序发送。 |

选项只表达调用方需求；具体 transport 可以拒绝不支持的组合并抛出 `UnsupportedCapabilityException`。

## 扩展端口

- `EventPublisher`：应用发布入口，支持单条和批量发布。
- `EventMessageTransport`：通用发送端口。

仅引入本模块不会创建 `EventPublisher` 实现。Spring 应用通常同时引入
[wuli3-event-spring-boot-starter](wuli3-event-spring-boot-starter.md)。

## 使用边界

- `EventEnvelopeTemplate.wrap(...)` 使用当前时间和 UUID；需要可测试时间或业务 ID 时使用带 `Supplier<String>` 的模板或直接构造信封。
- headers 只做浅复制，值对象本身仍应保持不可变。
- `afterCommit` 不是可靠消息保证；模块没有 Outbox、重试、去重或投递审计。
- 远程 `topic` 还必须满足具体消息中间件的限制。

## 验证

```bash
./gradlew :wuli3-event-core:test
./gradlew :wuli3-event-core:check
```
