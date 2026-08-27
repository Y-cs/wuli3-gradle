package com.kjs.wuli3.audit.transport;

import com.kjs.wuli3.audit.AuditLogPage;
import com.kjs.wuli3.audit.AuditLogQuery;

/**
 * 通过同步 HTTP、RPC 或其他请求响应协议查询独立审计服务。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuditLogQueryTransport {

    /** 执行一次远程审计日志查询。 */
    AuditLogPage query(AuditLogQuery query);
}
