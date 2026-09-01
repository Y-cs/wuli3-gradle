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

只有直接 peer 命中 `trusted-proxy-cidrs` 时才读取转发头；列表为空时忽略所有转发头。默认的
`TrustedHttpAuthContextResolver` 从可信内部 HTTP 请求的 `X-Principal-Type`、`X-Principal-Id` 和
`X-Principal-Name` 恢复 `AuthContext`。三个字段必须完整、非空白，且主体类型必须是 `CUSTOMER`、`ADMIN` 或
`SYSTEM`；否则会整体拒绝该认证上下文。
适用于统一网关完成身份认证、业务服务只恢复认证上下文的微服务架构。直接接收外部请求或使用 token、session、
安全框架 principal 等其他认证来源的应用，应提供自己的 `AuthContextResolver` Bean 替换默认实现。

Boot 管理的 `RestClient.Builder` 和 `RestTemplateBuilder` 会自动安装出站拦截器。当前拦截器使用
`ContextPropagator.standardContextEncoder()`（包含 `InvocationContextCodec` 和 `AuthContextCodec`），
会传播 `X-Request-Id`、`X-Origin-Ip`、`X-Principal-Type`、`X-Principal-Id` 和 `X-Principal-Name`；
现有自动配置没有提供缩小该 HTTP 白名单的扩展点，因此出站目标必须是可信内部服务。

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
- 默认只通知 5xx，或严重度为 `CRITICAL`、`FATAL` 的 `ErrorCodeException`；严重度只影响告警，不参与 HTTP 状态判定。
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
import com.kjs.wuli3.core.error.ErrorCodeException;

throw new ErrorCodeException(UserErrors.USER_NOT_FOUND);
```

对于 `ErrorCodeException`，默认 HTTP 状态由 `ErrorOrigin` 决定，`ErrorSeverity` 只决定告警优先级：

| 条件 | HTTP 状态 | `code` | `message` |
| --- | --- | --- | --- |
| `ErrorOrigin.CALLER` | 400 | 真实业务错误码，除非可见性隐藏 | 异常消息或错误码默认消息，除非可见性隐藏 |
| `ErrorOrigin.SERVER` | 500 | 真实业务错误码，除非可见性隐藏 | 异常消息或错误码默认消息，除非可见性隐藏 |
| `ErrorVisibility.PUBLIC`（默认） | 按 origin | 真实业务错误码 | 真实错误消息 |
| `ErrorVisibility.CODE_ONLY` | 按 origin | 真实业务错误码 | `WEB.INTERNAL_ERROR` 的默认消息 |
| `ErrorVisibility.MESSAGE_ONLY` | 按 origin | `WEB.INTERNAL_ERROR` | 真实错误消息 |
| `ErrorVisibility.INTERNAL` | 按 origin | `WEB.INTERNAL_ERROR` | `WEB.INTERNAL_ERROR` 的默认消息 |

### 错误元数据系统

wuli3-core 错误模型使用 `@ErrorMetadata` 注解声明错误的语义属性：

- **ErrorOrigin**（责任归属）：`CALLER`（调用方可修正）或 `SERVER`（服务端问题）
  - 决定 HTTP 状态码：CALLER → 400，SERVER → 500
  - 对于跨服务传播的错误（`ErrorCodeCarrier`），origin 会被保留，确保责任归属一致性

- **ErrorSeverity**（严重程度）：`NORMAL`、`WARNING`、`CRITICAL`、`FATAL`
  - 只影响告警：CRITICAL 和 FATAL 级别的错误会触发告警通知，即使是 4xx 状态
  - 不影响 HTTP 状态码判定

- **ErrorVisibility**（边界可见性）：控制错误信息在 HTTP 边界的暴露范围
  - `PUBLIC`：错误码和消息都对外输出（默认）
  - `CODE_ONLY`：只输出错误码，消息替换为通用内部错误消息
  - `MESSAGE_ONLY`：只输出消息，错误码替换为通用 INTERNAL_ERROR
  - `INTERNAL`：错误码和消息都隐藏，完全使用通用内部错误

可见性策略优先级：运行时覆盖（`withVisibility()`）> 字段级 `@ErrorMetadata` > 类级 `@ErrorMetadata` > 模块默认

Web 层的 `WebErrorResponseMapper` 是可见性过滤的单一真实来源（Single Source of Truth），
确保所有错误响应都经过统一的边界过滤，防止敏感内部信息泄露。

普通业务错误可以使用默认的 `CALLER`。数据库、缓存、消息投递、JSON 序列化等只能由服务端修复的错误，应在错误码类型或常量的 `@ErrorMetadata` 中声明 `origin = ErrorOrigin.SERVER`。应用可以注册 `WebErrorStatusResolver` 覆盖默认 400/500 映射。

错误码格式由 `WebErrorCodeResolver` 生成：

```text
SERVICE_CODE.ERROR_MODULE.ERROR_NAME
```

`SERVICE_CODE` 来自 `application.service.service-code`，未配置时省略。

Dubbo 等协议适配层接收到跨边界传播的错误时会抛出携带 `ErrorCodeCarrier` 的 `ErrorCodeException`。`ErrorCodeCarrier` 是 `ErrorCode` 的远程实现，携带 core 定义的稳定字符串错误码、**已经过提供方可见性过滤的消息**、来源和严重程度，不需要在消费端伪造业务枚举。Web starter 会沿用现有 `ErrorCodeException` 链路：

- `ErrorOrigin.CALLER` 默认返回 400，`ErrorOrigin.SERVER` 默认返回 500（origin 在传播时保留）
- `ErrorCodeCarrier` 始终使用 `PUBLIC` visibility，因为它携带的消息已经是提供方过滤后的结果
- `ErrorSeverity.CRITICAL` 和 `FATAL` 仍会触发错误告警（severity 在传播时保留）
- 消费方不需要依赖提供方的业务错误枚举，远程完整错误码也不会被替换为 Dubbo 自己的错误码

`ErrorCodeException` 是项目唯一错误异常。协议适配层只负责把传播载荷放入它，普通业务代码仍应声明枚举 `ErrorCode` 并抛出 `ErrorCodeException`。

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
| `ErrorCodeException`（传播错误） | 按传播错误的 `ErrorOrigin` | 按当前 Web 边界可见性策略 | 协议适配层接收的跨边界传播错误。 |
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
`ErrorCodeException` 不支持附加任意 `detail` 对象；业务需要返回其他结构化明细时，应在应用自己的异常处理器中显式定义响应 DTO 和可见性规则。

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
