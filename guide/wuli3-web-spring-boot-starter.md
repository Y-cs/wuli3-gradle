# wuli3-web-spring-boot-starter 使用指南

该 starter 为 Spring MVC 应用提供请求上下文、统一 JSON 配置、响应包装、异常映射和扩展 SPI。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-web-spring-boot-starter")
}
```

自动配置包含 `WebContextAutoConfiguration`、`WebJsonAutoConfiguration`、`WebErrorAutoConfiguration` 和
`WebResponseAutoConfiguration`。

## 请求上下文

默认过滤器在请求进入时创建 `InvocationContext`，写入 requestId、可信客户端 IP 和 MDC；请求结束后清理上下文。

```properties
wuli3.web.context.enabled=true
wuli3.web.context.filter-order=-2147483638
wuli3.web.context.request-id-header-name=X-Request-Id
wuli3.web.context.accept-external-request-id=true
wuli3.web.context.request-id-max-length=128
wuli3.web.context.trusted-proxy-cidrs=10.0.0.0/8,192.168.0.0/16
wuli3.web.context.client-ip-header-priority=X-Forwarded-For,X-Real-IP,Forwarded
```

只有直接 peer 命中 `trusted-proxy-cidrs` 时才读取转发头；列表为空时忽略所有转发头。公网身份信息不能从 header
直接恢复，应用应提供 `AuthContextResolver` Bean，从 token、session 或安全框架 principal 构造 `AuthContext`。

Boot 管理的 `RestClient.Builder` 和 `RestTemplateBuilder` 会自动安装出站拦截器，只传播 `X-Request-Id` 和
`X-Origin-Ip`，不会传播认证信息。

应用可以通过 Bean 替换以下默认 SPI：

- `RequestIdResolver`
- `ClientIpResolver`
- `AuthContextResolver`
- `WebErrorStatusResolver`
- `ErrorAlertNotifier`
- `ResourcePathResolver`

## JSON 配置

starter 会增量注册 wuli3 Java Time、`@ResourcePath` 和 `@Desensitized` Jackson 模块，不覆盖业务自己的
`Module` Bean。

资源类型到域名的默认映射：

```properties
wuli3.web.json.resource.path.image=https://static.example.com
wuli3.web.json.resource.path.file=https://files.example.com
```

应用注册的 `DesensitizationStrategy` Bean 会覆盖同名内置策略；注册
`DesensitizationVisibilityPolicy` Bean 可以控制何时允许输出原值。详细 JSON API 见
[wuli3-json](wuli3-json.md)。

## 统一响应启用方式

引入 `wuli3-web-spring-boot-starter` 后，默认自动启用：

- Controller 成功返回值统一包装。
- 业务异常和常见 Spring MVC 异常统一转换。
- 响应中自动携带当前请求上下文里的 `requestId`。

可通过配置关闭或调整：

```properties
wuli3.web.response.wrapper-enabled=true
wuli3.web.response.exception-handler-enabled=true
wuli3.web.response.wrap-response-entity-body=true
wuli3.web.response.success-message=
wuli3.web.response.validation-detail-enabled=true
```

应用侧扩展优先使用文档中列出的 SPI，例如 `WebErrorStatusResolver`、`ErrorAlertNotifier`、`RequestIdResolver` 和 `ClientIpResolver`。

## 错误告警扩展

业务系统可以实现 `ErrorAlertNotifier` 并注册为 Spring Bean，用于把统一异常处理捕获到的错误发送到日志、监控、IM、工单或告警平台。starter 只预留通知接口，不提供具体告警实现。

```java
@Component
final class BizErrorAlertNotifier implements ErrorAlertNotifier {

