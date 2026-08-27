package com.kjs.wuli3.opentelemetry.trace;

/**
 * OpenTelemetry Java Agent 写入日志 MDC 的标准字段名。
 *
 * <p>本类只提供字段契约，不自行写入或清理 MDC；MDC 生命周期必须跟随 Agent 管理的当前 Span。
 *
 * @author GuoYang create on 2026/8/18 10:00
 */
public final class TraceMdc {

    /** 当前 Trace 标识。 */
    public static final String TRACE_ID = "trace_id";

    /** 当前 Span 标识。 */
    public static final String SPAN_ID = "span_id";

    /** 当前 Trace flags，例如采样状态。 */
    public static final String TRACE_FLAGS = "trace_flags";

    private TraceMdc() {}
}
