package com.kjs.wuli3.audit.payload;

import com.kjs.wuli3.audit.AuditLogEntry;
import java.util.Objects;

/**
 * 可发送到独立审计服务的不可变审计载荷。
 *
 * <p>事件唯一标识和发生时间由外层事件信封提供；本对象把业务内容（{@link AuditLogEntry}）
 * 和事件创建时的来源快照（{@link AuditLogOrigin}）分开固化。
 *
 * @param schemaVersion 审计载荷结构版本
 * @param entry 业务方描述的审计内容
 * @param origin 框架补全的来源快照
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogPayload(int schemaVersion, AuditLogEntry entry, AuditLogOrigin origin) {

    /** 创建字段完整的审计载荷。 */
    public AuditLogPayload {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be greater than zero");
        }
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(origin, "origin");
    }
}
