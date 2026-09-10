# Context 传播层迁移至 ScopedValue 方案（阶段三）

## 文档信息

- **创建时间**: 2026-09-02
- **目标**: 将 `wuli3-context-propagation` 从 ThreadLocal 迁移到 Java 21 ScopedValue
- **状态**: 待评估
- **风险级别**: 高（破坏性变更）

---

## 背景与动机

### 现状

项目使用 ThreadLocal 实现跨调用链的上下文传播（`ContextStore`），支持：
- Web 请求上下文（`InvocationContext`）
- 认证上下文（`AuthContext`）
- 跨 Dubbo/HTTP/RabbitMQ/RocketMQ 协议边界的自动传播

### 问题

在虚拟线程（Virtual Threads）高并发场景下，ThreadLocal 存在潜在问题：
1. **内存占用**: 每个虚拟线程独立继承 ThreadLocal 副本，百万级虚拟线程会放大内存占用
2. **泄漏风险**: ThreadLocal 忘记 `remove()` 导致的泄漏在虚拟线程下更难排查
3. **语义不匹配**: ThreadLocal 是"线程本地"，虚拟线程的轻量级特性让"线程"概念变模糊

### ScopedValue 优势

- **显式作用域**: 绑定生命周期自动管理，无泄漏风险
- **结构化并发友好**: 与 `StructuredTaskScope` 自动配合，子任务自动继承
- **性能**: 在虚拟线程下比 ThreadLocal 更高效（JDK 内部优化）

---

## 方案概述

### 核心改动

将现有"返回可手动关闭作用域"的 API 重构为"传入回调，作用域自动管理"，彻底拥抱 ScopedValue 的设计理念。

### 改动范围

| 模块 | 影响 | 改动量 |
|------|------|--------|
| `wuli3-context-propagation` 核心层 | `ContextWriter`/`ContextProxy`/`ContextStore` API 重构 | 高 |
| `wuli3-dubbo-spring-boot-starter` | `DubboContextProviderFilter` 重写 | 中 |
| `wuli3-web-spring-boot-starter` | `ContextPropagationInterceptor` 保持不变 | 低 |
| `wuli3-rabbitmq-spring-boot-starter` | `RabbitContextSupport` 重写 | 中 |
| `wuli3-rocketmq-spring-boot-starter` | `RocketContextSupport` 重写 | 中 |
| 业务代码 | 裸调 `contextWriter.put()` 需要在 scope 内 | 未知 |

---

## 详细设计

### 1. 接口重构

#### 1.1 `ContextWriter` 接口

**变更前**:
```java
public interface ContextWriter {
    <T extends Context> void put(T context);
    void remove(Class<? extends Context> type);
    ContextScope restore(ContextSnapshot snapshot); // 返回可手动关闭的作用域
    void clear();
}
```

**变更后**:
```java
public interface ContextWriter {
    // 作用域管理改为回调式
    void runInScope(ContextSnapshot snapshot, Runnable task);
    <T> T callInScope(ContextSnapshot snapshot, Callable<T> task) throws Exception;
    
    // 修改操作必须在 scope 内调用
    <T extends Context> void put(T context);
    void remove(Class<? extends Context> type);
    void clear();
}
```

**破坏性变更**:
- 删除 `restore()` 方法，调用方必须改用 `runInScope`/`callInScope`
- `put`/`remove`/`clear` 必须在已有 scope 内调用，否则抛 `IllegalStateException`

#### 1.2 `ContextProxy` 接口

**变更前**:
```java
public interface ContextProxy {
    ContextSnapshot capture();
    ContextScope restore(ContextSnapshot snapshot);
    
    default Runnable wrap(Runnable task) {
        ContextSnapshot snapshot = capture();
        return () -> {
            try (ContextScope scope = restore(snapshot)) {
                task.run();
            }
        };
    }
}
```

**变更后**:
```java
public interface ContextProxy {
    ContextSnapshot capture();
    
    // 新增回调式恢复方法
    void runInScope(ContextSnapshot snapshot, Runnable task);
    <T> T callInScope(ContextSnapshot snapshot, Callable<T> task) throws Exception;
    
    // wrap 系列简化为直接绑定
    default Runnable wrap(Runnable task) {
        ContextSnapshot snapshot = capture();
        return () -> runInScope(snapshot, task);
    }
    
    default <T> Callable<T> wrap(Callable<T> task) {
        ContextSnapshot snapshot = capture();
        return () -> callInScope(snapshot, task);
    }
    
    default <T> Supplier<T> wrapSupplier(Supplier<T> supplier) {
        ContextSnapshot snapshot = capture();
        return () -> callInScope(snapshot, supplier::get);
    }
}
```

