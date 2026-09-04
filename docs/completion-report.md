# spring-core 与 shutdown 整理完成说明

## 当前模型

```text
starter 创建 ShutdownHook Bean
        ↓
@RegisterShutdownHook 声明阶段和优先级
        ↓
core 的 ShutdownHookAutoRegistrar 在 SmartInitializingSingleton 阶段统一发现
        ↓
ShutdownHookRegistry.register(...)
        ↓
GracefulShutdownCoordinator 在 Spring 关闭生命周期中按阶段执行
```

core 只保留三个与自身可观察工作直接相关的阶段：

1. `DRAIN_ASYNC_TASKS`：停止接收并排空本地异步工作；
2. `AWAIT_REMOTE_ACK`：等待已发出的远程操作确认；
3. `CLOSE_CLIENTS`：释放业务钩子管理的客户端资源。

服务摘除、入口流量治理和 Web Server graceful shutdown 由服务发现、Web 模块及 Spring Boot 自身负责，不再伪装成 core 的内部阶段。

## 已完成的代码调整

- 删除旧关闭总线、旧生命周期适配器、旧阶段编排器和状态模型；不保留兼容层。
- `@RegisterShutdownHook` 不再是组件，不会因注解本身产生 Bean。
- `ShutdownHookAutoRegistrar` 在所有单例初始化完成后一次性注册带注解的 hook。
- Event、RabbitMQ、RocketMQ starter 当前不接入 shutdown hook；后续模块接入时只需提供带注解的 `ShutdownHook` Bean。
- core 不再提供未被使用的 tracker、执行器包装和进程内 metrics 扩展。
- 删除阶段执行器的独立线程模型，阶段由 coordinator 在关闭线程中顺序执行。

## 使用约定

```java
@RegisterShutdownHook(phase = ShutdownPhase.AWAIT_REMOTE_ACK, priority = 0)
final class MyShutdownHook implements ShutdownHook {
    @Override
    public void shutdown(final ShutdownContext context) throws InterruptedException {
        // 使用 context.remaining() 限制等待时间，并响应中断。
    }
}
```

普通模块不应在构造函数、`@PostConstruct` 或自动配置方法中调用 `register`。只有确实需要运行时决定阶段或优先级时，才直接使用 `ShutdownHookRegistry`。

## 验证

```bash
./gradlew :wuli3-core-spring-boot-starter:test \
  :wuli3-event-spring-boot-starter:test \
  :wuli3-rabbitmq-spring-boot-starter:test \
  :wuli3-rocketmq-spring-boot-starter:test \
  --no-daemon
```

编译阶段仍有 `GracefulShutdownProperties` 的既有 Javadoc 解析警告，但不影响测试通过。
