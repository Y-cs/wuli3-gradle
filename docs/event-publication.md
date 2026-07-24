# 事件发布说明

## 业务代码

业务代码先创建稳定的事件信封，再通过 `EventPublisher` 选择本地或远程通道：

```java
final EventEnvelopeTemplate template = EventEnvelopeTemplate.of(
        "business-events", "order.paid.v1");
final EventEnvelope<OrderPaid> envelope = template.wrap(new OrderPaid(orderId, Instant.now()));

publisher.publish(envelope);
publisher.publish(envelope, new PublishOptions(PublishOptions.Channel.REMOTE));
```

`EventEnvelope` 的 `topic`、`eventType`、`eventId` 必须非空白。headers 在创建时做结构浅快照；payload 和 header value 应在发布后保持可序列化且不变。

## 两种通道

- `LOCAL`：立即调用 Spring `ApplicationEventPublisher`，监听器使用 `@EventListener` 或 `@TransactionalEventListener` 自行表达事务边界。LOCAL 不接受 async、delay 或 orderKey。
- `REMOTE`：有可同步事务时在 `AFTER_COMMIT` 提交到 transport；无事务时立即发送。实际事务存在但同步不可用时发布失败，避免消息先于回滚事务泄漏。

REMOTE 是尽力投递，不保证与业务数据库原子提交。提交后的发送失败不能回滚已提交事务；批量发布逐条提交，可能部分成功。

## RocketMQ

默认适配器沿用 Apache `rocketmq-spring-boot-starter` 的配置：

```properties
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=business-event-producer
wuli3.rocketmq.event.context-mode=INVOCATION_ONLY
```

topic 唯一来自 `EventEnvelope.topic`，不使用额外的默认 topic 配置。适配器支持同步、异步、同步/异步有序发送和 RocketMQ 5.x 毫秒延迟；毫秒延迟不能与 async 或 orderKey 组合，会在网络调用前抛出能力异常。

出站上下文默认只重建 request ID 和 origin IP。只有显式 `TRUSTED_INTERNAL` 才传播认证字段；调用方在 envelope headers 中提供的同名保留字段不会被信任。业务 headers 保留在 JSON 信封中，不映射为 RocketMQ Broker 属性。

模块还包含一个 package-private Java Client preview，用于实现审查。它没有自动配置、Bean、运行时依赖或启用开关，默认应用不会创建第二套 producer。
