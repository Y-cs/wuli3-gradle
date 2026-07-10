package com.kjs.wuli3.json.datatype.resource;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.Serial;
import java.util.Objects;

/**
 * ResourcePathModule
 *
 * @author GuoYang create on 2026/7/9 13:39
 */
@SuppressWarnings("this-escape")
public class ResourcePathModule extends SimpleModule {
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourcePathModule(final ResourcePathResolver resolver) {
        final ResourcePathResolver requiredResolver = Objects.requireNonNull(resolver, "resolver");
        this.setSerializerModifier(new ResourcePathSerializerModifier(requiredResolver));
        this.setDeserializerModifier(new ResourcePathDeserializerModifier(requiredResolver));
    }

}
