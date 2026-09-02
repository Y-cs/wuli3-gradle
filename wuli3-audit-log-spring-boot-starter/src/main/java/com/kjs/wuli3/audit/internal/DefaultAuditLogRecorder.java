package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.payload.AuditLog;
import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.audit.payload.AuditLogRuntimeSnapshot;
import com.kjs.wuli3.audit.protocol.AuditLogProtocolConstants;
import com.kjs.wuli3.audit.protocol.AuditLogPublishOptions;
import com.kjs.wuli3.core.assertion.Asserts;
import com.kjs.wuli3.core.time.ClockProvider;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.envelope.EventEnvelopeTemplate;
import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.Context;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认的上下文感知审计日志记录器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultAuditLogRecorder implements AuditLogRecorder {

    private static final EventEnvelopeTemplate ENVELOPE_TEMPLATE =
            EventEnvelopeTemplate.of(
                    AuditLogProtocolConstants.TOPIC, AuditLogProtocolConstants.EVENT_TYPE);

    private final String application;
    private final EventPublisher eventPublisher;
    private final @Nullable ContextReader contextReader;
    private final @Nullable TraceContextAccessor traceContextAccessor;

    /**
     * 创建使用指定上下文和标识来源的记录器。
     *
     * @param application 应用名称
     * @param eventPublisher 事件发布器
     * @param contextReader 上下文读取器；可为 {@code null}
     * @param traceContextAccessor 追踪上下文访问器；可为 {@code null}
     */
    public DefaultAuditLogRecorder(
            final String application,
            final EventPublisher eventPublisher,
            final @Nullable ContextReader contextReader,
            final @Nullable TraceContextAccessor traceContextAccessor) {
        Asserts.whenBlank(application).throwIllegalArgumentException("application cannot be blank");
        this.application = application;
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
        this.contextReader = contextReader;
        this.traceContextAccessor = traceContextAccessor;
    }

    @Override
    public AuditLogReceipt record(final AuditLog entry) {
        return this.record(entry, true);
    }

    @Override
    public AuditLogReceipt record(final AuditLog entry, final boolean afterCommit) {
        final AuditLog auditLog = Objects.requireNonNull(entry, "auditLog");
        final AuditLogRuntimeSnapshot runtimeSnapshot = this.captureRuntimeSnapshot();
        final AuditLogPayload payload = new AuditLogPayload(auditLog, runtimeSnapshot);
        final EventEnvelope<AuditLogPayload> envelope = ENVELOPE_TEMPLATE.wrap(payload);
        this.eventPublisher.publish(AuditLogPublishOptions.of(afterCommit), envelope);
        return new AuditLogReceipt(envelope.eventId(), envelope.occurredOn());
    }

    private AuditLogRuntimeSnapshot captureRuntimeSnapshot() {
        final Optional<AuthContext> authContext = this.context(AuthContext.class);
        final Optional<InvocationContext> invocationContext = this.context(InvocationContext.class);
        final Optional<TraceContext> traceContext = this.traceContext();
        return new AuditLogRuntimeSnapshot(
                this.application,
                authContext.map(DefaultAuditLogRecorder::principal).orElse(null),
                traceContext.map(DefaultAuditLogRecorder::trace).orElse(null),
                invocationContext.map(DefaultAuditLogRecorder::invocation).orElse(null));
    }

    private <T extends Context> Optional<T> context(final Class<T> type) {
        return this.contextReader == null ? Optional.empty() : this.contextReader.get(type);
    }

    private Optional<TraceContext> traceContext() {
        return this.traceContextAccessor == null ? Optional.empty() : this.traceContextAccessor.current();
    }

    private static AuditLogRuntimeSnapshot.AuditPrincipal principal(final AuthContext context) {
        return new AuditLogRuntimeSnapshot.AuditPrincipal(
                context.principalType(), context.principalId(), context.principalName());
    }

    private static AuditLogRuntimeSnapshot.AuditTrace trace(final TraceContext context) {
        return new AuditLogRuntimeSnapshot.AuditTrace(context.traceId(), context.spanId());
    }

    private static AuditLogRuntimeSnapshot.AuditInvocation invocation(final InvocationContext context) {
        return new AuditLogRuntimeSnapshot.AuditInvocation(context.requestId(), context.originIp());
    }
}
