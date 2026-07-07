package com.kjs.wuli3.propagation.carrier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple in-memory carrier useful for tests, message metadata, and adapter glue code.
 */
public final class MapContextCarrier implements ContextCarrierReader, ContextCarrierWriter {

    private final Map<String, String> values;

    public MapContextCarrier() {
        this(new LinkedHashMap<>());
    }

    public MapContextCarrier(final Map<String, String> values) {
        this.values = Objects.requireNonNull(values, "values");
    }

    @Override
    public Optional<String> get(final String name) {
        return Optional.ofNullable(this.values.get(name));
    }

    @Override
    public void set(final String name, final String value) {
        this.values.put(name, value);
    }

    public Map<String, String> asMap() {
        return Map.copyOf(this.values);
    }
}
