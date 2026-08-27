package com.kjs.wuli3.audit;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 可发送到独立审计服务的不可变审计载荷。
 *
 * <p>事件唯一标识和发生时间由外层事件信封提供；本对象固化业务内容及事件创建时的调用上下文。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogPayload(
        int schemaVersion,
        String application,
        String module,
        String targetId,
        String action,
        String content,
        AuditLogOutcome outcome,
        @Nullable AuditPrincipal operator,
        @Nullable String requestId,
        @Nullable String traceId,
        @Nullable String spanId,
        @Nullable String originIp) {

    public AuditLogPayload {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be greater than zero");
        }
        application = AuditLogPayload.requireNonBlank(application, "application");
        module = AuditLogPayload.requireNonBlank(module, "module");
        targetId = AuditLogPayload.requireNonBlank(targetId, "targetId");
        action = AuditLogPayload.requireNonBlank(action, "action");
        content = AuditLogPayload.requireNonBlank(content, "content");
        Objects.requireNonNull(outcome, "outcome");
        AuditLogPayload.requireNullOrNonBlank(requestId, "requestId");
        AuditLogPayload.requireNullOrNonBlank(traceId, "traceId");
        AuditLogPayload.requireNullOrNonBlank(spanId, "spanId");
        AuditLogPayload.requireNullOrNonBlank(originIp, "originIp");
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    private static void requireNullOrNonBlank(final @Nullable String value, final String name) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
    }
}
