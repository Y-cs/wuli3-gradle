package com.kjs.wuli3.web.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 解析标识当前 HTTP 调用的请求 ID。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface RequestIdResolver {

    /**
     * 返回当前请求的有效请求 ID。
     */
    String resolve(HttpServletRequest request);
}
