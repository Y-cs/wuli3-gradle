# 优雅关闭执行版

本文是根目录 [todo-list.md](../../todo-list.md) 中“优雅关闭”任务的执行清单，按实现顺序整理。

## 1. 先定义边界

- [ ] `wuli3-core` 和 `wuli3-event-core` 保持纯 Java，不引入 Spring 生命周期依赖。
- [ ] 将优雅关闭定义为正常 `SIGTERM` 场景下的协同退出，不把它当作 Outbox、重试或崩溃恢复机制。
- [ ] 明确每个客户端的生命周期所有者，禁止多个 starter 重复关闭同一个客户端。

## 2. 修正事件 executor

- [ ] 在 `wuli3-event-spring-boot-starter` 使用可参与 Spring 生命周期的 executor。
- [ ] 不再无条件创建或覆盖通用的 `applicationTaskExecutor`。
- [ ] 提供明确的事件 executor Bean 名称，并支持应用 Bean 覆盖默认实现。
- [ ] 配置拒绝新任务、等待在途任务和最大等待时长。
- [ ] 为 executor 增加关闭期间的指标或日志：提交数、完成数、拒绝数、超时数。

## 3. 增加事件排空协调

- [ ] 在 Spring starter 内增加事件关闭协调组件或等价的 `SmartLifecycle` 实现。
- [ ] 生命周期至少包含运行、停止接收、排空、关闭四个状态。
- [ ] 新任务拒绝后，等待已提交任务完成；超时后继续关闭并记录剩余任务。
- [ ] 校验生命周期 phase，确保事件排空不会早于 Web Server 的 graceful shutdown。
- [ ] 验证事务 `afterCommit` 回调与关闭协调器之间的竞态行为。

## 4. 补齐远程 transport

- [ ] RabbitMQ：区分 executor 任务完成与 publisher confirm 完成，按产品要求选择等待边界。
- [ ] RocketMQ v4：跟踪每次异步发送 callback，关闭时等待 callback 或超时。
- [ ] RocketMQ v5：保存 `CompletableFuture`，关闭时等待 Future 完成或超时。
- [ ] 异步失败、关闭中拒绝和超时都要保留事件 ID、topic 和 event type。
- [ ] 不在 transport 内关闭由外部 starter 或应用持有的 MQ 客户端。

## 5. 接入 Web 和部署层

- [ ] 显式设置 `server.shutdown=graceful`。
- [ ] 设置 `spring.lifecycle.timeout-per-shutdown-phase`，并让编排器终止宽限期更长。
- [ ] 在 readiness 或负载均衡层先摘除实例，避免关闭窗口继续接收流量。
- [ ] 停止定时任务和消息消费入口，防止关闭期间持续生成新事件。

## 6. 验收

- [ ] 单元测试覆盖 executor 关闭、任务排空、任务拒绝和超时。
- [ ] 集成测试覆盖 `ApplicationContext.close()`、长请求、事务提交后发布和远程 callback/Future。
- [ ] 在测试环境执行一次真实 `SIGTERM`，确认日志顺序和最终退出时间符合配置。
- [ ] 执行相关模块的 `test`、`check` 和应用上下文关闭集成测试。

## 7. 不属于本任务

- [ ] 不在本任务中实现 Outbox、持久重试、死信或跨进程事务。
- [ ] 对需要崩溃恢复的审计和关键业务事件，另行设计可恢复投递方案。
