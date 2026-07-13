package com.kjs.wuli3.core.assertion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.SystemErrors;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssertsTest {

    @Test
    void passesWhenAssertionsAreSatisfied() {
        assertThatCode(() -> Asserts.isTrue(true).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> Asserts.notNull("value").throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> Asserts.notBlank("value").throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> Asserts.notEmpty("value").throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> Asserts.isEmptyCollection(List.of()).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() ->
                        Asserts.isNotEmptyCollection(List.of("value")).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> Asserts.isEmptyMap(Map.of()).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
        assertThatCode(() ->
                        Asserts.isNotEmptyMap(Map.of("key", "value")).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
    }

    @Test
    void failsWithErrorCodeException() {
        assertThatThrownBy(() -> Asserts.notBlank(" ").throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        exception -> assertThat(exception.getErrorCode()).isSameAs(SystemErrors.ILLEGAL_ARGUMENT));
    }

    @Test
    void failsWithErrorCodeExceptionAndCustomMessage() {
        assertThatThrownBy(() -> Asserts.notEmpty("").throwException(SystemErrors.ILLEGAL_ARGUMENT, "empty value"))
                .isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
                    assertThat(exception.getErrorCode()).isSameAs(SystemErrors.ILLEGAL_ARGUMENT);
                    assertThat(exception).hasMessage("empty value");
                });
    }

    @Test
    void failsWithInternalErrorWhenOnlyMessageIsProvided() {
        assertThatThrownBy(() -> Asserts.isTrue(false).throwIllegalArgumentException("invalid state"))
                .isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
                    assertThat(exception.getErrorCode()).isSameAs(SystemErrors.ILLEGAL_ARGUMENT);
                    assertThat(exception).hasMessage("invalid state");
                });
    }

    @Test
    void failsWhenCollectionAndMapAssertionsAreNotSatisfied() {
        assertThatThrownBy(
                        () -> Asserts.isEmptyCollection(List.of("value")).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        exception -> assertThat(exception.getErrorCode()).isSameAs(SystemErrors.ILLEGAL_ARGUMENT));
        assertThatThrownBy(() -> Asserts.isNotEmptyCollection(List.of()).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .isInstanceOf(ErrorCodeException.class);
        assertThatThrownBy(
                        () -> Asserts.isEmptyMap(Map.of("key", "value")).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .isInstanceOf(ErrorCodeException.class);
        assertThatThrownBy(() -> Asserts.isNotEmptyMap(Map.of()).throwException(SystemErrors.ILLEGAL_ARGUMENT))
                .isInstanceOf(ErrorCodeException.class);
    }
}
