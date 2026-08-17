package com.kjs.wuli3.rabbit.internal;

import com.kjs.wuli3.event.PublishOptions;
import com.kjs.wuli3.event.options.AsyncPublishOptions;
import com.kjs.wuli3.event.options.TransactionalPublishOptions;

/** RabbitMQ 事件传输支持的不可变发布选项。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record RabbitPublishOptions(boolean async, boolean afterCommit)
        implements AsyncPublishOptions, TransactionalPublishOptions, PublishOptions {

    /** 创建同步、立即投递的默认选项。 */
    public RabbitPublishOptions() {
        this(false, false);
    }

    /** 返回启用异步发送的副本。 */
    public RabbitPublishOptions withAsync() {
        return new RabbitPublishOptions(true, this.afterCommit);
    }

    /** 返回要求事务提交后发送的副本。 */
    public RabbitPublishOptions withAfterCommit() {
        return new RabbitPublishOptions(this.async, true);
    }
}
