package com.kjs.wuli3.rocket.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.remote.RoutingEventTransport;
import com.kjs.wuli3.rocket.internal.RocketPublishOptions;
import com.kjs.wuli3.rocket.internal.RocketRemoteEventTransport;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 验证只实现 RoutingEventTransport（不实现 RemoteEventTransport）的业务方自定义传输，
 * 能否正确阻止默认远程传输被创建（修复前会重复注册炸容器）。
 */
class OnMissingConditionConflictTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration.class, RocketAutoConfiguration.class));

    @Test
    void customRoutingTransportBacksOffTheDefaultRemoteTransport() {
        this.contextRunner
                .withBean(RocketMQTemplate.class, () -> Mockito.mock(RocketMQTemplate.class))
                .withUserConfiguration(CustomRoutingTransportConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(EventPublisher.class);
                    assertThat(context).doesNotHaveBean(RocketRemoteEventTransport.class);
                    // 应该只有自定义的transport,默认的被condition挡住了
                    assertThat(context.getBeansOfType(RoutingEventTransport.class))
                            .hasSize(1);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomRoutingTransportConfig {

        @Bean
        RoutingEventTransport<RocketPublishOptions> customRocketTransport() {
            return new RoutingEventTransport<RocketPublishOptions>() {
                @Override
                public Class<RocketPublishOptions> supportedOptionsType() {
                    return RocketPublishOptions.class;
                }

                @Override
                public void send(final RocketPublishOptions options, final EventEnvelope<?>... envelopes) {
                    // no-op test stub
                }
            };
        }
    }
}
