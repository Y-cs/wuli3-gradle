package com.kjs.wuli3.event.inmemory;

import com.kjs.wuli3.event.EventBus;
import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public final class InMemoryEventBus implements EventBus {
    private final Executor executor;
    private final Map<Class<?>, List<EventHandler<? super EventEnvelope>>> handlers = new ConcurrentHashMap<>();

    public InMemoryEventBus(Executor executor) {
        this.executor = executor;
    }

    @Override
    public CompletionStage<Void> publish(EventEnvelope event) {
        List<EventHandler<? super EventEnvelope>> matchedHandlers = findHandlers(event);
        CompletableFuture<?>[] futures = matchedHandlers.stream()
                .map(handler -> CompletableFuture.runAsync(() -> handler.handle(event), executor))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends EventEnvelope> void register(Class<E> eventType, EventHandler<? super E> handler) {
        handlers.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add((EventHandler<? super EventEnvelope>) handler);
    }

    private List<EventHandler<? super EventEnvelope>> findHandlers(EventEnvelope event) {
        List<EventHandler<? super EventEnvelope>> matchedHandlers = new ArrayList<>();
        handlers.forEach((type, registeredHandlers) -> {
            if (type.isAssignableFrom(event.getClass())) {
                matchedHandlers.addAll(registeredHandlers);
            }
        });
        return matchedHandlers;
    }
}