**破坏性变更**:
- 删除 `restore()` 返回 `ContextScope` 的方法
- `wrap` 系列内部实现变化，但对调用方透明

#### 1.3 `ContextScope` 接口

**变更**: 整个接口**删除**，不再需要手动关闭作用域

---

### 2. 核心实现

#### 2.1 `ContextStore` 重写

**文件**: `../wuli3-context-propagation/src/main/java/com/kjs/wuli3/propagation/store/ContextStore.java`

```java
public final class ContextStore implements ContextReader, ContextWriter {

    // 使用 ScopedValue 替代 ThreadLocal
    private static final ScopedValue<ContextContainer> SCOPED_HOLDER = ScopedValue.newInstance();

    @Override
    public <T extends Context> Optional<T> get(final Class<T> type) {
        final ContextContainer current = SCOPED_HOLDER.orElse(null);
        return current == null ? Optional.empty() : current.get(type);
    }

    @Override
    public ContextSnapshot capture() {
        final ContextContainer current = SCOPED_HOLDER.orElse(null);
        return current == null ? ContextSnapshot.empty() : current.capture();
    }

    @Override
    public void runInScope(final ContextSnapshot snapshot, final Runnable task) {
        final ContextContainer restored = new ContextContainer();
        snapshot.values().forEach(restored::put);
        ScopedValue.where(SCOPED_HOLDER, restored).run(task);
    }

    @Override
    public <T> T callInScope(final ContextSnapshot snapshot, final Callable<T> task) throws Exception {
        final ContextContainer restored = new ContextContainer();
        snapshot.values().forEach(restored::put);
        return ScopedValue.where(SCOPED_HOLDER, restored).call(task);
    }

    @Override
    public <T extends Context> void put(final T context) {
        final ContextContainer current = SCOPED_HOLDER.get();
        if (current == null) {
            throw new IllegalStateException(
                "Cannot put context outside of scope. " +
                "Use ContextWriter.runInScope() to establish a scope first."
            );
        }
        current.put(context);
    }

    @Override
    public void remove(final Class<? extends Context> type) {
        final ContextContainer current = SCOPED_HOLDER.get();
        if (current != null) {
            current.remove(type);
        }
    }

    @Override
    public void clear() {
        final ContextContainer current = SCOPED_HOLDER.get();
        if (current != null) {
            // ScopedValue 的容器不可变，clear 只能清空当前容器内容
            // 实际作用域结束时会自动释放，这里只是标记清空
            current.values().forEach(ctx -> current.remove(ctx.type()));
        }
    }
}
```

**关键变化**:
- 删除 `private final ThreadLocal<ContextContainer> holder`
- 用 `static final ScopedValue<ContextContainer> SCOPED_HOLDER` 替代
- `restore()` 方法删除，改为 `runInScope`/`callInScope`
- `put`/`remove`/`clear` 必须在 scope 内调用

#### 2.2 `DefaultContextProxy` 适配

**文件**: `../wuli3-context-propagation/src/main/java/com/kjs/wuli3/propagation/DefaultContextProxy.java`

```java
public final class DefaultContextProxy implements ContextProxy {

    private final ContextReader contextReader;
    private final ContextWriter contextWriter;

    public DefaultContextProxy(final ContextStore contextStore) {
        this(contextStore, contextStore);
    }

    public DefaultContextProxy(final ContextReader contextReader, final ContextWriter contextWriter) {
        this.contextReader = Objects.requireNonNull(contextReader, "contextReader");
        this.contextWriter = Objects.requireNonNull(contextWriter, "contextWriter");
    }

    @Override
    public ContextSnapshot capture() {
        return this.contextReader.capture();
    }

    @Override
    public void runInScope(final ContextSnapshot snapshot, final Runnable task) {
        this.contextWriter.runInScope(Objects.requireNonNull(snapshot, "snapshot"), task);
    }

    @Override
    public <T> T callInScope(final ContextSnapshot snapshot, final Callable<T> task) throws Exception {
        return this.contextWriter.callInScope(Objects.requireNonNull(snapshot, "snapshot"), task);
    }
}
```

**关键变化**:
- 删除 `restore()` 方法实现
- 新增 `runInScope`/`callInScope` 方法委托给 `ContextWriter`

---

### 3. 集成点改造

#### 3.1 Dubbo Provider Filter

**文件**: `../wuli3-dubbo-spring-boot-starter/src/main/java/com/kjs/wuli3/dubbo/context/DubboContextProviderFilter.java`

**变更前**:
```java
@Override
public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
    // ...省略配置检查
    try (ContextScope scope = propagator.restore(encoder.extract(invocation::getAttachment))) {
        return invoker.invoke(invocation);
    }
}
```

