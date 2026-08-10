package com.kjs.wuli3.event.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.SpringLocalPublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionalEventTransportTest {

    @AfterEach
    void clearTransactionState() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void snapshotsAndDefersUntilCommit() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final RecordingTransport delegate = new RecordingTransport();
        final TransactionalEventTransport<SpringLocalPublishOptions> transport =
                new TransactionalEventTransport<>(delegate);
        final EventEnvelope<?>[] envelopes = {TransactionalEventTransportTest.envelope("event-1")};

        transport.send(new SpringLocalPublishOptions(false, true), envelopes);
        envelopes[0] = TransactionalEventTransportTest.envelope("event-2");

        assertThat(delegate.sent).isEmpty();
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(delegate.sent).extracting(EventEnvelope::eventId).containsExactly("event-1");
    }

    @Test
    void rejectsAnActiveTransactionWithoutSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final TransactionalEventTransport<SpringLocalPublishOptions> transport =
                new TransactionalEventTransport<>(new RecordingTransport());

        assertThatIllegalStateException()
                .isThrownBy(() -> transport.send(
                        new SpringLocalPublishOptions(false, true),
                        TransactionalEventTransportTest.envelope("event-1")))
                .withMessage("An actual transaction is active but transaction synchronization is not active");
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }

    private static final class RecordingTransport implements EventTransport<SpringLocalPublishOptions> {

        private final List<EventEnvelope<?>> sent = new ArrayList<>();

        @Override
        public Class<SpringLocalPublishOptions> supportedOptionsType() {
            return SpringLocalPublishOptions.class;
        }

        @Override
        public void send(final SpringLocalPublishOptions options, final EventEnvelope<?>... envelopes) {
            this.sent.addAll(List.of(envelopes));
        }
    }
}
