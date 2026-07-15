package com.kjs.wuli3.event;

/**
 * Cross-service event contract. Implementations define immutable business payload fields in addition to this stable
 * envelope and must not derive {@link #eventName()} from a Java class name.
 */
public interface IntegrationEvent extends Event {
    /**
     * Stable protocol name independent of the implementing Java type.
     *
     * @return protocol event name
     */
    String eventName();

    /**
     * Positive version of the serialized event contract, unrelated to an aggregate version.
     *
     * @return schema version
     */
    int schemaVersion();

    /**
     * Stable identity of the service that owns and publishes this contract.
     *
     * @return producer service identity
     */
    String producerService();

    /**
     * Transport metadata; concrete event fields carry the business payload.
     *
     * @return immutable transport metadata
     */
    EventMetadata metadata();
}