**变更后**:
```java
@Override
public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
    final DubboProperties properties = this.dubboProperties;
    final ContextPropagator encoder = this.contextPropagator;
    final ContextProxy propagator = this.contextProxy;
    if (properties == null || !properties.getContext().isEnabled() || encoder == null || propagator == null) {
        return invoker.invoke(invocation);
    }
    
    final ContextSnapshot snapshot = encoder.extract(invocation::getAttachment);
    try {
        return propagator.callInScope(snapshot, () -> invoker.invoke(invocation));
    } catch (RpcException e) {
        throw e;
    } catch (Exception e) {
        throw new RpcException("Context restoration failed", e);
    }
}
```

#### 3.2 RabbitMQ Context Support

**文件**: `../wuli3-rabbitmq-spring-boot-starter/src/main/java/com/kjs/wuli3/rabbit/internal/RabbitContextSupport.java`

**变更前**:
```java
public RabbitContextProxy restoreFrom(final Map<String, ?> headers) {
    final ContextSnapshot contextSnapshot = this.contextPropagator.extract(fieldReader);
    return new RabbitContextProxy(this.contextWriter, contextSnapshot);
}

// 调用方使用:
// try (ContextScope scope = rabbitContextSupport.restoreFrom(headers).restore(...)) { ... }
```

**变更后**:
```java
/**
 * 在恢复的上下文中执行消息处理任务。
 *
 * @param headers 消息 headers
 * @param task 消息处理任务
 */
public void runInScope(final Map<String, ?> headers, final Runnable task) {
    final Map<String, ?> actualHeaders = Objects.requireNonNull(headers, "headers");
    final Function<String, @Nullable String> fieldReader = key -> {
        final Object value = actualHeaders.get(key);
        return value == null ? null : value.toString();
    };
    final ContextSnapshot snapshot = this.contextPropagator.extract(fieldReader);
    this.contextWriter.runInScope(snapshot, task);
}

/**
 * 在恢复的上下文中执行消息处理任务并返回结果。
 *
 * @param headers 消息 headers
 * @param task 消息处理任务
 * @return 任务返回值
 */
public <T> T callInScope(final Map<String, ?> headers, final Callable<T> task) throws Exception {
    final Map<String, ?> actualHeaders = Objects.requireNonNull(headers, "headers");
    final Function<String, @Nullable String> fieldReader = key -> {
        final Object value = actualHeaders.get(key);
        return value == null ? null : value.toString();
    };
    final ContextSnapshot snapshot = this.contextPropagator.extract(fieldReader);
    return this.contextWriter.callInScope(snapshot, task);
}

// 删除 RabbitContextProxy 内部类
```

**调用方使用**:
```java
// 新用法
rabbitContextSupport.runInScope(message.getMessageProperties().getHeaders(), () -> {
    handleMessage(message);
});
```

#### 3.3 RocketMQ Context Support

**文件**: `../wuli3-rocketmq-spring-boot-starter/src/main/java/com/kjs/wuli3/rocket/internal/RocketContextSupport.java`

改动同 RabbitMQ，API 一致。

#### 3.4 Web Interceptor

**文件**: `../wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/interceptor/ContextPropagationInterceptor.java`

**无需改动**，该拦截器只做出站编码（`capture` + `inject`），不涉及 `restore`。

---

### 4. 业务代码影响

#### 4.1 显式 `put` 上下文的代码

**场景**: 业务代码在请求处理中途手动设置上下文

**变更前**:
```java
public void someBusinessMethod() {
    AuthContext auth = new AuthContext(...);
    contextWriter.put(auth);  // 直接 put
    // ... 后续逻辑使用 auth
}
```

**变更后**:
```java
public void someBusinessMethod() {
    // 方案 A: 在已有 scope 内调用（如果外层已经恢复了上下文）
    AuthContext auth = new AuthContext(...);
    contextWriter.put(auth);  // 必须在 Dubbo/Web filter 已经建立的 scope 内

    // 方案 B: 自己建立 scope（如果是独立调用）
    AuthContext auth = new AuthContext(...);
    ContextSnapshot snapshot = ContextSnapshot.of(auth);
    contextWriter.runInScope(snapshot, () -> {
        // ... 后续逻辑使用 auth
    });
}
```

**风险**: 如果业务代码裸调 `put` 且不在任何 scope 内，会抛 `IllegalStateException`。

#### 4.2 异步任务手动传播上下文

**场景**: 使用 `Executor` 提交异步任务，需要手动传播上下文

**变更前**:
```java
Runnable task = contextProxy.wrap(() -> {
    // 异步任务逻辑，自动恢复上下文
});
executor.submit(task);
```

