package com.kjs.wuli3.core.error;

/**
 * 表示应由哪一侧修正当前错误。
 *
 * <p>该语义独立于 {@link ErrorSeverity}。适配层可将调用方错误和服务方错误映射为自身协议的错误类别，
 * 例如 HTTP 的 4xx 与 5xx。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public enum ErrorOrigin {
    /** 调用方可以通过修正请求、状态或所选能力解决错误。 */
    CALLER,
    /** 服务方自身或其依赖需要修复后，操作才能继续。 */
    SERVER
}
