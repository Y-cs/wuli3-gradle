package com.kjs.wuli3.event;

public interface EventBus extends EventPublisher {
    <E extends EventEnvelope> void register(Class<E> eventType, EventHandler<? super E> handler);
}
