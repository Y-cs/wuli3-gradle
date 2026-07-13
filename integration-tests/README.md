# 发布产物消费验证

## 1. 目录定位

`integration-tests/` 包含独立于主构建的外部消费者 fixture，用于验证 Wuli3 发布产物，而不是验证业务流程或中间件连接。
它们不加入根 `settings.gradle.kts`，避免消费者通过项目依赖绕过真实 Maven 发布元数据。

当前包含：

- `gradle-consumer/`：验证 Gradle 项目通过 Wuli3 BOM 无版本消费代表组件，并运行最小 JUnit 测试。
- `maven-consumer/`：验证 Maven 项目导入 Wuli3 BOM 后无版本消费代表组件，并完成 Java 编译。

## 2. 验证流程

根任务 `verifyBomConsumers` 是本地和 CI 的固定入口：

```bash
./gradlew verifyBomConsumers --warning-mode fail
```

执行顺序如下：

1. `publishAllPublicationsToTemporaryRepository` 将 BOM 和公共组件发布到根目录
   `build/temporary-maven-repository/`。
2. Gradle consumer 使用 `build/consumer-gradle-home/` 作为独立 user home，解析临时仓库中的发布产物并运行测试。
3. Maven consumer 清理并使用 `build/consumer-maven-home/` 作为独立本地仓库，通过项目内 `settings.xml` 编译测试工程。

隔离缓存用于防止开发机已有的 SNAPSHOT、Maven mirror 或本地发布结果掩盖当前构建的元数据问题。

## 3. CI 使用

推荐在正式发布前执行：

```bash
./gradlew --no-daemon clean check --continue
./gradlew --no-daemon verifyBomConsumers apiCompatibilityCheck --warning-mode fail
```

CI 环境前置条件：

- JDK 21；
- Maven；
- Maven Central 网络访问；
- Gradle Wrapper 可执行。

消费验证不访问公司正式 Maven 仓库，也不需要发布凭据。正式发布任务应依赖该验证成功，正式版本禁止覆盖，凭据只允许来自
CI secret 或环境变量。

## 4. 验证边界

当前验证覆盖：

- BOM 是否包含被测组件的版本约束；
- Gradle Module Metadata 和 Maven POM 是否可以解析；
- 发布产物是否遗漏必要的传递依赖；
- Gradle 和 Maven 是否都能从发布产物编译代表性调用代码；
- Gradle consumer 的最小运行行为。

当前不覆盖：

- Spring Boot 完整应用启动；
- MySQL、Redis、RocketMQ、Elasticsearch、MongoDB 等真实基础设施连接；
- 业务接口端到端流程；
- 已发布版本之间的真实 API/ABI 兼容性对比。

API/ABI 对比由 `apiCompatibilityCheck` 承担；首次正式版本发布后，需要接入 japicmp 并指定已发布版本作为 baseline。

## 5. 扩展规则

新增公共组件或调整 BOM 时，应同时判断是否需要加入 consumer：

1. 组件必须先由统一 publishing convention 发布，并加入 BOM 约束。
2. consumer 依赖不写组件版本，确保版本确实来自 BOM。
3. 至少增加一处代表性类型引用，避免只验证坐标存在而未验证产物内容。
4. 只有依赖真实基础设施的行为才进入单独的应用集成测试，不在此处启动容器或外部服务。
5. fixture 不得依赖根项目源码路径或 Gradle project dependency。
