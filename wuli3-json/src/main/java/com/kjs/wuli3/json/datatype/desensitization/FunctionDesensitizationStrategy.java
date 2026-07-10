package com.kjs.wuli3.json.datatype.desensitization;

import java.util.Objects;
import java.util.function.Function;

/**
 * Function-backed strategy used by built-in and lightweight custom desensitization rules.
 */
final class FunctionDesensitizationStrategy implements DesensitizationStrategy {
    private final Function<String, String> function;
    private final String type;

    FunctionDesensitizationStrategy(final String type, final Function<String, String> function) {
        this.type = Objects.requireNonNull(type, "type");
        this.function = Objects.requireNonNull(function, "function");
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public String desensitize(final String value) {
        return this.function.apply(value);
    }
}
