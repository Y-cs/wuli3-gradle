package com.kjs.wuli3.propagation.carrier;

/**
 * Protocol-neutral writer for propagated key-value metadata.
 */
@FunctionalInterface
public interface ContextCarrierWriter {

    void set(String name, String value);
}
