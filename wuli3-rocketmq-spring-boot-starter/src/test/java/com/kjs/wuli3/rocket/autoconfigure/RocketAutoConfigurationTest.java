package com.kjs.wuli3.rocket.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.rocket.internal.RocketContextSupport;
import com.kjs.wuli3.rocket.internal.RocketRemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import java.util.Collection;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RocketAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RocketAutoConfiguration.class))
            .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class));

    @Test
    void registersDefaultEncoderAndTransportWhenTemplateIsAvailable() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RocketMessageWrapperEncoder.class);
            assertThat(context).hasSingleBean(RemoteEventTransport.class);
            assertThat(context).hasSingleBean(RocketRemoteEventTransport.class);
            assertThat(context).doesNotHaveBean(RocketContextSupport.class);
        });
    }

    @Test
    void registersInboundContextSupportWhenAWriterIsAvailable() {
        this.contextRunner.withBean(ContextStore.class, ContextStore::new).run(context -> {
            assertThat(context).hasSingleBean(RocketContextSupport.class);
        });
    }

    @Test
    void backsOffForApplicationProvidedBeans() {
        this.contextRunner
                .withBean(RemoteEventTransport.class, NoopRemoteTransport::new)
                .withBean(RocketMessageWrapperEncoder.class, () -> new RocketMessageWrapperEncoder(null,
                        new com.kjs.wuli3.propagation.encoding.ContextEncoder(java.util.List.of())))
                .run(context -> {
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RocketRemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketMessageWrapperEncoder.class);
                });
    }

    private static final class NoopRemoteTransport implements RemoteEventTransport {

        @Override
        public void send(
                final com.kjs.wuli3.event.EventEnvelope<?> envelope,
                final com.kjs.wuli3.event.PublishOptions options) {}

        @Override
        public void sends(
                final Collection<com.kjs.wuli3.event.EventEnvelope<?>> envelopes,
                final com.kjs.wuli3.event.PublishOptions options) {}
    }
}
