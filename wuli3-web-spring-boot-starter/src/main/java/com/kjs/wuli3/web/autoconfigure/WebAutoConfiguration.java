package com.kjs.wuli3.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.kjs.wuli3.core.error.ErrorCodeResolver;
import com.kjs.wuli3.json.provider.JacksonProvider;
import com.kjs.wuli3.json.provider.JsonMapperResourcePathAssembly;
import com.kjs.wuli3.propagation.accessor.InvocationContextAccessor;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.web.auth.AuthContextResolver;
import com.kjs.wuli3.web.config.properties.ApplicationServiceProperties;
import com.kjs.wuli3.web.config.properties.WebContextProperties;
import com.kjs.wuli3.web.config.properties.WebJsonResourcePathProperties;
import com.kjs.wuli3.web.config.properties.WebResponseProperties;
import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.RequestIdResolver;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import com.kjs.wuli3.web.error.WebErrorStatusResolver;
import com.kjs.wuli3.web.internal.autoconfigure.ContextConfiguration;
import com.kjs.wuli3.web.internal.context.DefaultClientIpResolver;
import com.kjs.wuli3.web.internal.context.DefaultRequestIdResolver;
import com.kjs.wuli3.web.internal.error.DefaultWebErrorStatusResolver;
import com.kjs.wuli3.web.internal.error.WebErrorCodeResolver;
import com.kjs.wuli3.web.internal.response.ApiResponseBodyAdvice;
import com.kjs.wuli3.web.internal.response.ApiResponseFactory;
import com.kjs.wuli3.web.internal.response.WebExceptionHandler;
import com.kjs.wuli3.web.internal.servlet.ContextFilter;
import com.kjs.wuli3.web.json.WebResourcePathResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

/**
 * Wires the web starter defaults while keeping every bean replaceable by user applications.
 */
@AutoConfiguration
@Import(ContextConfiguration.class)
@ConfigurationPropertiesScan("com.kjs.wuli3.web.config.properties")
public class WebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebResourcePathResolver.class)
    WebResourcePathResolver resourcePathResolver(final WebJsonResourcePathProperties properties) {
        return new WebResourcePathResolver(properties);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer webJackson2ObjectMapperBuilderCustomizer(
            final WebResourcePathResolver resourcePathResolvers) {
        final JsonMapperResourcePathAssembly jsonMapperResourcePathAssembly = new JsonMapperResourcePathAssembly(
                resourcePathResolvers);
        return builder -> {
            builder.modules(JacksonProvider.javaTimeModule(), jsonMapperResourcePathAssembly.resourcePathModule());
            builder.locale(JacksonProvider.defaultLocale());
            builder.timeZone(JacksonProvider.defaultTimeZone());
            for (ConfigFeature configFeature : JacksonProvider.featuresToEnable()) {
                builder.featuresToEnable(configFeature);
            }
            for (ConfigFeature configFeature : JacksonProvider.featuresToDisabled()) {
                builder.featuresToDisable(configFeature);
            }
        };
    }

    @Bean
    WebErrorCodeResolver webErrorCodeResolver(final ApplicationServiceProperties applicationServiceProperties) {
        return new WebErrorCodeResolver(applicationServiceProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    RequestIdResolver requestIdResolver(final WebContextProperties contextProperties) {
        return new DefaultRequestIdResolver(contextProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    ClientIpResolver clientIpResolver(final WebContextProperties contextProperties) {
        return new DefaultClientIpResolver(contextProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    WebErrorStatusResolver webErrorStatusResolver() {
        return new DefaultWebErrorStatusResolver();
    }

    @Bean
    @ConditionalOnBean(ContextWriter.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wuli3.web.context", name = "enabled", havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<ContextFilter> contextFilterRegistration(final ContextWriter contextWriter,
            final AuthContextResolver authContextResolver, final RequestIdResolver requestIdResolver,
            final ClientIpResolver clientIpResolver,
            @Qualifier("handlerExceptionResolver") final HandlerExceptionResolver handlerExceptionResolver,
            final WebContextProperties contextProperties) {
        final ContextFilter contextFilter = new ContextFilter(contextWriter, authContextResolver, requestIdResolver,
                clientIpResolver, handlerExceptionResolver, contextProperties);
        final FilterRegistrationBean<ContextFilter> registration = new FilterRegistrationBean<>(contextFilter);
        registration.setOrder(contextProperties.getFilterOrder());
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    ApiResponseFactory apiResponseFactory(final InvocationContextAccessor invocationContextAccessor,
            final ErrorCodeResolver errorCodeResolver, final WebResponseProperties responseProperties) {
        return new ApiResponseFactory(invocationContextAccessor, errorCodeResolver, responseProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wuli3.web.response", name = "wrapper-enabled", havingValue = "true",
            matchIfMissing = true)
    ApiResponseBodyAdvice apiResponseBodyAdvice(final ApiResponseFactory apiResponseFactory,
            final WebResponseProperties responseProperties, final ObjectMapper objectMapper) {
        return new ApiResponseBodyAdvice(apiResponseFactory, responseProperties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wuli3.web.response", name = "exception-handler-enabled", havingValue = "true",
            matchIfMissing = true)
    WebExceptionHandler webExceptionHandler(final ApiResponseFactory apiResponseFactory,
            final InvocationContextAccessor invocationContextAccessor, final WebResponseProperties responseProperties,
            final ObjectProvider<ErrorAlertNotifier> errorAlertNotifiers,
            final WebErrorStatusResolver webErrorStatusResolver) {
        final List<ErrorAlertNotifier> notifiers = errorAlertNotifiers.orderedStream()
                .toList();
        return new WebExceptionHandler(apiResponseFactory, invocationContextAccessor, responseProperties, notifiers,
                webErrorStatusResolver);
    }
}
