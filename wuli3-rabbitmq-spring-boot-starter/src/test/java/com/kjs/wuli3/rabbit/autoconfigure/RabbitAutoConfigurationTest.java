package com.kjs.wuli3.rabbit.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.rabbit.internal.RabbitContextSupport;
import com.kjs.wuli3.rabbit.internal.RabbitMessageEncoder;
import com.kjs.wuli3.rabbit.internal.RabbitPublishOptions;
import com.kjs.wuli3.rabbit.internal.RabbitRemoteEventTransport;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

class RabbitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RabbitAutoConfiguration.class))
            .withUserConfiguration(TestTaskExecutorConfiguration.class);

    @Test
    void registersDefaultEncoderAndTransportWhenTemplateIsAvailable() {
        this.contextRunner
                .withBean(RabbitTemplate.class, () -> mock(RabbitTemplate.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(RabbitMessageEncoder.class);
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RabbitRemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RabbitContextSupport.class);
                });
    }

    @Test
    void doesNotRegisterTransportWithoutARabbitTemplate() {
        this.contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(RemoteEventTransport.class);
            assertThat(context).doesNotHaveBean(RabbitRemoteEventTransport.class);
        });
    }

    @Test
    void registersInboundContextSupportWhenAWriterIsAvailable() {
        this.contextRunner
                .withBean(ContextStore.class, ContextStore::new)
                .run(context -> assertThat(context).hasSingleBean(RabbitContextSupport.class));
    }

    @Test
    void backsOffForApplicationProvidedBeans() {
        this.contextRunner
                .withBean(RabbitTemplate.class, () -> mock(RabbitTemplate.class))
                .withBean(NoopRabbitTransport.class, NoopRabbitTransport::new)
                .withBean(
                        RabbitMessageEncoder.class,
                        () -> new RabbitMessageEncoder(
                                null, new com.kjs.wuli3.propagation.encoding.ContextEncoder(java.util.List.of())))
                .run(context -> {
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RabbitRemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RabbitMessageEncoder.class);
                });
    }

    @Test
    void coexistsWithARemoteTransportForAnotherOptionsType() {
        this.contextRunner
                .withBean(RabbitTemplate.class, () -> mock(RabbitTemplate.class))
                .withUserConfiguration(OtherRemoteTransportConfiguration.class)
                .run(context -> {
                    assertThat(context).getBeans(RemoteEventTransport.class).hasSize(2);
                    assertThat(context).hasSingleBean(RabbitRemoteEventTransport.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestTaskExecutorConfiguration {

        @Bean("applicationTaskExecutor")
        TaskExecutor applicationTaskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OtherRemoteTransportConfiguration {

        @Bean
        RemoteEventTransport<OtherOptions> otherRemoteTransport() {
            return new OtherRemoteTransport();
        }
    }

    private static final class NoopRabbitTransport implements RemoteEventTransport<RabbitPublishOptions> {

        @Override
        public Class<RabbitPublishOptions> supportedOptionsType() {
            return RabbitPublishOptions.class;
        }

        @Override
        public void send(
                final RabbitPublishOptions options, final com.kjs.wuli3.event.envelope.EventEnvelope<?>... envelopes) {}
    }

    private record OtherOptions() implements PublishOptions {}

    private static final class OtherRemoteTransport implements RemoteEventTransport<OtherOptions> {

        @Override
        public Class<OtherOptions> supportedOptionsType() {
            return OtherOptions.class;
        }

        @Override
        public void send(
                final OtherOptions options, final com.kjs.wuli3.event.envelope.EventEnvelope<?>... envelopes) {}
    }
}
