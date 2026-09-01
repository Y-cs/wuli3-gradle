package com.kjs.wuli3.propagation.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContextPropagatorTest {

    @Test
    void standardEncoderWritesInvocationAndAuthenticationContexts() {
        final ContextPropagator encoder = new ContextPropagator(ContextPropagator.standardContextEncoder());
        final ContextSnapshot snapshot = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.inject(snapshot, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        InvocationContextCodec.REQUEST_ID, "request-42",
                        InvocationContextCodec.ORIGIN_IP, "10.0.0.8",
                        AuthContextCodec.PRINCIPAL_TYPE, "CUSTOMER",
                        AuthContextCodec.PRINCIPAL_ID, "7",
                        AuthContextCodec.PRINCIPAL_NAME, "alice"));
        assertThat(encoder.reservedFieldNames())
                .containsExactlyInAnyOrder(
                        InvocationContextCodec.REQUEST_ID,
                        InvocationContextCodec.ORIGIN_IP,
                        AuthContextCodec.PRINCIPAL_TYPE,
                        AuthContextCodec.PRINCIPAL_ID,
                        AuthContextCodec.PRINCIPAL_NAME);
    }

    @Test
    @SuppressWarnings("NullAway")
    void standardEncoderRoundTripsInvocationAndAuthenticationContexts() {
        final ContextPropagator encoder = new ContextPropagator(ContextPropagator.standardContextEncoder());
        final ContextSnapshot source = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.inject(source, fields::put);
        final ContextSnapshot decoded = encoder.extract(fields::get);

        assertThat(decoded.get(InvocationContext.class)).contains(new InvocationContext("10.0.0.8", "request-42"));
        assertThat(decoded.get(AuthContext.class)).contains(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
    }

    @Test
    @SuppressWarnings("NullAway")
    void decoderSkipsIncompleteOrInvalidContexts() {
        final ContextPropagator encoder = new ContextPropagator(ContextPropagator.standardContextEncoder());
        final Map<String, String> fields = Map.of(
                InvocationContextCodec.REQUEST_ID, "request-42",
                AuthContextCodec.PRINCIPAL_TYPE, "UNKNOWN",
                AuthContextCodec.PRINCIPAL_ID, "7",
                AuthContextCodec.PRINCIPAL_NAME, "alice");

        final ContextSnapshot decoded = encoder.extract(fields::get);

        assertThat(decoded.isEmpty()).isTrue();
    }

    @Test
    void customEncoderOnlyReadsWritesAndReservesConfiguredFields() {
        final ContextPropagator encoder = new ContextPropagator(List.of(new InvocationContextCodec()));
        final ContextSnapshot source = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.inject(source, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        InvocationContextCodec.REQUEST_ID, "request-42",
                        InvocationContextCodec.ORIGIN_IP, "10.0.0.8"));
        assertThat(encoder.reservedFieldNames())
                .containsExactlyInAnyOrder(InvocationContextCodec.REQUEST_ID, InvocationContextCodec.ORIGIN_IP);
    }
}
