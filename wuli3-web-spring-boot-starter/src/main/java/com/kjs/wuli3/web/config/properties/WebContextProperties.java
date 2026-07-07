package com.kjs.wuli3.web.config.properties;

import com.kjs.wuli3.web.context.RequestIds;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.util.unit.DataSize;

/**
 * Web 请求上下文构建配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.web.context")
public class WebContextProperties {

    /**
     * 是否启用 web 请求上下文过滤器。
     */
    private boolean enabled = true;

    /**
     * 上下文过滤器顺序，应尽早执行以便后续链路可读取 requestId 和调用上下文。
     */
    private int filterOrder = Ordered.HIGHEST_PRECEDENCE + 10;

    /**
     * requestId 请求头和响应头名称。
     */
    private String requestIdHeaderName = RequestIds.HEADER_NAME;

    /**
     * 是否接受调用方传入的 requestId。
     */
    private boolean acceptExternalRequestId = true;

    /**
     * 接受外部 requestId 时允许的最大长度。
     */
    private int requestIdMaxLength = 128;

    /**
     * 外部 requestId 不合法时的处理策略。
     */
    private InvalidRequestIdPolicy invalidRequestIdPolicy = InvalidRequestIdPolicy.REGENERATE;

    /**
     * 是否缓存请求体以支持下游重复读取。
     */
    private boolean requestBodyCacheEnabled = true;

    /**
     * 可缓存请求体的最大大小。
     */
    private DataSize maxCacheBodySize = DataSize.ofMegabytes(1);

    /**
     * 允许缓存请求体的 Content-Type 模式。
     */
    private List<String> cacheableContentTypes = new ArrayList<>(
            List.of("application/json", "application/*+json", "application/x-www-form-urlencoded", "text/*"));

    /**
     * 即使匹配可缓存类型也必须排除的 Content-Type 模式。
     */
    private List<String> excludedBodyCacheContentTypes =
            new ArrayList<>(List.of("multipart/*", "application/octet-stream", "text/event-stream"));

    /**
     * 是否信任代理转发的客户端 IP 请求头。
     */
    private boolean trustedProxyEnabled = false;

    /**
     * 可信代理开启后用于解析客户端 IP 的请求头优先级。
     */
    private List<String> clientIpHeaderPriority = new ArrayList<>(List.of("X-Forwarded-For", "X-Real-IP", "Forwarded"));

    public enum InvalidRequestIdPolicy {
        REGENERATE,
        USE_AS_IS
    }
}
