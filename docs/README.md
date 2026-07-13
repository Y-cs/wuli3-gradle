# Wuli3 项目分析与演进文档

## 文档目的

本组文档基于 2026-07-10 的当前工作区，系统说明项目的实现方式、设计边界、已确认风险和后续方向。主要读者是项目维护者、架构负责人、starter 开发者及业务接入团队。

文档只描述和评估当前代码，不表示其中建议已经实施。公共 API、配置和模块行为仍以源码与发布版本为准。

## 推荐阅读顺序

1. [项目全景与事实基线](01-project-overview.md)：模块关系、成熟度、构建与测试现状。
2. [实现机制与设计思路](02-implementation-analysis.md)：Core、JSON、上下文、事件、Web 与 starter 的运行机制。
3. [设计评价与改进建议](03-design-assessment.md)：应保留设计、P0-P3 问题矩阵及证据化建议。
4. [分阶段演进路线](04-evolution-roadmap.md)：从恢复基线到生产能力扩展的阶段顺序。
5. [可执行行动清单](05-action-checklist.md)：可以直接拆成 Issue/提交的实施步骤和验收方式。
6. [问题修复实施计划](06-remediation-plan.md)：锁定方案、PR 依赖顺序、兼容策略、测试矩阵与阶段关口。

## 核心结论

- 项目已形成“纯 Java 公共语义 -> JSON/上下文/事件机制 -> Spring Web 适配”的合理依赖主干。
- Core 的错误策略、半开时间区间、最小 ID/Clock 端口，以及上下文的协议无关 carrier/codec 值得保留。
- 当前最高优先级是恢复失败的全量 `check`，而不是继续增加基础工具或 starter 功能。
- 当前还存在一个确定性 JSON 缺陷：未知 `@ResourcePath` type 会漏写值并生成非法 JSON，已在修复计划中列为 F-18/P0。
- `ErrorCode` 的接口承诺、共享可变 `JsonMapper`、上下文浅复制和事件总线未声明语义是近期公共契约治理重点。
- Web starter 已有真实且较完整能力，但请求体缓存、外部 requestId 等默认策略需要更严格的资源与信任边界。
- MySQL、Redis、RocketMQ、Elasticsearch、MongoDB 模块当前是依赖聚合/占位 starter，不应被描述为已有项目级增强。
- 当前只有 BOM 具备 Maven publication；组件发布、独立消费验证和 API 兼容检查尚未形成闭环。

## 优先级

- `P0`：当前正确性、安全性或交付基线阻断。
- `P1`：公共契约、架构边界或发布能力的近期风险。
- `P2`：一致性、测试、维护成本与开发体验。
- `P3`：只有真实需求成立后再进入实现的候选方向。

## 相关模块文档

- [Core 操作手册](../wuli3-core/docs/operation-manual.md)
- [上下文传播说明](../wuli3-context-propagation/README.md)
- [事件 Core 说明](../wuli3-event-core/README.md)
- [Web Starter 说明](../wuli3-web-spring-boot-starter/README.md)
- [Web 统一响应说明](../wuli3-web-spring-boot-starter/docs/response-handling.md)
- [依赖 BOM 说明](../wuli3-dependencies/README.md)
- [构建约定说明](../build-logic/README.md)

## 维护规则

当模块职责、公共 API、自动配置默认值、质量门禁或发布流程变化时，应同步更新对应分析和路线图。已完成建议从行动清单移除前，需要在变更记录或 ADR 中保留最终决策及原因。
