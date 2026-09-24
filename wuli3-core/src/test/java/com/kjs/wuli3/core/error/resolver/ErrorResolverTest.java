package com.kjs.wuli3.core.error.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.SystemErrors;
import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.model.ErrorVisibility;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import org.junit.jupiter.api.Test;

/** 验证错误码解析和错误可见性传播规则。 */
class ErrorResolverTest {

    private final ErrorResolver resolver = new ErrorResolver("group");

    @Test
    void appliesVisibilityPolicies() {
        for (final ErrorVisibility visibility : ErrorVisibility.values()) {
            final ErrorCodeCarrier carrier = this.resolver.resolveBoundary(
                    new ErrorCodeException(SystemErrors.CONFIGURATION_MISSING).withVisibility(visibility));
            if (visibility == ErrorVisibility.PUBLIC) {
                assertThat(carrier.code()).isEqualTo("GROUP.SYSTEM.CONFIGURATION_MISSING");
                assertThat(carrier.message()).isEqualTo(SystemErrors.CONFIGURATION_MISSING.getMessage());
            } else if (visibility == ErrorVisibility.CODE_ONLY) {
                assertThat(carrier.code()).isEqualTo("GROUP.SYSTEM.CONFIGURATION_MISSING");
                assertThat(carrier.message()).isEqualTo(SystemErrors.INTERNAL_ERROR.getMessage());
            } else if (visibility == ErrorVisibility.MESSAGE_ONLY) {
                assertThat(carrier.code()).isEqualTo("GROUP.SYSTEM.INTERNAL_ERROR");
                assertThat(carrier.message()).isEqualTo(SystemErrors.CONFIGURATION_MISSING.getMessage());
            } else {
                assertThat(carrier.code()).isEqualTo("GROUP.SYSTEM.INTERNAL_ERROR");
                assertThat(carrier.message()).isEqualTo(SystemErrors.INTERNAL_ERROR.getMessage());
            }
            assertThat(carrier.sourceService()).isEqualTo("group");
        }
    }

    @Test
    void preservesRemoteOriginalIdentityAndNeverRestoresHiddenCode() {
        final ErrorCodeCarrier remote = new ErrorCodeCarrier(
                "ORDER.SECRET.FAILURE",
                "GROUP.SYSTEM.INTERNAL_ERROR",
                "内部错误",
                ErrorOrigin.SERVER,
                ErrorSeverity.CRITICAL,
                "order");
        final ErrorCodeCarrier forwarded =
                this.resolver.resolveBoundary(new ErrorCodeException(remote).withVisibility(ErrorVisibility.PUBLIC));

        assertThat(forwarded.originalCode()).isEqualTo(remote.originalCode());
        assertThat(forwarded.code()).isEqualTo(remote.code());
        assertThat(forwarded.getName()).isEqualTo("FAILURE");
        assertThat(forwarded.sourceService()).isEqualTo("order");
    }
}
