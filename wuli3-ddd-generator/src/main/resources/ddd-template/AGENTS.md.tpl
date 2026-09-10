# {{service}} 项目协作规范

本文件适用于本项目的开发者和 AI/Agent。修改前先阅读相关模块的现有实现与测试，保持改动聚焦、可审查、可回退。

## 项目结构与模块边界

本项目是 JDK 21 + Gradle Wrapper 的单体多模块 DDD 服务，Java 基础包名为 `{{basePackage}}`，初始领域为 `{{domain}}`。
根目录 `settings.gradle.kts` 声明模块，源码位于各模块 `src/main/java`，测试位于 `src/test/java`，资源位于 `src/main/resources`。

- `shared-kernel`：共享稳定的标识、状态和领域类型，生产依赖只允许 `wuli3-core`。
- `domain`：聚合、实体和值对象，只依赖 `shared-kernel`，不引入 Spring 或基础设施实现。
- `api`：对外契约、Command/Query 和响应 DTO，只依赖 `shared-kernel`。
- `app`：用例、应用服务以及 `port.in` / `port.out`；实现位于 `app.internal`，默认保持包级可见性。
- `infra`：仓储及外部系统适配，只能使用 App 的 `app.port.out` 接口，不能依赖 App 实现或输入端口。
- `adapter`：HTTP 等入站协议，依赖 `api` 和 `app`。
- `bootstrap`：Spring Boot 启动和最终组装，依赖 `infra` 和 `adapter`。

新增能力应放入承担该职责的模块；不要绕过现有架构测试或扩大依赖白名单来掩盖模块边界问题。

## 构建风格与验证命令

使用项目自带的 Gradle Wrapper，不依赖本机全局 Gradle；Windows 使用 `gradlew.bat`。

```bash
./gradlew test
./gradlew check
./gradlew clean check
./gradlew :domain:test
./gradlew :bootstrap:bootRun
```

- `test` 运行 JUnit 测试；`check` 执行测试、格式与 Checkstyle 检查、Forbidden APIs、Error Prone、NullAway 及架构门禁。
- 修改后先运行受影响模块的针对性测试，再运行相关模块 `check`；提交前执行 `./gradlew clean check`。
- 根工程统一应用 `com.kjs.wuli3.java-conventions`，模块只声明业务依赖；`bootstrap` 额外应用 Spring Boot 插件。
- 不在模块中重复配置 Java 版本、测试框架或公共质量规则，不通过关闭门禁解决检查失败。
- 依赖仓库统一配置在 `settings.gradle.kts`，依赖版本优先使用 Wuli3 BOM；BOM 坐标与版本通过根目录 `gradle.properties` 管理，约定插件版本在根构建文件管理。
- 新增依赖需说明必要性，优先复用项目已有能力；不要在各模块散落重复版本声明。

## 编码风格与设计约束

- Java 包名使用 `{{basePackage}}` 前缀，遵循现有目录、命名和构建约定。
- 围绕真实职责与稳定语义设计公共 API、扩展点和错误模型；职责混杂、命名误导或抽象不成立时，先修正边界再叠加功能。
- 优先使用已有工具与模式，避免无实际需求的抽象层；抽取接口时明确能力的意义、调用方和实现边界。
- 创建后不会重新赋值的字段、参数和局部变量必须使用 `final`；仅在重新赋值、延迟初始化或框架绑定确有需要时例外。
- 实例方法访问同类实例字段或方法时显式使用 `this`；静态成员调用使用类名限定。
- 生产代码使用 `java.time`，避免 `java.util.Date`、`Calendar` 和 `java.sql.Date/Time/Timestamp`。
- 使用 JSpecify 表达空安全语义；NullAway 检查包前缀通过 `wuli3.conventions.nullaway.annotated-packages` 配置为 `{{basePackage}}`，新增包应保持覆盖。
- 公共 API、跨模块约定、关键扩展点及容易误用的方法需要说明边界和设计原因。

## 测试指南

