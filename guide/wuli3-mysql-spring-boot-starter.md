# wuli3-mysql-spring-boot-starter 使用指南

该模块是 MyBatis-Plus Spring Boot 3 starter，并提供可选的 SQL 日志与慢 SQL 告警能力。它不创建数据源，也不负责数据库迁移。

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

wuli3:
  mysql:
    sql:
      enabled: true
      logging-enabled: true
      logging-level: debug
      exception-enabled: true
      slow-query-enabled: true
      slow-query-threshold: 1s
      include-parameters: false
      max-sql-length: 4096
      max-parameter-length: 256
      max-parameter-summary-length: 4096
    block-attack:
      enabled: true
```

Mapper 扫描、分页插件、类型处理器和事务边界按 MyBatis-Plus/Spring Boot 的标准方式配置。

SQL 观测默认关闭。开启后，普通 SQL 默认以 `DEBUG` 记录，可通过 `logging-level: info` 改为 `INFO`；达到阈值的 SQL 以 `WARN` 记录，执行异常的 SQL 独立以 `ERROR` 记录。所有 SQL 日志固定使用带换行和 ASCII 分隔符的多行格式。慢 SQL 和异常 SQL 都会通过 `SqlAlertNotifier` 派发。观测范围包括普通查询、游标查询、更新和批执行。

参数默认不渲染；开启参数后仍会对常见密码、Token、Secret、Credential、Authorization 和 Key 字段脱敏。单个参数、整体参数摘要和 SQL 文本分别限制长度。全表更新、删除防护默认开启，可通过 `wuli3.mysql.block-attack.enabled=false` 显式关闭。

## 使用边界

- 模块传递 `mybatis-plus-spring-boot3-starter` 和 BlockAttack 所需的 `mybatis-plus-jsqlparser-4.9`，并按条件注册 SQL 观测拦截器。
- `MysqlAutoConfiguration` 仍不创建数据源、迁移器、读写分离或连接池 Bean。
- 不负责建库、迁移、连接池选择、读写分离或 SQL 审计。
- 模块测试只验证自动配置可加载，不连接真实数据库。

## 验证

```bash
./gradlew :wuli3-mysql-spring-boot-starter:check
```
