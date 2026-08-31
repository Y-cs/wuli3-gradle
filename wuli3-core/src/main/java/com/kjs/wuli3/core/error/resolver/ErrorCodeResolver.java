package com.kjs.wuli3.core.error.resolver;

import com.kjs.wuli3.core.error.ErrorCode;

/** 将错误标识转换为适配层对外暴露的错误码字符串。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ErrorCodeResolver {

    String resolve(final ErrorCode errorCode);
}
