package com.kjs.wuli3.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjs.wuli3.core.error.code.ErrorCodeResolver;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.internal.response.ApiResponseBodyAdvice;
import com.kjs.wuli3.web.internal.response.ApiResponseFactory;
import com.kjs.wuli3.web.internal.response.WebExceptionHandler;
import com.kjs.wuli3.web.response.WebResponseProperties;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures response wrapping and exception mapping. */
@AutoConfiguration(after = {WebContextAutoConfiguration.class, WebErrorAutoConfiguration.class})
@EnableConfigurationProperties(WebResponseProperties.class)
public class WebResponseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ApiResponseFactory apiResponseFactory(
            final InvocationContextAccessor accessor,
            final ErrorCodeResolver errorCodeResolver,
            final WebResponseProperties properties) {
        return new ApiResponseFactory(accessor, errorCodeResolver, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "wuli3.web.response",
            name = "wrapper-enabled",
            havingValue = "true",
            matchIfMissing = true)
    ApiResponseBodyAdvice apiResponseBodyAdvice(
            final ApiResponseFactory factory, final WebResponseProperties properties, final ObjectMapper objectMapper) {
        return new ApiResponseBodyAdvice(factory, properties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "wuli3.web.response",
            name = "exception-handler-enabled",
            havingValue = "true",
            matchIfMissing = true)
    WebExceptionHandler webExceptionHandler(
            final ApiResponseFactory factory,
            final InvocationContextAccessor accessor,
            final WebResponseProperties properties,
            final ObjectProvider<ErrorAlertNotifier> notifierProvider,
            final WebErrorStatusResolver statusResolver) {
        final List<ErrorAlertNotifier> notifiers =
                notifierProvider.orderedStream().toList();
        return new WebExceptionHandler(factory, accessor, properties, notifiers, statusResolver);
    }
}
