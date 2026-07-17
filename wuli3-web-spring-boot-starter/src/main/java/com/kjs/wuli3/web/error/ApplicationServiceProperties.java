package com.kjs.wuli3.web.error;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Web starter 使用的应用元数据配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "application.service")
@Validated
public class ApplicationServiceProperties {

    private String serviceCode = "";

    @AssertTrue(message = "service-code must be empty or contain at most 64 letters, digits, '.', '_' or '-'")
    public boolean isServiceCodeValid() {
        return this.serviceCode.isBlank() || this.serviceCode.matches("[A-Za-z0-9._-]{1,64}");
    }
}
