package com.kjs.wuli3.core.error.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.kjs.wuli3.core.error.model.ErrorOrigin;
import com.kjs.wuli3.core.error.model.ErrorSeverity;
import com.kjs.wuli3.core.error.propagation.ErrorCodeCarrier;
import com.kjs.wuli3.core.error.propagation.ErrorCodePropagator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 验证错误传播协议的函数式字段编解码规则。
 *
 * @author GuoYang create on 2026/8/31 10:00
 */
class ErrorCodePropagatorTest {

    private final ErrorCodePropagator encoder = new ErrorCodePropagator();

    @Test
    void writesFixedPropagationFields() {
        final ErrorCodeCarrier protocol = ErrorCodePropagatorTest.protocol();
        final Map<String, String> fields = new LinkedHashMap<>();

        this.encoder.inject(protocol, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        ErrorCodePropagator.CODE, protocol.code(),
                        ErrorCodePropagator.MESSAGE, protocol.message(),
                        ErrorCodePropagator.ORIGIN, protocol.origin().name(),
                        ErrorCodePropagator.SEVERITY, protocol.severity().name(),
                        ErrorCodePropagator.SOURCE_SERVICE, protocol.sourceService()));
    }

    @Test
    @SuppressWarnings("NullAway")
    void roundTripsCompleteProtocol() {
        final ErrorCodeCarrier protocol = ErrorCodePropagatorTest.protocol();
        final Map<String, String> fields = new LinkedHashMap<>();

        this.encoder.inject(protocol, fields::put);

        assertThat(this.encoder.extract(fields::get)).contains(protocol);
    }

    @Test
    @SuppressWarnings("NullAway")
    void defaultsMissingSourceServiceToEmptyString() {
        final Map<String, String> fields = new LinkedHashMap<>();
        this.encoder.inject(ErrorCodePropagatorTest.protocol(), fields::put);
        fields.remove(ErrorCodePropagator.SOURCE_SERVICE);

        assertThat(this.encoder.extract(fields::get))
                .contains(new ErrorCodeCarrier(
                        "ORDER.ORDER.NOT_FOUND", "not found", ErrorOrigin.CALLER, ErrorSeverity.NORMAL, ""));
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsIncompleteOrInvalidFields() {
        final Map<String, String> fields = new LinkedHashMap<>();
        this.encoder.inject(ErrorCodePropagatorTest.protocol(), fields::put);
        fields.remove(ErrorCodePropagator.MESSAGE);
        assertThat(this.encoder.extract(fields::get)).isEmpty();

        fields.put(ErrorCodePropagator.MESSAGE, "not found");
        fields.put(ErrorCodePropagator.ORIGIN, "UNKNOWN");
        assertThat(this.encoder.extract(fields::get)).isEmpty();

        fields.put(ErrorCodePropagator.ORIGIN, ErrorOrigin.CALLER.name());
        fields.put(ErrorCodePropagator.CODE, " ");
        assertThat(this.encoder.extract(fields::get)).isEmpty();
    }

    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullArguments() {
        final ErrorCodeCarrier protocol = ErrorCodePropagatorTest.protocol();
        final Map<String, String> fields = new LinkedHashMap<>();

        assertThatNullPointerException().isThrownBy(() -> this.encoder.inject(null, fields::put));
        assertThatNullPointerException().isThrownBy(() -> this.encoder.inject(protocol, null));
        assertThatNullPointerException().isThrownBy(() -> this.encoder.extract(null));
    }

    private static ErrorCodeCarrier protocol() {
        return new ErrorCodeCarrier(
                "ORDER.ORDER.NOT_FOUND", "not found", ErrorOrigin.CALLER, ErrorSeverity.NORMAL, "order");
    }
}
