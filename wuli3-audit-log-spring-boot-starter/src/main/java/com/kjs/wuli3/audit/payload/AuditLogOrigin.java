package com.kjs.wuli3.audit.payload;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 审计事件创建时由框架从当前调用上下文补全的来源快照。
 *
 * <p>除 {@code application} 外的字段都可能缺失：当前调用没有认证上下文、没有有效 Span 或没有经过 Web 入口时，
 * 对应字段为 {@code null}。
 *
 * @param application 记录该事件的应用名称
 * @param operator 操作主体快照；当前调用没有认证上下文时为 {@code null}
 * @param requestId 关联调用链路的请求标识；没有调用上下文时为 {@code null}
 * @param traceId 当前 Trace 标识；没有有效 Span 时为 {@code null}
 * @param spanId 当前 Span 标识；没有有效 Span 时为 {@code null}
 * @param originIp 调用来源 IP；没有调用上下文时为 {@code null}
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogOrigin(
        String application,
        @Nullable AuditPrincipal operator,
        @Nullable String requestId,
        @Nullable String traceId,
        @Nullable String spanId,
        @Nullable String originIp) {

    /** 创建来源快照；可选字段允许为 {@code null}，但不允许为空白字符串。 */
    public AuditLogOrigin {
        if (Objects.requireNonNull(application, "application").isBlank()) {
            throw new IllegalArgumentException("application cannot be blank");
        }
        AuditLogOrigin.requireNullOrNonBlank(requestId, "requestId");
        AuditLogOrigin.requireNullOrNonBlank(traceId, "traceId");
        AuditLogOrigin.requireNullOrNonBlank(spanId, "spanId");
        AuditLogOrigin.requireNullOrNonBlank(originIp, "originIp");
    }

    /** 创建只有应用名称、没有任何调用上下文的来源快照。 */
    public static AuditLogOrigin ofApplication(final String application) {
        return new AuditLogOrigin(application, null, null, null, null, null);
    }

    private static void requireNullOrNonBlank(final @Nullable String value, final String name) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
    }
}
