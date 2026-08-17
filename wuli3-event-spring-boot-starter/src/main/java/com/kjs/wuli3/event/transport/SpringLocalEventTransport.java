package com.kjs.wuli3.event.transport;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import com.kjs.wuli3.event.options.SpringLocalPublishOptions;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 通过 Spring 应用事件机制立即发布本地事件信封。
 *
 * <p>作为传输链路末端的叶子实现，不解释 {@link SpringLocalPublishOptions} 携带的异步、
 * 事务等能力语义——这些横切能力由 {@link AsyncEventTransport}、
 * {@link TransactionalEventTransport} 等装饰器在外层处理，本类只负责真正发出事件。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class SpringLocalEventTransport implements EventTransport<SpringLocalPublishOptions> {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 使用指定的 Spring 事件发布器创建本地传输实现。
     *
     * @param applicationEventPublisher Spring 事件发布器
     */
    public SpringLocalEventTransport(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher =
                Objects.requireNonNull(applicationEventPublisher, "applicationEventPublisher cannot be null");
    }

    @Override
    public Class<SpringLocalPublishOptions> supportedOptionsType() {
        return SpringLocalPublishOptions.class;
    }

    @Override
    public void send(final SpringLocalPublishOptions options, final EventEnvelope<?>... envelope) {
        Objects.requireNonNull(options, "options cannot be null");
        Arrays.stream(Objects.requireNonNull(envelope, "envelope cannot be null"))
                .forEach(this.applicationEventPublisher::publishEvent);
    }
}
