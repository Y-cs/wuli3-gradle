# 待办清单

## 既有模块规划

- [ ] 一个日志模块
- [ ] 鉴权模块
- [ ] 提供统一的 JSON Resource Path 处理能力

### Elasticsearch

- [ ] 与 `wuli3-json` 集成，统一 Jackson 配置，保证 ES 文档序列化行为与 JSON 标准一致
- [ ] 批量操作工具，封装 `BulkProcessor`，提供简洁的批量写入 API
- [ ] 可选的搜索日志记录，记录慢查询和大结果集查询
- [ ] 测试支持，提供 Testcontainers 或嵌入式 ES 自动配置

### MongoDB

- [ ] 审计字段自动填充，集成 `wuli3-context-propagation`
- [ ] 逻辑删除支持，提供类似 MyBatis-Plus 的语义
- [ ] 与 `wuli3-json` 集成，统一序列化行为
- [ ] 测试支持，提供嵌入式 MongoDB 配置

## wuli3-core-spring-boot-starter

整合日志、配置加密和最小化的优雅关闭扩展点，作为所有 Spring Boot starter 的基础模块。

详见：[core-spring-boot-starter 实施方案](todo-list/core-spring-boot-starter/todo-list.md)

## 优雅关闭

### 目标

在收到正常终止信号时，停止接收新流量和新后台任务，等待正在执行的请求、事件任务及消息发送完成，再关闭下游客户端。优雅关闭不承诺在 `kill -9`、节点宕机或网络中断时保证消息不丢失。

### 当前架构与后续实现计划

- [x] 将 shutdown 公共契约收敛到 `ShutdownHook`、`ShutdownContext`、`ShutdownPhase`、`@RegisterShutdownHook`、注册表和协调器。
- [x] 删除尚未有生产使用方的 tracker、执行器包装、独立阶段执行器和进程内 metrics 扩展。
- [x] 暂不在 Event、RabbitMQ、RocketMQ 等业务 starter 中接入 shutdown hook；待真实关闭需求明确后再按模块增加实现。
- [ ] 为真实接入的异步任务定义可观察的停止接收、排空和远程确认语义。
- [ ] 为真实接入的消息模块补充 callback/Future/confirm 的完成和超时测试。
- [ ] 保持客户端生命周期所有权清晰：OSS 由 `OssClientManager` 关闭，RocketMQ v5 Producer 由应用 Bean 关闭，Redis、RabbitMQ 和 RocketMQ v4 继续由各自 starter 管理。

### 应用和部署配置

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 45s
  task:
    execution:
      shutdown:
        await-termination: true
        await-termination-period: 30s
      pool:
        shutdown:
          accept-tasks-after-context-close: false
```

- [ ] 真实接入异步 executor 时，确认其使用 Spring Boot 生命周期配置，而不是绕过配置的自定义线程池。
- [ ] 将 Kubernetes `terminationGracePeriodSeconds` 或其他编排器的终止宽限期设置为大于应用关闭等待时间，并预留传播余量。
- [ ] 在 readiness 或负载均衡层先摘除实例，再等待 Web Server 优雅停止；不能只依赖 JVM 退出。

### 关闭顺序

1. 停止定时任务、消息消费入口和其他会产生新工作的组件。
2. 将实例标记为不再接收流量，Web Server 停止接收新请求并等待在途请求。
3. 真实接入的模块停止接收新工作并在对应阶段内完成排空或远程确认。
4. 关闭 OSS、MQ、数据库、Redis、Tracing 等客户端。

若通过 `SmartLifecycle` 编排，事件排空阶段必须安排在 Web Server graceful shutdown 之后，避免在活跃请求仍未结束时提前关闭事件 executor。

### 验收测试

- [ ] 关闭钩子异常不会阻塞同阶段后续钩子和后续阶段。
- [ ] 关闭钩子使用 `ShutdownContext.remaining()` 主动限制等待时间。
- [ ] 真实接入异步模块后，补充任务排空、远程确认和超时测试。
- [ ] 启动长请求后发送终止信号，验证新请求被拒绝、在途请求完成、事件发送完成或按超时策略记录。
- [ ] 执行相关模块的 `test`、`check` 和一次应用上下文关闭集成测试。

### 可靠性边界

优雅关闭只能覆盖正常 `SIGTERM` 场景。审计日志等需要可靠投递的场景，Transport 必须表示远端已持久化接收，或事件已进入可恢复的本地 Outbox；仅提交到内存线程池不满足可靠性契约。

### 拆分文档

- [优雅关闭执行版](todo-list/graceful-shutdown/todo-list.md)
- [事件投递](todo-list/event-delivery/README.md)
- [消息与资源](todo-list/messaging/README.md)
