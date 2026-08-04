package com.kjs.wuli3.event.internal;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.spring.TransactionalEventTransport;
import java.util.Collection;
import java.util.Objects;

/**
 * 作为应用主事件发布器暴露的默认通道路由器。
 */
public final class RoutingEventPublisher implements EventPublisher {

    private final EventTransport localPublisher;
    private final EventTransport remotePublisher;

    /**
     * 创建将本地和远程通道路由到相应传输实现的发布器。
     *
     * @param localPublisher  立即执行的本地 Spring 传输实现
     * @param remotePublisher 将由事务同步包装的远程传输实现
     */
    public RoutingEventPublisher(
            final EventTransport localPublisher, final EventTransport remotePublisher) {
        this.localPublisher = new TransactionalEventTransport(
                Objects.requireNonNull(localPublisher, "localPublisher cannot be null"));
        this.remotePublisher = new TransactionalEventTransport(
                Objects.requireNonNull(remotePublisher, "remotePublisher cannot be null"));
    }

    @Override
    public void publish(final EventEnvelope<?> envelope, final PublishOptions options) {
        this.selectPublisher(options).send(envelope, options);
    }

    @Override
    public void publishes(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        this.selectPublisher(options).sends(envelopes, options);
    }

    private EventTransport selectPublisher(final PublishOptions options) {
        Objects.requireNonNull(options, "options cannot be null");
        return options.isLocal() ? this.localPublisher : this.remotePublisher;
    }
}
