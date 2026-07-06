package com.kjs.wuli3.web.internal.context;

import com.kjs.wuli3.propagation.context.AuthContext;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Default anonymous security context resolver.
 */
public final class DefaultAuthContextResolver implements AuthContextResolver {

    @Override
    public AuthContext resolve(final HttpServletRequest request) {
        return new AuthContext(0L, "");
    }
}
