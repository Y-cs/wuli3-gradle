package com.kjs.wuli3.event;

import com.kjs.wuli3.event.envelope.EventEnvelope;

/** 将固定选项类型的发布请求委托给单个传输实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public abstract class AbstractChannelEventPublisher<PO extends PublishOptions> implements EventPublisher {

    private final EventTransport<PO> eventTransport;

    protected AbstractChannelEventPublisher(final EventTransport<PO> eventTransport) {
        this.eventTransport = java.util.Objects.requireNonNull(eventTransport, "eventTransport cannot be null");
    }

    @Override
    public final <O extends PublishOptions> void publish(final O options, final EventEnvelope<?>... envelopes) {
        if (!this.eventTransport.supportedOptionsType().isInstance(options)) {
            throw new com.kjs.wuli3.event.error.UnsupportedCapabilityException("Expected options of type "
                    + this.eventTransport.supportedOptionsType().getName());
        }
        this.publishSupported(this.eventTransport.supportedOptionsType().cast(options), envelopes);
    }

    /** 使用该发布器支持的选项类型发送事件。 */
    public final void publishSupported(final PO options, final EventEnvelope<?>... envelopes) {
        this.eventTransport.send(options, envelopes);
    }
}
