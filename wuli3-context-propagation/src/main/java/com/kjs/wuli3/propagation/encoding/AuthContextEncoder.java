package com.kjs.wuli3.propagation.encoding;

import com.kjs.wuli3.propagation.context.AuthContext;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 将可信认证元数据写入出站协议字段。
 */
public final class AuthContextEncoder {

    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-Username";

    private AuthContextEncoder() {}

    /**
     * 将认证上下文编码为协议字段。
     *
     * @param context     待编码的认证上下文
     * @param fieldWriter 接收字段名和字段值的写入器
     * @throws NullPointerException 当任一参数为 {@code null} 时
     */
    public static void writeTo(final AuthContext context, final BiConsumer<String, String> fieldWriter) {
        final AuthContext actualContext = Objects.requireNonNull(context, "context");
        final BiConsumer<String, String> actualFieldWriter = Objects.requireNonNull(fieldWriter, "fieldWriter");
        actualFieldWriter.accept(AuthContextEncoder.USER_ID, String.valueOf(actualContext.userId()));
        actualFieldWriter.accept(AuthContextEncoder.USERNAME, actualContext.username());
    }
}
