# wuli3-opentelemetry-spring-boot-starter 使用指南

该 starter 面向 OpenTelemetry Java Agent 部署。Agent 是 SDK、自动埋点、上下文传播、采样和 OTLP 导出的唯一所有者；
starter 只使用 OpenTelemetry API 读取当前 Trace，并提供业务手工指标记录器，不会创建第二套 SDK 或 exporter。

## 引入与启动

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-opentelemetry-spring-boot-starter")
}
```

应用通过 Java Agent 启动，并用 Agent 的标准环境变量配置服务名和导出端点：

```bash
OTEL_SERVICE_NAME=orders \
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318 \
java -javaagent:/opt/opentelemetry-javaagent.jar -jar orders.jar
```

未安装 Agent 时，OpenTelemetry API 自动退化为 no-op：指标不会导出，Trace accessor 返回空，应用仍可正常启动。

## 记录业务指标

注入 `MetricRecorder` 后可以记录 Counter、Histogram 和同步 Gauge：

```java
import com.kjs.wuli3.opentelemetry.metrics.MetricRecorder;
import io.opentelemetry.api.common.Attributes;

final Attributes attributes = Attributes.builder()
        .put("operation", "create")
        .put("result", "success")
        .build();

metricRecorder.incrementCounter("orders.created", attributes);
metricRecorder.recordHistogram("orders.processing.duration", durationMillis, attributes);
metricRecorder.recordGauge("orders.pending", pendingCount, Attributes.empty());
```

instrument 会按名称缓存，可安全地被多个线程复用。属性必须是有限枚举等低基数值；禁止将 `traceId`、`requestId`、
订单号、用户标识或异常消息作为指标属性，否则会造成时序数量失控。单位和命名属于指标契约，应由业务统一约定。

## 获取当前 Trace

业务代码注入 `TraceContextAccessor`，不要从 MDC 反向构造追踪上下文：

```java
traceContextAccessor.traceId().ifPresent(traceId -> auditCommand.setTraceId(traceId));

traceContextAccessor.current().ifPresent(context -> {
    final String traceId = context.traceId();
    final String spanId = context.spanId();
});
```

`TraceContextAccessor` 和 `TraceContext` 均位于 OpenTelemetry starter，默认实现使用 `Span.current()` 读取 Agent
维护的当前上下文。

## MDC 日志关联

Java Agent 的 Logback MDC instrumentation 在有效 Span 内写入标准字段：

- `trace_id`
- `span_id`
- `trace_flags`

字段常量由 `TraceMdc` 提供。Logging starter 的默认 pattern 已读取 `trace_id/span_id`，并以 `traceId/spanId`
标签输出。starter 不手工写入或清理这些字段，因为 MDC 必须随 Agent 管理的 Span scope（包括嵌套 Span 和异步任务）
同步变化。

## 验证

```bash
./gradlew :wuli3-opentelemetry-spring-boot-starter:check
./gradlew :wuli3-logging-spring-boot-starter:check
```
