package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationModule;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationStrategyRegistry;
import com.kjs.wuli3.json.datatype.desensitization.DesensitizationVisibilityPolicy;
import java.util.Objects;

/**
 * 添加 {@link com.kjs.wuli3.json.datatype.desensitization.Desensitized} JSON 输出脱敏能力。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonMapperDesensitizationAssembly implements JsonMapperAssemblyChain {
    private final DesensitizationStrategyRegistry registry;
    private final DesensitizationVisibilityPolicy visibilityPolicy;

    public JsonMapperDesensitizationAssembly(
            final DesensitizationStrategyRegistry registry, final DesensitizationVisibilityPolicy visibilityPolicy) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.visibilityPolicy = Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
    }

    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
        mapperBuilder.addModule(this.desensitizationModule());
    }

    /** 创建脱敏 Jackson 模块。 */
    public Module desensitizationModule() {
        return new DesensitizationModule(this.registry, this.visibilityPolicy);
    }
}
