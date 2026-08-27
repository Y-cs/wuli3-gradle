package com.kjs.wuli3.audit.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.audit.AuditLogCommand;
import com.kjs.wuli3.audit.AuditLogPage;
import com.kjs.wuli3.audit.AuditLogPayload;
import com.kjs.wuli3.audit.AuditLogQuery;
import com.kjs.wuli3.audit.AuditLogQueryClient;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.AuditLogView;
import com.kjs.wuli3.audit.store.AuditLogStore;
import com.kjs.wuli3.audit.transport.AuditLogWriteTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AuditLogAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditLogAutoConfiguration.class))
            .withPropertyValues("spring.application.name=orders");

    @Test
    void doesNotCreateARecorderWithoutADurableTransport() {
        this.contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(AuditLogRecorder.class);
            assertThat(context).doesNotHaveBean(AuditLogQueryClient.class);
        });
    }

    @Test
    void configuresRecorderForAnApplicationTransport() {
        this.contextRunner
                .withBean(RecordingTransport.class, RecordingTransport::new)
                .run(context -> {
                    final AuditLogRecorder recorder = context.getBean(AuditLogRecorder.class);
                    final RecordingTransport transport = context.getBean(RecordingTransport.class);

                    recorder.record(AuditLogCommand.success("ORDER", "order-1", "CREATE", "created"));

                    assertThat(transport.events)
                            .singleElement()
                            .satisfies(event ->
                                    assertThat(event.payload().application()).isEqualTo("orders"));
                });
    }

    @Test
    void adaptsAnAuditServiceStoreForLocalWriteAndQuery() {
        this.contextRunner.withBean(RecordingStore.class, RecordingStore::new).run(context -> {
            final AuditLogRecorder recorder = context.getBean(AuditLogRecorder.class);
            final AuditLogQueryClient queryClient = context.getBean(AuditLogQueryClient.class);
            final RecordingStore store = context.getBean(RecordingStore.class);

            recorder.record(AuditLogCommand.success("ORDER", "order-1", "CREATE", "created"));
            final AuditLogPage page = queryClient.query(AuditLogQuery.all(0, 20));

            assertThat(store.events).hasSize(1);
            assertThat(page).isEqualTo(new AuditLogPage(List.of(), 0, 0, 20));
        });
    }

    private static final class RecordingTransport implements AuditLogWriteTransport {

        private final List<EventEnvelope<AuditLogPayload>> events = new ArrayList<>();

        @Override
        public void append(final EventEnvelope<AuditLogPayload> event) {
            this.events.add(event);
        }
    }

    private static final class RecordingStore implements AuditLogStore {

        private final List<EventEnvelope<AuditLogPayload>> events = new ArrayList<>();

        @Override
        public AuditLogView append(final EventEnvelope<AuditLogPayload> event) {
            this.events.add(event);
            return AuditLogView.from(1L, event.occurredOn(), event);
        }

        @Override
        public AuditLogPage query(final AuditLogQuery query) {
            return new AuditLogPage(List.of(), 0, query.pageNumber(), query.pageSize());
        }
    }
}
