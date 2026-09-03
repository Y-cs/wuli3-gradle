# wuli3-rocketmq-spring-boot-starter 使用指南

该 starter 根据所选 RocketMQ 客户端为事件模块提供 `RemoteEventTransport` 发送实现，并提供可手动使用的消息上下文恢复支持。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-rocketmq-spring-boot-starter")
}
```

模块会传递 `wuli3-event-spring-boot-starter`、`wuli3-context-propagation` 和 RocketMQ Spring Boot starter。

## RocketMQ 配置

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: order-service
```

以上是 v4 的配置方式；具体连接、认证和 producer 参数由 RocketMQ Spring Boot starter 管理。选择 v4 时没有 `RocketMQTemplate`，本模块不会注册 v4 transport。

## 选择客户端

默认使用由 RocketMQ Spring Boot starter 管理的 v4 `RocketMQTemplate`：

```yaml
wuli3:
  rocketmq:
    client-version: v4
```

`wuli3.rocketmq.client-version` 只决定 starter 自动注入哪个 transport：

| 配置 | 前提 | 自动注入 |
| --- | --- | --- |
| 未设置或 `v4` | 存在 `RocketMQTemplate` | 基于 v4 的 `RocketRemoteEventTransport` |
| `v5` | 运行时包含 Java Client v5，且存在一个 `Producer` Bean | 基于 v5 的 `RocketV5RemoteEventTransport` |

应用自己声明的 `RocketEventTransport` 优先于上述自动配置；其他 options 类型的
`RemoteEventTransport` 可以与 RocketMQ transport 同时注册。选择 v5 时设置 `client-version: v5`。
v5 依赖在本 starter 中是 `compileOnly`，应用必须显式引入它，并提供由 Spring 关闭的
`Producer` Bean；starter 会通过 SPI 创建可覆盖的 `ClientServiceProvider` Bean。已选择 v5
但未提供 `Producer` 时，应用会在启动时失败，不会静默回退到 v4 或默认 transport。

```kotlin
dependencies {
    implementation("org.apache.rocketmq:rocketmq-client-java")
}
```

```java
@Bean(destroyMethod = "close")
Producer rocketV5Producer(final ClientServiceProvider clientServiceProvider) throws ClientException {
    final ClientConfiguration clientConfiguration =
            ClientConfiguration.newBuilder().setEndpoints("your-v5-endpoint:8081").build();
    return clientServiceProvider.newProducerBuilder()
            .setClientConfiguration(clientConfiguration)
            .setTopics("orders")
            .build();
}
```

应用负责 v5 Producer 的 endpoint、凭据和预声明 topic；调用 `setTopics(...)` 时应列出该 Producer 会发送的全部 topic。v5 选中后不会注入 v4 transport，即使应用同时配置了 `RocketMQTemplate`。

## 发布远程事件

```java
final EventEnvelope<OrderPaid> envelope =
        EventEnvelopeTemplate.of("orders", "order.paid.v1").wrap(payload);

final RocketPublishOptions options = new RocketPublishOptions()
        .withAfterCommit();

eventPublisher.publish(options, envelope);
```

`RocketPublishOptions` 通过 `withAsync()`、`withAfterCommit()`、`withOrderKey(...)` 和
`withDelay(...)` 创建不可变副本。支持同步、异步、顺序和精确延迟发送，但精确延迟不能与
`async` 或 order key 组合。v4 支持异步顺序发送；Java Client v5 不支持异步 FIFO，选择 v5
时 `async` 不能与 order key 组合。编码器要求：

- topic 长度不超过 127 字节。
- topic 只能包含字母、数字、`%`、`-` 和 `_`。
- 非正数 delay 和空白 order key 会在构造选项时被拒绝。

## 上下文传播

`ContextPropagator` 读取可选的 `ContextReader`，并按显式白名单把传播字段写入 `RocketMessageWrapper.headers`。`EventEnvelope` 不承载传输 header，其 JSON body 只包含事件语义字段。

默认自动配置使用 `ContextPropagator.standardContextEncoder()`，当前会传播 `X-Request-Id`、`X-Origin-Ip`、
`X-Principal-Type`、`X-Principal-Id` 和 `X-Principal-Name`。因此该 starter 应只用于允许传播认证信息的可信消息边界。

若边界只允许传播调用标识，应显式覆盖该 Bean：

```java
@Bean
ContextPropagator rocketMqContextEncoder() {
    return new ContextPropagator(List.of(new InvocationContextCodec()));
}
```

同一个 `ContextPropagator` Bean 同时决定 `RocketMessageWrapperEncoder` 的出站字段和 `RocketContextSupport` 的入站字段。缩小白名单后，入站恢复也只会接受对应字段。

消费适配器需要先得到已解码的 `ContextProxy`，再显式恢复作用域：

```java
final ContextProxy contextProxy = rocketMqContextSupport.restoreFrom(messageExt.getProperties());
try (ContextScope ignored = contextProxy.restore(contextProxy.capture())) {
    listener.handle(envelope);
}
```

`restoreFrom` 只解码字段编码器识别出的上下文并返回 `ContextProxy`，不会自动注册或包裹 RocketMQ Listener。实际 Listener 仍应根据消息来源、线程模型、重试和死信策略决定调用时机。非法认证字段由 `AuthContextCodec` 忽略，避免消费适配器承担解析细节。

## 投递边界

- `afterCommit` 是提交后尽力发送，不是 Outbox 或可靠消息。
- 异步发送失败由 transport 记录日志，不会回滚已经提交的业务事务。
- 传播 header 属于 RocketMQ 消息属性，不进入 `EventEnvelope` 的 JSON body。
- 消费端的自动 Listener、幂等、重试和死信不在当前模块范围内；上下文恢复通过 `RocketContextSupport` 显式完成。

## 验证

```bash
./gradlew :wuli3-rocketmq-spring-boot-starter:test
./gradlew :wuli3-rocketmq-spring-boot-starter:check
```
