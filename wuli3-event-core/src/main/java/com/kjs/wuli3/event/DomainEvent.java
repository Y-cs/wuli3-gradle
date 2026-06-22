package com.kjs.wuli3.event;

public interface DomainEvent extends EventEnvelope {
    String aggregateId();

    String aggregateType();
}
