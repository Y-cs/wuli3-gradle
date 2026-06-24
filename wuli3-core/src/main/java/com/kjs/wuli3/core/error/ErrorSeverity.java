package com.kjs.wuli3.core.error;

/**
 * ErrorSeverity
 * <p>
 * 错误严重程度
 * </p>
 *
 * @author GuoYang create on 2026/6/24 11:50
 */
public enum ErrorSeverity {
    /**
     *
     * 不能为空，格式错误，数据不存在，配置不存在，库存不足
     */
    NORMAL,
    /**
     * 需要关注，但系统仍可继续运行
     * 配置错误，状态错误，订单状态异常，权限不足
     */
    WARNING,
    /**
     * 系统功能受影响
     * 数据库连接不可用，redis不可用，mq发送失败，接口调用超时
     */
    CRITICAL,
    /**
     * 系统不可恢复，需要立即处理
     * 启动失败，数据一致性错误
     */
    FATAL
}
