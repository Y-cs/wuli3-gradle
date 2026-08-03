# wuli3-redis-spring-boot-starter 使用指南

该模块聚合 Spring Data Redis 和 Redisson Spring Boot starter。当前不提供业务缓存封装、统一序列化器或自定义 Redis Bean。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-redis-spring-boot-starter")
}
```

## 最小配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

应用可以使用 Spring Data Redis，也可以按 Redisson starter 的配置方式启用 `RedissonClient`。两套客户端同时存在时，连接池、序列化和超时策略应由应用统一决定。

## 使用边界

- 模块当前只聚合上游依赖，`RedisAutoConfiguration` 不创建任何 Bean。
- 不提供缓存 key 规范、分布式锁业务封装或失败降级策略。
- 不修改 Spring Cache 默认行为。
- 模块测试不连接真实 Redis。

## 验证

```bash
./gradlew :wuli3-redis-spring-boot-starter:check
```
