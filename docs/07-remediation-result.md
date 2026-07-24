# 修复实施结果

## 1. 实施结论

本轮按“不保留兼容层，以目标契约为准”的原则完成了构建基线、公共契约、Web 默认行为、依赖边界和发布消费闭环。原计划中的弃用期与兼容门面未实施，相关旧 API 和不安全配置直接删除。

## 2. 已完成改动

### 2.1 构建与正确性基线

1. 删除与链式 `Asserts` 契约不一致的返回值测试，保留延迟抛错模型。
2. 修复未知 `@ResourcePath` type 序列化漏写 token；未知类型现在原样往返且始终生成合法 JSON。
3. Web 的 `MESSAGE_ONLY` 策略统一对外返回 `WEB.INTERNAL_ERROR` code，同时保留允许公开的业务消息。
4. 修复 `ResourcePathResolver` 默认 Bean 的接口级退让，用户实现可直接替换默认实现。
5. 删除仓库中的本机 JDK 绝对路径，并修复 Gradle 10 不再支持的 Project dependency notation。
6. `check` 同时执行 JaCoCo 覆盖率门禁并生成 XML/HTML 报告。

### 2.2 公共契约

1. 删除 `JacksonProvider.defaultJsonMapper()` 和公共 `Jsons.execute(...)`；`JacksonProvider.newJsonMapper()` 每次返回独立实例，工具类内部 Mapper 不再向调用方暴露。
2. `Context` 强制实现 `snapshotCopy()`；内置上下文复制扩展 Map 结构，捕获后的扩展修改不会污染快照。扩展值本身要求不可变，不做任意对象递归克隆。
3. 事件机制按 Spring 运行时边界重新收敛：Event Core 保留纯 Java 事件、发布选项和远程传输契约，Spring starter 负责 LOCAL 与 REMOTE 编排。
4. Context 移除 Guava，事件机制不重新引入 Guava EventBus；RocketMQ starter 只负责 MessageTransport 适配。

### 2.3 Web Starter

1. 请求体缓存默认关闭，需要重复读取时显式配置 `wuli3.web.context.request-body-cache-enabled=true`。
2. 删除 `InvalidRequestIdPolicy` 配置面；外部 requestId 只有通过字符集和长度校验才会使用，否则重新生成。
3. 属性绑定增加启动期校验：requestId header/长度、缓存大小、Content-Type 列表、代理 header 列表、service code 和资源路径映射均有明确边界。
4. 删除总入口 `WebAutoConfiguration`，拆分为 JSON、Context、Error、Response 四个自动配置，并在 Boot imports 中独立注册。
5. 默认 `ErrorCodeResolver` 按接口退让，自定义实现不会与默认 Bean 形成歧义。

### 2.4 发布与消费

1. 10 个 Java 组件统一发布主包、sources、Javadoc、POM 和 Gradle module metadata；BOM 单独发布。
2. BOM 纳入全部 10 个内部组件同版本约束。
3. 根任务 `publishAllPublicationsToTemporaryRepository` 将组件和 BOM 发布到临时 Maven 仓库。
4. 根任务 `verifyBomConsumers` 使用隔离 Gradle/Maven 缓存，从 BOM 无版本消费并真实编译代表组件；Maven 验证使用项目内 settings，避免用户镜像污染。
5. `apiCompatibilityCheck` 当前只生成首次发布基线状态报告。仓库尚无已发布旧版本，因此没有可比较的 API/ABI baseline；首次正式发布后应接入 japicmp 并替换该基线任务。

## 3. 破坏性变化

以下变化不提供兼容门面：

- `JacksonProvider.defaultJsonMapper()` 已删除，改用 `JacksonProvider.newJsonMapper()` 或 `JsonMapperFactory`。
- 公共 `Jsons.execute(...)` 已删除，业务代码使用 `toJson/fromJson`，基础设施直接持有自有 Mapper。
- 自定义 `Context` 必须实现 `snapshotCopy()`；可变实现必须返回独立副本。
- `WebContextProperties.InvalidRequestIdPolicy` 已删除，不再允许未校验 requestId。
- `WebAutoConfiguration` 已删除；应用应依赖 Boot 自动配置，不应显式导入 starter 内部装配类。
- 请求体缓存默认值由开启改为关闭。
- 事件模块已收敛为纯 Java `DomainEvent`/`EventEnvelope`/`PublishOptions`/`MessageTransport` 契约，以及 Spring starter 的 LOCAL、REMOTE 路由。
- 本地事件直接使用 Spring `ApplicationEventPublisher`；REMOTE 通过提交后网关尽力发送，不与业务数据库事务原子绑定。

## 4. 固定验收接口

```bash
./gradlew clean check --continue
./gradlew help --warning-mode fail
./gradlew publishAllPublicationsToTemporaryRepository --warning-mode fail
./gradlew verifyBomConsumers apiCompatibilityCheck --warning-mode fail
git diff --check
```

质量报告位于各模块 `build/reports/`；临时仓库位于根 `build/temporary-maven-repository/`；首次 API 基线状态位于 `build/reports/api-compatibility/status.properties`。

## 5. 后续方向

1. 首次正式发布后保存组件版本为 API baseline，引入 japicmp 对 10 个公共组件执行真实 API/ABI 对比。
2. 将临时发布、双消费者验证和正式发布串入 CI，正式版本禁止覆盖，凭据只从 CI secret 注入。
3. 继续补齐 Core collectors、错误元数据和 Web 非法属性启动失败的边界测试；新增公共能力时保持按风险扩展测试矩阵。
4. MySQL、Redis、Elasticsearch、MongoDB starter 继续保持依赖聚合定位；RocketMQ starter 的能力边界保持在事件传输适配。
