# wuli3-context-propagation

协议无关的上下文传播核心模块。

本模块只定义“传播什么”和“如何在载体中读写”，不依赖 Spring、Servlet、Dubbo、Feign、消息队列或具体 HTTP 客户端。

## 核心边界

- `ContextStore`：当前线程内上下文存储。
- `snapshot.ContextPropagator`：捕获、恢复和包装异步任务。
- `PropagationContext`：允许跨进程传播的上下文。
- `LocalContext`：只在当前进程内使用的上下文。
- `ContextCarrierReader` / `ContextCarrierWriter`：协议无关的字符串键值载体。
- `PropagationContextCodec`：单个上下文类型到载体的编码和解析规则。
- `ContextTransmitter`：把当前上下文写入载体，或从载体恢复上下文。

## 默认传播字段

`DefaultPropagationContextCodecs.invocationOnly()` 默认只传播调用链身份：

- `X-Request-Id`
- `X-Origin-Ip`

`DefaultPropagationContextCodecs.trustedInternal()` 额外传播认证元数据：

- `X-User-Id`
- `X-Username`

认证元数据只能用于可信内部调用链。公网 HTTP 入站不应直接信任这些字段，应由安全模块从 token、session 或框架 principal 中解析。

## 基本使用

创建一个当前线程内的上下文存储：

```java
final ContextStore contextStore = new ContextStore();
contextStore.put(new InvocationContext("10.0.0.1", "rid-1"));
contextStore.put(new AuthContext(42L, "alice"));
```

业务代码不要直接依赖 `ContextStore`，优先使用 accessor：

```java
final InvocationContextAccessor invocationAccessor = new InvocationContextAccessor(contextStore);
final AuthContextAccessor authAccessor = new AuthContextAccessor(contextStore);

final String requestId = invocationAccessor.requestId()
        .orElse("");
final Long userId = authAccessor.userId()
        .orElse(0L);
```

在异步任务中传播当前上下文：

```java
final ContextPropagator propagator = new DefaultContextPropagator(contextStore);

executor.execute(propagator.wrap(() -> {
    // 这里可以读取提交任务时捕获到的上下文。
}));
```

需要手动控制生命周期时，可以捕获快照并恢复：

```java
final ContextSnapshot snapshot = propagator.capture();
final ContextScope scope = propagator.restore(snapshot);
try {
    // 当前线程临时使用 snapshot 中的上下文。
} finally {
    scope.close();
}
```

## 跨协议读写

协议适配层需要把自己的元数据结构适配为 `ContextCarrierReader` / `ContextCarrierWriter`。核心模块不关心这些值来自 HTTP header、Dubbo attachment 还是消息 header。

写出当前上下文：

```java
final ContextTransmitter transmitter = new ContextTransmitter(
        contextStore,
        contextStore,
        DefaultPropagationContextCodecs.invocationOnly()
);
final MapContextCarrier carrier = new MapContextCarrier();

transmitter.writeTo(carrier);
```

从载体恢复上下文时必须使用作用域，防止线程复用时残留上一请求身份：

```java
final MapContextCarrier carrier = new MapContextCarrier(Map.of(
        InvocationContextCodec.REQUEST_ID, "rid-1",
        InvocationContextCodec.ORIGIN_IP, "10.0.0.1"
));

try (ContextScope ignored = transmitter.readScoped(carrier)) {
    // 在协议入站处理范围内调用业务逻辑
}
```

可信内部调用链需要传播认证元数据时，显式使用 `trustedInternal()`：

```java
final ContextTransmitter internalTransmitter = new ContextTransmitter(
        contextStore,
        contextStore,
        DefaultPropagationContextCodecs.trustedInternal()
);
```

## 适配层职责

协议适配应放在独立 starter 中：

- Web 入站：把 `HttpServletRequest` 适配为 `ContextCarrierReader`。
- HTTP client / Feign 出站：把 `ContextCarrierWriter` 写入请求 header。
- Dubbo consumer/provider：把 carrier 映射到 Dubbo attachment。
- Spring 异步执行：使用 `ContextPropagator.wrap(...)` 或 `TaskDecorator` 恢复快照。
