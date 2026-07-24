package com.kjs.wuli3.event;

import java.util.Collection;

/** 发布本地 Spring 事件或远程集成事件的统一入口。 */
public interface EventPublisher {

    /**
     * 使用显式选项发布一个事件信封。
     *
     * @param envelope 待发布事件
     * @param options 请求的发布语义
     */
    void publish(final EventEnvelope<?> envelope, final PublishOptions options);

    /**
     * 使用默认本地选项发布一个事件信封。
     *
     * @param envelope 待发布事件
     */
    default void publish(final EventEnvelope<?> envelope) {
        this.publish(envelope, PublishOptions.defaults());
    }

    /**
     * 使用显式选项发布多个事件信封。
     *
     * @param envelopes 待发布事件集合
     * @param options 请求的发布语义
     */
    void publishes(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options);

    /**
     * 使用默认本地选项发布多个事件信封。
     *
     * @param envelopes 待发布事件集合
     */
    default void publishes(final Collection<EventEnvelope<?>> envelopes) {
        this.publishes(envelopes, PublishOptions.defaults());
    }
}
