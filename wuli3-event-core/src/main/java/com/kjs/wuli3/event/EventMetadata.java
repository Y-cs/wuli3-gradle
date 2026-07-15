package com.kjs.wuli3.event;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable transport headers. Business payload belongs to the concrete integration event.
 *
 * @param headers transport headers, including correlation and causation data when required
 */
public record EventMetadata(Map<String, String> headers) {
    /**
     * Creates immutable event metadata.
     *
     * @param headers transport headers
     */
    public EventMetadata(final Map<String, String> headers) {
        this.headers = Map.copyOf(Objects.requireNonNull(headers, "headers"));
    }

    /**
     * Creates metadata without transport headers.
     *
     * @return empty metadata
     */
    public static EventMetadata empty() {
        return new EventMetadata(Map.of());
    }
}
