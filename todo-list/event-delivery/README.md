# 事件投递

- [ ] 将 `VirtualThreadTaskExecutor` 替换为生命周期可管理的事件 executor。
- [ ] 修正 `applicationTaskExecutor` 的 Bean 所有权和覆盖规则。
- [ ] 增加异步发布的停止接收、任务计数、排空和超时语义。
- [ ] 验证本地事件、事务 `afterCommit` 事件和远程事件使用一致的关闭窗口。
- [ ] 在关闭期间拒绝新任务时保留事件 ID 和异常原因。
- [ ] 记录提交、完成、拒绝和超时指标，便于判断是否发生事件丢失风险。
