package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.AuditLogCommand;
import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.audit.AuditLogProtocol;
import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.AuditPrincipal;
import com.kjs.wuli3.audit.transport.AuditLogWriteTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.opentelemetry.trace.TraceContextAccessor;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * 默认的上下文感知审计日志记录器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultAuditLogRecorder implements AuditLogRecorder {

    private final String application;
    private final AuditLogWriteTransport writeTransport;
    private final @Nullable ContextReader contextReader;
    private final @Nullable TraceContextAccessor traceContextAccessor;
    private final Clock clock;
    private final Supplier<String> eventIdSupplier;

    /** 创建使用指定上下文和标识来源的记录器。 */
    public DefaultAuditLogRecorder(
            final String application,
            final AuditLogWriteTransport writeTransport,
            final @Nullable ContextReader contextReader,
            final @Nullable TraceContextAccessor traceContextAccessor,
            final Clock clock,
            final Supplier<String> eventIdSupplier) {
        this.application = DefaultAuditLogRecorder.requireNonBlank(application, "application");
        this.writeTransport = Objects.requireNonNull(writeTransport, "writeTransport");
        this.contextReader = contextReader;
        this.traceContextAccessor = traceContextAccessor;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.eventIdSupplier = Objects.requireNonNull(eventIdSupplier, "eventIdSupplier");
    }

    @Override
    public AuditLogReceipt record(final AuditLogCommand command) {
        final AuditLogCommand actualCommand = Objects.requireNonNull(command, "command");
        final Instant occurredAt = this.clock.instant();
        final String eventId = DefaultAuditLogRecorder.requireNonBlank(this.eventIdSupplier.get(), "eventId");
        final Optional<AuthContext> authContext = this.context(AuthContext.class);
        final Optional<InvocationContext> invocationContext = this.context(InvocationContext.class);
        final Optional<TraceContext> traceContext = this.traceContext();
        final AuditLogPayload payload = new AuditLogPayload(
                AuditLogProtocol.SCHEMA_VERSION,
                this.application,
                actualCommand.module(),
                actualCommand.targetId(),
                actualCommand.action(),
                actualCommand.content(),
                actualCommand.outcome(),
                authContext.map(DefaultAuditLogRecorder::principal).orElse(null),
                invocationContext.map(InvocationContext::requestId).orElse(null),
                traceContext.map(TraceContext::traceId).orElse(null),
                traceContext.map(TraceContext::spanId).orElse(null),
                invocationContext.map(InvocationContext::originIp).orElse(null));
        final EventEnvelope<AuditLogPayload> event =
                new EventEnvelope<>(AuditLogProtocol.TOPIC, AuditLogProtocol.EVENT_TYPE, eventId, occurredAt, payload);
        this.writeTransport.append(event);
        return new AuditLogReceipt(eventId, occurredAt);
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
