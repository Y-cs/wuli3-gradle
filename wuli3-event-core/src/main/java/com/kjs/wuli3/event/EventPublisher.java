package com.kjs.wuli3.event;

import java.util.concurrent.CompletionStage;

public interface EventPublisher {
    CompletionStage<Void> publish(EventEnvelope event);
}
