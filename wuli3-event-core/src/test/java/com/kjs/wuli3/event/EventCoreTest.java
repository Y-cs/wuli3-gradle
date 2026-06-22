package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EventCoreTest {
    @Test
    void domainEventHasMetadata() {
        BasicDomainEvent event = BasicDomainEvent.create("created", "1", "Order");
        assertThat(event.version()).isEqualTo(1);
        assertThat(event.metadata().values()).isEmpty();
    }
}