    @Override
    public void alert(final ErrorAlertContext context) {
        if (context.status().is5xxServerError()) {
            // 业务侧自行接入告警平台。
        }
    }
}
```

调用规则：

- 统一异常处理捕获异常并确定响应状态后调用告警通知。
- 默认只通知 5xx，或严重度为 `CRITICAL`、`FATAL` 的 `ErrorCodeException`。
- 可同时注册多个 `ErrorAlertNotifier` Bean；调用顺序遵守 Spring `@Order` / `Ordered`。
- 没有注册 Bean 时不执行任何告警逻辑。
- 告警实现抛出 `RuntimeException` 时会被 starter 捕获并记录 warn 日志，不会影响原始错误响应。

`ErrorAlertContext` 字段：

| 字段 | 说明 |
| --- | --- |
| `error` | 原始异常对象。 |
| `status` | 即将返回的 HTTP 状态码。 |
| `responseCode` | 即将返回给调用方的错误码；可见性隐藏后会是对外错误码。 |
| `requestId` | 当前请求 ID，可能为 `null`。 |
| `method` | HTTP 方法。 |
| `requestUri` | 请求 URI。 |
| `remoteAddr` | 请求来源地址。 |

告警通知拿到的是从当前请求中提取出的不可变快照，不会把原始 `HttpServletRequest` 传给业务实现。

## 标准响应结构

成功和失败的统一响应都使用 `ApiResponse<T>`：

```json
{
  "code": "0",
  "message": "",
  "timestamp": 1780000000000,
  "requestId": "request-id",
  "data": {}
}
```

字段含义：

| 字段 | 说明 |
| --- | --- |
| `code` | 外部错误码。成功固定为 `0`；失败由 `WebErrorCodeResolver` 解析。 |
| `message` | 成功时使用 `wuli3.web.response.success-message`；失败时使用错误消息或框架映射消息。 |
| `timestamp` | 响应生成时间，Unix 毫秒时间戳。 |
| `requestId` | 当前请求 ID，来自 `X-Request-Id` 或 starter 自动生成的 ID；上下文缺失时可能为 `null`。 |
| `data` | 成功数据；失败时一般为 `null`，参数校验失败时可为 `ValidationErrorDetails`。 |

## 成功返回规则

普通 Controller 返回值会被包装：

```java
@GetMapping("/users/{id}")
UserDetail getUser(@PathVariable final Long id) {
    return userService.get(id);
}
```

返回：

```json
{
  "code": "0",
  "message": "",
  "timestamp": 1780000000000,
  "requestId": "request-id",
  "data": {
    "id": 1
  }
}
```

成功返回矩阵：

| 场景 | HTTP 状态 | 响应 body |
| --- | --- | --- |
| 普通对象、集合、`null` | 保留原状态，通常 200 | 包装为 `ApiResponse.success(data)`。 |
| `String` | 保留原状态 | 序列化为 JSON 字符串形式的 `ApiResponse`，并设置 `Content-Type: application/json`。 |
| 已返回 `ApiResponse<?>` | 保留原状态 | 原样返回，不重复包装。 |
| 已返回 `ProblemDetail` | 保留原状态 | 原样返回。 |
| `ResponseEntity<T>` 且 `wrap-response-entity-body=true` | 保留 `ResponseEntity` 状态和响应头 | body 继续按普通返回值规则包装。 |
| `ResponseEntity<T>` 且 `wrap-response-entity-body=false` | 保留 `ResponseEntity` 状态和响应头 | body 原样返回。 |
| `byte[]`、`Resource`、`ResponseBodyEmitter`、`StreamingResponseBody` | 保留原状态 | 原样返回，不包装。 |
| HTTP 204 或 304 | 204 或 304 | 原样返回，不写 body。 |

## 原生响应

使用 `@NativeResponse` 可以跳过成功响应包装：

```java
import com.kjs.wuli3.web.response.NativeResponse;

@NativeResponse
@GetMapping("/download")
Resource download() {
    return resource;
}
```

注解可放在 Controller 类或方法上；方法级配置优先于类级配置。

| 配置 | 成功响应 | 异常响应 |
| --- | --- | --- |
| `@NativeResponse` 或 `@NativeResponse(SUCCESS_ONLY)` | 不包装，原样返回 | 仍返回统一错误 `ApiResponse`。 |
| `@NativeResponse(ALL)` | 不包装，原样返回 | 返回 Spring 标准 `ProblemDetail`，不使用 `ApiResponse` 外壳。 |

`NativeResponseMode.ALL` 下，业务异常生成的 `ProblemDetail` 会额外带上：

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "错误消息",
  "code": "SERVICE.MODULE.ERROR_NAME",
  "requestId": "request-id"
}
```

## 业务异常

业务代码推荐抛出 `ErrorCodeException`：

```java
throw new ErrorCodeException(UserErrors.USER_NOT_FOUND);
```

返回规则：

| 条件 | HTTP 状态 | `code` | `message` |
| --- | --- | --- | --- |
| `ErrorSeverity.NORMAL` 或 `WARNING` | 400 | 真实业务错误码，除非可见性隐藏 | 异常消息或错误码默认消息，除非可见性隐藏 |
| `ErrorSeverity.CRITICAL` 或 `FATAL` | 500 | 真实业务错误码，除非可见性隐藏 | 异常消息或错误码默认消息，除非可见性隐藏 |
| `ErrorVisibility.CODE_ONLY` | 按 severity | 真实业务错误码 | `WEB.INTERNAL_ERROR` 的默认消息 |
| `ErrorVisibility.MESSAGE_ONLY` | 按 severity | `WEB.INTERNAL_ERROR` | 真实错误消息 |
| `ErrorVisibility.INTERNAL` | 按 severity | `WEB.INTERNAL_ERROR` | `WEB.INTERNAL_ERROR` 的默认消息 |

错误码格式由 `WebErrorCodeResolver` 生成：

```text
SERVICE_CODE.ERROR_MODULE.ERROR_NAME
```

`SERVICE_CODE` 来自 `application.service.service-code`，未配置时省略。

## 框架异常

统一异常处理覆盖的 Spring MVC 常见异常：

