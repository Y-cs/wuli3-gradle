package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.code.ErrorCode;
import org.springframework.http.HttpStatus;

/** 解析已处理 Web 错误应返回的 HTTP 状态。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface WebErrorStatusResolver {

    /** 根据当前异常和对外错误码返回 HTTP 状态。 */
    HttpStatus resolve(Throwable error, ErrorCode responseCode);
}
