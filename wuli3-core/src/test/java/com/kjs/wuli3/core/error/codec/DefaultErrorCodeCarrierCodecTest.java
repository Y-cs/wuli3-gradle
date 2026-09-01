package com.kjs.wuli3.core.error.codec;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrierCodec;
import com.kjs.wuli3.core.error.resolver.DefaultErrorCodeResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证错误编解码器保留稳定错误信息。 */
class DefaultErrorCodeCarrierCodecTest {

    private final ErrorCodeCarrierCodec codec =
            new ErrorCodeCarrierCodec(new DefaultErrorCodeResolver("group"), "group");

    @Test
    void roundTripsRemoteErrorWithoutLoadingLocalEnum() {
        final ErrorCodeCarrier protocol = new ErrorCodeCarrier(
                "ORDER.ORDER.NOT_FOUND", "not found", ErrorOrigin.CALLER, ErrorSeverity.NORMAL, "order");
        final ErrorCodeException exception = this.codec.decode(protocol);

        assertThat(exception.isRemoteError()).isTrue();
        assertThat(exception.asRemoteError()).contains(protocol);
        assertThat(exception.getMessage()).isEqualTo("not found");
        assertThat(exception.getErrorCode().getName()).isEqualTo("NOT_FOUND");
    }
}
