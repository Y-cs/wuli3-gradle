package com.kjs.wuli3.web.internal.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.web.context.WebContextProperties;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class DefaultClientIpResolverTest {

    @Test
    void forwardingHeadersAreIgnoredWithoutTrustedNetworks() {
        final MockHttpServletRequest request = DefaultClientIpResolverTest.request("10.0.0.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertThat(DefaultClientIpResolverTest.resolver().resolve(request)).isEqualTo("10.0.0.10");
    }

    @Test
    void forwardingHeadersAreIgnoredForUntrustedPeer() {
        final MockHttpServletRequest request = DefaultClientIpResolverTest.request("192.0.2.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertThat(DefaultClientIpResolverTest.resolver("10.0.0.0/8").resolve(request))
                .isEqualTo("192.0.2.10");
    }

    @Test
    void trustedHopsAreRemovedFromRightToLeft() {
        final MockHttpServletRequest request = DefaultClientIpResolverTest.request("10.0.0.3");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1, 10.0.0.2");

        assertThat(DefaultClientIpResolverTest.resolver("10.0.0.0/8").resolve(request))
                .isEqualTo("203.0.113.10");
    }

    @Test
    void forwardedHeaderSupportsQuotedIpv6WithPort() {
        final MockHttpServletRequest request = DefaultClientIpResolverTest.request("2001:db8:100::10");
        request.addHeader("Forwarded", "for=\"[2001:db8:200::20]:8443\";proto=https");

        assertThat(DefaultClientIpResolverTest.resolver("2001:db8:100::/48").resolve(request))
                .isEqualTo("2001:db8:200::20");
    }

    @Test
    void malformedForwardingChainFallsBackToPeer() {
        final MockHttpServletRequest request = DefaultClientIpResolverTest.request("10.0.0.3");
        request.addHeader("X-Forwarded-For", "not-an-ip, 10.0.0.2");

        assertThat(DefaultClientIpResolverTest.resolver("10.0.0.0/8").resolve(request))
                .isEqualTo("10.0.0.3");
    }

    @Test
    void invalidTrustedNetworkFailsFast() {
        assertThatThrownBy(() -> DefaultClientIpResolverTest.resolver("10.0.0.0/99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10.0.0.0/99");
    }

    private static DefaultClientIpResolver resolver(final String... trustedProxyCidrs) {
        final WebContextProperties properties = new WebContextProperties();
        properties.setTrustedProxyCidrs(new ArrayList<>(List.of(trustedProxyCidrs)));
        return new DefaultClientIpResolver(properties);
    }

    private static MockHttpServletRequest request(final String remoteAddr) {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/client-ip");
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
