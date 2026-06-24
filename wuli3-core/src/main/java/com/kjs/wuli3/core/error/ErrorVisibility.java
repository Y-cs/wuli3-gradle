package com.kjs.wuli3.core.error;

/**
 * ErrorVisibility
 * 错误可见级别
 *
 * @author GuoYang create on 2026/6/24 11:41
 */
public enum ErrorVisibility {

    // 公开，该级别会将错误信息向外输出。
    PUBLIC,
    CODE_ONLY,
    MESSAGE_ONLY,
    // 内部，该级别不会将错误信息向外输出。
    INTERNAL
}
