# wuli3-elasticsearch-spring-boot-starter 使用指南

该模块是 Spring Data Elasticsearch starter 的依赖聚合包。当前没有自定义客户端、索引模板或 Repository 基类。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-elasticsearch-spring-boot-starter")
}
```

## 最小配置

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: change-me
```

实体映射、Repository、索引创建和查询使用 Spring Data Elasticsearch 的标准 API。

## 使用边界

- 模块当前只传递 `spring-boot-starter-data-elasticsearch`。
- 模块不提供额外的自动配置或 Elasticsearch 封装。
- 不管理索引生命周期、mapping 迁移、别名切换或集群健康检查。

## 验证

```bash
./gradlew :wuli3-elasticsearch-spring-boot-starter:check
```
