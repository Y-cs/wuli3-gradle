package com.kjs.wuli3.web.autoconfigure;

import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.context.WebContextProperties;
import com.kjs.wuli3.web.internal.client.InvocationContextClientHttpRequestInterceptor;
import com.kjs.wuli3.web.internal.context.DefaultClientIpResolver;
import com.kjs.wuli3.web.internal.context.DefaultRequestIdResolver;
import com.kjs.wuli3.web.internal.servlet.ContextFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/** Configures request context capture and propagation. */
@AutoConfiguration
@EnableConfigurationProperties(WebContextProperties.class)
public class WebContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ContextStore contextStore() {
        return new ContextStore();
    }

    @Bean
    @ConditionalOnMissingBean
    InvocationContextAccessor invocationContextAccessor(final ContextReader contextReader) {
        return new InvocationContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    AuthContextAccessor authContextAccessor(final ContextReader contextReader) {
        return new AuthContextAccessor(contextReader);
    }

    /** Writes only invocation metadata to Boot-managed HTTP clients. */
    @Bean
    RestClientCustomizer wuli3InvocationContextRestClientCustomizer(final ContextReader contextReader) {
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(contextReader);
        return builder -> builder.requestInterceptor(interceptor);
    }

    /** Writes only invocation metadata to Boot-managed HTTP clients. */
    @Bean
    RestTemplateCustomizer wuli3InvocationContextRestTemplateCustomizer(final ContextReader contextReader) {
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(contextReader);
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }

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
            final ObjectProvider<AuthContextResolver> authContextResolvers,
            final RequestIdResolver requestIdResolver,
            final ClientIpResolver clientIpResolver,
            final WebContextProperties properties) {
        final ContextFilter filter = new ContextFilter(
                contextWriter, authContextResolvers.getIfAvailable(), requestIdResolver, clientIpResolver, properties);
        final FilterRegistrationBean<ContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}
