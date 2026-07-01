package com.kjs.wuli3.web.config;

import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.holder.ContextHolder;
import com.kjs.wuli3.propagation.holder.ContextReader;
import com.kjs.wuli3.web.accessor.WebContextAccessor;
import com.kjs.wuli3.web.auth.DefaultAuthContextResolver;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextHolder contextHolder() {
        return new ContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    public InvocationContextAccessor invocationContextAccessor(ContextReader contextReader) {
        return new InvocationContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthContextAccessor authContextAccessor(ContextReader contextReader) {
        return new AuthContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebContextAccessor webContextAccessor(ContextReader contextReader) {
        return new WebContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthContextResolver securityContextResolver() {
        return new DefaultAuthContextResolver();
    }
}
