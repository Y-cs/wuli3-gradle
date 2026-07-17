package com.kjs.wuli3.web.autoconfigure;

import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.context.WebContextProperties;
import com.kjs.wuli3.web.internal.context.DefaultClientIpResolver;
import com.kjs.wuli3.web.internal.context.DefaultRequestIdResolver;
import com.kjs.wuli3.web.internal.servlet.ContextFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** Configures request context capture and propagation. */
@AutoConfiguration
@Import(ContextPropagationConfiguration.class)
@EnableConfigurationProperties(WebContextProperties.class)
public class WebContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RequestIdResolver requestIdResolver(final WebContextProperties properties) {
        return new DefaultRequestIdResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    ClientIpResolver clientIpResolver(final WebContextProperties properties) {
        return new DefaultClientIpResolver(properties);
    }

    @Bean
    @ConditionalOnBean(ContextWriter.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wuli3.web.context", name = "enabled", havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<ContextFilter> contextFilterRegistration(
            final ContextWriter contextWriter,
            final AuthContextResolver authContextResolver,
            final RequestIdResolver requestIdResolver,
            final ClientIpResolver clientIpResolver,
            @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver handlerExceptionResolver,
            final WebContextProperties properties) {
        final ContextFilter filter = new ContextFilter(
                contextWriter,
                authContextResolver,
                requestIdResolver,
                clientIpResolver,
                handlerExceptionResolver,
                properties);
        final FilterRegistrationBean<ContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}
