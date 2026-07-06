package com.kjs.wuli3.web.annotation;

/**
 * 原生响应模式。
 */
public enum NativeResponseMode {
    /**
     * 只跳过正常返回值包装，异常仍返回统一错误体。
     */
    SUCCESS_ONLY,

    /**
     * 正常返回值和异常响应都跳过统一响应体包装。
     */
    ALL
}
