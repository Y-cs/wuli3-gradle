package com.kjs.wuli3.core.id;

import java.util.UUID;

/**
 * Generates standard UUID string identifiers.
 */
public final class UuidStringIdGenerator implements IdGenerator<String> {

    public static final UuidStringIdGenerator INSTANCE = new UuidStringIdGenerator();

    private UuidStringIdGenerator() {}

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
