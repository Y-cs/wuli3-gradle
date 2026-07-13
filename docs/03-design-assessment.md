# 设计评价与改进建议

## 1. 结论口径

优先级定义：

- **P0**：当前正确性、安全性或交付基线已被阻断。
- **P1**：公共契约、架构边界或发布能力存在近期必须治理的风险。
- **P2**：测试、一致性、维护成本或开发体验问题。
- **P3**：需求成立后再考虑的增强方向。

成本使用 S/M/L 表示相对改动规模，不代表工期。本文只提出后续改造，不在本次文档任务中修改实现。

## 2. 应保留的设计

1. **依赖方向清晰**：Core 不依赖 Spring；JSON、事件和上下文可独立使用；Web 作为协议适配层消费它们。
2. **契约与实现分离**：事件 core 与 in-memory 实现分模块；上下文 carrier/codec 不依赖 Servlet 或具体 RPC。
3. **错误语义分层**：错误身份、策略、异常载体、HTTP 状态和外部响应分别位于正确层级。
4. **Spring 默认值可替换**：Web 的 resolver、notifier、上下文组件大多通过 Bean 扩展点退让。
5. **质量规则集中**：JDK、NullAway、Forbidden APIs、格式、静态分析和覆盖率由约定插件统一配置。
6. **对不成熟能力保持克制**：事件 core 不伪装成可靠消息系统，数据类 starter 没有提前创造无真实边界的通用 Repository。

## 3. 发现总表

| 编号 | 优先级 | 类型 | 发现 | 影响 | 成本 |
| --- | --- | --- | --- | --- | --- |
| F-01 | P0 | 已确认问题 | 当前 `./gradlew check --continue` 有 5 个失败任务 | 无法形成可信发布和回归基线 | M |
| F-02 | P2 | 设计取舍 | `ErrorCode` 的 enum-only 约束只能运行期执行 | 类型系统不能阻止错误实现 | S |
| F-03 | P1 | 并发/状态 | 默认 `JsonMapper` 作为公共可变单例暴露 | 任一消费者可改变全局序列化行为 | M |
| F-04 | P1 | 生命周期 | 上下文快照只有容器级浅复制语义 | 可变扩展可能跨快照相互影响 | M |
| F-05 | P1 | 契约缺口 | 内存事件总线未定义顺序、订阅与关闭语义 | 调用方依赖偶然实现行为 | M |
| F-06 | P1 | 模块定位 | 五个数据/中间件 starter 仅为依赖聚合和空配置 | artifact 名称容易让接入方高估能力 | S |
| F-07 | P1 | 发布治理 | 普通库和 starter 没有 Maven publication | BOM 无法与组件形成外部消费闭环 | L |
| F-08 | P1 | Web 默认策略 | 请求体缓存与外部 requestId 接受默认开启 | 增加内存成本与信任边界风险 | M |
| F-09 | P2 | 自动配置 | Web 单一自动配置聚合多个关注点 | 条件复杂、局部关闭和测试成本上升 | M |
| F-10 | P2 | 配置安全 | 配置属性缺少启动期约束 | 非法配置延迟到请求期暴露 | S |
| F-11 | P2 | 依赖治理 | Context/Event/聚合 starter/Web 存在可替代或未使用的依赖声明 | 传递依赖、冲突和扫描成本增加 | S |
| F-12 | P2 | 测试治理 | 测试数量与公共 API 风险不匹配 | 错误、并发和失败路径容易回归 | L |
| F-13 | P2 | 构建治理 | included build 与主构建发布边界未形成闭环 | 配置易被误认为隐式共享 | M |
| F-14 | P2 | 升级准备 | Gradle 9.6 构建报告弃用功能 | Gradle 10 升级存在未知阻断 | S |
| F-15 | P1 | 自动配置缺陷 | 默认 `ErrorCodeResolver` Bean 不退让 | 用户替换时可能形成同类型注入歧义 | S |
| F-16 | P2 | 可移植性 | 根配置提交本机 JDK 绝对路径 | 构建配置泄漏个人环境并误导接入者 | S |
| F-17 | P2 | 空安全契约 | 若干公共包缺少 `@NullMarked` | 外部消费者无法获得完整 JSpecify 默认语义 | S |
| F-18 | P0 | 正确性缺陷 | 未支持的 `@ResourcePath` type 序列化时不写 token | Web 响应可能成为非法 JSON | S |

## 4. 高优先级建议

### F-01：恢复全量质量基线

**证据**：2026-07-10 在 `b150bbc` 执行 `./gradlew check --continue`，共有 5 个失败任务：Core 测试编译失败；Core、JSON、Web 格式失败；Web 的 39 个测试中 6 个失败。Web 失败中 5 个是 `SystemErrors` 的 MESSAGE_ONLY 策略与旧测试期望漂移，另 1 个是 F-18 的非法 JSON 缺陷。

