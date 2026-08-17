package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.kjs.wuli3.json.internal.JacksonAnnotationLookup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 为标记了 {@link ResourcePath} 的属性安装资源路径反序列化器。
 *
 * @author GuoYang create on 2026/7/9 11:45
 */
@RequiredArgsConstructor
public class ResourcePathDeserializerModifier extends BeanDeserializerModifier {
    private static final long serialVersionUID = 1L;

    private final transient ResourcePathResolver resolver;

    @Override
    public BeanDeserializerBuilder updateBuilder(
            final DeserializationConfig config, final BeanDescription beanDesc, final BeanDeserializerBuilder builder) {
        final Iterator<SettableBeanProperty> iterator = builder.getProperties();
        final List<SettableBeanProperty> properties = new ArrayList<>();
        iterator.forEachRemaining(properties::add);
        for (final SettableBeanProperty property : properties) {
            final ResourcePath annotation = JacksonAnnotationLookup.find(property, ResourcePath.class);
            if (annotation == null) {
                continue;
            }
            final JsonDeserializer<?> deserializer = new ResourcePathJsonDeserializer(this.resolver, annotation);
            builder.addOrReplaceProperty(property.withValueDeserializer(deserializer), true);
        }
        return builder;
    }
}
