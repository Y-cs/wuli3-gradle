package com.kjs.wuli3.web.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the request id that identifies the current HTTP invocation.
 */
@FunctionalInterface
public interface RequestIdResolver {

    /**
     * Return a valid request id for the current request.
     */
    String resolve(HttpServletRequest request);
}
