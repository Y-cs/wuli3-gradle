package com.kjs.wuli3.core.id;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidStringIdGeneratorTest {

    @Test
    void generatesStandardUuidString() {
        final String id = UuidStringIdGenerator.INSTANCE.nextId();

        assertThat(UUID.fromString(id).toString()).isEqualTo(id);
    }
}
