package com.kjs.wuli3.web;

import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.propagation.holder.ContextWriter;
import com.kjs.wuli3.web.config.ContextConfiguration;
import com.kjs.wuli3.web.config.properties.ApplicationServiceProperties;
import com.kjs.wuli3.web.error.GlobalExceptionHandler;
import com.kjs.wuli3.web.error.WebErrorCodeResolver;
import com.kjs.wuli3.web.factory.ResponseFactory;
import com.kjs.wuli3.web.filter.ContextFilter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ContextConfiguration.class)
@ConfigurationPropertiesScan("com.kjs.wuli3.web.config.properties")
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ErrorCodeResolver.class)
    public WebErrorCodeResolver webErrorCodeResolver(ApplicationServiceProperties applicationServiceProperties) {
        return new WebErrorCodeResolver(applicationServiceProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextFilter contextFilter(ContextWriter contextWriter, AuthContextResolver authContextResolver) {
        return new ContextFilter(contextWriter, authContextResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseFactory responseFactory(InvocationContextAccessor invocationContextAccessor,
            ErrorCodeResolver errorCodeResolver) {
        return new ResponseFactory(invocationContextAccessor, errorCodeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ErrorCodeResolver errorCodeResolver,
            InvocationContextAccessor invocationContextAccessor) {
        return new GlobalExceptionHandler(errorCodeResolver, invocationContextAccessor);
    }
}