**变更后**:
```java
// 完全透明，wrap 内部实现变化但调用方无感知
Runnable task = contextProxy.wrap(() -> {
    // 异步任务逻辑，自动恢复上下文
});
executor.submit(task);
```

**风险**: 无，`wrap` 系列方法对外签名不变。

---

## 迁移步骤

### 阶段 1: 核心层改造（1-2 周）

1. 修改 `ContextWriter`/`ContextProxy` 接口
2. 重写 `ContextStore` 实现（ScopedValue）
3. 更新 `DefaultContextProxy`
4. 删除 `ContextScope` 接口
5. 单元测试全部通过

### 阶段 2: 集成层改造（1 周）

1. 改造 `DubboContextProviderFilter`
2. 改造 `RabbitContextSupport`
3. 改造 `RocketContextSupport`
4. 集成测试验证传播链路

### 阶段 3: 业务代码排查（1-2 周）

1. 全局搜索 `contextWriter.put` 调用点
2. 逐一确认是否在 scope 内调用
3. 不在 scope 内的调用需要补充 `runInScope` 包装
4. 回归测试

### 阶段 4: 测试与上线（2 周）

1. 功能回归测试
2. 性能基准测试（对比 ThreadLocal 版本）
3. 灰度发布，监控内存/GC/吞吐量
4. 全量上线

**总计**: 约 5-7 周

---

## 兼容性说明

### ThreadLocal 兼容性

**阶段三方案不兼容 ThreadLocal**，原因：
1. API 形状完全不同（回调式 vs 返回式）
2. ScopedValue 的 `where().run()` 无法用 ThreadLocal 模拟（生命周期管理机制不同）
3. 保留双路径会导致实现复杂度翻倍，且无法在运行时平滑切换

**如需兼容**，必须采用阶段二方案（双路径 + 配置开关），但会带来：
- 代码维护成本高（两套实现）
- 测试矩阵翻倍
- 最终删除旧路径时仍是破坏性变更

### 回退方案

如果迁移后发现问题（性能回退/稳定性问题），回退路径：
1. 恢复 `ContextScope` 接口定义
2. 恢复 `ContextStore` 的 ThreadLocal 实现
3. 恢复 `ContextWriter.restore()` 方法
4. 恢复集成层的 try-with-resources 用法

**前提**: 迁移前必须打 Git tag 或创建回退分支。

---

## 风险评估

| 风险项 | 影响 | 缓解措施 |
|--------|------|----------|
| 业务代码裸调 `put` 导致运行时异常 | 高 | 代码全局搜索 + 逐一确认 + 充分测试 |
| 性能不如预期（ScopedValue 反而更慢） | 中 | 迁移前做基准测试，灰度验证 |
| 复杂嵌套 scope 场景出现 bug | 中 | 单元测试覆盖嵌套场景 |
| 回退成本高 | 高 | 打 tag + 准备回退脚本 |

---

## 成本收益分析

### 成本

- **开发时间**: 5-7 周（2-3 人）
- **测试时间**: 2 周全面回归
- **风险**: 破坏性变更，回退成本高

### 收益

- **内存优化**: 虚拟线程场景下内存占用降低（需实测量化）
- **防泄漏**: 彻底消除 ThreadLocal 泄漏风险
- **语义清晰**: 显式作用域，代码可读性提升
- **结构化并发**: 与 `StructuredTaskScope` 自动配合

### 建议

**仅在以下情况下执行迁移**:
1. 生产环境实测发现 ThreadLocal 有明显内存/性能问题
2. 计划大规模使用结构化并发（`StructuredTaskScope`）
3. 团队有充足时间窗口和测试资源

**否则**: 保持现状，ThreadLocal 在虚拟线程下依然可用且稳定。

---

## 附录

### A. ScopedValue vs ThreadLocal 对比

| 特性 | ThreadLocal | ScopedValue |
|------|-------------|-------------|
| 生命周期 | 手动管理（易泄漏） | 自动管理（显式作用域） |
| 虚拟线程性能 | 较慢（每线程副本） | 较快（共享不可变绑定） |
| 可变性 | 可变（set/remove） | 不可变（只能嵌套绑定） |
| 继承性 | 通过 InheritableThreadLocal | 通过 StructuredTaskScope 自动继承 |
| API 复杂度 | 简单（get/set/remove） | 中等（where().run() 回调式） |

### B. 参考资料

- [JEP 429: Scoped Values (Incubator)](https://openjdk.org/jeps/429)
- [JEP 446: Scoped Values (Preview)](https://openjdk.org/jeps/446)
- [JEP 481: Scoped Values (Third Preview)](https://openjdk.org/jeps/481)
- [Virtual Threads - JEP 444](https://openjdk.org/jeps/444)

---

**文档结束**
