package com.kjs.wuli3.propagation.encoding;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContextEncoderTest {

    @Test
    void standardEncoderWritesInvocationAndAuthenticationContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final ContextSnapshot snapshot =
                ContextSnapshot.of(new InvocationContext("10.0.0.8", "request-42"), new AuthContext(7L, "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.writeTo(snapshot, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        InvocationContextEncoder.REQUEST_ID, "request-42",
                        InvocationContextEncoder.ORIGIN_IP, "10.0.0.8",
                        AuthContextEncoder.USER_ID, "7",
                        AuthContextEncoder.USERNAME, "alice"));
        assertThat(encoder.reservedFieldNames())
                .containsExactlyInAnyOrder(
                        InvocationContextEncoder.REQUEST_ID,
                        InvocationContextEncoder.ORIGIN_IP,
                        AuthContextEncoder.USER_ID,
                        AuthContextEncoder.USERNAME);
    }

    @Test
    @SuppressWarnings("NullAway")
    void standardEncoderRoundTripsInvocationAndAuthenticationContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final ContextSnapshot source =
                ContextSnapshot.of(new InvocationContext("10.0.0.8", "request-42"), new AuthContext(7L, "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.writeTo(source, fields::put);
        final ContextSnapshot decoded = encoder.readFrom(fields::get);

        assertThat(decoded.get(InvocationContext.class)).contains(new InvocationContext("10.0.0.8", "request-42"));
        assertThat(decoded.get(AuthContext.class)).contains(new AuthContext(7L, "alice"));
    }

    @Test
    @SuppressWarnings("NullAway")
    void decoderSkipsIncompleteOrInvalidContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final Map<String, String> fields = Map.of(
                InvocationContextEncoder.REQUEST_ID, "request-42",
                AuthContextEncoder.USER_ID, "not-a-number",
                AuthContextEncoder.USERNAME, "alice");

        final ContextSnapshot decoded = encoder.readFrom(fields::get);

        assertThat(decoded.isEmpty()).isTrue();
    }

    @Test
    void customEncoderOnlyReadsWritesAndReservesConfiguredFields() {
        final ContextEncoder encoder = new ContextEncoder(List.of(new InvocationContextEncoder()));
        final ContextSnapshot source =
                ContextSnapshot.of(new InvocationContext("10.0.0.8", "request-42"), new AuthContext(7L, "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.writeTo(source, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        InvocationContextEncoder.REQUEST_ID, "request-42",
                        InvocationContextEncoder.ORIGIN_IP, "10.0.0.8"));
        assertThat(encoder.reservedFieldNames())
                .containsExactlyInAnyOrder(InvocationContextEncoder.REQUEST_ID, InvocationContextEncoder.ORIGIN_IP);
    }
}
