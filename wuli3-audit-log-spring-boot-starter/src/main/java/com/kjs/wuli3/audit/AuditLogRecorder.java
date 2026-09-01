package com.kjs.wuli3.audit;

/**
 * 创建并发送包含当前调用上下文的审计事件。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuditLogRecorder {

    /**
     * 记录一次审计操作，默认在当前事务提交后发送。
     *
     * @param entry 业务方提供的审计内容
     * @return 事件写入回执
     */
    default AuditLogReceipt record(final AuditLogEntry entry) {
        return this.record(entry, true);
    }

    /**
     * 记录一次审计操作并指定是否在事务提交后发送。
     *
     * @param entry 业务方提供的审计内容
     * @param afterCommit 是否延后到当前事务提交后再发布
     * @return 事件写入回执
     */
    AuditLogReceipt record(AuditLogEntry entry, boolean afterCommit);
}
