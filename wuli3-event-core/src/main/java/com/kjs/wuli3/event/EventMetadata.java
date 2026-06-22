package com.kjs.wuli3.event;

import java.util.Map;

public record EventMetadata(Map<String, String> values) {
    public EventMetadata {
        values = Map.copyOf(values);
    }

    public static EventMetadata empty() {
        return new EventMetadata(Map.of());
    }
}
