package com.kjs.wuli3.event.transport;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.TransactionalPublishOptions;
import java.util.Objects;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 装饰器：当 {@link PublishOptions} 具备 {@link TransactionalPublishOptions} 能力且要求
 * 提交后发布时，将委托传输的调用延后到当前事务提交后执行；否则立即同步转发给委托传输实现。
 *
 * <p>不绑定具体的 Options 类型，只在运行时通过 {@code instanceof} 识别是否具备事务能力，
 * 因此可以叠加在任意 {@link EventTransport} 之上，与其他能力装饰器（如
 * {@link AsyncEventTransport}）自由组合。注意事务同步的注册必须发生在调用线程上，
 * 所以当与 {@link AsyncEventTransport} 组合时，本装饰器应包裹在外层，
 * 由它先在当前事务线程完成注册，再在提交后回调中决定是否异步分派。
 *
 * @param <O> 委托传输实现所支持的发布选项类型
 */
public final class TransactionalEventTransport<O extends PublishOptions> implements EventTransport<O> {

    private final EventTransport<O> delegate;

    /**
     * 使用指定的委托传输实现创建事务感知装饰器。
     *
     * @param delegate 提交后调用的传输实现
     */
    public TransactionalEventTransport(final EventTransport<O> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    }

    @Override
    public Class<O> supportedOptionsType() {
        return this.delegate.supportedOptionsType();
    }

    @Override
    public void send(final O options, final EventEnvelope<?>... envelopes) {
        Objects.requireNonNull(options, "options cannot be null");
        final EventEnvelope<?>[] snapshot =
                Objects.requireNonNull(envelopes, "envelopes cannot be null").clone();
        final boolean wantsAfterCommit =
                options instanceof TransactionalPublishOptions transactional && transactional.afterCommit();
        if (!wantsAfterCommit || !TransactionSynchronizationManager.isActualTransactionActive()) {
            this.delegate.send(options, snapshot);
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "An actual transaction is active but transaction synchronization is not active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                TransactionalEventTransport.this.delegate.send(options, snapshot);
            }
        });
    }
}
