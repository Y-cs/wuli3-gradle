package com.kjs.wuli3.web.auth;

import com.kjs.wuli3.propagation.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Resolves authentication context from an HTTP request.
 */
@FunctionalInterface
public interface AuthContextResolver {

    Optional<AuthContext> resolve(final HttpServletRequest request);
}
