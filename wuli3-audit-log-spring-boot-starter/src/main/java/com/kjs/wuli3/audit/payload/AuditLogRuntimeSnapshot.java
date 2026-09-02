package com.kjs.wuli3.audit.payload;

import com.kjs.wuli3.core.assertion.Asserts;
import com.kjs.wuli3.propagation.context.PrincipalType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 审计事件创建时由框架从当前调用上下文补全的来源快照。
 *
 * <p>除 {@code application} 外的字段都可能缺失：当前调用没有认证上下文、没有有效 Span 或没有经过 Web 入口时，
 * 对应字段为 {@code null}。
 *
 * @param application 记录该事件的应用名称
 * @param operator 操作主体快照；当前调用没有认证上下文时为 {@code null}
 * @param trace 关联调用跟踪的请求标识；没有跟踪上下文时为 {@code null}
 * @param invocation 关联调用链路的请求标识；没有调用上下文时为 {@code null}
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogRuntimeSnapshot(
        String application,
        @Nullable AuditPrincipal operator,
        @Nullable AuditTrace trace,
        @Nullable AuditInvocation invocation) {

    /** 创建来源快照；可选字段允许为 {@code null}，但不允许为空白字符串。 */
    public AuditLogRuntimeSnapshot {
        if (Objects.requireNonNull(application, "application").isBlank()) {
            throw new IllegalArgumentException("application cannot be blank");
        }
    }

    /** 操作主体的运行时身份快照。 */
    public record AuditPrincipal(PrincipalType principalType, String principalId, String principalName) {
        public AuditPrincipal {
            Objects.requireNonNull(principalType, "principalType");
            Asserts.whenBlank(principalId).throwIllegalArgumentException("principalId cannot be blank");
            Asserts.whenBlank(principalName).throwIllegalArgumentException("principalName cannot be blank");
        }
    }

    public record AuditTrace(@Nullable String traceId,@Nullable  String spanId) {
        public AuditTrace {
            AuditLogRuntimeSnapshot.requireNullOrNonBlank(traceId, "traceId");
            AuditLogRuntimeSnapshot.requireNullOrNonBlank(spanId, "spanId");
        }
    }

    public record AuditInvocation(@Nullable String requestId,@Nullable  String originIp) {
        public AuditInvocation {
            AuditLogRuntimeSnapshot.requireNullOrNonBlank(requestId, "requestId");
            AuditLogRuntimeSnapshot.requireNullOrNonBlank(originIp, "originIp");
        }
    }

    private static void requireNullOrNonBlank(final @Nullable String value, final String name) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
    }
}
