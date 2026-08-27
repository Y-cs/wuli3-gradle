package com.kjs.wuli3.audit;

/**
 * 查询独立审计服务的应用侧入口。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuditLogQueryClient {

    /** 使用指定条件查询审计日志。 */
    AuditLogPage query(AuditLogQuery query);
}
