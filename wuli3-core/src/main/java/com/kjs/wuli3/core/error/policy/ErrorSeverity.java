package com.kjs.wuli3.core.error.policy;

/**
 * 错误对系统运行和告警处置的影响等级。
 *
 * <p>该等级不决定 HTTP 状态；HTTP 适配层应使用 {@link ErrorOrigin} 判断调用方错误或系统错误。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public enum ErrorSeverity {
    /**
     * 常规业务或输入错误，无需额外关注。
     */
    NORMAL,
    /**
     * 需要关注，但影响通常局限于当前请求或局部业务。
     */
    WARNING,
    /**
     * 服务功能受影响，需要及时处理。
     */
    CRITICAL,
    /**
     * 服务不可恢复或继续运行可能造成严重后果，需要立即处理。
     */
    FATAL
}
