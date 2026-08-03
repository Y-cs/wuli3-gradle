package com.kjs.wuli3.web.internal.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.propagation.context.InvocationContext;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.WebContextProperties;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ContextFilterTest {

    @Test
    void doesNotCreateAuthenticationContextWithoutResolver() throws Exception {
        final ContextStore contextStore = new ContextStore();
        final ContextFilter filter = ContextFilterTest.filter(contextStore, null);

        filter.doFilter(
                new MockHttpServletRequest("GET", "/orders"), new MockHttpServletResponse(), (request, response) -> {
                    assertThat(contextStore.get(InvocationContext.class))
                            .map(InvocationContext::requestId)
                            .contains("rid-1");
                    assertThat(contextStore.get(AuthContext.class)).isEmpty();
                });

        assertThat(contextStore.get(InvocationContext.class)).isEmpty();
        assertThat(contextStore.get(AuthContext.class)).isEmpty();
    }

    @Test
    void storesAuthenticationContextWhenApplicationProvidesResolver() throws Exception {
        final ContextStore contextStore = new ContextStore();
        final AuthContextResolver authContextResolver = request -> Optional.of(new AuthContext(7L, "alice"));
        final ContextFilter filter = ContextFilterTest.filter(contextStore, authContextResolver);

        filter.doFilter(
                new MockHttpServletRequest("GET", "/orders"), new MockHttpServletResponse(), (request, response) -> {
                    assertThat(contextStore.get(AuthContext.class))
                            .map(AuthContext::userId)
                            .contains(7L);
                });

        assertThat(contextStore.get(AuthContext.class)).isEmpty();
    }

    private static ContextFilter filter(
            final ContextStore contextStore, final @Nullable AuthContextResolver authContextResolver) {
        return new ContextFilter(
                contextStore,
                authContextResolver,
                request -> "rid-1",
                request -> "127.0.0.1",
                new WebContextProperties());
    }
}
