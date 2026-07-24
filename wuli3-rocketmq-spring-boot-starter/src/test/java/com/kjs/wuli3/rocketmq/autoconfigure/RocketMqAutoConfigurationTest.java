package com.kjs.wuli3.rocketmq.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import com.kjs.wuli3.rocketmq.internal.RocketMqRemoteEventMessageTransport;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RocketMqAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RocketMqAutoConfiguration.class))
            .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class));

    @Test
    void registersTheLegacyTransportWhenATemplateIsAvailable() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RemoteEventMessageTransport.class);
            assertThat(context).hasSingleBean(RocketMqRemoteEventMessageTransport.class);
        });
    }

    @Test
    void backsOffForAnApplicationTransport() {
        this.contextRunner
                .withBean(RemoteEventMessageTransport.class, NoopRemoteTransport::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(RemoteEventMessageTransport.class);
                    assertThat(context).doesNotHaveBean(RocketMqRemoteEventMessageTransport.class);
                });
    }

    private static final class NoopRemoteTransport implements RemoteEventMessageTransport {

        @Override
        public void send(
                final com.kjs.wuli3.event.EventEnvelope<?> envelope,
                final com.kjs.wuli3.event.PublishOptions options) {}

        @Override
        public void sends(
                final java.util.Collection<com.kjs.wuli3.event.EventEnvelope<?>> envelopes,
                final com.kjs.wuli3.event.PublishOptions options) {}
    }
}
