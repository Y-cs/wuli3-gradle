package com.kjs.wuli3.audit.transport;

import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.event.envelope.EventEnvelope;

/**
 * 将审计事件写入独立审计服务或本地持久化 Outbox。
 *
 * <p>方法正常返回表示事件已经被远端持久接收，或已经进入能够恢复投递的本地持久化 Outbox。实现必须允许至少一次投递，
 * 并以事件信封的 {@code eventId} 作为消费端幂等键；不得在仅提交到内存队列后返回成功。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface AuditLogWriteTransport {

    /** 持久接收一条审计事件；无法满足契约时应抛出运行时异常。 */
    void append(EventEnvelope<AuditLogPayload> event);

    // TODO: provide an outbox-backed implementation before remote audit delivery is considered lossless.
}