| 异常类型 | HTTP 状态 | 统一错误码 | 说明 |
| --- | --- | --- | --- |
| `MethodArgumentNotValidException` | 400 | `WEB.BAD_REQUEST` | Bean Validation 参数校验失败。 |
| `ConstraintViolationException` | 400 | `WEB.BAD_REQUEST` | 方法参数或路径参数约束失败。 |
| `MissingServletRequestParameterException` | 400 | `WEB.BAD_REQUEST` | 缺少必填请求参数。 |
| `MethodArgumentTypeMismatchException` | 400 | `WEB.BAD_REQUEST` | 请求参数类型转换失败。 |
| `ServletRequestBindingException` | 400 | `WEB.BAD_REQUEST` | 请求绑定失败。 |
| `HttpMessageNotReadableException` | 400 | `WEB.BAD_REQUEST` | JSON 或请求体不可读。 |
| `IllegalArgumentException`、`IllegalStateException` | 500 | `WEB.INTERNAL_ERROR` | 未显式转换的编程异常。 |
| `HttpRequestMethodNotSupportedException` | 405 | `WEB.BAD_REQUEST` | HTTP 方法不支持。 |
| `HttpMediaTypeNotSupportedException` | 415 | `WEB.BAD_REQUEST` | 请求媒体类型不支持。 |
| `NoHandlerFoundException`、`NoResourceFoundException` | 404 | `WEB.NOT_FOUND` | MVC handler 或静态资源不存在。 |
| `ResponseStatusException` | 异常指定状态 | 按状态映射 | 消息使用异常消息。 |
| `ErrorResponseException` | 异常指定状态 | 按状态映射 | `NativeResponseMode.ALL` 下返回异常自带 body。 |
| `HttpMessageNotWritableException` | 500 | `WEB.INTERNAL_ERROR` | 响应写出失败，不暴露内部细节。 |
| 其他 `Exception` | 500 | `WEB.INTERNAL_ERROR` | 未分类异常。 |

状态到错误码的收敛规则：

| HTTP 状态 | 错误码 |
| --- | --- |
| 401 | `WEB.UNAUTHORIZED` |
| 403 | `WEB.FORBIDDEN` |
| 404 | `WEB.NOT_FOUND` |
| 5xx | `WEB.INTERNAL_ERROR` |
| 其他 4xx | `WEB.BAD_REQUEST` |

starter 不直接依赖 Spring Security。异常 cause 链中出现以下类名时，会映射为安全错误：

| 类名 | HTTP 状态 | 错误码 |
| --- | --- | --- |
| `org.springframework.security.core.AuthenticationException` | 401 | `WEB.UNAUTHORIZED` |
| `org.springframework.security.access.AccessDeniedException` | 403 | `WEB.FORBIDDEN` |

## 参数校验详情

`wuli3.web.response.validation-detail-enabled=true` 时，以下异常会在失败响应的 `data.errors` 中返回字段级详情：

- `MethodArgumentNotValidException`
- `ConstraintViolationException`
- `MissingServletRequestParameterException`
- `MethodArgumentTypeMismatchException`

示例：

```json
{
  "code": "WEB.BAD_REQUEST",
  "message": "请求参数错误",
  "timestamp": 1780000000000,
  "requestId": "request-id",
  "data": {
    "errors": [
      {
        "field": "name",
        "code": "NotBlank",
        "message": "must not be blank"
      }
    ]
  }
}
```

关闭字段级详情：

```properties
wuli3.web.response.validation-detail-enabled=false
```

关闭后，失败响应只返回统一 `message`，`data` 为 `null`。

字段详情只包含稳定的 `field`、`code` 和 `message`，不会回显 rejected value。

## 未覆盖场景

以下场景不由 `response` 包保证统一处理：

- Spring MVC 之外的请求链路，例如 Servlet Filter 在进入 `DispatcherServlet` 前直接写出的响应。
- 容器级错误页、容器级 404、Tomcat/Jetty/Undertow 自己生成的错误响应。
- WebFlux 响应链路；本 starter 面向 Spring MVC。
- 异步任务、消息消费、定时任务等非 HTTP 调用链路中的异常。
- 已经手动写入 `HttpServletResponse` 的接口，例如直接调用 `getWriter()` 或 `getOutputStream()` 输出。
- 文件下载、字节数组、`Resource`、`ResponseBodyEmitter`、`StreamingResponseBody` 等原生或流式响应 body。
- HTTP 204 和 304 响应 body。
- 自定义异常类型的细粒度业务语义；未转换为 `ErrorCodeException`、`ResponseStatusException` 或 `ErrorResponseException` 的异常会按兜底 `Exception` 处理。
- `@NativeResponse(ALL)` 下 `ErrorResponseException` 会返回异常自带 body，starter 不重写其中字段。

如果业务需要覆盖这些场景，应在应用侧增加更靠近场景的处理，例如自定义 Filter、容器错误页、专用 `@ControllerAdvice`、或显式抛出 `ErrorCodeException`。

## 验证

```bash
./gradlew :wuli3-web-spring-boot-starter:test
./gradlew :wuli3-web-spring-boot-starter:check
```
