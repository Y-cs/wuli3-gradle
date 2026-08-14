package com.kjs.wuli3.rabbit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RabbitPublishOptionsTest {

    @Test
    void createsImmutableAsyncAndTransactionalCopies() {
        final RabbitPublishOptions defaults = new RabbitPublishOptions();
        final RabbitPublishOptions changed = defaults.withAsync().withAfterCommit();

        assertThat(defaults).isEqualTo(new RabbitPublishOptions(false, false));
        assertThat(changed).isEqualTo(new RabbitPublishOptions(true, true));
    }
}
