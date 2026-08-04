package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.internal.DefaultRemoteEventTransport;
import com.kjs.wuli3.event.internal.RoutingEventPublisher;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.event.spring.SpringLocalEventTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * 自动配置本地和尽力而为的远程事件发布。
 */
@AutoConfiguration
public class EventAutoConfiguration {

    /** 创建事件自动配置。 */
    public EventAutoConfiguration() {}

    /** 创建本地 Spring 事件传输实现。 */
    @Bean
    @ConditionalOnMissingBean(SpringLocalEventTransport.class)
    SpringLocalEventTransport springLocalEventMessageTransport(
            final ApplicationEventPublisher applicationEventPublisher) {
        return new SpringLocalEventTransport(applicationEventPublisher);
    }

    /** 未配置远程传输实现时的占位 bean。 */
    @Bean
    @ConditionalOnMissingBean(RemoteEventTransport.class)
    DefaultRemoteEventTransport defaultRemoteEventMessageTransport() {
        return new DefaultRemoteEventTransport();
    }

    /** 创建按通道路由的应用事件发布器。 */
    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    EventPublisher eventPublisher(
            final SpringLocalEventTransport springLocalEventMessageTransport,
            final RemoteEventTransport remoteEventMessageTransport) {
        return new RoutingEventPublisher(springLocalEventMessageTransport, remoteEventMessageTransport);
    }
}
