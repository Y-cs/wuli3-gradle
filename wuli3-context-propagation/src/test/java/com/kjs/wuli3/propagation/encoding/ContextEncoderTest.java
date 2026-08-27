package com.kjs.wuli3.propagation.encoding;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.snapshot.ContextSnapshot;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContextEncoderTest {

    @Test
    void standardEncoderWritesInvocationAndAuthenticationContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final ContextSnapshot snapshot = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.writeTo(snapshot, fields::put);

        assertThat(fields)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        InvocationContextEncoder.REQUEST_ID, "request-42",
                        InvocationContextEncoder.ORIGIN_IP, "10.0.0.8",
                        AuthContextEncoder.PRINCIPAL_TYPE, "CUSTOMER",
                        AuthContextEncoder.PRINCIPAL_ID, "7",
                        AuthContextEncoder.PRINCIPAL_NAME, "alice"));
        assertThat(encoder.reservedFieldNames())
                .containsExactlyInAnyOrder(
                        InvocationContextEncoder.REQUEST_ID,
                        InvocationContextEncoder.ORIGIN_IP,
                        AuthContextEncoder.PRINCIPAL_TYPE,
                        AuthContextEncoder.PRINCIPAL_ID,
                        AuthContextEncoder.PRINCIPAL_NAME);
    }

    @Test
    @SuppressWarnings("NullAway")
    void standardEncoderRoundTripsInvocationAndAuthenticationContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final ContextSnapshot source = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
        final Map<String, String> fields = new LinkedHashMap<>();

        encoder.writeTo(source, fields::put);
        final ContextSnapshot decoded = encoder.readFrom(fields::get);

        assertThat(decoded.get(InvocationContext.class)).contains(new InvocationContext("10.0.0.8", "request-42"));
        assertThat(decoded.get(AuthContext.class)).contains(new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
    }

    @Test
    @SuppressWarnings("NullAway")
    void decoderSkipsIncompleteOrInvalidContexts() {
        final ContextEncoder encoder = new ContextEncoder(ContextEncoder.standardContextEncoder());
        final Map<String, String> fields = Map.of(
                InvocationContextEncoder.REQUEST_ID, "request-42",
                AuthContextEncoder.PRINCIPAL_TYPE, "UNKNOWN",
                AuthContextEncoder.PRINCIPAL_ID, "7",
                AuthContextEncoder.PRINCIPAL_NAME, "alice");

        final ContextSnapshot decoded = encoder.readFrom(fields::get);

        assertThat(decoded.isEmpty()).isTrue();
    }

    @Test
    void customEncoderOnlyReadsWritesAndReservesConfiguredFields() {
        final ContextEncoder encoder = new ContextEncoder(List.of(new InvocationContextEncoder()));
        final ContextSnapshot source = ContextSnapshot.of(
                new InvocationContext("10.0.0.8", "request-42"), new AuthContext(PrincipalType.CUSTOMER, "7", "alice"));
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
