package com.kjs.wuli3.core.error.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorOrigin;
import com.kjs.wuli3.core.error.ErrorPropagationProtocol;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import com.kjs.wuli3.core.error.resolver.DefaultErrorCodeResolver;
import org.junit.jupiter.api.Test;

/** 验证错误编解码器保留稳定错误信息。 */
class DefaultErrorCodecTest {

    private final ErrorCodec codec = new DefaultErrorCodec(new DefaultErrorCodeResolver("group"), "group");

    @Test
    void serializesLocalErrorWithVisibilityAndMetadata() {
        final ErrorCodeException exception = new ErrorCodeException(CommonErrors.ILLEGAL_ARGUMENT, "denied");
        final ErrorPropagationProtocol protocol = this.codec.serialize(exception, ErrorVisibility.CODE_ONLY);

        assertThat(protocol.code()).isEqualTo("GROUP.COMMON.ILLEGAL_ARGUMENT");
        assertThat(protocol.message()).isEqualTo("Internal server error");
        assertThat(protocol.origin()).isEqualTo(ErrorOrigin.CALLER);
        assertThat(protocol.severity()).isEqualTo(ErrorSeverity.NORMAL);
        assertThat(protocol.sourceService()).isEqualTo("group");
    }

    @Test
    void roundTripsRemoteErrorWithoutLoadingLocalEnum() {
        final ErrorPropagationProtocol protocol = new ErrorPropagationProtocol(
                "ORDER.ORDER.NOT_FOUND", "not found", ErrorOrigin.CALLER, ErrorSeverity.NORMAL, "order");
        final ErrorCodeException exception = this.codec.deserialize(protocol);

        assertThat(exception.isRemoteError()).isTrue();
        assertThat(exception.asRemoteError()).contains(protocol);
        assertThat(exception.getMessage()).isEqualTo("not found");
        assertThat(exception.getErrorCode().getName()).isEqualTo("NOT_FOUND");
    }
}
