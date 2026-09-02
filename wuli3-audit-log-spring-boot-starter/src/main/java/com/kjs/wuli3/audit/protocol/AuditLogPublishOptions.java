package com.kjs.wuli3.audit.protocol;

import com.kjs.wuli3.event.options.TransactionalPublishOptions;

/**
 * 审计事件的发布选项，同时作为事件路由键。
 *
 * <p>{@code RoutingEventPublisher} 按发布选项的具体类型选择传输实现，因此本类型决定了审计事件被投递到哪个
 * {@code RemoteEventTransport}。业务服务通过注册支持本选项类型的传输实现来决定审计日志走消息队列、
 * 同步 HTTP 还是本地直写，记录方无需感知。
 *
 * <p>投递保证同样取决于当前项目注册的传输实现：本 starter 不提供 Outbox，因此不承诺至少一次投递。
 *
 * @param afterCommit 是否延后到当前事务提交后再发布
 * @author GuoYang create on 2026/8/17 11:53
 */
public record AuditLogPublishOptions(boolean afterCommit) implements TransactionalPublishOptions {

    /** 在当前事务提交后发布；没有活动事务时立即发布。 */
    public static final AuditLogPublishOptions AFTER_COMMIT = new AuditLogPublishOptions(true);

    /** 立即发布，不等待事务提交。 */
    public static final AuditLogPublishOptions IMMEDIATE = new AuditLogPublishOptions(false);

    /** 按是否需要提交后发布返回对应的共享选项实例。 */
    public static AuditLogPublishOptions of(final boolean afterCommit) {
        return afterCommit ? AuditLogPublishOptions.AFTER_COMMIT : AuditLogPublishOptions.IMMEDIATE;
    }
}
