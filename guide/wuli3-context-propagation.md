# wuli3-context-propagation 使用指南

固定上下文模型、当前线程存储和异步快照能力的核心模块。

本模块不提供协议无关的 carrier、通用 codec 或入站上下文恢复框架。协议边界只有真实适配器时才实现，避免为 HTTP、消息或 RPC 预先建立一层未被使用的抽象。

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
- `snapshot.ContextPropagator`：在快照操作能力之上包装异步任务。
- `InvocationContext`：请求标识和来源地址。
- `AuthContext`：可信内部链路可传播的认证元数据。

上下文不提供任意 key/value 扩展袋。出现租户、区域或灰度等真实需求时，应新增受控的值对象和明确的传播契约。

普通 `Context` 不会进入 `ContextSnapshot`，因此不会随异步任务或远程协议迁移。无传播上下文时使用 `ContextSnapshot.empty()` 表示，而不是复用可变 `ContextContainer` 的空实例。

## 出站编码

两个固定编码器只负责把已有上下文写给具体协议的字段写入器：

- `InvocationContextEncoder` 写入 `X-Request-Id` 和 `X-Origin-Ip`。
- `AuthContextEncoder` 写入 `X-User-Id` 和 `X-Username`，只能用于明确可信的内部边界。

HTTP 或消息适配器先捕获一次快照，再按自身的协议白名单写入所需字段：

```java
final ContextSnapshot snapshot = contextReader.capture();
snapshot.get(InvocationContext.class)
        .ifPresent(context -> InvocationContextEncoder.writeTo(context, headers::set));
```

公网入站请求不应信任认证 header；认证上下文应由安全模块从 token、session 或框架 principal 中解析。入站适配器在完成字段校验和信任判断后，可以构造 `ContextSnapshot` 并通过 `ContextWriter#restore` 在调用作用域内安装它；核心模块不提供会绕过这些规则的通用字段解码器。

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
