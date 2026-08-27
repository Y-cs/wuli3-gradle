# wuli3-audit-log-spring-boot-starter 使用指南

该 starter 定义独立审计服务使用的稳定协议，并为业务应用提供上下文感知的记录入口。它不依赖具体 HTTP、RPC、
消息中间件或数据库实现。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-audit-log-spring-boot-starter")
}
```

## 记录审计日志

业务代码只提交业务事实：

```java
final AuditLogReceipt receipt = auditLogRecorder.record(AuditLogCommand.success(
        "ORDER",
        orderId.toString(),
        "CREATE",
        "创建订单"));
```

`targetId` 必填。`AuditLogRecorder` 会同步捕获当前 `AuthContext`、`InvocationContext` 和可选的
`TraceContextAccessor`，构造 `EventEnvelope<AuditLogPayload>` 后交给 `AuditLogWriteTransport`。回执中的
`eventId` 是生产者侧幂等标识；数据库自增 `logId` 只由独立审计服务持久化时生成。

默认载荷包含：

- 应用名、模块、目标 ID、动作、内容和成功/失败结果。
- `principalType/principalId/principalName` 操作主体快照。
- `requestId`、`traceId`、`spanId` 和来源 IP；对应上下文不存在时为空。
- 固定协议版本。事件 ID 和发生时间由外层 `EventEnvelope` 提供。

## 写入 Transport

starter 不提供静默 No-op，也不预设 HTTP、RPC 或 MQ。应用必须提供 `AuditLogWriteTransport` Bean 才会创建
`AuditLogRecorder`：

```java
@Bean
AuditLogWriteTransport auditLogWriteTransport() {
    return event -> auditClient.append(event);
}
```

Transport 正常返回必须表示远端已经持久接收，或者事件已经进入可恢复投递的本地持久化 Outbox。只提交到内存线程池
不满足接口契约。允许至少一次投递，独立服务必须以 `EventEnvelope.eventId` 幂等写入。

当前版本只固定上述契约，Outbox 和具体远程 Transport 留待后续实现。

## 查询 Transport

查询使用独立的同步请求响应端口，不与写入 Transport 混合：

```java
final AuditLogPage page = auditLogQueryClient.query(new AuditLogQuery(
        "ORDER", targetId, null, null, null, null, traceId,
        occurredFrom, occurredTo, 0, 50));
```

`AuditLogPage` 不依赖 Spring Data，可以直接映射到 HTTP 或 RPC 协议。页码从 0 开始，单页最多 200 条。

## 独立审计服务

独立服务实现 `AuditLogStore`，负责：

- 以 `eventId` 幂等写入并由数据库生成自增 `logId`。
- 保存不可变操作主体和请求/追踪快照。
- 按 `AuditLogQuery` 返回分页结果。

当容器中只有 `AuditLogStore` 而没有远程 Transport 时，自动配置会创建本地 Store 适配器，便于独立审计服务内部复用
相同的 Recorder 和 QueryClient。

## 边界

- 审计记录属于应用/集成层，不要求领域对象依赖该 starter。
- `SYSTEM` 主体必须由任务或服务边界显式建立，缺少认证上下文不会自动降级为系统操作。
- 操作人和链路字段在生产者侧固化，消费者不得从自己的当前上下文重新推导。
- starter 不负责授权查询结果；独立审计服务必须在查询入口实施访问控制。

## 验证

```bash
./gradlew :wuli3-audit-log-spring-boot-starter:check
```
