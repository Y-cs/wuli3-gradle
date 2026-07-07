package com.kjs.wuli3.web.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web starter 使用的应用元数据配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application.service")
public class ApplicationServiceProperties {

    private String serviceCode = "";
}
