package com.kjs.wuli3.event.autoconfigure;

import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.RoutingEventPublisher;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import com.kjs.wuli3.event.transport.AsyncEventTransport;
import com.kjs.wuli3.event.transport.SpringLocalEventTransport;
import com.kjs.wuli3.event.transport.TransactionalEventTransport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.List;

/**
 * 自动配置本地和尽力而为的远程事件发布。
 */
@AutoConfiguration
public class EventAutoConfiguration {

    /** 创建本地 Spring 事件传输实现。 */
    @Bean
    @ConditionalOnMissingBean(SpringLocalEventTransport.class)
    SpringLocalEventTransport springLocalEventMessageTransport(
            final ApplicationEventPublisher applicationEventPublisher) {
        return new SpringLocalEventTransport(applicationEventPublisher);
    }

    @Bean("applicationTaskExecutor")
    TaskExecutor applicationTaskExecutor() {
        return new VirtualThreadTaskExecutor("event-publisher-executor");
    }

    /** 创建按具体选项类型路由的应用事件发布器。 */
    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    EventPublisher eventPublisher(
            final SpringLocalEventTransport springLocalEventMessageTransport,
            final List<RemoteEventTransport<?>> remoteEventTransports,
            @Qualifier("applicationTaskExecutor") final TaskExecutor executor) {
        final EventTransport<?> localTransport = new TransactionalEventTransport<>(
                new AsyncEventTransport<>(springLocalEventMessageTransport, executor));
        final RoutingEventPublisher publisher = new RoutingEventPublisher();
        EventAutoConfiguration.register(publisher, localTransport);
        remoteEventTransports.forEach(
                transport -> EventAutoConfiguration.register(publisher, new TransactionalEventTransport<>(transport)));
        return publisher;
    }

    private static <PO extends PublishOptions> void register(
            final RoutingEventPublisher publisher, final EventTransport<PO> transport) {
        publisher.register(transport);
    }
}
