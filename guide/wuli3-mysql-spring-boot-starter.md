# wuli3-mysql-spring-boot-starter 使用指南

该模块是 MyBatis-Plus Spring Boot 3 starter 的依赖聚合包。当前没有自定义 Mapper、Repository、数据源 Bean 或运行时配置。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-mysql-spring-boot-starter")
    runtimeOnly("com.mysql:mysql-connector-j")
}
```

版本建议由 `wuli3-dependencies` BOM 管理。数据库驱动由应用根据实际数据库显式选择。

## 最小配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app
    username: app
    password: change-me

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
```

Mapper 扫描、分页插件、类型处理器和事务边界按 MyBatis-Plus/Spring Boot 的标准方式配置。

## 使用边界

- 模块当前只传递 `mybatis-plus-spring-boot3-starter`。
- `MysqlAutoConfiguration` 不创建任何 Bean。
- 不负责建库、迁移、连接池选择、读写分离或 SQL 审计。
- 模块测试只验证自动配置可加载，不连接真实数据库。

## 验证

```bash
./gradlew :wuli3-mysql-spring-boot-starter:check
```
