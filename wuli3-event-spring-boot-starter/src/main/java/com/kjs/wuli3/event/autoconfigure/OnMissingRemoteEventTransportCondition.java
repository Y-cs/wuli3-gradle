package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.remote.RemoteEventTransport;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Matches when no registered remote event transport declares the requested options type.
 *
 * <p>Raw or otherwise unresolved transport types are treated as matches for the requested type. This
 * conservative behavior preserves the usual auto-configuration back-off semantics when a transport's
 * capabilities cannot be determined without creating its bean.
 */
final class OnMissingRemoteEventTransportCondition extends SpringBootCondition implements ConfigurationCondition {

    private static final String ANNOTATION_NAME = ConditionalOnMissingRemoteEventTransport.class.getName();

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final Map<String, Object> attributes = metadata.getAnnotationAttributes(ANNOTATION_NAME);
        if (attributes == null) {
            return ConditionOutcome.match();
        }
        final Class<?> optionsType = Objects.requireNonNull((Class<?>) attributes.get("optionsType"), "optionsType");
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) {
            return ConditionOutcome.match("No bean factory is available to inspect remote transports");
        }
        for (final String beanName : beanFactory.getBeanNamesForType(RemoteEventTransport.class, true, false)) {
            final ResolvableType transportType =
                    OnMissingRemoteEventTransportCondition.transportType(beanFactory, beanName);
            if (transportType.equals(ResolvableType.NONE)) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ANNOTATION_NAME)
                        .because("a remote event transport has an unresolved options type"));
            }
            final Class<?> supportedOptionsType = transportType.resolveGeneric(0);
            if (supportedOptionsType == null || supportedOptionsType.equals(optionsType)) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ANNOTATION_NAME)
                        .because("a remote event transport supports " + optionsType.getName()));
            }
        }
        return ConditionOutcome.match(ConditionMessage.forCondition(ANNOTATION_NAME)
                .because("no remote event transport supports " + optionsType.getName()));
    }

    private static ResolvableType transportType(
            final ConfigurableListableBeanFactory beanFactory, final String beanName) {
        final ResolvableType declaredTransportType = OnMissingRemoteEventTransportCondition.transportType(
                beanFactory.getBeanDefinition(beanName).getResolvableType());
        if (!declaredTransportType.equals(ResolvableType.NONE)) {
            return declaredTransportType;
        }
        return OnMissingRemoteEventTransportCondition.transportType(beanFactory.getType(beanName, false));
    }

    private static ResolvableType transportType(final ResolvableType beanType) {
        final ResolvableType transportType = beanType.as(RemoteEventTransport.class);
        if (transportType.equals(ResolvableType.NONE)
                || transportType.hasUnresolvableGenerics()
                || transportType.resolveGeneric(0) == null) {
            return ResolvableType.NONE;
        }
        return transportType;
    }

    private static ResolvableType transportType(final @Nullable Class<?> beanType) {
        if (beanType == null) {
            return ResolvableType.NONE;
        }
        return OnMissingRemoteEventTransportCondition.transportType(ResolvableType.forClass(beanType));
    }
}
