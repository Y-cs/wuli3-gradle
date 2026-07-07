package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.codec.InvocationContextCodec;

/**
 * Request id names used by web context propagation.
 */
public final class RequestIds {
    public static final String HEADER_NAME = InvocationContextCodec.REQUEST_ID;
    public static final String MDC_KEY = "requestId";

    private RequestIds() {}
}
