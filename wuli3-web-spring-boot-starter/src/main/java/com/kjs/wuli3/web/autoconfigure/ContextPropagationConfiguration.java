package com.kjs.wuli3.web.autoconfigure;

import com.kjs.wuli3.propagation.accessor.AuthContextAccessor;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.snapshot.ContextPropagator;
import com.kjs.wuli3.propagation.snapshot.DefaultContextPropagator;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextStore;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.context.WebContextAccessor;
import com.kjs.wuli3.web.internal.client.InvocationContextClientHttpRequestInterceptor;
import com.kjs.wuli3.web.internal.context.DefaultAuthContextResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ContextPropagationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ContextStore contextStore() {
        return new ContextStore();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPropagator contextPropagator(final ContextWriter contextWriter) {
        return new DefaultContextPropagator(contextWriter);
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

    @Bean
    @ConditionalOnMissingBean
    WebContextAccessor webContextAccessor(final ContextReader contextReader) {
        return new WebContextAccessor(contextReader);
    }

    @Bean
    @ConditionalOnMissingBean
    AuthContextResolver securityContextResolver() {
        return new DefaultAuthContextResolver();
    }

    /** 为 Boot 管理的 RestClient 写入调用链上下文。 */
    @Bean
    RestClientCustomizer wuli3InvocationContextRestClientCustomizer(final ContextStore contextStore) {
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(contextStore);
        return builder -> builder.requestInterceptor(interceptor);
    }

    /** 为 Boot 管理的 RestTemplate 写入调用链上下文。 */
    @Bean
    RestTemplateCustomizer wuli3InvocationContextRestTemplateCustomizer(final ContextStore contextStore) {
        final InvocationContextClientHttpRequestInterceptor interceptor =
                new InvocationContextClientHttpRequestInterceptor(contextStore);
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }
}
