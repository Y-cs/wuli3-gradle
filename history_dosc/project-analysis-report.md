# wuli3-gradle 当前实现复审报告

## 1. 审查结论

- 审查日期：2026-07-24
- 审查范围：当前工作区全部模块、公共 API、自动配置、测试、构建约定、发布消费验证和现有说明文档
- 代码审查结论：`REQUEST CHANGES`
- 架构状态：`BLOCK`
- 适用阶段：可继续作为 `0.1.0-SNAPSHOT` 演进，不建议作为稳定业务底座发布

项目的基本分层方向是成立的：`core` 和 `context-propagation` 保持框架无关，Web 协议适配留在 starter，RocketMQ 通过传输端口接入，BOM 也能被 Maven/Gradle 消费者解析。当前问题不是模块数量本身，而是若干公共抽象扩大了安全边界或统一了本应分离的业务语义，同时质量门禁和发布门禁尚未形成可信闭环。

发布前必须优先解决：

1. Web 上下文、校验错误和告警上下文可能泄漏敏感数据。
2. 可信代理模型允许来源 IP 被伪造。
3. 跨协议上下文读取可能残留上一请求的认证身份。
4. Web Jackson 配置可能覆盖 Spring Boot 和业务模块注册。
5. 本地 Spring 事件与远程消息被统一成同一信封，使用语义不自然。
6. 当前 `check` 无法通过，API 兼容任务也不执行真实兼容性比较。

## 2. 已满足的设计预期

### 2.1 模块依赖方向基本合理

- `wuli3-core` 不依赖 Spring，错误身份、错误策略与 Web/HTTP 表达分离。
- `wuli3-json` 通过 Jackson Module 扩展时间、枚举、资源路径和脱敏能力，没有把 Spring 依赖带入 JSON 核心。
- `wuli3-context-propagation` 把上下文、载体和协议编解码分开，HTTP、RPC、消息队列适配可以位于独立 starter。
- RocketMQ 自动配置要求存在 `RocketMQTemplate`，并允许应用通过 `RemoteEventTransport` 替换默认实现。
- 缺少远程传输时会显式失败，而不是静默丢弃远程事件。
- 数据 starter 没有制造统一 Repository、Client 等缺乏真实语义的中间抽象。

### 2.2 已修复的历史问题不能继续作为现状缺陷

未跟踪文档 `wuli3-core/docs/design-analysis.md` 基于更早代码，其中以下结论已经过时：

- `DateRange`、`TimeRange` 已排除空区间重叠。
- `DateRange.closed` 已显式处理 `LocalDate.MAX`。
- `ErrorCode` 已明确要求由 enum 实现，并对非法实现显式失败。
- `Asserts` 的实现、测试和操作手册均已统一为链式延迟抛错，不返回被校验对象。
- `wuli3-core` 已使用 JDK 并发集合替代 Guava。

后续应更新或归档该旧报告，避免重复推动已经完成的整改。

## 3. 阻断问题

### 3.1 HIGH：WebContext 扩大敏感数据生命周期

证据：

- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/context/WebContext.java:23`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/context/WebContext.java:27`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/context/WebContext.java:59`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/context/WebContext.java:73`

`WebContext.from(...)` 复制所有请求头、参数、完整 URL 和 query string；`@ToString` 会输出这些字段；`snapshotCopy()` 又允许它们进入异步上下文快照。Authorization、Cookie、密码、验证码、token 和个人数据可能因此进入日志、诊断对象或异步任务。

建议将默认上下文缩小为 `requestId`、`method`、`requestUri`、`locale` 等安全元数据。原始 headers、parameters、query string 和完整 URL 不应进入长期上下文；确需读取时，应在 Servlet 边界显式读取并由业务承担生命周期。

### 3.2 HIGH：校验响应和告警上下文可能泄漏敏感原值

证据：

- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/response/ValidationErrorDetailsFactory.java:43`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/response/ValidationErrorDetailsFactory.java:52`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/response/ValidationErrorDetailsFactory.java:67`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/response/WebResponseProperties.java:40`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/error/ErrorAlertContext.java:10`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/response/WebExceptionHandler.java:191`

字段校验、类型转换和约束校验会把 rejected/invalid value 放进错误响应，且该能力默认开启。异常告警还携带原始 query string，并对所有已处理异常执行通知扩展点。

建议错误项只保留稳定的 `field`、`code`、`message`，不返回用户原值；告警上下文删除 query string，只对 5xx 或显式高严重度错误默认通知。

