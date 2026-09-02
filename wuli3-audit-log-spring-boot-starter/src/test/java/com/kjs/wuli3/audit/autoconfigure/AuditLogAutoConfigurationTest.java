package com.kjs.wuli3.audit.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.audit.payload.AuditLog;
import com.kjs.wuli3.audit.AuditLogRecorder;
import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.audit.protocol.AuditLogPublishOptions;
import com.kjs.wuli3.audit.protocol.AuditLogStore;
import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class AuditLogAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AopAutoConfiguration.class,
                    EventAutoConfiguration.class,
                    AuditLogStoreAutoConfiguration.class,
                    AuditLogAutoConfiguration.class))
            .withPropertyValues("spring.application.name=orders");

    @Test
    void configuresRecorderOnTopOfTheSharedEventPublisher() {
        this.contextRunner
                .withBean(RecordingTransport.class, RecordingTransport::new)
                .run(context -> {
                    final AuditLogRecorder recorder = context.getBean(AuditLogRecorder.class);
                    final RecordingTransport transport = context.getBean(RecordingTransport.class);

                    recorder.record(AuditLog.success("ORDER", "order-1", "CREATE", "created"));

                    assertThat(transport.envelopes)
                            .singleElement()
                            .satisfies(event -> assertThat(
                                            event.payload().runtimeSnapshot().application())
                                    .isEqualTo("orders"));
                });
    }

    @Test
    void appliesTheAnnotationDrivenAdvisorToApplicationBeans() {
        this.contextRunner
                .withBean(RecordingTransport.class, RecordingTransport::new)
                .withUserConfiguration(AuditedServiceConfiguration.class)
                .run(context -> {
                    final AuditedService service = context.getBean(AuditedService.class);
                    final RecordingTransport transport = context.getBean(RecordingTransport.class);

                    service.rename("order-3", "new name");

                    assertThat(transport.envelopes).singleElement().satisfies(event -> {
                        assertThat(event.payload().auditLog().targetId()).isEqualTo("order-3");
                        assertThat(event.payload().auditLog().content()).isEqualTo("重命名为 new name");
                    });
                });
    }

    @Test
    void adaptsAnAuditServiceStoreForLocalWrite() {
        this.contextRunner.withBean(RecordingStore.class, RecordingStore::new).run(context -> {
            final AuditLogRecorder recorder = context.getBean(AuditLogRecorder.class);
            final RecordingStore store = context.getBean(RecordingStore.class);

            recorder.record(AuditLog.success("ORDER", "order-1", "CREATE", "created"));

            assertThat(store.envelopes).hasSize(1);
        });
    }

    @Test
    void backsOffEntirelyWhenDisabled() {
        this.contextRunner
                .withBean(RecordingStore.class, RecordingStore::new)
                .withPropertyValues("wuli3.audit-log.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditLogRecorder.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class AuditedServiceConfiguration {

        @Bean
        AuditedService auditedService() {
            return new AuditedService();
        }
    }

    static class AuditedService {

        @com.kjs.wuli3.audit.annotation.AuditLog(
                module = "ORDER",
                action = "RENAME",
                targetId = "#{#orderId}",
                content = "重命名为 #{#name}")
        public void rename(final String orderId, final String name) {
            // 仅用于验证注解驱动的记录路径
        }
    }

    private static final class RecordingTransport implements RemoteEventTransport<AuditLogPublishOptions> {

        private final List<EventEnvelope<AuditLogPayload>> envelopes = new ArrayList<>();

        @Override
        public Class<AuditLogPublishOptions> supportedOptionsType() {
            return AuditLogPublishOptions.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void send(final AuditLogPublishOptions options, final EventEnvelope<?>... events) {
            Arrays.stream(events)
                    .map(event -> (EventEnvelope<AuditLogPayload>) event)
                    .forEach(this.envelopes::add);
        }
    }

    private static final class RecordingStore implements AuditLogStore {

        private final List<EventEnvelope<AuditLogPayload>> envelopes = new ArrayList<>();

        @Override
        public void append(final EventEnvelope<AuditLogPayload> event) {
            this.envelopes.add(event);
        }
    }
}
