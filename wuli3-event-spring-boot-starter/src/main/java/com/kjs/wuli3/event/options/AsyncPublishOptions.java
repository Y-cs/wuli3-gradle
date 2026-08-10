package com.kjs.wuli3.event.options;

import com.kjs.wuli3.event.PublishOptions;

/**
 * SpringLocalPublishOptions
 * @author GuoYang create on 2026/8/6 19:02
 */
public interface AsyncPublishOptions extends PublishOptions {

    boolean async();
}
