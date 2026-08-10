package com.kjs.wuli3.event.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import com.kjs.wuli3.core.error.policy.ResolvedErrorPolicy;
import org.junit.jupiter.api.Test;

class EventErrorTest {

    @Test
    void sendFailureUsesSystemErrorPolicy() {
        final SendFailedException exception = new SendFailedException("发送失败", new IllegalStateException());

        assertThat(exception.getResolvedErrorPolicy())
                .isEqualTo(
                        new ResolvedErrorPolicy(ErrorSeverity.CRITICAL, ErrorVisibility.INTERNAL, ErrorOrigin.SYSTEM));
    }
}
