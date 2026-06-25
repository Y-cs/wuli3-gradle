package com.kjs.wuli3.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorPolicy;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;
import com.kjs.wuli3.core.error.SystemErrors;
import com.kjs.wuli3.core.function.Functions;
import com.kjs.wuli3.core.page.PageQuery;
import com.kjs.wuli3.core.page.PageResult;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreSmokeTest {
    @Test
    void pageQueryCalculatesOffset() {
        assertThat(new PageQuery(2, 20).offset()).isEqualTo(20);
    }

    @Test
    void pageResultCopiesRecords() {
        PageResult<String> result = new PageResult<>(List.of("a"), 1, 1, 10);
        assertThat(result.records()).containsExactly("a");
    }

    @Test
    void checkedExceptionIsWrapped() {
        assertThatThrownBy(() -> Functions.uncheckedSupplier(() -> {
                throw new IOException("io");
        }).get())
                .isInstanceOf(ErrorCodeException.class)
                .hasMessage("io");
    }

    @Test
    void systemErrorExposesName() {
        assertThat(SystemErrors.INTERNAL_ERROR.getName()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void errorCodeExceptionUpdatesPolicyWithControlledMethods() {
        ErrorCodeException exception = new ErrorCodeException(SystemErrors.INTERNAL_ERROR)
                .severity(ErrorSeverity.CRITICAL)
                .visibility(ErrorVisibility.INTERNAL);

        assertThat(exception.getErrorPolicy().severity()).isEqualTo(ErrorSeverity.CRITICAL);
        assertThat(exception.getErrorPolicy().visibility()).isEqualTo(ErrorVisibility.INTERNAL);
    }

    @Test
    void errorCodeExceptionPolicyOperatorReplacesPolicy() {
        ErrorCodeException exception = new ErrorCodeException(SystemErrors.INTERNAL_ERROR)
                .policy(policy -> policy.withSeverity(ErrorSeverity.WARNING)
                        .withVisibility(ErrorVisibility.CODE_ONLY));

        assertThat(exception.getErrorPolicy().severity()).isEqualTo(ErrorSeverity.WARNING);
        assertThat(exception.getErrorPolicy().visibility()).isEqualTo(ErrorVisibility.CODE_ONLY);
    }

    @Test
    void errorCodeExceptionIgnoresNullPolicyOperatorResults() {
        ErrorCodeException exception = new ErrorCodeException(SystemErrors.INTERNAL_ERROR)
                .severity(ErrorSeverity.WARNING)
                .policy(null)
                .policy(policy -> null);

        assertThat(exception.getErrorPolicy().severity()).isEqualTo(ErrorSeverity.WARNING);
        assertThat(exception.getErrorPolicy().visibility()).isEqualTo(ErrorVisibility.PUBLIC);
    }

    @Test
    void errorPolicyReturnedByExceptionIsImmutable() {
        ErrorCodeException exception = new ErrorCodeException(SystemErrors.INTERNAL_ERROR);
        ErrorPolicy policy = exception.getErrorPolicy();
        ErrorPolicy updatedPolicy = policy.withSeverity(ErrorSeverity.FATAL);

        assertThat(updatedPolicy.severity()).isEqualTo(ErrorSeverity.FATAL);
        assertThat(exception.getErrorPolicy().severity()).isEqualTo(ErrorSeverity.NORMAL);
    }
}
