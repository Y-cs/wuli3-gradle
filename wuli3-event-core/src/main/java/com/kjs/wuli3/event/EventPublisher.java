package com.kjs.wuli3.event;

import com.kjs.wuli3.event.envelope.EventEnvelope;

/** 根据发布选项类型将事件信封路由到对应传输实现。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface EventPublisher {

    /**
     * 使用指定发布选项发送一个或多个事件信封。
     *
     * @param options 发布选项
     * @param envelopes 待发布事件信封
     * @param <PO> 发布选项类型
     */
    <PO extends PublishOptions> void publish(final PO options, final EventEnvelope<?>... envelopes);
}
