package com.kjs.wuli3.audit;

import java.util.Objects;

/**
 * 业务代码提交的审计日志内容；操作主体和调用链信息由框架从当前上下文补全。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogCommand(String module, String targetId, String action, String content, AuditLogOutcome outcome) {

    public AuditLogCommand {
        module = AuditLogCommand.requireNonBlank(module, "module");
        targetId = AuditLogCommand.requireNonBlank(targetId, "targetId");
        action = AuditLogCommand.requireNonBlank(action, "action");
        content = AuditLogCommand.requireNonBlank(content, "content");
        Objects.requireNonNull(outcome, "outcome");
    }

    /** 创建一条成功操作命令。 */
    public static AuditLogCommand success(
            final String module, final String targetId, final String action, final String content) {
        return new AuditLogCommand(module, targetId, action, content, AuditLogOutcome.SUCCESS);
    }

    /** 创建一条失败操作命令。 */
    public static AuditLogCommand failure(
            final String module, final String targetId, final String action, final String content) {
        return new AuditLogCommand(module, targetId, action, content, AuditLogOutcome.FAILURE);
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}
