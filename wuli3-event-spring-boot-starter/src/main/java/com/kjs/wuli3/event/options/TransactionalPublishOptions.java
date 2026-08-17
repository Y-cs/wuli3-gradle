package com.kjs.wuli3.event.options;

import com.kjs.wuli3.event.PublishOptions;

/**
 * 声明事件是否应在事务提交后发布的发布选项。
 * @author GuoYang create on 2026/8/6 19:02
 */
public interface TransactionalPublishOptions extends PublishOptions {

    /** 返回是否在事务提交后发布。 */
    boolean afterCommit();
}
