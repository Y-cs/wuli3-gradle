# 消息与资源

## RabbitMQ

- [ ] 等待 executor 中已提交的发送任务。
- [ ] 如果业务依赖 publisher confirm，继续等待 confirm，而不是只等待 `send` 方法返回。
- [ ] 不在 Wuli3 transport 中关闭外部管理的 `RabbitTemplate` 或连接工厂。

## RocketMQ

- [ ] v4 跟踪异步 callback，并在关闭时等待或超时记录。
- [ ] v5 跟踪 `sendAsync` Future。
- [ ] v5 Producer 继续由应用 Bean 通过 `destroyMethod = "close"` 管理。
- [ ] 不在 Wuli3 transport 中重复关闭 RocketMQ Spring 客户端。

## 其他资源

- [ ] OSS 使用 `OssClientManager` 的 Spring Bean 销毁流程。
- [ ] Redis、数据库和 Tracing 客户端继续由对应框架管理。
- [ ] 检查事件 executor 排空时下游客户端仍处于可用状态。
