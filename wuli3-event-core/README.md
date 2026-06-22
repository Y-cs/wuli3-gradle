# wuli3-event-core

DDD 事件抽象模块。

- `DomainEvent`：服务内领域事件。
- `IntegrationEvent`：服务间集成事件契约。
- `EventPublisher` / `EventBus`：事件发布抽象。
- `EventHandler`：事件处理器抽象。

本模块只定义契约，不绑定 MQ、不做持久化 outbox。