- 使用 JUnit Jupiter 和 AssertJ，测试放在对应模块 `src/test/java`，类名以 `Test` 结尾。
- 新增公共 API、领域规则、错误处理、事件行为和装配逻辑时，补充对应单元测试或 Spring 上下文测试。
- 测试覆盖可观察行为、边界值和失败路径，避免只验证实现细节。
- 修复缺陷时补充能够复现问题的回归测试；重构前确认现有行为有测试保护。
- 保留 `SharedKernelArchitectureTest`、`InfraArchitectureTest` 和 `verifySharedKernelDependencies` 门禁。
- 涉及数据库或消息中间件的测试应明确环境前提；普通单元测试不依赖开发者机器上的外部服务。
- 验证失败时修复原因；无法运行的检查需明确说明阻塞原因与未验证范围，不得声称已通过。

## 提交与 Pull Request 规范

提交信息采用简短的 Conventional Commit 风格，例如 `feat: 增加订单状态查询`、`fix: 修正状态转换校验`、`test: 补充领域边界测试`。
可使用 `feat:`、`fix:`、`chore:`、`test:`、`docs:`、`refactor:` 等前缀；一次提交聚焦一个明确目的。

PR 说明变更目的、影响模块、验证命令与结果。依赖升级需说明原因和兼容性影响；公共契约或运行配置变化需说明迁移方式。

## Git 使用与 Agent 工作约定

- 修改前检查 `git status --short` 和相关 diff，识别已有改动；不要覆盖或回退他人的工作。
- 保持变更范围聚焦，不夹带无关格式化、重构或本机配置。
- AI/Agent 创建或修改且确认需要版本管理的文件，应执行 `git add <path>`，只暂存本次产生的改动。
- 同一文件存在他人修改时，按变更块选择性暂存，避免整文件带入无关内容；不要直接使用 `git add .` 或 `git add -A` 暂存所有改动。
- 暂存后检查 `git diff --cached`，确认只包含本次预期变更；提交前核对验证结果。
- 未经明确授权，不执行丢弃改动、重写提交历史或强制推送等破坏性操作。
- 完成后说明修改文件、行为变化、验证结果和剩余风险。

## 注释使用说明

注释使用中文，说明代码无法直接表达的意图、边界和使用约束。避免重复名称、类型、赋值、取值或显而易见的流程。

### Java 文件头部注释

必须用一句话说明文件职责或用途，并标明作者的 Git 用户名和创建时间，时间格式为 `yyyy/M/d HH:mm`。
新增文件可通过 `git config user.name` 获取作者；已有文件保留原始作者和创建时间。按需使用 `注意：` 说明边界，使用 `使用方式：` 说明固定用法。

```java
/**
 * {文件职责或用途}。
 *
 * 注意：{限制、前置条件或兼容性说明}。
 * 使用方式：{固定或特殊用法}。
 *
 * @author {git username} create on {yyyy/M/d HH:mm}
 */
```

### 方法注释

必须简洁说明方法职责、可观察行为或业务语义。按需说明前置条件、幂等性、线程安全及调用约束。
参数名称和类型不足以表达语义时添加 `@param`；返回值有特殊含义或空值语义时添加 `@return`；调用方需要处理特定异常时添加 `@throws`。

```java
/**
 * {方法职责或行为}。
 *
 * 注意：{前置条件、边界或调用约束}。
 *
 * @param parameterName {参数语义}
 * @return {返回值语义}
 * @throws ExceptionType {异常触发条件}
 */
```

### 字段注释

字段名和类型无法充分表达语义时添加注释，说明取值范围、单位、生命周期或并发约束；配置字段说明默认值、生效条件及配置关系。
显而易见的普通字段不添加重复说明。

```java
/** 最大重试次数；取值范围为 0 至 3。 */
private final int maxRetryTimes;
```

## 配置与安全

不要提交私有仓库凭据、数据库口令、访问密钥、本机绝对路径或 IDE 私有配置。运行环境的连接和认证信息通过外部配置提供。
