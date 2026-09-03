# wuli3-context-propagation 使用指南

固定上下文模型、当前线程存储和异步快照能力的核心模块。

本模块不提供协议无关的 carrier 或全局入站恢复框架。它只定义固定上下文的字段读写契约，并由真实协议适配器决定哪些上下文可跨越该边界，避免把信任策略隐藏在通用传输组件中。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-context-propagation")
}
```

## 核心边界

- `Context`：当前执行中可存储的上下文，不默认跨边界传递。
- `PropagationContext`：允许进入快照、跨异步任务和协议边界传递的上下文。
- `ContextContainer`：当前线程的可变上下文存储，保存全部 `Context`。
- `ContextSnapshot`：只包含 `PropagationContext` 的不可变快照，是异步和出站协议共同使用的传递值。
- `ContextStore`：基于当前线程保存完整上下文，并实现读取、修改、捕获和恢复能力。
- `ContextReader` / `ContextWriter`：分别暴露读取与修改当前上下文的能力，依赖方只注入所需一侧。
- `ContextProxy`：在快照操作能力之上包装异步任务。
- `InvocationContext`：请求标识和来源地址。
- `AuthContext`：可信内部链路可传播的认证主体快照，由 `principalType`、`principalId` 和 `principalName` 组成。
- `ContextFieldCodec`：一个固定 `PropagationContext` 与协议字段之间的双向映射。
- `ContextPropagator`：按显式白名单组合多个 `ContextFieldCodec`，统一读取、写入和保留字段计算。

上下文不提供任意 key/value 扩展袋。出现租户、区域或灰度等真实需求时，应新增受控的值对象和明确的传播契约。

普通 `Context` 不会进入 `ContextSnapshot`，因此不会随异步任务或远程协议迁移。无传播上下文时使用 `ContextSnapshot.empty()` 表示，而不是复用可变 `ContextContainer` 的空实例。

## 协议字段编码

两个固定 codec 定义稳定字段契约：

- `InvocationContextCodec` 写入 `X-Request-Id` 和 `X-Origin-Ip`。
- `AuthContextCodec` 写入 `X-Principal-Type`、`X-Principal-Id` 和 `X-Principal-Name`。

认证主体的三个字段必须同时存在且非空白，`X-Principal-Type` 必须是 `CUSTOMER`、`ADMIN` 或 `SYSTEM`。
解码遇到缺失或非法字段时会整体拒绝该认证上下文，不会恢复部分身份信息。

`ContextPropagator.standardContextEncoder()` 当前返回上述两个 codec，因此会同时读写调用标识和认证信息。协议适配器若只允许传播调用标识，应显式构造白名单：

```java
final ContextPropagator invocationOnly =
        new ContextPropagator(List.of(new InvocationContextCodec()));
```

协议适配器使用 `ContextPropagator` 声明白名单，而不是逐一判断上下文类型：

```java
final ContextPropagator propagator =
        new ContextPropagator(ContextPropagator.standardContextEncoder());
propagator.reservedFieldNames().forEach(headers::remove);
propagator.inject(contextReader.capture(), headers::set);
```

`reservedFieldNames()` 只包含当前实例所配置编码器管理的字段，不会自动加入未配置编码器的字段。

## 基本使用

创建一个当前线程内的上下文存储：

```java
final ContextStore contextStore = new ContextStore();
contextStore.put(new InvocationContext("10.0.0.1", "rid-1"));
contextStore.put(new AuthContext(PrincipalType.CUSTOMER, "42", "alice"));
```

业务代码不要直接依赖 `ContextStore`，优先使用 accessor：

```java
final InvocationContextAccessor invocationAccessor = new InvocationContextAccessor(contextStore);
final AuthContextAccessor authAccessor = new AuthContextAccessor(contextStore);

final String requestId = invocationAccessor.requestId()
        .orElse("");
final String principalId = authAccessor.principalId()
        .orElse("");
```

在异步任务中传播当前上下文：

```java
final ContextProxy contextProxy = new DefaultContextProxy(contextStore);

executor.execute(contextProxy.wrap(() -> {
    // 这里可以读取提交任务时捕获到的上下文。
}));
```

需要手动控制生命周期时，可以捕获快照并恢复：

```java
final ContextSnapshot snapshot = contextProxy.capture();
final ContextScope scope = contextProxy.restore(snapshot);
try {
    // 当前线程临时使用 snapshot 中的上下文。
} finally {
    scope.close();
}
```
