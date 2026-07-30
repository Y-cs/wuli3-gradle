package com.kjs.wuli3.rocketmq.autoconfigure;

import com.kjs.wuli3.event.autoconfigure.EventAutoConfiguration;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import com.kjs.wuli3.propagation.codec.DefaultPropagationContextCodecs;
import com.kjs.wuli3.propagation.codec.PropagationContextCodec;
import com.kjs.wuli3.propagation.context.PropagationContext;
import com.kjs.wuli3.propagation.store.ContextReader;
import com.kjs.wuli3.propagation.store.ContextWriter;
import com.kjs.wuli3.propagation.transmission.ContextTransmitter;
import com.kjs.wuli3.rocketmq.internal.RocketMqEventMessageEncoder;
import com.kjs.wuli3.rocketmq.internal.RocketMqRemoteEventMessageTransport;
import java.util.List;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** 自动配置默认的 RocketMQ 远程事件传输实现。 */
@AutoConfiguration
@AutoConfigureAfter(name = "org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
@AutoConfigureBefore(EventAutoConfiguration.class)
@ConditionalOnBean(RocketMQTemplate.class)
@EnableConfigurationProperties(RocketMqEventProperties.class)
public class RocketMqAutoConfiguration {

    /** 创建 RocketMQ 事件传输自动配置。 */
    public RocketMqAutoConfiguration() {}

    /**
     * 创建由不同 RocketMQ 客户端实现共享的事件编码器。
     *
     * @param contextReaders 可选的当前上下文读取器
     * @param contextWriters 可选的当前上下文写入器
     * @param properties 事件传输配置
     * @return 公共事件编码器
     */
    @Bean
    @ConditionalOnMissingBean
    RocketMqEventMessageEncoder rocketMqEventMessageEncoder(
            final ObjectProvider<ContextReader> contextReaders,
            final ObjectProvider<ContextWriter> contextWriters,
            final RocketMqEventProperties properties) {
        final @Nullable ContextReader contextReader = contextReaders.getIfUnique();
        final @Nullable ContextWriter contextWriter = contextWriters.getIfUnique();
        return new RocketMqEventMessageEncoder(RocketMqAutoConfiguration.contextTransmitter(
                contextReader, contextWriter, properties.getContextMode()));
    }

    /**
     * 创建当前生效的 RocketMQ Template 传输实现。
     *
     * @param rocketMQTemplate Apache RocketMQ 模板
     * @param encoder 公共事件编码器
     * @return 远程传输实现
     */
    @Bean
    @ConditionalOnMissingBean(RemoteEventMessageTransport.class)
    RocketMqRemoteEventMessageTransport rocketMqRemoteEventMessageTransport(
            final RocketMQTemplate rocketMQTemplate, final RocketMqEventMessageEncoder encoder) {
        return new RocketMqRemoteEventMessageTransport(rocketMQTemplate, encoder);
    }

    private static @Nullable ContextTransmitter contextTransmitter(
            final @Nullable ContextReader contextReader,
            final @Nullable ContextWriter contextWriter,
            final RocketMqContextMode contextMode) {
        if (contextReader == null || contextWriter == null) {
            return null;
        }
        final List<PropagationContextCodec<? extends PropagationContext>> codecs =
                contextMode == RocketMqContextMode.TRUSTED_INTERNAL
                        ? DefaultPropagationContextCodecs.trustedInternal()
                        : DefaultPropagationContextCodecs.invocationOnly();
        return new ContextTransmitter(contextReader, contextWriter, codecs);
    }
}
