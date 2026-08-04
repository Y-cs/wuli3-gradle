package com.kjs.wuli3.event.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
    void sendsImmediatelyWithoutAnActualTransaction() {
        final RecordingTransport delegate = new RecordingTransport();
        final TransactionalEventTransport transport = new TransactionalEventTransport(delegate);

        transport.send(
                TransactionalEventTransportTest.envelope("event-1"),
                TransactionalEventTransportTest.remote());

        assertThat(delegate.sent()).hasSize(1);
    }

    @Test
    void defersUntilAfterCommitWhenSynchronizationIsActive() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final RecordingTransport delegate = new RecordingTransport();
        final TransactionalEventTransport transport = new TransactionalEventTransport(delegate);

        transport.send(
                TransactionalEventTransportTest.envelope("event-1"),
                TransactionalEventTransportTest.remote());

        assertThat(delegate.sent()).isEmpty();
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(delegate.sent()).hasSize(1);
    }

    @Test
    void doesNotSendWhenTheTransactionRollsBack() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final RecordingTransport delegate = new RecordingTransport();
        final TransactionalEventTransport transport = new TransactionalEventTransport(delegate);

        transport.send(
                TransactionalEventTransportTest.envelope("event-1"),
                TransactionalEventTransportTest.remote());
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization ->
                        synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(delegate.sent()).isEmpty();
    }

    @Test
    void failsWhenAnActualTransactionHasNoSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final TransactionalEventTransport transport =
                new TransactionalEventTransport(new RecordingTransport());

        assertThatIllegalStateException()
                .isThrownBy(() -> transport.send(
                        TransactionalEventTransportTest.envelope("event-1"),
                        TransactionalEventTransportTest.remote()))
                .withMessage("An actual transaction is active but transaction synchronization is not active");
    }

    @Test
    void snapshotsBatchBeforeTheTransactionCommits() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        final RecordingTransport delegate = new RecordingTransport();
        final TransactionalEventTransport transport = new TransactionalEventTransport(delegate);
        final List<EventEnvelope<?>> envelopes = new ArrayList<>();
        envelopes.add(TransactionalEventTransportTest.envelope("event-1"));

        transport.sends(envelopes, TransactionalEventTransportTest.remote());
        envelopes.add(TransactionalEventTransportTest.envelope("event-2"));
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        assertThat(delegate.sent()).extracting(EventEnvelope::eventId).containsExactly("event-1");
    }

    private static EventEnvelope<String> envelope(final String eventId) {
        return new EventEnvelope<>("orders", "order.paid.v1", eventId, Instant.EPOCH, "payload");
    }

    private static PublishOptions remote() {
        return new PublishOptions(PublishOptions.Channel.REMOTE).afterCommit();
    }

    private static final class RecordingTransport implements EventTransport {

        private final List<EventEnvelope<?>> sent = new ArrayList<>();

        @Override
        public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
            this.sent.add(envelope);
        }

        @Override
        public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
            this.sent.addAll(envelopes);
        }

        private List<EventEnvelope<?>> sent() {
            return this.sent;
        }
    }
}
