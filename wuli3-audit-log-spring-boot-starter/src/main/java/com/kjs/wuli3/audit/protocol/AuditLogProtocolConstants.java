package com.kjs.wuli3.audit.protocol;

/**
 * 审计日志远程协议的稳定名称和版本。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class AuditLogProtocolConstants {

    /** 审计事件的逻辑主题。 */
    public static final String TOPIC = "audit-log";

    /** 第一版审计事件契约名称。 */
    public static final String EVENT_TYPE = "audit.log.recorded.v1";

    private AuditLogProtocolConstants() {}
}
