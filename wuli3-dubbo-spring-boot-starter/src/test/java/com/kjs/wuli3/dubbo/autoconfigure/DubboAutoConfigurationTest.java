package com.kjs.wuli3.dubbo.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.propagation.ContextProxy;
import com.kjs.wuli3.propagation.codec.ContextPropagator;
import com.kjs.wuli3.propagation.store.ContextStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * 验证 Dubbo 自动配置的默认组件和配置属性绑定。
 *
 * @author GuoYang create on 2026/8/28 17:59
 */
class DubboAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(DubboAutoConfiguration.class));

    /** 验证默认上下文组件可用且各传播开关能够从外部配置绑定。 */
    @Test
    void registersDefaultContextInfrastructureAndBindsSwitches() {
        this.contextRunner
                .withPropertyValues("wuli3.dubbo.context.enabled=false", "wuli3.dubbo.error.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(ContextStore.class);
                    assertThat(context).hasSingleBean(ContextProxy.class);
                    assertThat(context).hasSingleBean(ContextPropagator.class);
                    final DubboProperties properties = context.getBean(DubboProperties.class);
                    assertThat(properties.getContext().isEnabled()).isFalse();
                    assertThat(properties.getError().isEnabled()).isFalse();
                });
    }
}
