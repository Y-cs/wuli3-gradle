package com.kjs.wuli3.web.error;

/**
 * 供业务应用接入 Web 错误告警的扩展点。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface ErrorAlertNotifier {

    /**
     * 在统一异常处理器捕获错误后发送告警。
     */
    void alert(final ErrorAlertContext context);
}
