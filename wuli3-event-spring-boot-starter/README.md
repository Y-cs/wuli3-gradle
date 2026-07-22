# wuli3-event-spring-boot-starter

该 starter 提供围绕单一 `Event<T>` 的类型安全发布编排。第一层只选择 `local()`、`remote()` 或 `customize(...)`，每个分支只暴露自身支持的处理函数，最终由 `publish(event)` 触发完整流程。

```java
publisher.local().afterCommit().async().publish(event);
publisher.remote().delay(Duration.ofSeconds(10)).async().publish(event);
publisher.customize(outboxPublication).publish(event);
```

本地策略使用 Spring `ApplicationEventPublisher`。`afterCommit()` 要求活动事务；没有事务或事务回滚时返回失败结果。异步和延时仅改变执行方式，只有下游 Transport 确认投送后结果才是成功。

层次划分如下：

- `local/remote/customize`：选择发布策略。
- `afterCommit/delay`：约束触发时机。
- `sync/async`：选择执行方式。
- `EventPublishTransport`：定义最终投送边界并执行 `publish`。

`remote()` 使用容器中唯一的 `RemoteEventTransport`，具体 MQ starter 负责提供。`delay(...)` 是进程内延时，不提供持久化保证。outbox 属于应用自定义 Transport，通过 `customize(...)` 接入，只有最终投送完成才返回成功。
