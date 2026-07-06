package com.kjs.wuli3.web.context;

/**
 * Request id names used by web context propagation.
 */
public final class RequestIds {
    public static final String HEADER_NAME = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private RequestIds() {
    }
}
