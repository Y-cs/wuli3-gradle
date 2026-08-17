package com.kjs.wuli3.event.options;

/**
 * Spring 本地事件发布选项，支持异步执行和事务提交后发布。
 * @author GuoYang create on 2026/8/6 19:02
 */
public record SpringLocalPublishOptions(boolean async, boolean afterCommit)
        implements AsyncPublishOptions, TransactionalPublishOptions {}
