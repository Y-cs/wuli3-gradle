package com.kjs.wuli3.web.internal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.encoding.AuthContextEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class TrustedHttpAuthContextResolverTest {

    private final TrustedHttpAuthContextResolver resolver = new TrustedHttpAuthContextResolver();

    @Test
    void resolvesAuthenticationContextFromTrustedHeaders() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.USER_ID, "42");
        request.addHeader(AuthContextEncoder.USERNAME, "alice");

        assertThat(this.resolver.resolve(request)).contains(new AuthContext(42L, "alice"));
    }

    @Test
    void returnsEmptyWhenAuthenticationHeadersAreIncomplete() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.USER_ID, "42");

        assertThat(this.resolver.resolve(request)).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserIdIsMalformed() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(AuthContextEncoder.USER_ID, "not-a-number");
        request.addHeader(AuthContextEncoder.USERNAME, "alice");

        assertThat(this.resolver.resolve(request)).isEmpty();
    }
}
