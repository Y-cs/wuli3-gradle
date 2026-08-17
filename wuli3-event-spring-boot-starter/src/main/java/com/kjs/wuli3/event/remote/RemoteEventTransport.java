package com.kjs.wuli3.event.remote;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;

/** 远程事件传输的标记接口。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface RemoteEventTransport<PO extends PublishOptions> extends EventTransport<PO> {}
