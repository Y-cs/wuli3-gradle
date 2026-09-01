package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.remote.RoutingEventTransport;
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
 * 当已注册的传输均未声明请求的选项类型时匹配。
 *
 * <p>扫描 {@link RoutingEventTransport} 而非更窄的 {@code RemoteEventTransport}：{@code EventAutoConfiguration}
 * 按 {@code RoutingEventTransport} 收集 Bean 注册进路由器，条件判断的扫描范围必须与之一致，否则只实现
 * {@code RoutingEventTransport}（不继承 {@code RemoteEventTransport}）的业务方自定义传输会被本条件忽略，
 * 导致默认远程传输被错误创建，并在收集阶段与业务方的传输发生 optionsType 重复注册。
 *
 * <p>原始类型或无法解析的传输类型会被视为匹配请求类型。这样可以在不创建 Bean 的情况下无法确定传输能力时，保持自动配置通常的回退语义。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
final class OnMissingRoutingEventTransportCondition extends SpringBootCondition implements ConfigurationCondition {

    private static final String ANNOTATION_NAME = ConditionalOnMissingRoutingEventTransport.class.getName();

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
        for (final String beanName : beanFactory.getBeanNamesForType(RoutingEventTransport.class, true, false)) {
            final ResolvableType transportType =
                    OnMissingRoutingEventTransportCondition.transportType(beanFactory, beanName);
            if (transportType.equals(ResolvableType.NONE)) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ANNOTATION_NAME)
                        .because("a routing event transport has an unresolved options type"));
            }
            final Class<?> supportedOptionsType = transportType.resolveGeneric(0);
            if (supportedOptionsType == null || supportedOptionsType.equals(optionsType)) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(ANNOTATION_NAME)
                        .because("a routing event transport supports " + optionsType.getName()));
            }
        }
        return ConditionOutcome.match(ConditionMessage.forCondition(ANNOTATION_NAME)
                .because("no routing event transport supports " + optionsType.getName()));
    }

    private static ResolvableType transportType(
            final ConfigurableListableBeanFactory beanFactory, final String beanName) {
        final ResolvableType declaredTransportType = OnMissingRoutingEventTransportCondition.transportType(
                beanFactory.getBeanDefinition(beanName).getResolvableType());
        if (!declaredTransportType.equals(ResolvableType.NONE)) {
            return declaredTransportType;
        }
        return OnMissingRoutingEventTransportCondition.transportType(beanFactory.getType(beanName, false));
    }

    private static ResolvableType transportType(final ResolvableType beanType) {
        final ResolvableType transportType = beanType.as(RoutingEventTransport.class);
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
        return OnMissingRoutingEventTransportCondition.transportType(ResolvableType.forClass(beanType));
    }
}
