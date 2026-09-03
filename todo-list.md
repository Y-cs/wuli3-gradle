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

整合日志、配置加密、优雅关闭总线，作为所有 Spring Boot starter 的基础模块。

详见：[core-spring-boot-starter 实施方案](todo-list/core-spring-boot-starter/todo-list.md)

## 优雅关闭

### 目标

在收到正常终止信号时，停止接收新流量和新后台任务，等待正在执行的请求、事件任务及消息发送完成，再关闭下游客户端。优雅关闭不承诺在 `kill -9`、节点宕机或网络中断时保证消息不丢失。

### 当前架构需要修正的点

- [ ] 将 `EventAutoConfiguration` 中无条件创建的 `VirtualThreadTaskExecutor` 改为 Spring 生命周期可管理的 executor。当前实现不会等待在途任务完成，`spring.task.execution.shutdown.*` 也不会作用于它。
- [ ] 不让事件 starter 无条件占用通用 Bean 名称 `applicationTaskExecutor`。优先使用明确的事件 executor 名称，并允许应用覆盖。
- [ ] 为异步事件发布增加停止接收、任务计数、排空和超时语义；executor 关闭后新的异步发布应明确拒绝。
- [ ] RabbitMQ 异步发送纳入事件排空过程，并区分“任务执行完成”和“publisher confirm 完成”。
- [ ] RocketMQ v4 跟踪异步 callback；RocketMQ v5 保存 `sendAsync` 返回的 Future，并在关闭时等待或超时记录。
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

- [ ] 确认事件 executor 使用上述 Spring Boot 配置，而不是绕过配置的自定义虚拟线程 executor。
- [ ] 将 Kubernetes `terminationGracePeriodSeconds` 或其他编排器的终止宽限期设置为大于应用关闭等待时间，并预留传播余量。
- [ ] 在 readiness 或负载均衡层先摘除实例，再等待 Web Server 优雅停止；不能只依赖 JVM 退出。

### 关闭顺序

1. 停止定时任务、消息消费入口和其他会产生新工作的组件。
2. 将实例标记为不再接收流量，Web Server 停止接收新请求并等待在途请求。
3. 事件 executor 停止接收新的异步发布并排空已有任务。
4. 等待 RabbitMQ、RocketMQ 等远程发送的 callback、confirm 或 Future 完成。
5. 关闭 OSS、MQ、数据库、Redis、Tracing 等客户端。

若通过 `SmartLifecycle` 编排，事件排空阶段必须安排在 Web Server graceful shutdown 之后，避免在活跃请求仍未结束时提前关闭事件 executor。

### 验收测试

- [ ] executor 关闭后新的异步发布会被拒绝，并产生可观测日志或明确异常。
- [ ] Spring `ApplicationContext.close()` 会等待已提交的事件任务，达到超时后继续关闭并记录未完成数量。
- [ ] 事务 `afterCommit` 回调在关闭窗口内可以完成；超出窗口时行为明确且不会无限阻塞。
- [ ] RabbitMQ 异步发送任务、RocketMQ v4 callback、RocketMQ v5 Future 都有完成和超时测试。
- [ ] 启动长请求后发送终止信号，验证新请求被拒绝、在途请求完成、事件发送完成或按超时策略记录。
- [ ] 执行相关模块的 `test`、`check` 和一次应用上下文关闭集成测试。

### 可靠性边界

优雅关闭只能覆盖正常 `SIGTERM` 场景。审计日志等需要可靠投递的场景，Transport 必须表示远端已持久化接收，或事件已进入可恢复的本地 Outbox；仅提交到内存线程池不满足可靠性契约。

### 拆分文档

- [优雅关闭执行版](todo-list/graceful-shutdown/todo-list.md)
- [事件投递](todo-list/event-delivery/README.md)
- [消息与资源](todo-list/messaging/README.md)
