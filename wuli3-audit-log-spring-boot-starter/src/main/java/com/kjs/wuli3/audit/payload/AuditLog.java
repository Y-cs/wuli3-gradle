package com.kjs.wuli3.audit.payload;

import java.util.Objects;

/**
 * 业务代码描述的审计内容；操作主体和调用链信息由框架从当前上下文补全为 {@code AuditLogRuntimeSnapshot}。
 *
 * @param module 发生操作的业务模块
 * @param targetId 被操作对象的业务标识
 * @param action 操作动作名称
 * @param content 操作的可读描述
 * @param outcome 操作执行结果
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLog(String module, String targetId, String action, String content, AuditLogOutcome outcome) {

    /** 创建字段完整的审计内容。 */
    public AuditLog {
        module = AuditLog.requireNonBlank(module, "module");
        targetId = AuditLog.requireNonBlank(targetId, "targetId");
        action = AuditLog.requireNonBlank(action, "action");
        content = AuditLog.requireNonBlank(content, "content");
        Objects.requireNonNull(outcome, "outcome");
    }

    /** 创建一条成功操作的审计内容。 */
    public static AuditLog success(
            final String module, final String targetId, final String action, final String content) {
        return new AuditLog(module, targetId, action, content, AuditLogOutcome.SUCCESS);
    }

    /** 创建一条失败操作的审计内容。 */
    public static AuditLog failure(
            final String module, final String targetId, final String action, final String content) {
        return new AuditLog(module, targetId, action, content, AuditLogOutcome.FAILURE);
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    /**
     * 审计操作的执行结果。
     *
     * @author GuoYang create on 2026/8/17 11:53
     */
    public enum AuditLogOutcome {
        /** 操作成功完成。 */
        SUCCESS,
        /** 操作执行失败。 */
        FAILURE
    }
}