### 3.3 HIGH：可信代理开关信任任意来源

证据：

- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/context/WebContextProperties.java:81`
- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/internal/context/DefaultClientIpResolver.java:21`

`trustedProxyEnabled=true` 后，解析器直接采用第一个转发头值，没有验证 `request.getRemoteAddr()` 是否属于可信代理。只要应用存在绕过网关的直连路径，调用方即可伪造审计、限流或风控使用的客户端 IP。

建议用显式 `trusted-proxy-cidrs` 替代全局布尔信任：只有直接 peer 命中可信网段时才解析转发链，并从右向左剥离可信 hop；非法 CIDR 启动失败，非法头值回退到直接 peer。当前未跟踪的 `DefaultClientIpResolverTest` 已表达这一预期，但生产属性尚未实现。

### 3.4 HIGH：跨协议读取可能残留旧认证上下文

证据：

- `wuli3-context-propagation/src/main/java/com/kjs/wuli3/propagation/transmission/ContextTransmitter.java:38`
- `wuli3-context-propagation/README.md:92`

`readFrom()` 只写入成功解析的上下文。当后一个载体缺少认证字段或字段非法时，线程中原有 `AuthContext` 不会被移除。在线程池、消息消费或 RPC provider 场景中，这可能造成跨请求身份串用。

建议提供作用域式 `readScoped()`：进入前保存旧快照并清除本次 codec 管理的上下文，读取新载体，关闭 scope 时恢复旧快照。协议适配器必须使用 try-with-resources，并覆盖“前一请求有认证、后一请求无认证”的回归测试。

### 3.5 HIGH：Web Jackson 自定义器可能覆盖业务模块

证据：

- `wuli3-web-spring-boot-starter/src/main/java/com/kjs/wuli3/web/autoconfigure/WebJsonAutoConfiguration.java:42`

当前调用 `builder.modules(...)` 设置模块列表。该操作不是增量追加，可能替换 Spring Boot 或业务通过 `Module` Bean 注册的模块，并关闭模块自动发现，导致 JDK8、ParameterNames、Kotlin 或业务自定义序列化器静默失效。

建议改为增量安装方式，并新增自动配置测试：应用自定义 `Module` Bean 与 wuli3 时间、资源路径、脱敏模块必须同时生效。

### 3.6 HIGH：本地与远程事件的统一抽象产生语义错配

证据：

- `wuli3-event-core/src/main/java/com/kjs/wuli3/event/envelope/EventEnvelope.java:13`
- `wuli3-event-core/src/main/java/com/kjs/wuli3/event/EventPublisher.java:6`
- `wuli3-event-spring-boot-starter/src/main/java/com/kjs/wuli3/event/transport/SpringLocalEventTransport.java:18`

`EventEnvelope` 仍强制要求远程消息关注的 `topic`、稳定事件类型和事件 ID，Spring 最终发布的也是整个信封。普通 `@EventListener(OrderPaid.class)` 无法自然按 payload 类型监听，业务即使只需要本地事件也必须构造远程元数据。

建议本地事件直接使用 Spring `ApplicationEventPublisher` 发布业务对象；`EventEnvelope`、`PublishOptions` 和传输能力只服务远程集成事件。不要为了统一入口重新制造框架事件总线，也不要在没有可靠投递需求时重新引入 Outbox。

原有全局 `LOCAL`/`REMOTE` 选项及默认通道已经删除；当前由具体 options 类型选择 transport，事务、异步和 RocketMQ 能力也由对应选项显式表达。指南已同步到该代码契约。

### 3.7 HIGH：仓库自身质量门禁不通过

当前工作区执行 `rtk ./gradlew check`：

- 事件 core、Spring 事件 starter 和 RocketMQ starter 的独立 `check` 已通过。
- 仓库级门禁仍被 `wuli3-context-propagation` 的格式问题和 `ContextEncoder` 星号导入的 Checkstyle 错误阻断。
- 未跟踪的 Web 测试仍属于独立工作区改动，本次事件重构未修改它。

此前对纯 `HEAD` 快照执行同一检查时，Web 的 39 个测试全部因 `slf4j-simple` 与 Logback 冲突而无法启动 Spring 上下文。根因位于：

- `build-logic/src/main/kotlin/com/kjs/wuli3/JavaConventionsPlugin.kt:95`
- `build-logic/src/main/kotlin/com/kjs/wuli3/SpringConventionsPlugin.kt:12`

