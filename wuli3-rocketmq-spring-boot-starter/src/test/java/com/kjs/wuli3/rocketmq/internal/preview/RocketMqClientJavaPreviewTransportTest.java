package com.kjs.wuli3.rocketmq.internal.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.rocketmq.autoconfigure.RocketMqContextMode;
import com.kjs.wuli3.rocketmq.internal.RocketMqEventMessageEncoder;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.junit.jupiter.api.Test;

class RocketMqClientJavaPreviewTransportTest {

    @Test
    void constructionDoesNotCreateOrProbeAClient() {
        final Producer producer = mock(Producer.class);
        final ClientServiceProvider clientServiceProvider = mock(ClientServiceProvider.class);

        RocketMqClientJavaPreviewTransportTest.transport(producer, clientServiceProvider);

        verifyNoInteractions(producer, clientServiceProvider);
    }

    @Test
    void mapsSynchronousAndAsynchronousSendsToTheInjectedProducer() throws Exception {
        final Producer producer = mock(Producer.class);
        final ClientServiceProvider clientServiceProvider = mock(ClientServiceProvider.class);
        final MessageBuilder builder = RocketMqClientJavaPreviewTransportTest.builder(clientServiceProvider);
        final Message message = mock(Message.class);
        when(builder.build()).thenReturn(message);
        when(producer.sendAsync(message)).thenReturn(CompletableFuture.completedFuture(mock(SendReceipt.class)));
        final RocketMqClientJavaPreviewTransport transport =
                RocketMqClientJavaPreviewTransportTest.transport(producer, clientServiceProvider);

        transport.send(
                RocketMqClientJavaPreviewTransportTest.envelope(), RocketMqClientJavaPreviewTransportTest.remote());
        transport.send(
                RocketMqClientJavaPreviewTransportTest.envelope(),
                RocketMqClientJavaPreviewTransportTest.remote().async());

        verify(producer).send(message);
        verify(producer).sendAsync(message);
    }

    @Test
    void mapsOrderingAndExactDelayOnTheJavaClientMessageBuilder() {
        final Producer producer = mock(Producer.class);
        final ClientServiceProvider clientServiceProvider = mock(ClientServiceProvider.class);
        final MessageBuilder builder = RocketMqClientJavaPreviewTransportTest.builder(clientServiceProvider);
        when(builder.build()).thenReturn(mock(Message.class));
        final Clock clock = Clock.fixed(Instant.ofEpochMilli(1_000L), ZoneOffset.UTC);
        final RocketMqClientJavaPreviewTransport transport = new RocketMqClientJavaPreviewTransport(
                producer,
                clientServiceProvider,
                new RocketMqEventMessageEncoder(null, RocketMqContextMode.INVOCATION_ONLY),
                clock);

        transport.send(
                RocketMqClientJavaPreviewTransportTest.envelope(),
                RocketMqClientJavaPreviewTransportTest.remote().setOrderKey("order-42"));
        transport.send(
                RocketMqClientJavaPreviewTransportTest.envelope(),
                RocketMqClientJavaPreviewTransportTest.remote().setDelayTime(Duration.ofSeconds(5)));

        verify(builder).setMessageGroup("order-42");
        verify(builder).setDeliveryTimestamp(6_000L);
        verify(builder, times(2)).setTopic("orders");
        verify(builder, times(2)).setKeys("event-1");
    }

    @Test
    void remainsPackagePrivateForReviewOnlyUse() {
        assertThat(Modifier.isPublic(RocketMqClientJavaPreviewTransport.class.getModifiers()))
                .isFalse();
        assertThat(RocketMqClientJavaPreviewTransport.class.getDeclaredConstructors())
                .allSatisfy(constructor -> assertThat(Modifier.isPublic(constructor.getModifiers()))
                        .isFalse());
    }

    private static RocketMqClientJavaPreviewTransport transport(
            final Producer producer, final ClientServiceProvider clientServiceProvider) {
        return new RocketMqClientJavaPreviewTransport(
                producer,
                clientServiceProvider,
                new RocketMqEventMessageEncoder(null, RocketMqContextMode.INVOCATION_ONLY),
                Clock.systemUTC());
    }

    private static MessageBuilder builder(final ClientServiceProvider clientServiceProvider) {
        final MessageBuilder builder = mock(MessageBuilder.class);
        when(clientServiceProvider.newMessageBuilder()).thenReturn(builder);
        when(builder.setTopic(anyString())).thenReturn(builder);
        when(builder.setBody(any(byte[].class))).thenReturn(builder);
        when(builder.setKeys(anyString())).thenReturn(builder);
        when(builder.setTag(anyString())).thenReturn(builder);
        when(builder.setMessageGroup(anyString())).thenReturn(builder);
        when(builder.setDeliveryTimestamp(anyLong())).thenReturn(builder);
        return builder;
    }

    private static EventEnvelope<String> envelope() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static PublishOptions remote() {
        return new PublishOptions(PublishOptions.Channel.REMOTE);
    }
}
