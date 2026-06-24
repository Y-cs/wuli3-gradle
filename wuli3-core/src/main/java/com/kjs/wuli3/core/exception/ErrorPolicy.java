package com.kjs.wuli3.core.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * ErrorPolicy
 *
 * @author GuoYang create on 2026/6/24 14:15
 */
@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class ErrorPolicy {

    // 错误严重程度
    private ErrorSeverity severity = ErrorSeverity.NORMAL;

    // 错误可见级别
    private ErrorVisibility visibility = ErrorVisibility.PUBLIC;

}
