package com.kjs.wuli3.json.datatype.desensitization;

import java.util.function.Function;

/**
 * Masks one stable sensitive-data semantic at the JSON serialization boundary.
 */
public interface DesensitizationStrategy {

    /**
     * Stable semantic key used by {@link Desensitized#type()}.
     */
    String type();

    /**
     * Returns the JSON string value that should be exposed for the original value.
     */
    String desensitize(String value);

    static DesensitizationStrategy of(final String type, final Function<String, String> function) {
        return new FunctionDesensitizationStrategy(type, function);
    }
}
