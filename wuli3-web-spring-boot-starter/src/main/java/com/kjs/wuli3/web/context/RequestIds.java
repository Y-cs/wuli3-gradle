package com.kjs.wuli3.web.context;

import com.kjs.wuli3.propagation.encoding.InvocationContextEncoder;

/**
 * Web 上下文传播使用的请求 ID 名称。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class RequestIds {
    public static final String HEADER_NAME = InvocationContextEncoder.REQUEST_ID;
    public static final String MDC_KEY = "requestId";

    private RequestIds() {}
}
