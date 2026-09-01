package com.kjs.wuli3.web.autoconfigure;

import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategy;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategyRegistry;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationVisibilityPolicy;
import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;
import com.kjs.wuli3.json.provider.JacksonProvider;
import com.kjs.wuli3.json.provider.JsonMapperDesensitizationAssembly;
import com.kjs.wuli3.json.provider.JsonMapperResourcePathAssembly;
import com.kjs.wuli3.web.json.WebJsonResourcePathProperties;
import com.kjs.wuli3.web.json.WebResourcePathResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures JSON extensions owned by the web starter.
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@EnableConfigurationProperties(WebJsonResourcePathProperties.class)
public class WebJsonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResourcePathResolver.class)
    ResourcePathResolver resourcePathResolver(final WebJsonResourcePathProperties properties) {
        return new WebResourcePathResolver(properties);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer webJackson2ObjectMapperBuilderCustomizer(
            final ResourcePathResolver resourcePathResolver,
            final ObjectProvider<DesensitizationStrategy> desensitizationStrategies,
            final ObjectProvider<DesensitizationVisibilityPolicy> visibilityPolicy) {
        final JsonMapperResourcePathAssembly resourceAssembly =
                new JsonMapperResourcePathAssembly(resourcePathResolver);
        final JsonMapperDesensitizationAssembly desensitizationAssembly = new JsonMapperDesensitizationAssembly(
                DesensitizationStrategyRegistry.standardWithOverrides(
                        desensitizationStrategies.orderedStream().toList()),
                visibilityPolicy.getIfAvailable(DesensitizationVisibilityPolicy::alwaysMask));
        return builder -> {
            builder.postConfigurer(objectMapper -> objectMapper.registerModules(
                    JacksonProvider.javaTimeOverrideModule(),
                    resourceAssembly.resourcePathModule(),
                    desensitizationAssembly.desensitizationModule()));
            builder.locale(JacksonProvider.defaultLocale());
            builder.timeZone(JacksonProvider.defaultTimeZone());
            for (final ConfigFeature feature : JacksonProvider.featuresToEnable()) {
                builder.featuresToEnable(feature);
            }
            for (final ConfigFeature feature : JacksonProvider.featuresToDisabled()) {
                builder.featuresToDisable(feature);
            }
        };
    }
}