Java 约定插件把 Lombok 开关与 SLF4J 依赖绑定，并向所有测试加入 `slf4j-simple`；Spring starter 测试又通过 `spring-boot-starter-test` 获得 Logback，形成两个实现冲突。日志 API/实现不应与 Lombok 开关耦合；公共库也不应无条件携带日志实现。

## 4. 复杂度与简化空间

### 4.1 可直接删除的占位复杂度

MySQL、Redis、Elasticsearch、MongoDB 四个 starter 的自动配置类为空，对应测试只执行空上下文：

- `wuli3-mysql-spring-boot-starter/src/main/java/com/kjs/wuli3/mysql/autoconfigure/MysqlAutoConfiguration.java:5`
- `wuli3-mysql-spring-boot-starter/src/test/java/com/kjs/wuli3/mysql/MysqlAutoConfigurationTest.java:8`

其他三个模块结构相同。如果定位只是依赖聚合，应删除空自动配置、imports 和无行为测试，并在 README 明确其为 dependency bundle；只有出现真实组织级默认策略时才添加自动配置。

### 4.2 事件层可减少一组公开概念

将本地事件交还 Spring 后，可以删除本地 `EventEnvelope` 路由、LOCAL 能力校验以及统一通道路由器。远程侧只保留：

```text
RemoteEventEnvelope -> RemoteEventPublisher -> RemoteEventMessageTransport
```

事务提交后尽力投递应作为远程发布器的显式能力，而不是同时包装本地和远程传输。

### 4.3 上下文扩展袋应由真实场景驱动

`ContextKey`、`ExtendableContext` 和 `AbstractContext` 提供可变扩展袋，但当前生产代码没有稳定业务 key，主要增加复制、并发和生命周期复杂度。若近期没有租户、灰度或区域等已确认上下文，应先删除扩展袋；需要时再以显式值对象或受控 key 集合引入。

### 4.4 收窄未使用或伪公开 API

- `JsonMapperNumberStrAssembly` 当前未进入标准 assembly，也没有生产调用方，可删除或改为明确的可选模块。
- `JsonFunction` 是 public，但唯一消费入口 `Jsons.execute` 是包级方法，应降为包私有。
- Web README 声明 `internal` 不属于扩展 API，但多个 internal 类和构造器仍为 public；应通过可见性或兼容性规则真正收口。

## 5. 业务场景覆盖

| 场景 | 当前状态 | 结论 |
| --- | --- | --- |
| Spring MVC 统一响应、异常映射、参数校验 | 已实现 | 主路径完整，但敏感原值回显和日志实现冲突阻断生产使用 |
| 文件、字节、SSE、流式响应、204/304 | 已显式跳过包装 | 设计合适 |
| 请求 ID、MDC、请求上下文清理 | 已实现 | 基本合适，需缩小 WebContext 数据面 |
| 反向代理客户端 IP | 仅有全局信任开关 | 场景不完整，缺可信 CIDR 和多跳代理模型 |
| 线程池上下文传播 | 有 capture/restore/wrap | 机制完整，缺 Spring `TaskDecorator` 等业务适配 |
| HTTP client/Feign/Dubbo 出站传播 | 核心接口已预留 | 没有 starter 适配，业务需自行实现 |
| RPC/MQ 入站上下文恢复 | 有 `readFrom` | 生命周期不安全，可能残留旧认证上下文 |
| Spring 本地事件 | 通过信封发布 | 不符合 Spring 类型分派习惯，应直接发布业务事件 |
| RocketMQ 同步、异步、顺序、精确延迟 | 已实现能力校验和发送分支 | 适合非关键、尽力而为通知 |
| 事务提交后远程发送 | 显式 `afterCommit()` | DB 提交后发送失败不可恢复，不适合订单/支付等可靠集成事件 |
| 批量远程发送 | 循环逐条发送 | 可能部分成功，无批次结果和恢复语义 |
| MySQL/Redis/Elasticsearch/MongoDB | 依赖聚合 | 适合作为依赖别名，不是组织级 starter 能力 |
| BOM 的 Maven 消费 | 已验证通过 | 可用 |
| BOM 的 Gradle 消费 | 显式 clean 后通过 | 根任务不 clean，残留结果可导致 EOF/NoSuchFileException，复现性不足 |
| API/ABI 兼容检查 | 仅写状态文件 | 未实现真实兼容性验证 |

