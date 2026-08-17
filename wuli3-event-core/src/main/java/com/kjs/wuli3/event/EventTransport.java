package com.kjs.wuli3.event;

import com.kjs.wuli3.event.envelope.EventEnvelope;

/** 通过具体的投递机制发送事件信封。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface EventTransport<PO extends PublishOptions> {

    /**
     * 获取该传输实现所支持的传输选项类型。
     *
     * @return 传输选项类型
     */
    Class<PO> supportedOptionsType();

    /**
     * 按请求的传输能力发送一个或多个事件。
     *
     * @param options 请求的通道和投递能力
     * @param envelopes 待发送事件
     */
    void send(final PO options, final EventEnvelope<?>... envelopes);
}
