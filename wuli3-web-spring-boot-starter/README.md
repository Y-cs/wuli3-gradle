# wuli3-web-spring-boot-starter

Spring MVC Web 能力增强 starter。

提供：

- 统一响应体 `ApiResponse<T>`。
- Controller 返回值自动包装为统一响应体。
- 统一异常处理。
- 错误告警通知扩展点 `ErrorAlertNotifier`。
- 参数校验错误映射。
- `X-Request-Id` 生成和透传。
- MDC `requestId` 写入。
- `ContextPropagator` 等线程内上下文传播基础 Bean。
- 为 Boot 管理的 `RestClient.Builder` 和 `RestTemplateBuilder` 自动配置调用链上下文出站传播。
- Java Time Jackson 配置。

统一响应和异常处理的完整使用说明见 [Web 统一响应处理使用文档](docs/response-handling.md)。

## 包边界

- `auth`、`context`、`error`、`json`、`response` 按 Web 能力组织公开契约与配置。
- `autoconfigure` 只负责 Spring Boot 自动配置和内部组件装配。
- `internal` 存放默认实现与框架适配代码，不作为应用扩展 API；应用应通过公开 SPI 替换默认行为。

默认响应结构：

```json
{
  "code": "0",
  "message": "",
  "timestamp": 1780000000000,
  "requestId": "...",
  "data": {}
}
```

处理规则：

- 普通返回值自动包装为 `ApiResponse<T>`。
- 已经是 `ApiResponse<T>` 的返回值不会重复包装。
- `ResponseEntity` 会保留 HTTP 状态码和响应头，并对 body 执行统一包装。
- 文件、字节数组、流式响应、SSE 和 `ProblemDetail` 等原生响应不会包装。
- 方法或类标注 `@NativeResponse` 时，默认只跳过成功响应包装，异常仍返回统一错误体。
- 方法或类标注 `@NativeResponse(NativeResponseMode.ALL)` 时，成功和异常都跳过统一响应体；异常返回 Spring 标准 `ProblemDetail`。
- 异常场景使用语义 HTTP 状态码，例如参数错误 400、方法不支持 405、媒体类型不支持 415、系统异常 500。
- 业务代码需要返回可识别错误时必须抛出 `ErrorCodeException`；裸 `IllegalArgumentException`、`IllegalStateException`
  等编程异常按服务端 500 处理。
- 参数校验失败时，字段级错误详情默认放在失败响应的 `data.errors` 中，每项只包含稳定的 `field`、`code`
  和 `message`，不回显 rejected/invalid value。
- `ErrorAlertNotifier` 默认只接收 5xx，或 core-error 策略严重度为 `CRITICAL`、`FATAL` 的异常。
- HTTP 客户端出站只传播 `X-Request-Id` 和 `X-Origin-Ip`，不会自动传播用户认证信息。

配置项：

```properties
# 是否启用 Controller 返回值自动包装，默认 true
wuli3.web.response.wrapper-enabled=true

# 是否启用统一异常处理，默认 true
wuli3.web.response.exception-handler-enabled=true

# 是否包装 ResponseEntity 的 body，默认 true
wuli3.web.response.wrap-response-entity-body=true

# 成功响应 message，默认空字符串
wuli3.web.response.success-message=

# 是否返回参数校验字段级详情，默认 true
wuli3.web.response.validation-detail-enabled=true
```
