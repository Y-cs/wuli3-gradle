package com.kjs.wuli3.json.datatype.desensitization;

import java.util.Objects;
import java.util.function.Function;

/**
 * 供内置规则和轻量自定义规则使用的函数式脱敏策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
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
