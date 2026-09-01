# wuli3-audit-log-spring-boot-starter 使用指南

该 starter 定义独立审计服务使用的稳定事件协议，并为业务应用提供上下文感知的记录入口。它不依赖具体 HTTP、RPC、
消息中间件或数据库实现，也不提供审计日志查询 API。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-audit-log-spring-boot-starter")
}
```

## 记录审计日志

业务代码只提交业务事实：

```java
final AuditLogReceipt receipt = auditLogRecorder.record(AuditLogEntry.success(
        "ORDER",
        orderId.toString(),
        "CREATE",
        "创建订单"));
```

`targetId` 必填。`AuditLogRecorder` 会同步捕获当前 `AuthContext`、`InvocationContext` 和可选的
`TraceContextAccessor`，构造 `EventEnvelope<AuditLogPayload>` 后交给共享 `EventPublisher`。回执中的
`eventId` 是生产者侧幂等标识。

默认载荷包含：

- 应用名、模块、目标 ID、动作、内容和成功/失败结果。
- `principalType/principalId/principalName` 操作主体快照。
- `requestId`、`traceId`、`spanId` 和来源 IP；对应上下文不存在时为空。
- 固定协议版本。事件 ID 和发生时间由外层 `EventEnvelope` 提供。

## 事件发布与存储

审计事件使用 `AuditLogPublishOptions` 作为路由键。应用可以注册支持该选项类型的远程
`RemoteEventTransport`，将事件发送到 HTTP、RPC 或消息中间件；独立审计服务也可以通过 `AuditLogStore`
接收并持久化事件。未注册远程传输时，事件仍遵循事件 starter 的本地发布语义。

传输实现应保证成功返回时事件已被远端持久接收，或已进入可恢复投递的本地持久化 Outbox。只提交到内存线程池
不满足可靠投递契约。允许至少一次投递，独立服务必须以 `EventEnvelope.eventId` 幂等写入。

## 独立审计服务

独立服务实现 `AuditLogStore`，负责：

- 以 `eventId` 幂等写入。
- 保存不可变操作主体和请求/追踪快照。
- 将收到的 `EventEnvelope<AuditLogPayload>` 映射到自身的持久化模型。

当容器中提供 `AuditLogStore` 时，自动配置会创建本地事件传输适配器，便于独立审计服务直接复用记录入口。

## 边界

- 审计记录属于应用/集成层，不要求领域对象依赖该 starter。
- `SYSTEM` 主体必须由任务或服务边界显式建立，缺少认证上下文不会自动降级为系统操作。
- 操作人和链路字段在生产者侧固化，消费者不得从自己的当前上下文重新推导。
- starter 不负责审计数据的查询、授权或展示；这些能力由独立审计服务自行提供。

## 验证

```bash
./gradlew :wuli3-audit-log-spring-boot-starter:check
```
