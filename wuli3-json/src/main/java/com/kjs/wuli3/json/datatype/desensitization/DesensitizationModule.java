package com.kjs.wuli3.json.datatype.desensitization;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.Serial;
import java.util.Objects;

/**
 * 在序列化阶段应用项目脱敏注解的 Jackson 模块。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@SuppressWarnings("this-escape")
public final class DesensitizationModule extends SimpleModule {
    @Serial
    private static final long serialVersionUID = 1L;

    public DesensitizationModule(
            final DesensitizationStrategyRegistry registry, final DesensitizationVisibilityPolicy visibilityPolicy) {
        final DesensitizationStrategyRegistry requiredRegistry = Objects.requireNonNull(registry, "registry");
        final DesensitizationVisibilityPolicy requiredVisibilityPolicy =
                Objects.requireNonNull(visibilityPolicy, "visibilityPolicy");
        this.setSerializerModifier(new DesensitizationSerializerModifier(requiredRegistry, requiredVisibilityPolicy));
    }
}
