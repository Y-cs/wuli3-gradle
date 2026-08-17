package com.kjs.wuli3.core.id;

import java.util.UUID;

/**
 * 生成标准 UUID 字符串标识符。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class UuidStringIdGenerator implements IdGenerator<String> {

    public static final UuidStringIdGenerator INSTANCE = new UuidStringIdGenerator();

    private UuidStringIdGenerator() {}

    /** 返回新的随机 UUID 字符串。 */
    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
