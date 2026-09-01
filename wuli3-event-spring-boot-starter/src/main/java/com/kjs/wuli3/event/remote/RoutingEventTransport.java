package com.kjs.wuli3.event.remote;

import com.kjs.wuli3.event.EventTransport;
import com.kjs.wuli3.event.PublishOptions;

/**
 * 需要被 {@code EventAutoConfiguration} 自动收集进 {@code RoutingEventPublisher} 的传输标记接口。
 *
 * <p>与仅由自动配置显式装配的 {@link com.kjs.wuli3.event.transport.SpringLocalEventTransport} 区分，
 * 避免同一 {@link PublishOptions} 类型被重复注册。实现不要求一定是跨进程投递——只要求"希望被自动收集"，
 * 例如进程内直写持久化端口的传输也可以实现本接口。跨进程的远程投递实现应改用更严格的
 * {@link RemoteEventTransport}，它同时参与 {@code @ConditionalOnMissingRemoteEventTransport} 的条件判断。
 *
 * @param <PO> 支持的发布选项类型
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface RoutingEventTransport<PO extends PublishOptions> extends EventTransport<PO> {}
