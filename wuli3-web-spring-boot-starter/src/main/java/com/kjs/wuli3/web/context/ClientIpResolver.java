package com.kjs.wuli3.web.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client IP address represented by a servlet request.
 */
@FunctionalInterface
public interface ClientIpResolver {

    /**
     * Return the trusted client IP for the current request.
     */
    String resolve(HttpServletRequest request);
}
