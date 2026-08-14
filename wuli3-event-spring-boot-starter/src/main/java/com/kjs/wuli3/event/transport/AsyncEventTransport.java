package com.kjs.wuli3.event.transport;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.AsyncPublishOptions;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.springframework.core.task.TaskExecutor;

/**
 * 装饰器：当 {@link PublishOptions} 具备 {@link AsyncPublishOptions} 能力且请求异步发布时，
 * 将委托传输的调用提交到指定的 {@link Executor} 上执行；否则同步转发给委托传输实现。
 *
 * <p>不绑定具体的 Options 类型，只在运行时通过 {@code instanceof} 识别是否具备异步能力，
 * 因此可以叠加在任意 {@link EventTransport} 之上，与其他能力装饰器（如
 * {@link TransactionalEventTransport}）自由组合。
 *
 * @param <O> 委托传输实现所支持的发布选项类型
 * @author GuoYang create on 2026/8/6 19:16
 */
public final class AsyncEventTransport<O extends PublishOptions> implements EventTransport<O> {

    private final EventTransport<O> delegate;
    private final TaskExecutor executor;

    /**
     * 使用指定的委托传输实现和异步执行器创建装饰器。
     *
     * @param delegate 实际执行发送的传输实现
     * @param executor 异步分派使用的执行器
     */
    public AsyncEventTransport(final EventTransport<O> delegate, final TaskExecutor executor) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
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
        if (options instanceof AsyncPublishOptions asyncOptions && asyncOptions.async()) {
            this.executor.execute(() -> this.delegate.send(options, snapshot));
            return;
        }
        this.delegate.send(options, snapshot);
    }
}
