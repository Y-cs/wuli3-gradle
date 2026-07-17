package com.kjs.wuli3.web.autoconfigure;

import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.web.error.ApplicationServiceProperties;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.internal.error.DefaultWebErrorStatusResolver;
import com.kjs.wuli3.web.internal.error.WebErrorCodeResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures the web-facing error model. */
@AutoConfiguration
@EnableConfigurationProperties(ApplicationServiceProperties.class)
public class WebErrorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ErrorCodeResolver.class)
    ErrorCodeResolver webErrorCodeResolver(final ApplicationServiceProperties properties) {
        return new WebErrorCodeResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    WebErrorStatusResolver webErrorStatusResolver() {
        return new DefaultWebErrorStatusResolver();
    }
}
