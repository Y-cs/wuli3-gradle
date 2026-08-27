package com.kjs.wuli3.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.audit.AuditLogCommand;
import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.AuditPrincipal;
import com.kjs.wuli3.audit.transport.AuditLogWriteTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultAuditLogRecorderTest {

    @Test
    void capturesContextBeforeWritingTheEvent() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new AuthContext(PrincipalType.ADMIN, "42", "alice"));
        contextStore.put(new InvocationContext("203.0.113.8", "request-1"));
        final RecordingTransport transport = new RecordingTransport();
        final Instant occurredAt = Instant.parse("2026-08-17T00:00:00Z");
        final DefaultAuditLogRecorder recorder = new DefaultAuditLogRecorder(
                "orders",
                transport,
                contextStore,
                () -> Optional.of(new TraceContext("trace-1", "span-1")),
                Clock.fixed(occurredAt, ZoneOffset.UTC),
                () -> "event-1");

        final AuditLogReceipt receipt =
                recorder.record(AuditLogCommand.success("ORDER", "order-7", "CREATE", "created order"));

        assertThat(receipt).isEqualTo(new AuditLogReceipt("event-1", occurredAt));
        assertThat(transport.events).singleElement().satisfies(event -> {
            assertThat(event.topic()).isEqualTo("audit-log");
            assertThat(event.eventType()).isEqualTo("audit.log.recorded.v1");
            assertThat(event.eventId()).isEqualTo("event-1");
            assertThat(event.occurredOn()).isEqualTo(occurredAt);
            assertThat(event.payload().operator()).isEqualTo(new AuditPrincipal(PrincipalType.ADMIN, "42", "alice"));
            assertThat(event.payload().requestId()).isEqualTo("request-1");
            assertThat(event.payload().traceId()).isEqualTo("trace-1");
            assertThat(event.payload().spanId()).isEqualTo("span-1");
            assertThat(event.payload().originIp()).isEqualTo("203.0.113.8");
        });
    }

    @Test
    void recordsWithoutOptionalContexts() {
        final RecordingTransport transport = new RecordingTransport();
        final DefaultAuditLogRecorder recorder =
                new DefaultAuditLogRecorder("jobs", transport, null, null, Clock.systemUTC(), () -> "event-2");

        recorder.record(AuditLogCommand.failure("JOB", "job-9", "RUN", "job failed"));

        assertThat(transport.events)
                .singleElement()
                .extracting(EventEnvelope::payload)
                .satisfies(payload -> {
                    assertThat(payload.operator()).isNull();
                    assertThat(payload.requestId()).isNull();
                    assertThat(payload.traceId()).isNull();
                    assertThat(payload.spanId()).isNull();
                    assertThat(payload.originIp()).isNull();
                });
    }

    private static final class RecordingTransport implements AuditLogWriteTransport {

        private final List<EventEnvelope<AuditLogPayload>> events = new ArrayList<>();

        @Override
        public void append(final EventEnvelope<AuditLogPayload> event) {
            this.events.add(event);
        }
    }
}
