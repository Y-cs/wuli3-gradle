package com.kjs.wuli3.event;

import java.util.concurrent.CompletionStage;

public interface EventPublisher {
    /** Publishes an event and reports asynchronous handler completion through the returned stage. */
    CompletionStage<Void> publish(EventEnvelope event);
}
