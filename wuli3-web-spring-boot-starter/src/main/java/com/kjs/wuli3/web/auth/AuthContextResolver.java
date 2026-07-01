package com.kjs.wuli3.web.auth;

import com.kjs.wuli3.propagation.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves authentication context from an HTTP request.
 */
@FunctionalInterface
public interface AuthContextResolver {

    AuthContext resolve(HttpServletRequest request);
}
