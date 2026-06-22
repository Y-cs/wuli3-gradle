# wuli3-web-spring-boot-starter

Spring MVC Web 能力增强 starter。

提供：

- 统一响应体 `ApiResponse<T>`。
- 统一异常处理。
- 参数校验错误映射。
- `X-Request-Id` 生成和透传。
- MDC `requestId` 写入。
- Java Time Jackson 配置。

默认响应结构：

```json
{
  "code": "0",
  "message": "success",
  "data": {},
  "requestId": "..."
}
```
