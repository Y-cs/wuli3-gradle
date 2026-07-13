package com.kjs.wuli3.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.json.provider.JacksonProvider;
import org.junit.jupiter.api.Test;

class BomConsumerTest {
    @Test
    void resolvesAndUsesVersionlessComponents() {
        assertNotNull(UuidStringIdGenerator.INSTANCE.nextId());
        assertNotNull(JacksonProvider.newJsonMapper());
    }
}
