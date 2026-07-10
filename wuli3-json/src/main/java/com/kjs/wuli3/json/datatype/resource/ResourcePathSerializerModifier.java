package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.kjs.wuli3.json.internal.JacksonAnnotationLookup;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * ResourcePathSerializerModifier
 *
 * @author GuoYang create on 2026/7/9 10:02
 */
@RequiredArgsConstructor
public class ResourcePathSerializerModifier extends BeanSerializerModifier {
    private static final long serialVersionUID = 1L;

    private final transient ResourcePathResolver resolver;

    @Override
    public List<BeanPropertyWriter> changeProperties(
            final SerializationConfig config,
            final BeanDescription beanDesc,
            final List<BeanPropertyWriter> beanProperties) {
        for (final BeanPropertyWriter writer : beanProperties) {
            final ResourcePath annotation = JacksonAnnotationLookup.find(writer, ResourcePath.class);
            if (annotation != null) {
                writer.assignSerializer(new ResourcePathJsonSerializer(this.resolver, annotation));
            }
        }
        return beanProperties;
    }
}
