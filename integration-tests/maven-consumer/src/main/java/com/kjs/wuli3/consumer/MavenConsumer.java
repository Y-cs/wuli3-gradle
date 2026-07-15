package com.kjs.wuli3.consumer;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.event.EventMetadata;
import com.kjs.wuli3.json.provider.JacksonProvider;

public final class MavenConsumer {
    private MavenConsumer() {}

    public static String createIdentifier() {
        JacksonProvider.newJsonMapper();
        EventMetadata.empty();
        return UuidStringIdGenerator.INSTANCE.nextId();
    }
}
