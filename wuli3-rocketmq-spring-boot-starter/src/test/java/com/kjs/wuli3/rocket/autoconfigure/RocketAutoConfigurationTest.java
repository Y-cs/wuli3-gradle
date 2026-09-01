package com.kjs.wuli3.rocket.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.rocket.internal.RocketContextSupport;
import com.kjs.wuli3.rocket.internal.RocketPublishOptions;
import com.kjs.wuli3.rocket.internal.RocketRemoteEventTransport;
import com.kjs.wuli3.rocket.internal.RocketV5RemoteEventTransport;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RocketAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RocketAutoConfiguration.class));

    @Test
    void registersDefaultEncoderAndTransportWhenTemplateIsAvailable() {
        this.contextRunner
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(RocketMessageWrapperEncoder.class);
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketRemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RocketContextSupport.class);
                });
    }

    @Test
    void registersInboundContextSupportWhenAWriterIsAvailable() {
        this.contextRunner
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .withBean(ContextStore.class, ContextStore::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(RocketContextSupport.class);
                });
    }

    @Test
    void backsOffForApplicationProvidedBeans() {
        this.contextRunner
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .withBean(RemoteEventTransport.class, NoopRocketTransport::new)
                .withBean(
                        RocketMessageWrapperEncoder.class,
                        () -> new RocketMessageWrapperEncoder(
                                null, new ContextPropagator(java.util.List.of())))
                .run(context -> {
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RocketRemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketMessageWrapperEncoder.class);
                });
    }

    @Test
    void coexistsWithARemoteTransportForAnotherOptionsType() {
        this.contextRunner
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .withBean(OtherRemoteTransport.class, OtherRemoteTransport::new)
                .run(context -> {
                    assertThat(context).getBeans(RemoteEventTransport.class).hasSize(2);
                    assertThat(context).hasSingleBean(RocketRemoteEventTransport.class);
                });
    }

    @Test
    void selectsV5TransportWhenConfigured() {
        this.contextRunner
                .withPropertyValues("wuli3.rocketmq.client-version=v5")
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .withBean(Producer.class, () -> mock(Producer.class))
                .withBean(ClientServiceProvider.class, () -> mock(ClientServiceProvider.class))
                .run(context -> {
                    assertThat(context.getBean(RocketProperties.class).getClientVersion())
                            .isEqualTo(RocketProperties.ClientVersion.V5);
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketV5RemoteEventTransport.class);
                    assertThat(context).doesNotHaveBean(RocketRemoteEventTransport.class);
                });
    }

    @Test
    void failsStartupWhenV5IsSelectedWithoutAnApplicationProducer() {
        this.contextRunner
                .withPropertyValues("wuli3.rocketmq.client-version=v5")
                .withBean(ClientServiceProvider.class, () -> mock(ClientServiceProvider.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasMessageContaining("Producer");
                });
    }

    @Test
    void registersV5TransportWithoutV4Template() {
        this.contextRunner
                .withPropertyValues("wuli3.rocketmq.client-version=v5")
                .withBean(Producer.class, () -> mock(Producer.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ClientServiceProvider.class);
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketV5RemoteEventTransport.class);
                });
    }

    @Test
    void keepsV4TransportUsableWithoutTheOptionalV5Client() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader("org.apache.rocketmq.client.apis"))
                .withBean(RocketMQTemplate.class, () -> mock(RocketMQTemplate.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(RemoteEventTransport.class);
                    assertThat(context).hasSingleBean(RocketRemoteEventTransport.class);
                });
    }

    private static final class NoopRocketTransport implements RemoteEventTransport<RocketPublishOptions> {

        @Override
        public Class<RocketPublishOptions> supportedOptionsType() {
            return RocketPublishOptions.class;
        }

        @Override
        public void send(
                final RocketPublishOptions options, final com.kjs.wuli3.event.envelope.EventEnvelope<?>... envelopes) {}
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
