package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogPage;
import com.kjs.wuli3.audit.AuditLogQuery;
import com.kjs.wuli3.audit.AuditLogQueryClient;
import com.kjs.wuli3.audit.transport.AuditLogQueryTransport;
import java.util.Objects;

/**
 * 将应用查询入口委托给已配置查询 Transport。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultAuditLogQueryClient implements AuditLogQueryClient {

    private final AuditLogQueryTransport queryTransport;

    public DefaultAuditLogQueryClient(final AuditLogQueryTransport queryTransport) {
        this.queryTransport = Objects.requireNonNull(queryTransport, "queryTransport");
    }

    @Override
    public AuditLogPage query(final AuditLogQuery query) {
        return this.queryTransport.query(Objects.requireNonNull(query, "query"));
    }
}
