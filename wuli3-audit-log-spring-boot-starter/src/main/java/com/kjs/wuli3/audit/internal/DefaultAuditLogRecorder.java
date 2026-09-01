package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogEntry;
import com.kjs.wuli3.audit.AuditLogProtocol;
import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.payload.AuditLogOrigin;
import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.audit.payload.AuditPrincipal;
import com.kjs.wuli3.audit.publish.AuditLogPublishOptions;
import com.kjs.wuli3.core.id.IdGenerator;
import com.kjs.wuli3.core.time.ClockProvider;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * 默认的上下文感知审计日志记录器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultAuditLogRecorder implements AuditLogRecorder {

    private final String application;
    private final EventPublisher eventPublisher;
    private final @Nullable ContextReader contextReader;
    private final @Nullable TraceContextAccessor traceContextAccessor;
    private final ClockProvider clockProvider;
    private final IdGenerator<String> eventIdGenerator;

    /**
     * 创建使用指定上下文和标识来源的记录器。
     *
     * @param application 应用名称
     * @param eventPublisher 事件发布器
     * @param contextReader 上下文读取器；可为 {@code null}
     * @param traceContextAccessor 追踪上下文访问器；可为 {@code null}
     * @param clockProvider 时钟提供器
     * @param eventIdGenerator 事件标识生成器
     */
    public DefaultAuditLogRecorder(
            final String application,
            final EventPublisher eventPublisher,
            final @Nullable ContextReader contextReader,
            final @Nullable TraceContextAccessor traceContextAccessor,
            final ClockProvider clockProvider,
            final IdGenerator<String> eventIdGenerator) {
        this.application = DefaultAuditLogRecorder.requireNonBlank(application, "application");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.contextReader = contextReader;
        this.traceContextAccessor = traceContextAccessor;
        this.clockProvider = Objects.requireNonNull(clockProvider, "clockProvider");
        this.eventIdGenerator = Objects.requireNonNull(eventIdGenerator, "eventIdGenerator");
    }

    @Override
    public AuditLogReceipt record(final AuditLogEntry entry) {
        return this.record(entry, true);
    }

    @Override
    public AuditLogReceipt record(final AuditLogEntry entry, final boolean afterCommit) {
        final AuditLogEntry actualEntry = Objects.requireNonNull(entry, "entry");
        final Instant occurredAt = this.clockProvider.instant();
        final String eventId = DefaultAuditLogRecorder.requireNonBlank(this.eventIdGenerator.nextId(), "eventId");
        final AuditLogOrigin origin = this.buildOrigin();
        final AuditLogPayload payload = new AuditLogPayload(AuditLogProtocol.SCHEMA_VERSION, actualEntry, origin);
        final EventEnvelope<AuditLogPayload> envelope =
                new EventEnvelope<>(AuditLogProtocol.TOPIC, AuditLogProtocol.EVENT_TYPE, eventId, occurredAt, payload);
        this.eventPublisher.publish(AuditLogPublishOptions.of(afterCommit), envelope);
        return new AuditLogReceipt(eventId, occurredAt);
    }

    private AuditLogOrigin buildOrigin() {
        final Optional<AuthContext> authContext = this.context(AuthContext.class);
        final Optional<InvocationContext> invocationContext = this.context(InvocationContext.class);
        final Optional<TraceContext> traceContext = this.traceContext();
        return new AuditLogOrigin(
                this.application,
                authContext.map(DefaultAuditLogRecorder::principal).orElse(null),
                invocationContext.map(InvocationContext::requestId).orElse(null),
                traceContext.map(TraceContext::traceId).orElse(null),
                traceContext.map(TraceContext::spanId).orElse(null),
                invocationContext.map(InvocationContext::originIp).orElse(null));
    }

    private <T extends com.kjs.wuli3.propagation.context.Context> Optional<T> context(final Class<T> type) {
        return this.contextReader == null ? Optional.empty() : this.contextReader.get(type);
    }

    private Optional<TraceContext> traceContext() {
        return this.traceContextAccessor == null ? Optional.empty() : this.traceContextAccessor.current();
    }

    private static AuditPrincipal principal(final AuthContext context) {
        return new AuditPrincipal(context.principalType(), context.principalId(), context.principalName());
    }

    private static String requireNonBlank(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}
