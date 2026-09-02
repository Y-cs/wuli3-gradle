package com.kjs.wuli3.core.assertion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.builtin.CommonErrors;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 验证失败条件断言的判断语义与异常转换行为。
 *
 * @author GuoYang create on 2026/9/1 17:55
 */
class AssertsTest {

    @Test
    void exposesFailureConditionSemantics() {
        assertThat(Asserts.whenFalse(false).condition()).isTrue();
        assertThat(Asserts.whenTrue(true).condition()).isTrue();
        assertThat(Asserts.whenNull(null).condition()).isTrue();
        assertThat(Asserts.whenNotNull("value").condition()).isTrue();
        assertThat(Asserts.whenBlank(" ").condition()).isTrue();
        assertThat(Asserts.whenNotBlank("value").condition()).isTrue();
        assertThat(Asserts.whenEmpty("").condition()).isTrue();
        assertThat(Asserts.whenNotEmpty("value").condition()).isTrue();
        assertThat(Asserts.whenEmptyCollection(List.of()).condition()).isTrue();
        assertThat(Asserts.whenNotEmptyCollection(List.of("value")).condition()).isTrue();
        assertThat(Asserts.whenEmptyMap(Map.of()).condition()).isTrue();
        assertThat(Asserts.whenNotEmptyMap(Map.of("key", "value")).condition()).isTrue();
    }

    @Test
    void exposesNonFailureConditionSemantics() {
        assertThat(Asserts.whenFalse(true).condition()).isFalse();
        assertThat(Asserts.whenTrue(false).condition()).isFalse();
        assertThat(Asserts.whenNull("value").condition()).isFalse();
        assertThat(Asserts.whenNotNull(null).condition()).isFalse();
        assertThat(Asserts.whenBlank("value").condition()).isFalse();
        assertThat(Asserts.whenNotBlank(" ").condition()).isFalse();
        assertThat(Asserts.whenEmpty("value").condition()).isFalse();
        assertThat(Asserts.whenNotEmpty("").condition()).isFalse();
        assertThat(Asserts.whenEmptyCollection(List.of("value")).condition()).isFalse();
        assertThat(Asserts.whenNotEmptyCollection(List.of()).condition()).isFalse();
        assertThat(Asserts.whenEmptyMap(Map.of("key", "value")).condition()).isFalse();
        assertThat(Asserts.whenNotEmptyMap(Map.of()).condition()).isFalse();
    }

    @Test
    void treatsNullAsEmptyButNotAsNonEmpty() {
        assertThat(Asserts.whenBlank(null).condition()).isTrue();
        assertThat(Asserts.whenNotBlank(null).condition()).isFalse();
        assertThat(Asserts.whenEmpty(null).condition()).isTrue();
        assertThat(Asserts.whenNotEmpty(null).condition()).isFalse();
        assertThat(Asserts.whenEmptyCollection(null).condition()).isTrue();
        assertThat(Asserts.whenNotEmptyCollection(null).condition()).isFalse();
        assertThat(Asserts.whenEmptyMap(null).condition()).isTrue();
        assertThat(Asserts.whenNotEmptyMap(null).condition()).isFalse();
    }

    @Test
    void reversesFailureCondition() {
        assertThat(Asserts.whenTrue(true).reversed().condition()).isFalse();
        assertThat(Asserts.whenTrue(false).reversed().condition()).isTrue();
    }

    @Test
    void doesNotThrowWhenFailureConditionIsFalse() {
        assertThatCode(() -> Asserts.whenTrue(false).throwException(CommonErrors.ILLEGAL_ARGUMENT))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsErrorCodeException() {
        assertThatThrownBy(() -> Asserts.whenTrue(true).throwException(CommonErrors.ILLEGAL_ARGUMENT))
                .isInstanceOfSatisfying(
                        ErrorCodeException.class,
                        exception -> assertThat(exception.getErrorCode()).isSameAs(CommonErrors.ILLEGAL_ARGUMENT));
    }

    @Test
    void throwsErrorCodeExceptionWithCustomMessage() {
        assertThatThrownBy(() -> Asserts.whenEmpty("").throwException(CommonErrors.ILLEGAL_ARGUMENT, "empty value"))
                .isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
                    assertThat(exception.getErrorCode()).isSameAs(CommonErrors.ILLEGAL_ARGUMENT);
                    assertThat(exception).hasMessage("empty value");
                });
    }

    @Test
    void throwsIllegalArgumentErrorCode() {
        assertThatThrownBy(() -> Asserts.whenFalse(false).throwIllegalArgumentException("invalid state"))
                .isInstanceOfSatisfying(ErrorCodeException.class, exception -> {
                    assertThat(exception.getErrorCode()).isSameAs(CommonErrors.ILLEGAL_ARGUMENT);
                    assertThat(exception).hasMessage("invalid state");
                });
    }
}
