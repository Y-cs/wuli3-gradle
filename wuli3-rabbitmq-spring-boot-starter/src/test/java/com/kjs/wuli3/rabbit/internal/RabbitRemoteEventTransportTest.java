package com.kjs.wuli3.rabbit.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.SendFailedException;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

class RabbitRemoteEventTransportTest {

    @Test
    void mapsEachEnvelopeTopicToAnExchangeAndEventTypeToARoutingKey() {
        final RabbitTemplate template = mock(RabbitTemplate.class);
        final RabbitRemoteEventTransport transport = RabbitRemoteEventTransportTest.transport(template);

        transport.send(
                new RabbitPublishOptions(),
                RabbitRemoteEventTransportTest.envelope(),
                RabbitRemoteEventTransportTest.envelope("payments", "payment.settled.v1"));

        verify(template).send(eq("orders"), eq("order.paid.v1"), any(Message.class));
        verify(template).send(eq("payments"), eq("payment.settled.v1"), any(Message.class));
    }

    @Test
    void wrapsSynchronousTemplateFailures() {
        final RabbitTemplate template = mock(RabbitTemplate.class);
        doThrow(new IllegalStateException("unavailable"))
                .when(template)
                .send(eq("orders"), eq("order.paid.v1"), any(Message.class));
        final RabbitRemoteEventTransport transport = RabbitRemoteEventTransportTest.transport(template);

        assertThatThrownBy(() -> transport.send(new RabbitPublishOptions(), RabbitRemoteEventTransportTest.envelope()))
                .isInstanceOf(SendFailedException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void encodesBeforeSchedulingAnAsynchronousSend() {
        final RabbitTemplate template = mock(RabbitTemplate.class);
        final QueuingTaskExecutor executor = new QueuingTaskExecutor();
        final ContextStore contextStore = new ContextStore();
        contextStore.put(new InvocationContext("10.0.0.8", "request-42"));
        final RabbitRemoteEventTransport transport = new RabbitRemoteEventTransport(
                template,
                new RabbitMessageEncoder(
                        contextStore, new ContextPropagator(ContextPropagator.standardContextEncoder())),
                executor);

        transport.send(new RabbitPublishOptions().withAsync(), RabbitRemoteEventTransportTest.envelope());
        contextStore.put(new InvocationContext("10.0.0.9", "request-43"));

        verifyNoInteractions(template);
        executor.runAll();

        final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(template).send(eq("orders"), eq("order.paid.v1"), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getMessageProperties().getHeaders())
                .containsEntry("X-Request-Id", "request-42")
                .containsEntry("X-Origin-Ip", "10.0.0.8");
    }

    @Test
    void wrapsFailureToStartAnAsynchronousSend() {
        final RabbitTemplate template = mock(RabbitTemplate.class);
        final TaskExecutor rejectingExecutor = task -> {
            throw new IllegalStateException("rejected");
        };
        final RabbitRemoteEventTransport transport = new RabbitRemoteEventTransport(
                template, new RabbitMessageEncoder(null, new ContextPropagator(List.of())), rejectingExecutor);

        assertThatThrownBy(() -> transport.send(
                        new RabbitPublishOptions().withAsync(), RabbitRemoteEventTransportTest.envelope()))
                .isInstanceOf(SendFailedException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
        verifyNoInteractions(template);
    }

    private static RabbitRemoteEventTransport transport(final RabbitTemplate template) {
        return new RabbitRemoteEventTransport(
                template, new RabbitMessageEncoder(null, new ContextPropagator(List.of())), new SyncTaskExecutor());
    }

    private static EventEnvelope<String> envelope() {
        return RabbitRemoteEventTransportTest.envelope("orders", "order.paid.v1");
    }

    private static EventEnvelope<String> envelope(final String topic, final String eventType) {
        return new EventEnvelope<>(topic, eventType, "event-1", Instant.EPOCH, "payload");
    }

    private static final class QueuingTaskExecutor implements TaskExecutor {

        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(final Runnable task) {
            this.tasks.add(task);
        }

        void runAll() {
            this.tasks.forEach(Runnable::run);
        }
    }
}
