# wuli3-event-core

纯 Java 事件数据模型，不提供进程内事件总线，也不绑定 Spring、MQ 或数据库。

- `Event`：领域事件和集成事件共享的事件身份与发生时间。
- `DomainEvent`：领域模型产生的本地事实标记，不强制事件来自聚合。
- `IntegrationEvent`：包含稳定事件名、schema 版本、生产服务和传输 metadata 的跨服务契约。
- `EventMetadata`：不可变传输 header，不承载业务 payload。

具体事件应使用不可变 record/class 表达真实业务 payload。某个事件可以显式同时实现 `DomainEvent` 和
`IntegrationEvent`，但这不会触发自动外发；应用层仍需明确选择本地发布或可靠集成发布。

Spring Boot 应用直接使用 `ApplicationEventPublisher`、`@EventListener` 和
`@TransactionalEventListener` 实现本地事件。普通 `@EventListener` 默认同步执行；`@Async` 与事务提交后监听必须由
应用显式选择，不能视为可靠消息。

持久化 outbox、重试、死信、MQ 投递和消费幂等属于独立基础设施能力，不由本模块提供。

## 为什么不提供本地 EventBus

正式业务运行环境是 Spring Boot。Spring 已经提供同步发布、Bean 生命周期、监听器排序、事务阶段和异步执行能力；自建
EventBus 会形成第二套生命周期和事务语义，Guava EventBus 同样不能与 Spring 事务和容器自然集成。因此本仓库不提供
`event-inmemory`、Spring 事件包装层或 Guava 事件实现。

领域模型可以保持无 Spring 依赖，由应用服务调用 `ApplicationEventPublisher` 发布具体事件。需要可靠跨服务投递时，应用层
应显式生成集成事件并写入与业务数据同事务的 outbox，而不是依赖本地监听器发送 MQ。
