package com.kjs.wuli3.web.autoconfigure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ConfigFeature;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategy;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategyRegistry;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationVisibilityPolicy;
import com.kjs.wuli3.json.datatype.resource.CompositeResourcePathResolver;
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
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Configures JSON extensions owned by the web starter.
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@AutoConfiguration
@EnableConfigurationProperties({WebJsonResourcePathProperties.class, JacksonProperties.class})
public class WebJsonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResourcePathResolver.class)
    ResourcePathResolver resourcePathResolver(final WebJsonResourcePathProperties properties) {
        return new WebResourcePathResolver(properties);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer webJackson2ObjectMapperBuilderCustomizer(
            final ObjectProvider<ResourcePathResolver> resourcePathResolvers,
            final ObjectProvider<DesensitizationStrategy> desensitizationStrategies,
            final ObjectProvider<DesensitizationVisibilityPolicy> visibilityPolicy,
            final JacksonProperties jacksonProperties) {
        final ResourcePathResolver resourcePathResolver = new CompositeResourcePathResolver(
                resourcePathResolvers.orderedStream().toList());
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
            if (jacksonProperties.getLocale() == null) {
                builder.locale(JacksonProvider.defaultLocale());
            }
            if (jacksonProperties.getTimeZone() == null) {
                builder.timeZone(JacksonProvider.defaultTimeZone());
            }
            for (final ConfigFeature feature : JacksonProvider.featuresToEnable()) {
                if (WebJsonAutoConfiguration.isExplicitlyConfigured(jacksonProperties, feature)) {
                    builder.featuresToEnable(feature);
                }
            }
            for (final ConfigFeature feature : JacksonProvider.featuresToDisable()) {
                if (WebJsonAutoConfiguration.isExplicitlyConfigured(jacksonProperties, feature)) {
                    builder.featuresToDisable(feature);
                }
            }
        };
    }

    private static boolean isExplicitlyConfigured(final JacksonProperties properties, final ConfigFeature feature) {
        return !switch (feature) {
            case DeserializationFeature deserializationFeature ->
                properties.getDeserialization().containsKey(deserializationFeature);
            case SerializationFeature serializationFeature ->
                properties.getSerialization().containsKey(serializationFeature);
            case MapperFeature mapperFeature -> properties.getMapper().containsKey(mapperFeature);
            default -> false;
        };
    }
}
