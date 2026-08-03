# wuli3-mongodb-spring-boot-starter 使用指南

该模块是 Spring Data MongoDB starter 的依赖聚合包。当前没有自定义 `MongoTemplate`、Repository 或连接策略。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-mongodb-spring-boot-starter")
}
```

## 最小配置

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/app
```

文档映射、Repository、事务和索引行为使用 Spring Data MongoDB 的标准 API。

## 使用边界

- 模块当前只传递 `spring-boot-starter-data-mongodb`。
- `MongodbAutoConfiguration` 不创建任何 Bean。
- 不提供多租户路由、分片策略、索引迁移或连接健康策略。
- 模块测试不连接真实 MongoDB。

## 验证

```bash
./gradlew :wuli3-mongodb-spring-boot-starter:check
```
