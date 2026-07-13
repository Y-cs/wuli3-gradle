# 可执行行动清单

## 1. 使用方式

每个任务应拆成独立 Issue 或提交。除纯文档任务外，每项都要同时提交测试；标有“兼容”的任务必须在 PR 中说明源码、二进制和行为影响。

## 2. 阶段 0：立即处理

| 顺序 | 任务 | 影响模块 | 实施要点 | 验证 |
| --- | --- | --- | --- | --- |
| 0.1 | 恢复 `Asserts` 契约 | core | 保留链式 API，删除返回值漂移测试 | `:wuli3-core:check` |
| 0.2 | 恢复格式基线 | core/json/web | 格式化 Spotless 报告文件，确认无行为 diff | 各模块 `spotlessCheck` |
| 0.3 | 修复 JSON/Web 基线 | json/web | 未支持资源 type 原样输出；MESSAGE_ONLY 测试对齐既有策略 | 两模块 `check` |
| 0.4 | 恢复全量检查 | 全部 | 使用 `--continue` 确认无被首个失败遮挡的问题 | `clean check --continue` |
| 0.5 | 修复 Gradle 弃用项 | build-logic/根构建 | 改用 `DependencyHandler.project` 创建 BOM 依赖 | `help --warning-mode fail` |
| 0.6 | 移除本机 JDK 路径 | 根构建 | 删除仓库级绝对路径，保留 Java 21 toolchain 声明 | 干净环境运行 `javaToolchains` |
| 0.7 | 校正文档成熟度 | 五个 starter | 明确“依赖聚合/占位”及未提供能力 | 人工审阅链接和 POM 描述 |

## 3. 阶段 1：公共契约收敛

| 顺序 | 任务 | 影响模块 | 实施要点 | 验证 |
| --- | --- | --- | --- | --- |
| 1.1 | 错误码契约测试 | core | 保护既有 enum-only 取舍，覆盖默认、字段覆盖、非法模块和非 enum | `:wuli3-core:test` |
| 1.2 | 收敛共享 Mapper | json | 记录所有权；新增独立/只读入口；旧入口兼容弃用 | JSON 并发与隔离测试 |
| 1.3 | 上下文不可变或复制协议 | context | 防止扩展 Map 跨快照共享；保留嵌套 scope | 快照、线程池、异常退出测试 |
| 1.4 | 固化事件语义 | event-* | Javadoc 定义顺序、失败、Executor 所有权；扩展测试 | 多 handler/并发测试 |
| 1.5 | 审计 Web 默认值 | web | 评估 body cache 与外部 requestId；设计兼容配置 | 超限、字符、并发内存测试 |
| 1.6 | 清理依赖 | context/event/聚合 starter/web | 用 JDK 替代简单工厂；删除未使用声明；核对 API/runtime scope | `dependencies`、`check` |
| 1.7 | 修复错误码解析器退让 | web | 按 `ErrorCodeResolver` 接口增加缺 Bean 条件，验证用户覆盖 | `ApplicationContextRunner` 测试 |
| 1.8 | 补 JSpecify 包契约 | context/json/web | 为遗漏公共包增加 `@NullMarked`，显式标注真实 nullable | NullAway + API 审阅 |

## 4. 阶段 2：Starter 规范化

| 顺序 | 任务 | 影响模块 | 实施要点 | 验证 |
| --- | --- | --- | --- | --- |
| 2.1 | 拆分 Web 内部自动配置 | web | JSON/Context/Response/Error 独立条件与顺序；不改公开前缀 | `ApplicationContextRunner` 矩阵 |
| 2.2 | 配置启动校验 | web | header、长度、DataSize、列表项等快速失败 | 非法属性启动失败测试 |
| 2.3 | 自动配置模板 | build-logic/docs | 定义启用、退让、覆盖、缺类、关闭测试规范 | 在一个 starter 试点 |
| 2.4 | 可观测边界 | web/context/event | 定义结构化字段和扩展点，不绑定监控实现 | 日志/metrics 契约测试 |
| 2.5 | Starter 能力评审 | 五个 starter | 每项增强提供两个复用场景和准入材料 | 架构评审记录 |

## 5. 阶段 3：发布治理

| 顺序 | 任务 | 影响模块 | 实施要点 | 验证 |
| --- | --- | --- | --- | --- |
| 3.1 | 划分发布边界 | 全部 | 列出外部 artifact、内部实现和稳定级别 | 坐标清单评审 |
| 3.2 | 公共组件发布约定 | build-logic | Maven publication、sources/Javadoc、POM 元数据 | 发布到临时仓库 |
| 3.3 | BOM 完整性校验 | dependencies | 内部坐标、版本和外部约束机器化核对 | 专用 Gradle verification task |
| 3.4 | 独立消费验证 | 测试工程 | Gradle/Maven 导入 BOM 并运行最小代码 | 空缓存 CI job |
| 3.5 | API 兼容检查 | 公共模块 | 选定基线版本；0.x 报告、1.0 阻断 | 兼容报告 |
| 3.6 | 正式仓库流水线 | 根构建/CI | 快照/正式路由、环境凭据、顺序和重试 | dry run + 受控发布 |

## 6. 后续能力模板

任何新公共能力或 starter 增强在进入实现前，都应回答：

- 消费者是谁，至少两个复用场景是什么？
- 稳定语义和明确不负责的边界是什么？
- 默认行为是否有成本或安全影响？
- 用户如何关闭、覆盖和回退？
- null、线程安全、所有权、生命周期和失败行为是什么？
- 需要哪些单元、并发、自动配置、兼容和消费测试？
- 对现有源码、二进制、配置和运行行为有何影响？
- 文档、发布说明和弃用周期如何维护？

缺少上述答案时，优先留在应用层验证，不进入基础库公共 API。

## 7. 完成定义

一项任务只有同时满足以下条件才算完成：

1. 实现与设计边界一致，没有顺带扩张职责。
2. 正常、边界和失败场景有自动化测试。
3. `check` 通过，必要时消费/兼容测试通过。
4. 公共行为、配置与迁移方式已更新文档。
5. 依赖、资源所有权和安全影响已经审阅。
6. 提交只包含该任务相关改动，可独立回退。
