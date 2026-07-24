package com.kjs.wuli3.event.internal;

import com.kjs.wuli3.event.EventEnvelope;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.remote.RemoteEventMessageTransport;
import java.util.Collection;

/**
 * 未配置远程事件传输实现时使用的占位实现。
 */
public final class DefaultRemoteEventMessageTransport implements RemoteEventMessageTransport {
    private static final String MESSAGE = "No RemoteEventMessageTransport is configured";

    /** 创建表示未配置远程传输实现的占位对象。 */
    public DefaultRemoteEventMessageTransport() {}

    @Override
    public void send(final EventEnvelope<?> envelope, final PublishOptions options) {
        throw new IllegalStateException(DefaultRemoteEventMessageTransport.MESSAGE);
    }

    @Override
    public void sends(final Collection<EventEnvelope<?>> envelopes, final PublishOptions options) {
        throw new IllegalStateException(DefaultRemoteEventMessageTransport.MESSAGE);
    }
}
