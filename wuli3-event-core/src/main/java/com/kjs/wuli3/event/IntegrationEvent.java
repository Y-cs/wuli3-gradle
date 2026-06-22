package com.kjs.wuli3.event;

public interface IntegrationEvent extends EventEnvelope {
    String sourceService();
}
