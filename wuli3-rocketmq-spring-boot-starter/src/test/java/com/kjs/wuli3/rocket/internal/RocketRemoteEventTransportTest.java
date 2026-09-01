package com.kjs.wuli3.rocket.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.SendFailedException;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.rocket.internal.wrapper.RocketMessageWrapperEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

class RocketRemoteEventTransportTest {

    @Test
    void mapsSupportedPublicationModesToRocketMqTemplate() {
        final RocketMQTemplate template = mock(RocketMQTemplate.class);
        final RocketRemoteEventTransport transport = RocketRemoteEventTransportTest.transport(template);
        final EventEnvelope<String> envelope = RocketRemoteEventTransportTest.envelope();

        transport.send(RocketRemoteEventTransportTest.options(), envelope);
        transport.send(RocketRemoteEventTransportTest.options().withAsync(), envelope);
        transport.send(RocketRemoteEventTransportTest.options().withOrderKey("order-42"), envelope);
        transport.send(
                RocketRemoteEventTransportTest.options()
                        .withOrderKey("order-42")
                        .withAsync(),
                envelope);
        transport.send(RocketRemoteEventTransportTest.options().withDelay(Duration.ofSeconds(5)), envelope);

        verify(template).syncSend(eq("orders"), any(Message.class));
        verify(template).asyncSend(eq("orders"), any(Message.class), any(SendCallback.class));
        verify(template).syncSendOrderly(eq("orders"), any(Message.class), eq("order-42"));
        verify(template).asyncSendOrderly(eq("orders"), any(Message.class), eq("order-42"), any(SendCallback.class));
        verify(template).syncSendDelayTimeMills(eq("orders"), any(Message.class), eq(5000L));
    }

    @Test
    void sendsEachEnvelope() {
        final RocketMQTemplate template = mock(RocketMQTemplate.class);
        final RocketRemoteEventTransport transport = RocketRemoteEventTransportTest.transport(template);

        transport.send(
                RocketRemoteEventTransportTest.options(),
                RocketRemoteEventTransportTest.envelope(),
                RocketRemoteEventTransportTest.envelope());

        verify(template, org.mockito.Mockito.times(2)).syncSend(eq("orders"), any(Message.class));
    }

    @Test
    void wrapsTemplateFailuresAndRejectsInvalidOptionsBeforeTemplateInteraction() {
        final RocketMQTemplate failingTemplate = mock(RocketMQTemplate.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("unavailable"))
                .when(failingTemplate)
                .syncSend(eq("orders"), any(Message.class));
        final RocketRemoteEventTransport failingTransport = RocketRemoteEventTransportTest.transport(failingTemplate);

        assertThatThrownBy(() -> failingTransport.send(
                        RocketRemoteEventTransportTest.options(), RocketRemoteEventTransportTest.envelope()))
                .isInstanceOf(SendFailedException.class)
                .hasCauseInstanceOf(IllegalStateException.class);

        final RocketMQTemplate untouchedTemplate = mock(RocketMQTemplate.class);
        final RocketRemoteEventTransport untouchedTransport =
                RocketRemoteEventTransportTest.transport(untouchedTemplate);
        final EventEnvelope<String> invalidEnvelope =
                new EventEnvelope<>("invalid topic", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
        assertThatThrownBy(() -> untouchedTransport.send(RocketRemoteEventTransportTest.options(), invalidEnvelope))
                .isInstanceOf(ErrorCodeException.class);
        verifyNoInteractions(untouchedTemplate);
    }

    private static RocketRemoteEventTransport transport(final RocketMQTemplate template) {
        return new RocketRemoteEventTransport(
                template, new RocketMessageWrapperEncoder(null, new ContextPropagator(List.of())));
    }

    private static EventEnvelope<String> envelope() {
        return new EventEnvelope<>("orders", "order.paid.v1", "event-1", Instant.EPOCH, "payload");
    }

    private static RocketPublishOptions options() {
        return new RocketPublishOptions();
    }
}
