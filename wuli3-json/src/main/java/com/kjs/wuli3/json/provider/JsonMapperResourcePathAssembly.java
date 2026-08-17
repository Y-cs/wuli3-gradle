package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.kjs.wuli3.json.datatype.resource.ResourcePathModule;
import com.kjs.wuli3.json.datatype.resource.ResourcePathResolver;
import java.util.Objects;

/**
 * 添加 {@link com.kjs.wuli3.json.datatype.resource.ResourcePath} 转换能力。
 *
 * @author GuoYang create on 2026/8/17 11:53
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

    /** 创建资源路径 Jackson 模块。 */
    public Module resourcePathModule() {
        return new ResourcePathModule(resourcePathResolver);
    }
}
