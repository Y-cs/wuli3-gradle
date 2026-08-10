package com.kjs.wuli3.rocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;

import com.kjs.wuli3.rocket.internal.RocketPublishOptions;
import org.junit.jupiter.api.Test;

class RocketPublishOptionsTest {

    @Test
    void createsIndependentImmutableVariants() {
        final RocketPublishOptions defaults = new RocketPublishOptions();
        final RocketPublishOptions configured =
                defaults.withAsync().withAfterCommit().withOrderKey("order-42");

        assertThat(defaults.async()).isFalse();
        assertThat(defaults.afterCommit()).isFalse();
        assertThat(defaults.orderKey()).isNull();
        assertThat(configured.async()).isTrue();
        assertThat(configured.afterCommit()).isTrue();
        assertThat(configured.orderKey()).isEqualTo("order-42");
    }

    @Test
    void rejectsInvalidDelayAndOrderKey() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RocketPublishOptions().withDelay(Duration.ZERO))
                .withMessage("delay must be positive");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RocketPublishOptions().withOrderKey(" "))
                .withMessage("orderKey cannot be blank");
    }
}
