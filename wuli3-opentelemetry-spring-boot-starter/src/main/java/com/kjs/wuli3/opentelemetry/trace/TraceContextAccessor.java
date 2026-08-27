package com.kjs.wuli3.opentelemetry.trace;

import java.util.Optional;

/**
 * 提供当前 OpenTelemetry 追踪上下文的只读访问契约。
 *
 * <p>调用方应读取当前 Span 上下文，不应从 MDC 反向构造追踪上下文。
 *
 * @author GuoYang create on 2026/8/18 10:00
 */
@FunctionalInterface
public interface TraceContextAccessor {

    /**
     * 获取当前有效的追踪上下文。
     *
     * @return 当前追踪上下文；没有有效 Span 时为空
     */
    Optional<TraceContext> current();

    /** 获取当前 Trace 标识；没有有效 Span 时为空。 */
    default Optional<String> traceId() {
        return this.current().map(TraceContext::traceId);
    }

    /** 获取当前 Span 标识；没有有效 Span 时为空。 */
    default Optional<String> spanId() {
        return this.current().map(TraceContext::spanId);
    }
}
