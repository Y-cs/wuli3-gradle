# wuli3-rocketmq-spring-boot-starter

该 starter 为 `wuli3-event-spring-boot-starter` 提供 RocketMQ `RemoteEventTransport`，并使用 CloudEvents 1.0 structured JSON 传输统一的 `Event<T>`。

```properties
wuli3.rocketmq.event.topic=business-events
wuli3.rocketmq.event.source=urn:service:order-service
wuli3.rocketmq.event.send-timeout=3s
```

`eventId/type/occurredAt` 映射为 CloudEvents 属性，只有 `payload` 进入 CloudEvents data。事件 type 同时作为 RocketMQ tag。远程发布由 event starter 的异步或延时策略触发，Broker 返回 `SEND_OK` 后才得到成功结果。

业务事务与远程投送需要原子性时，由应用通过 event starter 的 `customize(...)` 提供 outbox Transport；RocketMQ Transport 只负责最终 relay。
