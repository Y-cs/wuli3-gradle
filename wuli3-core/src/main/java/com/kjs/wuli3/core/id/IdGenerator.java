package com.kjs.wuli3.core.id;

/**
 * 生成标识符，使调用方不依赖具体的分配算法。
 *
 * @param <T> identifier value type
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface IdGenerator<T> {

    /** 生成下一个标识符。 */
    T nextId();

    /**
     * 使用前缀生成字符串形式的标识符。
     *
     * @param prefix 标识符前缀
     * @return 拼接后的字符串标识符
     */
    default String nextIdStr(final String prefix) {
        return prefix + nextId();
    }
}
