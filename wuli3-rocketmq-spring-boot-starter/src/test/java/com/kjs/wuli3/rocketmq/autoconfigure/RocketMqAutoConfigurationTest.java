package com.kjs.wuli3.rocketmq.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.rocketmq.internal.RocketMqEventMessageEncoder;
import com.kjs.wuli3.rocketmq.internal.RocketMqRemoteEventMessageTransport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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

    @Test
    void defaultContextModeOnlyPropagatesInvocationContext() {
        this.contextRunner.withBean(ContextStore.class, ContextStore::new).run(context -> {
            final ContextStore contextStore = context.getBean(ContextStore.class);
            contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
            contextStore.put(new AuthContext(7L, "alice"));

            final String body = new String(
                    context.getBean(RocketMqEventMessageEncoder.class)
                            .encode(RocketMqAutoConfigurationTest.event(), RocketMqAutoConfigurationTest.remote())
                            .body(),
                    StandardCharsets.UTF_8);

            assertThat(body).contains("request-42", "10.0.0.8").doesNotContain("X-User-Id", "alice");
        });
    }

    @Test
    void trustedInternalContextModePropagatesAuthenticationContext() {
        this.contextRunner
                .withPropertyValues("wuli3.rocketmq.event.context-mode=TRUSTED_INTERNAL")
                .withBean(ContextStore.class, ContextStore::new)
                .run(context -> {
                    final ContextStore contextStore = context.getBean(ContextStore.class);
                    contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
                    contextStore.put(new AuthContext(7L, "alice"));

                    final String body = new String(
                            context.getBean(RocketMqEventMessageEncoder.class)
                                    .encode(
                                            RocketMqAutoConfigurationTest.event(),
                                            RocketMqAutoConfigurationTest.remote())
                                    .body(),
                            StandardCharsets.UTF_8);

                    assertThat(body).contains("X-User-Id", "7", "alice");
                });
    }

    private static EventEnvelope<String> event() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static PublishOptions remote() {
        return new PublishOptions(PublishOptions.Channel.REMOTE);
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
