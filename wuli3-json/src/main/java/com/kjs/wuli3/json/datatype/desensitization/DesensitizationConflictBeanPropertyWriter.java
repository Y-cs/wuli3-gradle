package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import java.io.Serial;
import java.util.Objects;
import org.jspecify.annotations.NullUnmarked;

/**
 * Fails serialization for ambiguous property-level transformation combinations.
 */
final class DesensitizationConflictBeanPropertyWriter extends BeanPropertyWriter {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;

    DesensitizationConflictBeanPropertyWriter(final BeanPropertyWriter writer, final String message) {
        super(writer);
        this.message = Objects.requireNonNull(message, "message");
    }

    @NullUnmarked
    @Override
    public void serializeAsField(final Object bean, final JsonGenerator gen, final SerializerProvider prov)
            throws Exception {
        throw JsonMappingException.from(gen, this.message);
    }

    @NullUnmarked
    @Override
    public void serializeAsElement(final Object bean, final JsonGenerator gen, final SerializerProvider prov)
            throws Exception {
        throw JsonMappingException.from(gen, this.message);
    }
}
