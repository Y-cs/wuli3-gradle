package com.kjs.wuli3.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.audit.AuditLogEntry;
import com.kjs.wuli3.audit.AuditLogReceipt;
import com.kjs.wuli3.audit.payload.AuditLogOrigin;
import com.kjs.wuli3.audit.payload.AuditPrincipal;
import com.kjs.wuli3.audit.publish.AuditLogPublishOptions;
import com.kjs.wuli3.core.time.ClockProvider;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.opentelemetry.trace.TraceContext;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultAuditLogRecorderTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-08-17T00:00:00Z");

    @Test
    void capturesContextBeforePublishingTheEvent() {
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new AuthContext(PrincipalType.ADMIN, "42", "alice"));
        contextStore.put(new InvocationContext("203.0.113.8", "request-1"));
        final RecordingEventPublisher publisher = new RecordingEventPublisher();
        final DefaultAuditLogRecorder recorder = new DefaultAuditLogRecorder(
                "orders",
                publisher,
                contextStore,
                () -> Optional.of(new TraceContext("trace-1", "span-1")),
                ClockProvider.fixed(DefaultAuditLogRecorderTest.OCCURRED_AT, ZoneOffset.UTC),
                () -> "event-1");

        final AuditLogReceipt receipt =
                recorder.record(AuditLogEntry.success("ORDER", "order-7", "CREATE", "created order"));

        assertThat(receipt).isEqualTo(new AuditLogReceipt("event-1", DefaultAuditLogRecorderTest.OCCURRED_AT));
        assertThat(publisher.envelopes()).singleElement().satisfies(event -> {
            assertThat(event.topic()).isEqualTo("audit-log");
            assertThat(event.eventType()).isEqualTo("audit.log.recorded.v1");
            assertThat(event.eventId()).isEqualTo("event-1");
            assertThat(event.occurredOn()).isEqualTo(DefaultAuditLogRecorderTest.OCCURRED_AT);
            assertThat(event.payload().entry())
                    .isEqualTo(AuditLogEntry.success("ORDER", "order-7", "CREATE", "created order"));
            assertThat(event.payload().origin())
                    .isEqualTo(new AuditLogOrigin(
                            "orders",
                            new AuditPrincipal(PrincipalType.ADMIN, "42", "alice"),
                            "request-1",
                            "trace-1",
                            "span-1",
                            "203.0.113.8"));
        });
    }

    @Test
    void recordsWithoutOptionalContexts() {
        final RecordingEventPublisher publisher = new RecordingEventPublisher();
        final DefaultAuditLogRecorder recorder = new DefaultAuditLogRecorder(
                "jobs",
                publisher,
                null,
                null,
                ClockProvider.fixed(DefaultAuditLogRecorderTest.OCCURRED_AT, ZoneOffset.UTC),
                () -> "event-2");

        recorder.record(AuditLogEntry.failure("JOB", "job-9", "RUN", "job failed"));

        assertThat(publisher.envelopes())
                .singleElement()
                .extracting(EventEnvelope::payload)
                .satisfies(payload -> assertThat(payload.origin()).isEqualTo(AuditLogOrigin.ofApplication("jobs")));
    }

    @Test
    void routesThroughTheAuditPublishOptionsAndDefaultsToAfterCommit() {
        final RecordingEventPublisher publisher = new RecordingEventPublisher();
        final DefaultAuditLogRecorder recorder = new DefaultAuditLogRecorder(
                "orders",
                publisher,
                null,
                null,
                ClockProvider.fixed(DefaultAuditLogRecorderTest.OCCURRED_AT, ZoneOffset.UTC),
                () -> "event-3");

        recorder.record(AuditLogEntry.success("ORDER", "order-1", "CREATE", "created"));
        recorder.record(AuditLogEntry.success("ORDER", "order-2", "CREATE", "created"), false);

        assertThat(publisher.options())
                .containsExactly(AuditLogPublishOptions.AFTER_COMMIT, AuditLogPublishOptions.IMMEDIATE);
    }
}
