package com.kjs.wuli3.core.error.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.error.code.CommonErrors;
import com.kjs.wuli3.core.error.code.ErrorCode;
import com.kjs.wuli3.core.error.code.ErrorFrameworkErrors;
import com.kjs.wuli3.core.error.code.SystemErrors;
import com.kjs.wuli3.core.error.exception.ErrorCodeException;
import com.kjs.wuli3.core.error.policy.ErrorOrigin;
import com.kjs.wuli3.core.error.policy.ErrorPolicy;
import com.kjs.wuli3.core.error.policy.ErrorSeverity;
import com.kjs.wuli3.core.error.policy.ErrorVisibility;
import com.kjs.wuli3.core.error.policy.ResolvedErrorPolicy;
import org.junit.jupiter.api.Test;

class ErrorMetadataParserTest {

    @Test
    void resolvesModulePolicyAndConstantOverride() {
        final ErrorMetadataParser parser = ErrorMetadataParser.instance();

        assertThat(parser.getErrorModule(TestErrors.DEFAULT).value()).isEqualTo("TEST");
        assertThat(parser.getErrorPolicy(TestErrors.DEFAULT))
                .isEqualTo(
                        new ResolvedErrorPolicy(ErrorSeverity.WARNING, ErrorVisibility.CODE_ONLY, ErrorOrigin.SYSTEM));
        assertThat(parser.getErrorPolicy(TestErrors.OVERRIDDEN))
                .isEqualTo(new ResolvedErrorPolicy(ErrorSeverity.NORMAL, ErrorVisibility.PUBLIC, ErrorOrigin.CALLER));
    }

    @Test
    void exceptionAllowsExplicitPolicyOverride() {
        final ErrorCodeException exception = new ErrorCodeException(TestErrors.DEFAULT)
                .severity(ErrorSeverity.FATAL)
                .visibility(ErrorVisibility.INTERNAL)
                .origin(ErrorOrigin.CALLER);

        assertThat(exception.getResolvedErrorPolicy())
                .isEqualTo(new ResolvedErrorPolicy(ErrorSeverity.FATAL, ErrorVisibility.INTERNAL, ErrorOrigin.CALLER));
    }

    @Test
    void separatesCommonAndSystemBuiltInErrorPolicies() {
        final ErrorMetadataParser parser = ErrorMetadataParser.instance();

        assertThat(parser.getErrorModule(CommonErrors.ILLEGAL_ARGUMENT).value()).isEqualTo("COMMON");
        assertThat(parser.getErrorPolicy(CommonErrors.ILLEGAL_ARGUMENT))
                .isEqualTo(new ResolvedErrorPolicy(ErrorSeverity.NORMAL, ErrorVisibility.PUBLIC, ErrorOrigin.CALLER));
        assertThat(parser.getErrorPolicy(SystemErrors.CONFIGURATION_MISSING))
                .isEqualTo(
                        new ResolvedErrorPolicy(ErrorSeverity.CRITICAL, ErrorVisibility.INTERNAL, ErrorOrigin.SYSTEM));
    }

    @Test
    void rejectsErrorCodeWithoutModuleMetadata() {
        assertThatThrownBy(() -> {
                    throw new ErrorCodeException(MissingModuleErrors.VALUE);
                })
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        exception ->
                                assertThat(exception.getErrorCode()).isEqualTo(ErrorFrameworkErrors.MODULE_NOT_FOUND));
    }

    @ErrorModule(
            value = "TEST",
            policy =
                    @ErrorPolicy(
                            severity = ErrorSeverity.WARNING,
                            visibility = ErrorVisibility.CODE_ONLY,
                            origin = ErrorOrigin.SYSTEM))
    private enum TestErrors implements ErrorCode {
        DEFAULT("默认错误"),

        @ErrorPolicy(severity = ErrorSeverity.NORMAL, visibility = ErrorVisibility.PUBLIC, origin = ErrorOrigin.CALLER)
        OVERRIDDEN("覆盖错误");

        private final String message;

        TestErrors(final String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    private enum MissingModuleErrors implements ErrorCode {
        VALUE("缺少模块");

        private final String message;

        MissingModuleErrors(final String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
