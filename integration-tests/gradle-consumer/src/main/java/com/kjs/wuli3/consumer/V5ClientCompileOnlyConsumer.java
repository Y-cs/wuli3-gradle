package com.kjs.wuli3.consumer;

import org.apache.rocketmq.client.apis.ClientServiceProvider;

/** 仅用于验证 BOM 可在不引入运行时依赖的前提下解析 v5 Java Client。 */
public final class V5ClientCompileOnlyConsumer {

    private V5ClientCompileOnlyConsumer() {}

    public static Class<ClientServiceProvider> clientServiceProviderType() {
        return ClientServiceProvider.class;
    }
}
