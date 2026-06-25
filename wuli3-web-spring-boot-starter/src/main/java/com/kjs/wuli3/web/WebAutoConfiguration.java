package com.kjs.wuli3.web;

import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.web.error.WebErrorCodeResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

/**
 * Web starter 的自动配置入口。
 */
@AutoConfiguration
@ConfigurationPropertiesScan("com.kjs.wuli3.web.config.properties")
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ErrorCodeResolver.class)
    public WebErrorCodeResolver webErrorCodeResolver() {
        return new WebErrorCodeResolver();
    }

}
