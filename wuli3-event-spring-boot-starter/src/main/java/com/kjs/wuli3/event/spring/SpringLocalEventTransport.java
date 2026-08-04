package com.kjs.wuli3.event.spring;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 通过 Spring 应用事件机制立即发布本地事件信封。
 */
public final class SpringLocalEventTransport implements EventTransport {

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
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        SpringLocalEventTransport.validate(options);
        this.applicationEventPublisher.publishEvent(Objects.requireNonNull(envelope, "envelope cannot be null"));
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        SpringLocalEventTransport.validate(options);
        final List<EventEnvelope<?>> snapshot =
                List.copyOf(Objects.requireNonNull(envelopes, "envelopes cannot be null"));
        snapshot.forEach(this.applicationEventPublisher::publishEvent);
    }

    private static void validate(final PublishOptions options) {
        final PublishOptions requiredOptions = Objects.requireNonNull(options, "options cannot be null");
        if (!requiredOptions.isLocal()) {
            throw new IllegalArgumentException("Spring local transport requires the LOCAL channel");
        }
        if (requiredOptions.isAsync()
                || requiredOptions.getDelayTime() != null
                || requiredOptions.getOrderKey() != null) {
            throw new EventTransport.UnsupportedCapabilityException(
                    "LOCAL publication does not support async, delay, or ordering");
        }
    }
}
