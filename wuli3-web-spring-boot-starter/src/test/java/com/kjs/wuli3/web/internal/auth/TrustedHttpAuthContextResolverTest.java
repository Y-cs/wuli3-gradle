package com.kjs.wuli3.web.internal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.PrincipalType;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class TrustedHttpAuthContextResolverTest {

    private final TrustedHttpAuthContextResolver resolver = new TrustedHttpAuthContextResolver();

    @Test
    void resolvesAuthenticationContextFromTrustedHeaders() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.PRINCIPAL_TYPE, "CUSTOMER");
        request.addHeader(AuthContextEncoder.PRINCIPAL_ID, "42");
        request.addHeader(AuthContextEncoder.PRINCIPAL_NAME, "alice");

        assertThat(this.resolver.resolve(request)).contains(new AuthContext(PrincipalType.CUSTOMER, "42", "alice"));
    }

    @Test
    void returnsEmptyWhenAuthenticationHeadersAreIncomplete() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.PRINCIPAL_TYPE, "CUSTOMER");
        request.addHeader(AuthContextEncoder.PRINCIPAL_ID, "42");

        assertThat(this.resolver.resolve(request)).isEmpty();
    }

    @Test
    void returnsEmptyWhenPrincipalTypeIsUnknown() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.PRINCIPAL_TYPE, "UNKNOWN");
        request.addHeader(AuthContextEncoder.PRINCIPAL_ID, "42");
        request.addHeader(AuthContextEncoder.PRINCIPAL_NAME, "alice");

        assertThat(this.resolver.resolve(request)).isEmpty();
    }

    @Test
    void returnsEmptyWhenAnyAuthenticationHeaderIsBlank() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.PRINCIPAL_TYPE, "CUSTOMER");
        request.addHeader(AuthContextEncoder.PRINCIPAL_ID, " ");
        request.addHeader(AuthContextEncoder.PRINCIPAL_NAME, "alice");

        assertThat(this.resolver.resolve(request)).isEmpty();
    }
}
