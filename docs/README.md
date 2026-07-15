# Wuli3 项目分析与演进文档

## 文档目的

本组文档以 2026-07-10 的分析基线为起点，系统说明项目的实现方式、设计边界、已确认风险、修复过程和后续方向。主要读者是项目维护者、架构负责人、starter 开发者及业务接入团队。

01-05 记录修复前的事实与判断，06 是当时的实施计划，07 记录后续完成状态。历史文档中的内存事件总线分析已被当前
[`wuli3-event-core` 说明](../wuli3-event-core/README.md)取代。公共 API、配置和模块行为仍以源码与发布版本为准。

## 推荐阅读顺序

1. [项目全景与事实基线](01-project-overview.md)：模块关系、成熟度、构建与测试现状。
2. [实现机制与设计思路](02-implementation-analysis.md)：Core、JSON、上下文、事件、Web 与 starter 的运行机制。
3. [设计评价与改进建议](03-design-assessment.md)：应保留设计、P0-P3 问题矩阵及证据化建议。
4. [分阶段演进路线](04-evolution-roadmap.md)：从恢复基线到生产能力扩展的阶段顺序。
5. [可执行行动清单](05-action-checklist.md)：可以直接拆成 Issue/提交的实施步骤和验收方式。
6. [问题修复实施计划](06-remediation-plan.md)：锁定方案、PR 依赖顺序、兼容策略、测试矩阵与阶段关口。
7. [修复实施结果](07-remediation-result.md)：本轮实际完成项、破坏性变化、验收接口和剩余发布前置。

## 核心结论

- 项目已形成“纯 Java 公共语义与事件模型 -> JSON/上下文机制 -> Spring 运行时适配”的合理依赖主干。
- Core 的错误策略、半开时间区间、最小 ID/Clock 端口，以及上下文的协议无关 carrier/codec 值得保留。
- 全量 `clean check` 已恢复，未知 `@ResourcePath` type 序列化缺陷已修复，JaCoCo 报告和覆盖率门禁已并入 `check`。
- 共享可变 `JsonMapper` 入口已删除，上下文快照语义已形成显式公共契约；本地事件统一使用 Spring 原生机制。
- Web starter 已收紧请求体缓存和外部 requestId 默认策略，并按 JSON、Context、Error、Response 拆分自动配置边界。
- MySQL、Redis、RocketMQ、Elasticsearch、MongoDB 模块当前是依赖聚合/占位 starter，不应被描述为已有项目级增强。
- BOM 与 10 个 Java 组件已具备统一 publication，并通过隔离 Gradle/Maven consumer 验证；真实 API/ABI 对比仍需首次正式发布版本作为 baseline。

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
