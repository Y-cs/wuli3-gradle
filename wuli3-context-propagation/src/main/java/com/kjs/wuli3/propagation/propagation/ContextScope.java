package com.kjs.wuli3.propagation.propagation;

/**
 * Restores the previous context when a propagated context scope ends.
 */
@FunctionalInterface
public interface ContextScope extends AutoCloseable {

    @Override
    void close();
}
