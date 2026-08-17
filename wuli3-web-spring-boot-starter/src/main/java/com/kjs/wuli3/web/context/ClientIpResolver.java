package com.kjs.wuli3.web.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 解析 Servlet 请求所代表的客户端 IP 地址。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface ClientIpResolver {

    /**
     * 返回当前请求中经过信任校验的客户端 IP。
     */
    String resolve(HttpServletRequest request);
}
