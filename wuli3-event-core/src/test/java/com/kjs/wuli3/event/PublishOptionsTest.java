package com.kjs.wuli3.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class PublishOptionsTest {

    @Test
    void createsImmutableRemoteOptions() {
        final PublishOptions remote = new PublishOptions(PublishOptions.Channel.REMOTE);
        final PublishOptions configured =
                remote.async().setDelayTime(Duration.ofSeconds(5)).setOrderKey("order-42");

        assertThat(remote.isRemote()).isTrue();
        assertThat(remote.isAsync()).isFalse();
        assertThat(remote.getDelayTime()).isNull();
        assertThat(configured.isAsync()).isTrue();
        assertThat(configured.getDelayTime()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configured.getOrderKey()).isEqualTo("order-42");
    }

    @Test
    void rejectsBlankOrderKey() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PublishOptions.defaults().setOrderKey(" "))
                .withMessage("orderKey cannot be blank");
    }
}
