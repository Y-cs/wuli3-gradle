package com.kjs.wuli3.json.datatype.desensitization;

import java.util.function.Function;

/**
 * 在 JSON 序列化边界处理一种稳定敏感数据语义的脱敏策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface DesensitizationStrategy {

    /**
     * 返回供 {@link Desensitized#type()} 引用的稳定语义键。
     */
    String type();

    /**
     * 返回原值脱敏后可写入 JSON 的字符串。
     */
    String desensitize(String value);

    /** 使用函数创建脱敏策略。 */
    static DesensitizationStrategy of(final String type, final Function<String, String> function) {
        return new FunctionDesensitizationStrategy(type, function);
    }
}
