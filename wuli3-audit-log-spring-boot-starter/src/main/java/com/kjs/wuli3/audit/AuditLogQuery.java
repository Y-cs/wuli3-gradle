package com.kjs.wuli3.audit;

import com.kjs.wuli3.propagation.context.PrincipalType;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * 独立审计服务支持的稳定查询条件。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogQuery(
        @Nullable String module,
        @Nullable String targetId,
        @Nullable String action,
        @Nullable PrincipalType principalType,
        @Nullable String principalId,
        @Nullable String requestId,
        @Nullable String traceId,
        @Nullable Instant occurredFrom,
        @Nullable Instant occurredTo,
        int pageNumber,
        int pageSize) {

    public AuditLogQuery {
        AuditLogQuery.requireNullOrNonBlank(module, "module");
        AuditLogQuery.requireNullOrNonBlank(targetId, "targetId");
        AuditLogQuery.requireNullOrNonBlank(action, "action");
        AuditLogQuery.requireNullOrNonBlank(principalId, "principalId");
        AuditLogQuery.requireNullOrNonBlank(requestId, "requestId");
        AuditLogQuery.requireNullOrNonBlank(traceId, "traceId");
        if (occurredFrom != null && occurredTo != null && occurredFrom.isAfter(occurredTo)) {
            throw new IllegalArgumentException("occurredFrom must not be after occurredTo");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must not be negative");
        }
        if (pageSize < 1 || pageSize > 200) {
            throw new IllegalArgumentException("pageSize must be between 1 and 200");
        }
    }

    /** 创建不带过滤条件的分页查询。 */
    public static AuditLogQuery all(final int pageNumber, final int pageSize) {
        return new AuditLogQuery(null, null, null, null, null, null, null, null, null, pageNumber, pageSize);
    }

    private static void requireNullOrNonBlank(final @Nullable String value, final String name) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
    }
}
