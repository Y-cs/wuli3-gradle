package com.kjs.wuli3.event.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.event.BasicDomainEvent;
import com.kjs.wuli3.event.DomainEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class InMemoryEventBusTest {
    @Test
    void publishesToRegisteredHandler() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final InMemoryEventBus bus = new InMemoryEventBus(executor);
        final AtomicInteger count = new AtomicInteger();
        bus.register(DomainEvent.class, event -> count.incrementAndGet());

        bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                .toCompletableFuture()
                .join();
        executor.shutdown();

        assertThat(count).hasValue(1);
    }

    @Test
    void propagatesHandlerFailure() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final InMemoryEventBus bus = new InMemoryEventBus(executor);
        bus.register(DomainEvent.class, event -> {
            throw new IllegalStateException("failed");
        });

        assertThatThrownBy(() -> bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                        .toCompletableFuture()
                        .join())
                .hasRootCauseMessage("failed");
        executor.shutdown();
    }

    @Test
    void repeatedAndAssignableRegistrationsEachReceiveEvent() {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final InMemoryEventBus bus = new InMemoryEventBus(executor);
        final AtomicInteger count = new AtomicInteger();
        bus.register(DomainEvent.class, event -> count.incrementAndGet());
        bus.register(DomainEvent.class, event -> count.incrementAndGet());

        bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                .toCompletableFuture()
                .join();
        executor.shutdown();

        assertThat(count).hasValue(2);
    }

    @Test
    void noHandlerCompletesSuccessfully() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final InMemoryEventBus bus = new InMemoryEventBus(executor);

        bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                .toCompletableFuture()
                .join();
        executor.shutdown();
    }
}
