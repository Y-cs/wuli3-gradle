package com.kjs.wuli3.audit.store;

import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.event.envelope.EventEnvelope;

/**
 * 独立审计服务实现的持久化端口。
 *
 * <p>{@link #append(EventEnvelope)} 必须以 {@code eventId} 幂等写入。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface AuditLogStore {

    /**
     * 幂等持久化审计事件。
     *
     * @param event 待持久化的审计事件
     */
    void append(EventEnvelope<AuditLogPayload> event);
}
