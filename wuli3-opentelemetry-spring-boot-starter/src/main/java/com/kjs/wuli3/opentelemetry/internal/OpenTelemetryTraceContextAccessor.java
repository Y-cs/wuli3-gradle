package com.kjs.wuli3.opentelemetry.internal;

import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import java.util.Optional;

/**
 * 从 OpenTelemetry 当前 Span 读取 Trace/Span 标识。
 *
 * <p>该适配器只使用 OpenTelemetry API；SDK 和当前上下文由 Java Agent 提供。
 *
 * @author GuoYang create on 2026/8/18 10:00
 */
public final class OpenTelemetryTraceContextAccessor implements TraceContextAccessor {

    @Override
    public Optional<TraceContext> current() {
        final SpanContext spanContext = Span.current().getSpanContext();
        if (!spanContext.isValid()) {
            return Optional.empty();
        }
        return Optional.of(new TraceContext(spanContext.getTraceId(), spanContext.getSpanId()));
    }
}
