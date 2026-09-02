package com.kjs.wuli3.audit.payload;

import java.util.Objects;

/**
 * 可发送到独立审计服务的不可变审计载荷。
 *
 * <p>事件唯一标识和发生时间由外层事件信封提供；本对象把业务内容（{@link AuditLog}）
 * 和事件创建时的来源快照（{@link AuditLogRuntimeSnapshot}）分开固化。
 *
 * @param auditLog 业务方描述的审计内容
 * @param runtimeSnapshot 框架补全的运行时快照
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogPayload(AuditLog auditLog, AuditLogRuntimeSnapshot runtimeSnapshot) {

    /** 创建字段完整的审计载荷。 */
    public AuditLogPayload {
        Objects.requireNonNull(auditLog, "auditLog");
        Objects.requireNonNull(runtimeSnapshot, "runtimeSnapshot");
    }
}
