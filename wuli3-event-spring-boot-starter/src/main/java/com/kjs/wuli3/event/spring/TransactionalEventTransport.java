package com.kjs.wuli3.event.spring;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 将远程传输调用延后到当前事务提交后执行。
 */
public final class TransactionalEventTransport implements EventTransport {

    private final EventTransport delegate;

    /**
     * 使用指定的远程传输实现创建事务感知包装器。
     *
     * @param delegate 提交后调用的远程传输实现
     */
    public TransactionalEventTransport(final EventTransport delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    }

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        Objects.requireNonNull(envelope, "envelope cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (!options.isAfterCommit() || !TransactionSynchronizationManager.isActualTransactionActive()) {
            this.delegate.send(envelope, options);
            return;
        }
        TransactionalEventTransport.requireSynchronization();
        this.registerAfterCommit(() -> this.delegate.send(envelope, options));
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        Objects.requireNonNull(envelopes, "envelopes cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (!options.isAfterCommit() || !TransactionSynchronizationManager.isActualTransactionActive()) {
            this.delegate.sends(envelopes, options);
            return;
        }
        TransactionalEventTransport.requireSynchronization();
        final List<EventEnvelope<?>> snapshot = List.copyOf(envelopes);
        this.registerAfterCommit(() -> this.delegate.sends(snapshot, options));
    }

    private static void requireSynchronization() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "An actual transaction is active but transaction synchronization is not active");
        }
    }

    private void registerAfterCommit(final Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
