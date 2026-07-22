# wuli3-event-core

`wuli3-event-core` 只定义一个不可变的 `Event<T>` 数据载体，不定义本地事件、远程事件、领域事件或集成事件等继承层次。

```java
final Event<OrderCreated> event = Event.of("order.created", new OrderCreated("order-1"));
```

`eventId` 标识业务事实，`occurredAt` 表示业务事实发生时间，`type` 是独立于 Java 类名的稳定契约名，`payload` 可以是任意非空数据结构。同步、异步、事务提交后、延时、outbox 和具体渠道均属于发布编排，不进入 Event。

Spring 发布编排由 `wuli3-event-spring-boot-starter` 提供，具体远程渠道由对应 starter 提供。
