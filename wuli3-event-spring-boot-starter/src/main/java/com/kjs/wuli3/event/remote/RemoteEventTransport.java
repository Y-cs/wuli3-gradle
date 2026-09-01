package com.kjs.wuli3.event.remote;

import com.kjs.wuli3.event.PublishOptions;

/**
 * 跨进程远程事件传输的标记接口。
 *
 * <p>除了被 {@code EventAutoConfiguration} 自动收集（继承自 {@link RoutingEventTransport}），本接口的
 * 实现还会参与 {@code @ConditionalOnMissingRemoteEventTransport} 的条件判断：某个 {@link PublishOptions}
 * 类型是否已有远程传输覆盖。仅需要被自动收集但不是远程投递的实现（例如进程内直写持久化端口）应改用
 * {@link RoutingEventTransport}，避免被误判为"已有远程传输"。
 *
 * @param <PO> 支持的发布选项类型
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface RemoteEventTransport<PO extends PublishOptions> extends RoutingEventTransport<PO> {}
