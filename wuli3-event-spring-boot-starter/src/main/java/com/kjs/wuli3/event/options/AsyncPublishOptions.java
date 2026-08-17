package com.kjs.wuli3.event.options;

import com.kjs.wuli3.event.PublishOptions;

/**
 * 声明事件发布是否异步执行的发布选项。
 * @author GuoYang create on 2026/8/6 19:02
 */
public interface AsyncPublishOptions extends PublishOptions {

    /** 返回是否异步发布。 */
    boolean async();
}
