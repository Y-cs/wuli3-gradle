package com.kjs.wuli3.audit.internal;

import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.event.EventPublisher;
import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.envelope.EventEnvelope;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 记录发布调用的测试用事件发布器。 */
final class RecordingEventPublisher implements EventPublisher {

    private final List<EventEnvelope<AuditLogPayload>> envelopes = new ArrayList<>();
    private final List<PublishOptions> options = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <PO extends PublishOptions> void publish(final PO publishOptions, final EventEnvelope<?>... events) {
        this.options.add(publishOptions);
        Arrays.stream(events)
                .map(event -> (EventEnvelope<AuditLogPayload>) event)
                .forEach(this.envelopes::add);
    }

    List<EventEnvelope<AuditLogPayload>> envelopes() {
        return List.copyOf(this.envelopes);
    }

    List<PublishOptions> options() {
        return List.copyOf(this.options);
    }
}
