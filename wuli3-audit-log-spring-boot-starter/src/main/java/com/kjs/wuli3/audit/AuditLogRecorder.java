package com.kjs.wuli3.audit;

/**
 * 创建并发送包含当前调用上下文的审计事件。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuditLogRecorder {

    /**
     * 记录一次审计操作。
     *
     * @param command 业务方提供的审计内容
     * @return 事件写入回执；其中不包含由独立审计服务生成的数据库 logId
     */
    AuditLogReceipt record(AuditLogCommand command);
}
