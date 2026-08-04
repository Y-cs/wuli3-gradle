package com.kjs.wuli3.event.internal;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventTransport;
import java.util.Collection;

/**
 * 未配置远程事件传输实现时使用的占位实现。
 */
public final class DefaultRemoteEventTransport implements RemoteEventTransport {
    private static final String MESSAGE = "No RemoteEventMessageTransport is configured";

    /** 创建表示未配置远程传输实现的占位对象。 */
    public DefaultRemoteEventTransport() {}

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        throw new IllegalStateException(DefaultRemoteEventTransport.MESSAGE);
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        throw new IllegalStateException(DefaultRemoteEventTransport.MESSAGE);
    }
}
