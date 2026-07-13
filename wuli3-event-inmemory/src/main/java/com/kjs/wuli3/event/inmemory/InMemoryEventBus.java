package com.kjs.wuli3.event.inmemory;

import com.kjs.wuli3.event.EventBus;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * Asynchronous in-memory event bus. A publish snapshots matching registrations, submits each once with no ordering
 * guarantee, and completes exceptionally when any submitted handler fails. The caller owns the executor lifecycle.
 */
public final class InMemoryEventBus implements EventBus {
    private final Executor executor;
    private final Map<Class<?>, List<EventHandler<? super EventEnvelope>>> handlers = new ConcurrentHashMap<>();

    public InMemoryEventBus(final Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public CompletionStage<Void> publish(final EventEnvelope event) {
        Objects.requireNonNull(event, "event");
        final List<EventHandler<? super EventEnvelope>> matchedHandlers = this.findHandlers(event);
        final CompletableFuture<?>[] futures = matchedHandlers.stream()
                .map(handler -> CompletableFuture.runAsync(() -> handler.handle(event), this.executor))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends EventEnvelope> void register(final Class<E> eventType, final EventHandler<? super E> handler) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(handler, "handler");
        this.handlers
                .computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add((EventHandler<? super EventEnvelope>) handler);
    }

    private List<EventHandler<? super EventEnvelope>> findHandlers(final EventEnvelope event) {
        final List<EventHandler<? super EventEnvelope>> matchedHandlers = new ArrayList<>();
        this.handlers.forEach((type, registeredHandlers) -> {
            if (type.isAssignableFrom(event.getClass())) {
                matchedHandlers.addAll(registeredHandlers);
            }
        });
        return List.copyOf(matchedHandlers);
    }
}
