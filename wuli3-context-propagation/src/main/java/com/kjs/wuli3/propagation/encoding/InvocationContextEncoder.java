package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.InvocationContext;
import java.util.Objects;
import java.util.function.BiConsumer;

/** 将调用元数据写入出站协议字段。 */
public final class InvocationContextEncoder {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String ORIGIN_IP = "X-Origin-Ip";

    private InvocationContextEncoder() {}

    /**
     * 将调用上下文编码为协议字段。
     *
     * @param context 待编码的调用上下文
     * @param fieldWriter 接收字段名和字段值的写入器
     * @throws NullPointerException 当任一参数为 {@code null} 时
     */
    public static void writeTo(final InvocationContext context, final BiConsumer<String, String> fieldWriter) {
        final InvocationContext actualContext = Objects.requireNonNull(context, "context");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(InvocationContextEncoder.REQUEST_ID, actualContext.requestId());
        actualFieldWriter.accept(InvocationContextEncoder.ORIGIN_IP, actualContext.originIp());
    }
}
