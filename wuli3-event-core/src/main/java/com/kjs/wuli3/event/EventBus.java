package com.kjs.wuli3.event;

public interface EventBus extends EventPublisher {
    /** Registers a handler. Repeated registrations are independent and each receives subsequent matching events. */
    <E extends EventEnvelope> void register(Class<E> eventType, EventHandler<? super E> handler);
}
