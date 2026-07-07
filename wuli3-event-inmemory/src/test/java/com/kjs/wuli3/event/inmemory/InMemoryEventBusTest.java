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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        InMemoryEventBus bus = new InMemoryEventBus(executor);
        AtomicInteger count = new AtomicInteger();
        bus.register(DomainEvent.class, event -> count.incrementAndGet());

        bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                .toCompletableFuture()
                .join();
        executor.shutdown();

        assertThat(count).hasValue(1);
    }

    @Test
    void propagatesHandlerFailure() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        InMemoryEventBus bus = new InMemoryEventBus(executor);
        bus.register(DomainEvent.class, event -> {
            throw new IllegalStateException("failed");
        });

        assertThatThrownBy(() -> bus.publish(BasicDomainEvent.create("created", "1", "Order"))
                        .toCompletableFuture()
                        .join())
                .hasRootCauseMessage("failed");
        executor.shutdown();
    }
}
