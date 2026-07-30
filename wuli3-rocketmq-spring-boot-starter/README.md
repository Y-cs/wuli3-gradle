# wuli3-rocketmq-spring-boot-starter

基于 RocketMQ 的远程事件发送适配器。

## 上下文传播

Encoder 使用 RocketMQ 专属的 `ContextTransmitter` 写入传播字段，不复用 Web 层的传播策略，也不会向 Spring
容器暴露全局 `ContextTransmitter` Bean。

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

当前 starter 只实现发送端。后续增加 Listener 时，必须在单条消息的处理作用域内调用
`ContextTransmitter.readScoped()`，确保处理结束或异常退出后恢复此前线程上下文。

载体字段缺失或非法时按不可信协议输入处理：忽略对应上下文，不转换为 `ErrorCodeException`。只有业务 Handler
需要表达稳定业务错误时才使用 core-error 体系。
