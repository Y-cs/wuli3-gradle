package com.kjs.wuli3.event;

@FunctionalInterface
public interface EventHandler<E extends EventEnvelope> {
    void handle(E event);
}
