# shutdown 边界与模块接入说明

## 设计边界

`wuli3-core-spring-boot-starter` 只负责 Spring 容器内可观察、可等待的关闭工作：

- 本地异步任务排空；
- 远程发送确认等待；
- 业务钩子管理的客户端释放。

Web Server graceful shutdown、readiness 变化、服务发现注销和入口流量治理不属于 core 的阶段，应由 Spring Boot、Web 或服务发现模块分别实现。

## 注册方式

模块提供一个实现 `ShutdownHook` 的 Spring Bean，并用注解声明阶段：

```java
@RegisterShutdownHook(phase = ShutdownPhase.AWAIT_REMOTE_ACK, priority = 0)
final class NacosShutdownHook implements ShutdownHook {
    @Override
    public void shutdown(final ShutdownContext context) {
        // 停止监听并释放 Nacos 客户端资源。
    }
}
```

core 的 `ShutdownHookAutoRegistrar` 会在所有单例初始化完成后统一发现并注册。模块自动配置不应直接调用 `ShutdownHookRegistry.register`。

## 三个现有模块

- Event、RabbitMQ、RocketMQ 当前尚未接入 core shutdown hook。

当 `wuli3.spring.shutdown.enabled=false` 时，core 的协调器、注册表和自动注册器不创建。

## 配置

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 45s

wuli3:
  spring:
    shutdown:
      enabled: true
      phase-timeout: 30s
      phases:
        drain-async-tasks:
          timeout: 30s
        await-remote-ack:
          timeout: 10s
        close-clients:
          timeout: 5s
```

`server.shutdown` 和 `spring.lifecycle.timeout-per-shutdown-phase` 控制 Spring Boot 自身的生命周期；`wuli3.spring.shutdown.phases` 只控制 Wuli3 hook 的阶段预算。
