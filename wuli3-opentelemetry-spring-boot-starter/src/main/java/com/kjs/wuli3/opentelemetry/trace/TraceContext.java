package com.kjs.wuli3.opentelemetry.trace;

import java.util.Objects;

/**
 * 当前 OpenTelemetry Trace/Span 标识的只读视图。
 *
 * @param traceId 当前 Trace 标识
 * @param spanId 当前 Span 标识
 * @author GuoYang create on 2026/8/18 10:00
 */
public record TraceContext(String traceId, String spanId) {

    /** 创建字段完整的追踪上下文视图。 */
    public TraceContext {
        TraceContext.requireNonBlank(traceId, "traceId");
        TraceContext.requireNonBlank(spanId, "spanId");
    }

    private static void requireNonBlank(final String value, final String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
