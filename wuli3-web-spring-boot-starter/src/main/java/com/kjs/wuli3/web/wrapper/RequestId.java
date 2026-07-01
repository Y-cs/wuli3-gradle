package com.kjs.wuli3.web.wrapper;

/**
 * Request id names used by web context propagation.
 */
public final class RequestId {
    public static final String HEADER_NAME = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private RequestId() {
    }
}