**建议**：保留操作手册已经明确的 `Asserts` 链式 API，删除漂移的返回值测试；统一应用格式；保持 MESSAGE_ONLY 生产策略并校正 Web 测试；修复 F-18。完成前不得开始公共 API 扩展。

**验收**：`./gradlew clean check` 成功，且断言 API 的选择在 Javadoc、测试和操作手册中一致。

### F-18：保证未知资源类型仍生成合法 JSON

**证据**：`ResourcePathJsonSerializer` 只在 resolver 支持或 type 为 `default` 时写出 String；未知非默认 type 直接 return。Web 实测响应出现 `{"data":{"path"}}`，JSON parser 报错。

**建议**：与反序列化行为保持对称，resolver 不支持时原样写出字符串。新增 JSON 模块回归测试并在 Web 集成测试验证响应可解析。

**验收**：任意未知 type 的 String 均原样往返；非 String 仍清晰失败；所有 Web JSON 可由标准 parser 解析。

### F-03：收敛共享 JSON 状态

**证据**：`JacksonProvider.defaultJsonMapper()` 返回静态共享且可配置的 `JsonMapper`；`Jsons` 使用同一实例。

**建议**：立即在 API 文档声明调用方不得修改；应用集成优先使用 `JsonMapperFactory` 创建独立实例。后续新增只读导向的 reader/writer API，再弃用直接返回共享 Mapper 的入口。不要突然返回 copy，否则可能改变性能和模块注册预期。

**验收**：并发使用、独立工厂隔离、标准 feature/module 一致性有测试；文档明确实例所有权。

### F-04：定义上下文快照隔离等级

**证据**：`ContextContainer.copy()` 复制 Map 结构，但 `AuthContext`、`InvocationContext` 继承的扩展 Map 仍位于原对象内；快照保存相同上下文对象引用。

**建议**：优先把传播上下文设计为不可变值，扩展项采用创建新上下文的 copy-on-write 方式。若必须支持可变上下文，则为 `Context` 引入显式快照复制协议，并验证自定义类型。文档在改造前明确当前是浅复制。

**验收**：捕获后修改原上下文扩展不会影响快照；嵌套 scope、异常退出和线程池复用后均恢复/清理正确。

### F-05：固化事件总线语义

**证据**：`publish` 并发提交全部匹配 handler；接口只有 `register`，没有反注册句柄；实现接收外部 Executor，不拥有其关闭权；当前测试只覆盖单处理器成功和失败。

**建议**：先用 Javadoc 定义“无顺序保证、至少一次进程内调用/或准确措辞、失败聚合方式、Executor 所有权、无 handler 行为”。新增多 handler、父类型匹配、重复注册、并发注册/发布和部分失败测试。动态反注册只有真实需求出现时通过 `Subscription extends AutoCloseable` 新 API 引入。

**验收**：所有声明语义均有测试；调用方不需要阅读实现判断是否等待完成或关闭 Executor。

### F-06：诚实表达 Starter 成熟度

**证据**：MySQL、Redis、RocketMQ、Elasticsearch、MongoDB 自动配置类均为空，测试只验证上下文可加载；实际价值来自 `api` 暴露的第三方 starter。

**建议**：README、发布 POM description 和模块矩阵标记为“依赖聚合”。若不准备独立演进，可考虑以 BOM + 官方 starter 使用说明替代空 artifact；若保留，则为每个模块定义能力准入条件，不为“看起来完整”增加无意义 Bean。

**验收**：接入方能从文档准确判断提供与未提供的行为；新增任何默认行为都具备属性、退让条件和 `ApplicationContextRunner` 测试。

### F-07：建立可消费的发布闭环

**证据**：主构建只有 `wuli3-dependencies` 应用 `maven-publish`；Java 库和 starter 没有 publication。`build-logic` 的公司仓库配置属于独立 included build，不会作用于主构建。

**建议**：新增“可发布 Java 组件”约定，仅由对外模块应用；生成源码/Javadoc、POM 元数据并统一仓库路由。先发布到临时目录 Maven 仓库，再用独立 Gradle/Maven consumer 验证 BOM 与组件；通过后才接正式仓库。

**兼容性**：artifact 坐标、依赖 scope 和 Gradle Module Metadata 一经发布就是公共契约，应先冻结命名。

**验收**：独立消费者仅导入 BOM 即可无版本引用所有目标组件，且 POM/API 依赖传播符合设计。

### F-08：重新评估侵入式 Web 默认值

