package com.kjs.wuli3.rocketmq.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventMessageTransport.UnsupportedCapabilityException;
import com.kjs.wuli3.event.PublishOptions;
import java.time.Duration;
import java.time.Instant;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

class RocketMqRemoteEventMessageTransportTest {

    @Test
    void mapsSupportedPublicationModesToRocketMqTemplate() {
        final RocketMQTemplate template = mock(RocketMQTemplate.class);
        final RocketMqRemoteEventMessageTransport transport =
                RocketMqRemoteEventMessageTransportTest.transport(template);
        final EventEnvelope<String> envelope = RocketMqRemoteEventMessageTransportTest.envelope();

        transport.send(envelope, RocketMqRemoteEventMessageTransportTest.remote());
        transport.send(
                envelope, RocketMqRemoteEventMessageTransportTest.remote().async());
        transport.send(
                envelope, RocketMqRemoteEventMessageTransportTest.remote().setOrderKey("order-42"));
        transport.send(
                envelope,
                RocketMqRemoteEventMessageTransportTest.remote()
                        .setOrderKey("order-42")
                        .async());
        transport.send(
                envelope, RocketMqRemoteEventMessageTransportTest.remote().setDelayTime(Duration.ofSeconds(5)));

        verify(template).syncSend(eq("orders"), any(Message.class));
        verify(template).asyncSend(eq("orders"), any(Message.class), any(SendCallback.class));
        verify(template).syncSendOrderly(eq("orders"), any(Message.class), eq("order-42"));
        verify(template).asyncSendOrderly(eq("orders"), any(Message.class), eq("order-42"), any(SendCallback.class));
        verify(template).syncSendDelayTimeMills(eq("orders"), any(Message.class), eq(5000L));
    }

    @Test
    void rejectsUnsupportedExactDelayCombinationsBeforeTemplateInteraction() {
        final RocketMQTemplate template = mock(RocketMQTemplate.class);
        final RocketMqRemoteEventMessageTransport transport =
                RocketMqRemoteEventMessageTransportTest.transport(template);

        assertThatThrownBy(() -> transport.send(
                        RocketMqRemoteEventMessageTransportTest.envelope(),
                        RocketMqRemoteEventMessageTransportTest.remote().async().setDelayTime(Duration.ofSeconds(1))))
                .isInstanceOf(UnsupportedCapabilityException.class);
        verifyNoInteractions(template);
    }

    private static RocketMqRemoteEventMessageTransport transport(final RocketMQTemplate template) {
        return new RocketMqRemoteEventMessageTransport(template, new RocketMqEventMessageEncoder(null));
    }

    private static EventEnvelope<String> envelope() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static PublishOptions remote() {
        return new PublishOptions(PublishOptions.Channel.REMOTE);
    }
}
