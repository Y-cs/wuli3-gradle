package com.kjs.wuli3.core.error.code;

/** 将错误标识转换为适配层对外暴露的错误码字符串。 */
public interface ErrorCodeResolver {

    String resolve(final ErrorCode errorCode);
}
