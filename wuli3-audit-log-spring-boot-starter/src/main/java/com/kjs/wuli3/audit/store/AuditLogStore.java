package com.kjs.wuli3.audit.store;

import com.kjs.wuli3.audit.AuditLogPage;
import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.audit.AuditLogQuery;
import com.kjs.wuli3.audit.AuditLogView;
import com.kjs.wuli3.event.envelope.EventEnvelope;

/**
 * 独立审计服务实现的持久化端口。
 *
 * <p>{@link #append(EventEnvelope)} 必须以 {@code eventId} 幂等写入，并由数据库生成 {@code logId}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface AuditLogStore {

    /** 幂等持久化审计事件并返回数据库视图。 */
    AuditLogView append(EventEnvelope<AuditLogPayload> event);

    /** 查询已持久化的审计日志。 */
    AuditLogPage query(AuditLogQuery query);
}
