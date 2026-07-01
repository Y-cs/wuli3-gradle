package com.kjs.wuli3.web.auth;

import com.kjs.wuli3.propagation.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Default anonymous security context resolver.
 */
public final class DefaultAuthContextResolver implements AuthContextResolver {

    @Override
    public AuthContext resolve(HttpServletRequest request) {
        return new AuthContext(0L, "");
    }
}