**证据**：`requestBodyCacheEnabled` 和 `acceptExternalRequestId` 默认均为 true；过滤器对 JSON、表单和 text body 最多缓存 1 MiB。

**建议**：先补内存边界、超限、编码、异步请求和 requestId 日志注入测试；明确可信代理/网关模型。对于通用基础 starter，更保守的目标是请求体缓存默认关闭、外部 ID 只接受受控字符集；如改变默认值，应提供版本迁移说明和显式旧行为配置。

**验收**：默认行为有安全说明；非法/超限输入失败可定位；高并发下内存上限可估算。

### F-15：让默认错误码解析器真正退让

**证据**：`WebAutoConfiguration.webErrorCodeResolver(...)` 没有 `@ConditionalOnMissingBean`，而 `ApiResponseFactory` 按接口注入 `ErrorCodeResolver`。其他 resolver 默认 Bean 均支持退让，类注释也声明默认值可替换。

**建议**：按接口类型增加退让条件，并用 `ApplicationContextRunner` 验证默认 Bean、用户自定义 Bean、多个候选 Bean 三种场景。若确实要求固定 Web 实现，则应移除“可替换”承诺并用 qualifier 明确选择；当前设计更适合前一种方案。

**验收**：应用提供一个自定义 `ErrorCodeResolver` 时上下文只有预期候选，`ApiResponseFactory` 使用该实现。

## 5. 中优先级建议

### F-09/F-10：拆分 Web 自动配置并校验属性

将 Jackson、Context、Response、Error 拆为内部自动配置单元，入口通过 imports 加载，并用 `before/after` 表达顺序。保留现有公开扩展接口和配置前缀。为 header 名、requestId 长度、body 大小等增加启动期约束，错误信息包含完整属性键。

验收应覆盖：默认装配、每个开关关闭、用户 Bean 覆盖、非法属性启动失败、缺少可选类时不误装配。

### F-11：压缩基础依赖面

源码扫描显示：Core 已无第三方运行时库；上下文模块只用 Guava 的并发 Map 工厂，可直接用 JDK；Event Core 和五个聚合 starter 暴露了没有源码用法的 Core 依赖；Web 的 Commons/Hutool/Guava 等声明中只有 Commons Lang 有直接使用；JSON 的 Hutool 用于标准脱敏策略。应按源码和发布 POM 逐项清理，尤其避免 `hutool-all` 这类聚合依赖向消费者泄漏。

验收应使用 `dependencies`/`dependencyInsight` 与发布 POM，而不只以编译通过为准。

### F-12：按风险建立测试矩阵

当前测试类数量不能替代契约覆盖。优先补：

- Core 错误元数据与 Stream 聚合的直接测试。
- JSON 注解组合、不支持类型、共享/独立 Mapper 隔离和失败信息。
- 上下文可变扩展、线程池异常退出、codec 冲突与不可信输入。
- 事件多 handler、并发、父类型匹配和失败聚合。
- Web 自动配置关闭/覆盖、过滤器边界、响应 converter 矩阵和 notifier 隔离。
- 发布物与 BOM 的独立消费测试。

覆盖率阈值可以保留为最低线，但公共契约的场景矩阵比单一百分比更重要。

### F-13/F-14：补构建治理测试和升级准备

为约定插件增加 Gradle TestKit，验证普通 Java/Spring 模块获得的工具链、依赖和任务连接。使用 `--warning-mode all` 记录弃用来源并清零。明确主构建与 included build 各自的仓库、版本和发布职责，不依赖隐式继承。

### F-02/F-16/F-17：补齐类型、环境和空安全契约

- `ErrorCode` 已在 Javadoc 明确 enum-only，应补默认策略、字段覆盖和非法非 enum 的测试；除非出现动态错误码需求，不必为了编译期表达而重写模型。
- 删除根 `gradle.properties` 的用户绝对 JDK 路径，保留 toolchain 版本，由 mise、`JAVA_HOME`、Gradle 自动发现或用户级配置提供安装位置。
- 为 Context 的 `accessor/context/store`、JSON 的 `provider`、Web 的 `config.properties/json` 等公共包补 `@NullMarked`，逐个核对真实可空参数；不要只依赖 NullAway 的包前缀配置代替发布 API 注解。

## 6. 暂不建议推进的方向

- 不在没有可靠投递需求时把内存事件总线扩成通用 MQ 框架。
- 不在没有两个以上稳定场景时向 Core 增加字符串、Bean、IO 万用工具。
- 不为五个空 starter 预造统一 Repository、Client 或跨技术配置模型。
- 不在认证边界未明确前传播公网传入的用户身份 header。
- 不在发布闭环和 API 兼容策略建立前承诺 1.0 稳定版。
