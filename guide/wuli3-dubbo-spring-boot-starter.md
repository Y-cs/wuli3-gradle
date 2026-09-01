# wuli3-dubbo-spring-boot-starter 使用指南

## 1. 模块定位

该 starter 基于 Dubbo 原生 Filter 扩展提供两项能力：

- 在 consumer 和 provider 之间传播 Wuli3 调用上下文。
- 在服务边界传播 core 定义的 `ErrorCodeCarrier`，并在消费端还原为唯一的 `ErrorCodeException`，避免序列化提供方的业务错误枚举或原始异常。

starter 不引入 Spring Cloud `DiscoveryClient`，也不决定注册中心。Dubbo Nacos 依赖保持 `compileOnly`，由应用按部署环境选择并在运行时引入。

## 2. 引入方式

使用 Nacos 的应用可以声明：

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-dubbo-spring-boot-starter")
    runtimeOnly("org.apache.dubbo:dubbo-registry-nacos")
}
```

starter 已经传递依赖 `wuli3-core` 和 `wuli3-context-propagation`。Dubbo Filter 通过标准 SPI 文件注册并使用 `@Activate` 自动生效，业务应用不需要额外配置 `filter` 属性。

## 3. 配置

```yaml
dubbo:
  registry:
    address: nacos://127.0.0.1:8848
  config-center:
    address: nacos://127.0.0.1:8848

wuli3:
  dubbo:
    context:
      enabled: true
    error:
      enabled: true
```

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `wuli3.dubbo.context.enabled` | `true` | 是否启用 consumer/provider 上下文传播。 |
| `wuli3.dubbo.error.enabled` | `true` | 是否启用 provider/consumer 错误转换。 |

应用可以注册自己的 `ContextPropagator` Bean 控制传播字段。错误来源服务固定读取 Dubbo provider URL 中的
`application` 参数，并由 core 的 `DefaultErrorCodeResolver` 生成 `SERVICE.MODULE.ERROR_NAME`；缺少
`application` 参数时来源服务为空，错误码不添加服务前缀。

## 4. 上下文传播

consumer Filter 捕获当前 `ContextSnapshot`，通过 Dubbo invocation attachments 写出；provider Filter 从 attachments 读取并在服务方法调用期间恢复上下文，调用结束后恢复 provider 线程原有状态。

默认 `ContextPropagator` 传播 Wuli3 标准上下文字段。需要减少字段时，应在应用侧替换 `ContextPropagator` Bean，而不是修改 Dubbo Filter。

对于返回 `CompletableFuture` 的业务方法，provider Filter 只保证调用服务方法这一段线程中的上下文。业务自行启动的异步任务不会自动继承 `ThreadLocal`，必须使用 `ContextPropagator.wrap(...)`、`wrapSupplier(...)`，或使用已经集成 Wuli3 上下文传播的执行器。`ContextScope` 属于创建它的线程，不能延迟到 Future 完成线程再关闭。

## 5. 错误传播

### 5.1 边界模型

`ErrorCode` 和 `ErrorCodeException` 是统一错误模型。跨服务边界只传播 core 的 `ErrorCodeCarrier` 字段：

```text
code + message + origin + severity + sourceService
```

这些字段通过 core 的 `ErrorCodePropagator` 写入 Dubbo response attachments，固定使用
`X-Wuli3-Error-Code`、`X-Wuli3-Error-Message`、`X-Wuli3-Error-Origin`、
`X-Wuli3-Error-Severity` 和 `X-Wuli3-Error-Source-Service`。Dubbo Filter 只向编码器提供
`Result::setAttachment` 和 `Result::getAttachment`，不重复实现字段校验和枚举解析。

传输过程不包含业务枚举类，不依赖消费方拥有提供方的错误码 class，也不序列化原始异常对象。consumer Filter 读取成功后，将 Dubbo 的占位异常替换为携带 `ErrorCodeCarrier` 的 `ErrorCodeException`。

| provider 结果 | 边界行为 |
| --- | --- |
| `ErrorCodeException`（本地错误） | 解析完整字符串错误码、消息、来源和严重程度后传播。 |
| `ErrorCodeException`（已有远程错误） | 保留已有 `ErrorCodeCarrier` 的完整码和最初来源，支持多跳调用继续传播。 |
| 其他运行时异常或 RPC 结果异常 | 收敛为 `SYSTEM.INTERNAL_ERROR`，不传播原始异常类型、消息或业务栈。 |
| 正常结果 | 不写入错误 attachments，不做转换。 |

未知 JDK 异常的转换只发生在 Dubbo provider 边界。例如遗漏分类的 `IllegalArgumentException` 到达 provider Filter 时，会被安全收敛为来源 `SERVER`、严重度 `CRITICAL`、可见性 `INTERNAL` 的内部错误。这不意味着项目应全局捕获或替换 JDK 异常：

- 可预期的业务失败应声明业务 `ErrorCode` 并抛出 `ErrorCodeException`。
- 数据库、缓存、消息等 SDK 异常应在对应基础设施适配器中包装为模块错误。
- 启动参数、配置绑定和纯 Java API 契约错误应保留原生异常，让调用点和启动日志保有诊断信息。

消费方若同时使用 Web starter，携带 `ErrorCodeCarrier` 的 `ErrorCodeException` 会直接进入现有 HTTP 错误处理链路，根据远程错误的 `origin` 确定 400/500，并根据边界可见性策略控制错误码和消息是否对外可见。

### 5.2 降级行为

错误 attachments 缺失或枚举策略字段非法时，consumer 不会根据不完整数据构造 `ErrorCodeException`，而是保留 Dubbo 原始异常。未安装该 starter 的消费方只会看到 provider 设置的通用远程调用失败异常，不会得到提供方的业务类或内部异常详情。

## 6. 限制

- 上下文只覆盖 Dubbo 调用线程；业务创建的异步任务需要显式传播。
- starter 只处理经过 Dubbo Filter 的 RPC 调用，不处理定时任务、消息消费或应用自行建立的线程边界。
- 错误契约不携带任意结构化 detail、Java 堆栈或异常类名。需要新的稳定字段时，应先扩展 core 的协议无关契约，再由具体协议适配。
- Nacos 地址、命名空间、认证和配置加载由应用的 Dubbo 配置负责，starter 不包装注册中心生命周期。

## 7. 验证

```bash
./gradlew :wuli3-dubbo-spring-boot-starter:test
./gradlew :wuli3-dubbo-spring-boot-starter:check
```
