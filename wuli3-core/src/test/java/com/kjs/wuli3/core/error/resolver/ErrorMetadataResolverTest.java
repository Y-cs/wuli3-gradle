package com.kjs.wuli3.core.error.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorMetadata;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorOrigin;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.builtin.ErrorFrameworkErrors;
import org.junit.jupiter.api.Test;

/** 验证错误模块及字段元数据解析规则。 */
class ErrorMetadataResolverTest {

    @Test
    void resolvesClassMetadataAndFieldOverride() {
        final ErrorMetadataResolver resolver = ErrorMetadataResolver.instance();
        assertThat(resolver.getErrorModule(TestErrors.DEFAULT).name()).isEqualTo("TEST");
        assertThat(resolver.getOrigin(TestErrors.DEFAULT)).isEqualTo(ErrorOrigin.SERVER);
        assertThat(resolver.getSeverity(TestErrors.DEFAULT)).isEqualTo(ErrorSeverity.WARNING);
        assertThat(resolver.getOrigin(TestErrors.OVERRIDDEN)).isEqualTo(ErrorOrigin.CALLER);
        assertThat(resolver.getSeverity(TestErrors.OVERRIDDEN)).isEqualTo(ErrorSeverity.NORMAL);
    }

    @Test
    void defaultsMetadataWhenClassHasNoMetadata() {
        final ErrorMetadataResolver resolver = ErrorMetadataResolver.instance();
        assertThat(resolver.getOrigin(DefaultErrors.VALUE)).isEqualTo(ErrorOrigin.CALLER);
        assertThat(resolver.getSeverity(DefaultErrors.VALUE)).isEqualTo(ErrorSeverity.NORMAL);
    }

    @Test
    void resolvesMetadataFromDeclaringTypeForConstantSpecificClass() {
        final ErrorMetadataResolver resolver = ErrorMetadataResolver.instance();
        assertThat(resolver.getOrigin(TypeAnnotatedErrors.CONSTANT_SPECIFIC)).isEqualTo(ErrorOrigin.SERVER);
        assertThat(resolver.getSeverity(TypeAnnotatedErrors.CONSTANT_SPECIFIC)).isEqualTo(ErrorSeverity.CRITICAL);
    }

    @Test
    void rejectsErrorCodeWithoutModuleMetadata() {
        assertThatThrownBy(() -> ErrorMetadataResolver.instance().getErrorModule(MissingModuleErrors.VALUE))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        exception ->
                                assertThat(exception.getErrorCode()).isEqualTo(ErrorFrameworkErrors.MODULE_NOT_FOUND));
    }

    @ErrorModule(
            name = "TEST",
            defaultMetadata = @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.WARNING))
    private enum TestErrors implements ErrorCode {
        DEFAULT("默认错误"),
        @ErrorMetadata(origin = ErrorOrigin.CALLER, severity = ErrorSeverity.NORMAL)
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

    @ErrorModule(name = "DEFAULTS")
    private enum DefaultErrors implements ErrorCode {
        VALUE("默认错误");

        private final String message;

        DefaultErrors(final String message) {
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

    @ErrorModule(name = "TYPE_ANNOTATED")
    @ErrorMetadata(origin = ErrorOrigin.SERVER, severity = ErrorSeverity.CRITICAL)
    private enum TypeAnnotatedErrors implements ErrorCode {
        CONSTANT_SPECIFIC {
            @Override
            public String getMessage() {
                return "常量特定错误";
            }
        };

        @Override
        public abstract String getMessage();
    }
}
