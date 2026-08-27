package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogPage;
import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.audit.AuditLogQuery;
import com.kjs.wuli3.audit.store.AuditLogStore;
import com.kjs.wuli3.audit.transport.AuditLogQueryTransport;
import com.kjs.wuli3.audit.transport.AuditLogWriteTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import java.util.Objects;

/**
 * 独立审计服务内部直接委托给持久化端口的本地 Transport。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class StoreBackedAuditLogTransport implements AuditLogWriteTransport, AuditLogQueryTransport {

    private final AuditLogStore auditLogStore;

    public StoreBackedAuditLogTransport(final AuditLogStore auditLogStore) {
        this.auditLogStore = Objects.requireNonNull(auditLogStore, "auditLogStore");
    }

    @Override
    public void append(final EventEnvelope<AuditLogPayload> event) {
        this.auditLogStore.append(Objects.requireNonNull(event, "event"));
    }

    @Override
    public AuditLogPage query(final AuditLogQuery query) {
        return this.auditLogStore.query(Objects.requireNonNull(query, "query"));
    }
}
