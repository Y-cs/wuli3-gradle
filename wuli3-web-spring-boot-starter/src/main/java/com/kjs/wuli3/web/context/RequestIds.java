package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;

/**
 * Request id names used by web context propagation.
 */
public final class RequestIds {
    public static final String HEADER_NAME = InvocationContextEncoder.REQUEST_ID;
    public static final String MDC_KEY = "requestId";

    private RequestIds() {}
}
