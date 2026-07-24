package com.kjs.wuli3.rocketmq.autoconfigure;

/**
 * 控制哪些当前线程上下文可以跨越 RocketMQ 边界传播。
 */
public enum RocketMqContextMode {
    /**
     * 只传播调用标识。
     */
    INVOCATION_ONLY,
    /**
     * 仅在显式可信的内部链路中额外传播认证字段。
     */
    TRUSTED_INTERNAL
}
