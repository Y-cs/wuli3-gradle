package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.kjs.wuli3.json.datatype.resource.ResourcePathModule;
import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;

import java.util.Objects;

/**
 * Adds {@link com.kjs.wuli3.json.datatype.resource.ResourcePath} conversion support.
 */
public final class JsonMapperResourcePathAssembly implements JsonMapperAssemblyChain {

    private final ResourcePathResolver resourcePathResolver;

    public JsonMapperResourcePathAssembly(final ResourcePathResolver resourcePathResolver) {
        this.resourcePathResolver = Objects.requireNonNull(resourcePathResolver, "resourcePathResolver");
    }

    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
        mapperBuilder.addModule(this.resourcePathModule());
    }

    public Module resourcePathModule() {
        return new ResourcePathModule(resourcePathResolver);
    }
}
