package com.kjs.wuli3.web.context;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.validation.annotation.Validated;

/**
 * Web 请求上下文构建配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.web.context")
@Validated
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
    @NotBlank
    private String requestIdHeaderName = RequestIds.HEADER_NAME;

    /**
     * 是否接受调用方传入的 requestId。
     */
    private boolean acceptExternalRequestId = true;

    /**
     * 接受外部 requestId 时允许的最大长度。
     */
    @Min(1)
    @Max(512)
    private int requestIdMaxLength = 128;

    /**
     * 允许作为直接 peer 信任转发头的代理网段；为空时忽略所有转发头。
     */
    private List<@NotBlank String> trustedProxyCidrs = new ArrayList<>();

    /**
     * 命中可信代理网段后用于解析客户端 IP 的请求头优先级。
     */
    @NotEmpty
    private List<@NotBlank String> clientIpHeaderPriority =
            new ArrayList<>(List.of("X-Forwarded-For", "X-Real-IP", "Forwarded"));
}
