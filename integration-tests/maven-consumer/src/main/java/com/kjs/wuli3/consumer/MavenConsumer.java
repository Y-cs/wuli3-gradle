package com.kjs.wuli3.consumer;

import com.kjs.wuli3.core.id.UuidStringIdGenerator;
import com.kjs.wuli3.event.Event;
import com.kjs.wuli3.json.provider.JacksonProvider;

public final class MavenConsumer {
    private MavenConsumer() {}

    public static String createIdentifier() {
        JacksonProvider.newJsonMapper();
        Event.of("consumer.test", "payload");
        return UuidStringIdGenerator.INSTANCE.nextId();
    }
}
