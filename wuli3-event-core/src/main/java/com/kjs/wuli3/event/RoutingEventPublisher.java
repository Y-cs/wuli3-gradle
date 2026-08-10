package com.kjs.wuli3.event;

import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.error.UnsupportedCapabilityException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** 根据具体发布选项类型选择传输实现的事件发布器。 */
public final class RoutingEventPublisher implements EventPublisher {

    private final Map<Class<? extends PublishOptions>, EventTransport<?>> transports = new ConcurrentHashMap<>();

    /** 注册指定选项类型对应的传输实现。 */
    public <PO extends PublishOptions> void register(final EventTransport<PO> transport) {
        final EventTransport<PO> requiredTransport = Objects.requireNonNull(transport, "transport cannot be null");
        final Class<PO> optionsType =
                Objects.requireNonNull(requiredTransport.supportedOptionsType(), "supportedOptionsType cannot be null");
        final EventTransport<?> existing = this.transports.putIfAbsent(optionsType, requiredTransport);
        if (existing != null) {
            throw new IllegalStateException("A transport is already registered for " + optionsType.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <PO extends PublishOptions> void publish(final PO options, final EventEnvelope<?>... envelopes) {
        final PO requiredOptions = Objects.requireNonNull(options, "options cannot be null");
        final EventTransport<PO> transport = (EventTransport<PO>) this.transports.get(requiredOptions.getClass());
        if (transport == null) {
            throw new UnsupportedCapabilityException(
                    "No transport registered for " + requiredOptions.getClass().getName());
        }
        transport.send(requiredOptions, envelopes);
    }
}
