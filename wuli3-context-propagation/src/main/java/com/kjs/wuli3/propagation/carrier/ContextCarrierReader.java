package com.kjs.wuli3.propagation.carrier;

import java.util.Optional;

/**
 * Protocol-neutral reader for propagated key-value metadata.
 */
@FunctionalInterface
public interface ContextCarrierReader {

    Optional<String> get(String name);
}