对于 REMOTE 事件，必须在文档中明确两档使用边界：

- 非关键通知、缓存失效、可重建信号：当前 best-effort transport 可用。
- 订单、支付、库存扣减、审计等不可丢事件：应用必须提供 transactional outbox、CDC 或其他可恢复 transport；当前实现不提供该保证。

## 6. 测试与发布门禁评价

### 6.1 测试缺口

- `wuli3-core` 的 error、stream 公共契约缺少直接单元测试。
- 本地事件测试只验证信封交给 publisher，没有真实 `@EventListener` / `@TransactionalEventListener` 类型分派测试。
- Web 缺少敏感 header、敏感参数、校验原值不回显、可信代理 CIDR、业务 Jackson Module 共存测试。
- Context propagation 缺少线程复用下的无认证/非法认证覆盖测试。
- 四个数据 starter 的 context-load 测试没有验证任何业务行为。
- 没有 RocketMQ broker、数据库或缓存容器级集成测试；当前只证明编码与调用分支，不证明真实中间件兼容性。

当前统一 JaCoCo 行覆盖阈值为 45%，对基石项目偏低，而且空自动配置会稀释指标意义。应优先补关键契约和边界测试，再按模块设置更有意义的阈值。

### 6.2 发布验证缺口

`verifyBomConsumers` 的 Maven 消费者本次通过。Gradle 消费者首次和重复执行分别出现 `EOFException`、`NoSuchFileException`，显式执行 `clean test` 后通过，随后 `verifyGradleBomConsumer` 通过。这说明发布物基本可消费，但验证任务受旧 `build/test-results` 影响，不具备稳定复现性。

`build.gradle.kts:60` 的 `apiCompatibilityCheck` 只写入：

```text
status=baseline-created
comparison=skipped-no-published-baseline
```

它不是 API/ABI 比较，却在 README 中作为 CI 门禁展示。首个未发布版本可以没有历史基线，但任务应改名为状态/baseline 任务；建立首个发布基线后，应接入真实兼容性工具。

## 7. 建议实施顺序

### 阶段 0：恢复可信构建

1. 解耦 Lombok、SLF4J API 和测试日志实现，消除 Spring 测试的 Logback 冲突。
2. 完成可信代理 CIDR 生产实现，使未跟踪安全测试能够编译和通过。
3. 修复三个事件模块的格式门禁。
4. 让 Gradle 消费者使用隔离/清理后的输出目录。
5. 重新执行 `clean check`、`verifyBomConsumers` 和 warning-as-error Javadoc。

### 阶段 1：收敛安全边界

1. 缩小 `WebContext`，移除原始 headers、parameters、query 和完整 URL。
2. 校验错误不再返回 rejected/invalid value。
3. 告警上下文删除 query string，并设置合理的默认告警条件。
4. 为 `ContextTransmitter` 增加作用域式入站恢复，防止身份残留。
5. Web Jackson 模块改为增量注册并增加业务 Module 共存测试。

### 阶段 2：简化事件与 starter

1. 本地事件直接使用 Spring 原生类型；远程事件保留独立信封和发布端口。
2. 明确 best-effort 与可靠事件的业务边界，不扩大当前实现的保证。
3. 删除空数据自动配置、preview 生产代码和未使用/伪公开 JSON API。
4. 收紧 `internal` 可见性和公共 API 清单。

### 阶段 3：补齐业务接入闭环

1. 根据真实项目需要提供 HTTP client/Feign、Spring async、MQ consumer 等适配器，不在 core 中预埋框架依赖。
2. 增加代表性业务消费者和真实中间件容器测试。
3. 首次发布后建立真实 API/ABI 兼容基线。

## 8. 最终判断

当前项目不是“整体设计失败”，而是已经形成了较好的模块骨架，但公共边界尚未收敛到可稳定复用的程度。最需要避免的是继续增加通用抽象或 starter 功能：先恢复构建可信度，修复 Web/上下文安全边界，再拆开本地与远程事件语义，最后删除没有行为价值的占位结构。

达到以下停止条件后，才适合把项目定义为可供业务工程稳定复用的基础版本：

- 全量 `clean check` 通过，且不依赖任务缓存掩盖测试问题。
- Maven、Gradle 消费验证可重复通过。
- Web 敏感数据、可信代理和上下文身份残留测试通过。
- 本地 Spring 事件和远程集成事件有独立、清晰的使用契约。
- README 与实际保证一致，API 兼容门禁不再是占位实现。
