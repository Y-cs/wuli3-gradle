package com.kjs.wuli3.event.options;

/**
 * SpringLocalPublishOptions
 * @author GuoYang create on 2026/8/6 19:02
 */
public record SpringLocalPublishOptions(boolean async, boolean afterCommit)
        implements AsyncPublishOptions, TransactionalPublishOptions {

}
