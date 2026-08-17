package com.kjs.wuli3.web.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Web 统一响应处理配置。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "wuli3.web.response")
@Validated
public class WebResponseProperties {

    /**
     * 是否启用 Controller 返回值自动包装。
     */
    private boolean wrapperEnabled = true;

    /**
     * 是否启用统一异常处理。
     */
    private boolean exceptionHandlerEnabled = true;

    /**
     * 是否包装 ResponseEntity 的 body。
     */
    private boolean wrapResponseEntityBody = true;

    /**
     * 成功响应的 message。
     */
    private String successMessage = "";

    /**
     * 是否在参数校验失败时返回字段级错误详情。
     */
    private boolean validationDetailEnabled = true;
}
