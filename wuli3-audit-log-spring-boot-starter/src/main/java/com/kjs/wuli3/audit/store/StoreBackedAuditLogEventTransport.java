package com.kjs.wuli3.audit.store;

import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.audit.publish.AuditLogPublishOptions;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.remote.RoutingEventTransport;
import java.util.Arrays;
import java.util.Objects;

/**
 * 独立审计服务内部直接委托给持久化端口的写路径传输实现。
 *
 * <p>实现 {@code RoutingEventTransport} 而非 {@code RemoteEventTransport}：本实现是进程内直写数据库，
 * 不是跨进程投递，不应参与 {@code @ConditionalOnMissingRemoteEventTransport} 的条件判断；但仍需要被
 * {@code EventAutoConfiguration} 自动收集进 {@code RoutingEventPublisher}，因此选用语义更贴切的
 * {@code RoutingEventTransport}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class StoreBackedAuditLogEventTransport implements RoutingEventTransport<AuditLogPublishOptions> {

    private final AuditLogStore auditLogStore;

    /**
     * 创建基于持久化端口的写路径传输实现。
     *
     * @param auditLogStore 持久化端口
     */
    public StoreBackedAuditLogEventTransport(final AuditLogStore auditLogStore) {
        this.auditLogStore = Objects.requireNonNull(auditLogStore, "auditLogStore");
    }

    @Override
    public Class<AuditLogPublishOptions> supportedOptionsType() {
        return AuditLogPublishOptions.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void send(final AuditLogPublishOptions options, final EventEnvelope<?>... envelopes) {
        Objects.requireNonNull(options, "options");
        Arrays.stream(Objects.requireNonNull(envelopes, "envelopes"))
                .map(envelope -> (EventEnvelope<AuditLogPayload>) envelope)
                .forEach(this.auditLogStore::append);
    }
}
