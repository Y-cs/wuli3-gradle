# wuli3-ddd-generator 使用指南

## 用途

`wuli3-ddd-generator` 同时提供 Gradle 插件和独立 Java CLI，用于生成单体多模块 DDD 服务。生成结果是普通 Gradle 工程，生成完成后不依赖生成器运行。

仓库内的完整生成结果位于 [`examples/order-service`](../examples/order-service)，可以直接查看模块结构。

## Gradle 任务

推荐通过 Gradle 插件提供的强类型任务生成服务：

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("com.kjs.wuli3.ddd-generator") version "0.1.0-SNAPSHOT"
}
```

```bash
./gradlew generateDddService \
  --service order-service \
  --base-package com.example.order \
  --domain order \
  --persistence mysql \
  --messaging rocketmq \
  --output ./services
```

`generateDddService` 是一次性脚手架任务，每次执行都会检查目标服务目录；目录非空时任务失败，不会覆盖业务代码。未传 `--output` 时，服务生成到应用该插件的根工程目录。

命令需要在应用了 `com.kjs.wuli3.ddd-generator` 插件的 Gradle 工程根目录执行，而不是在 Wuli3 源码仓库根目录直接执行。前提条件如下：

- 使用 JDK 21；
- 工程包含可执行的 Gradle Wrapper；
- `pluginManagement.repositories` 能解析插件 marker；本地开发可以使用 `mavenLocal()`；
- Wuli3 BOM、Starter 和 `build-logic` 版本已经发布到配置的仓库；本地开发也可以先发布到 Maven Local。

## CLI

在发布了 Wuli3 构件和 `build-logic` 插件的环境中执行：

```bash
java -cp wuli3-ddd-generator.jar com.kjs.wuli3.generator.DddGenerator generate \
  --service order-service \
  --package com.example.order \
  --domain order \
  --persistence mysql \
  --messaging rocketmq \
  --output ./services
```

参数说明：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `--service` | 是 | 小写服务名，可包含数字和连字符。 |
| `--package` | 是 | 小写 Java 基础包名。 |
| `--domain` | 是 | 领域名，可包含数字和连字符。 |
| `--persistence` | 否 | `none`（默认）或 `mysql`。 |
| `--messaging` | 否 | `none`（默认）、`rocketmq` 或 `rabbitmq`。 |
| `--wuli3-version` | 否 | Wuli3 BOM 和 starter 版本，默认 `0.1.0-SNAPSHOT`。 |
| `--build-logic-version` | 否 | Wuli3 约定插件版本，默认 `0.1.0-SNAPSHOT`。 |
| `--output` | 否 | 生成父目录，默认当前目录。 |

Gradle 任务使用 `--base-package`，其余参数与 CLI 一致。

目标服务目录必须不存在或为空，生成器不会覆盖已有业务文件。

## 模块边界

生成工程包含七个 Gradle 模块：

```text
shared-kernel -> domain / api -> app -> infra
                              app -> adapter
                    infra / adapter -> bootstrap
```

- `shared-kernel` 只依赖 `wuli3-core`，放置跨领域共享的状态、标识和类型约束。
- `domain` 保存聚合、实体和值对象，只依赖 `shared-kernel`。
- `api` 保存对外 Facade、Command/Query 和响应 DTO，只依赖 `shared-kernel`。
- `app` 保存用例、应用服务以及 `port.in` / `port.out`。
- `infra` 依赖 `app` 和 `domain`，但只能使用 `app.port.out`，实现仓储和外部系统适配。
- `adapter` 依赖 `api` 和 `app`，负责 HTTP 等入站协议。
- `bootstrap` 依赖 `infra` 和 `adapter`，只负责 Spring Boot 启动和组装。

根工程统一向子模块应用 `com.kjs.wuli3.java-conventions`，由该约定提供 Java 21、Wuli3 BOM、
测试依赖、JaCoCo 和静态质量门禁。各模块构建文件只声明自己的业务依赖；`bootstrap` 再额外应用
`org.springframework.boot`。`com.kjs.wuli3.spring-conventions` 面向 Spring Boot Starter 和自动配置模块，
不会应用到 `domain`、`api` 或 `shared-kernel`。

## 架构门禁

生成工程使用两类互补门禁：

- `shared-kernel` 的 `verifySharedKernelDependencies` Gradle 任务检查生产依赖声明，只允许 `wuli3-core`；
- `SharedKernelArchitectureTest` 使用 ArchUnit 检查 Shared Kernel 字节码只能依赖自身、JDK 和 `wuli3-core`；
- `InfraArchitectureTest` 使用 ArchUnit 的依赖白名单检查 `infra` 字节码依赖。

`InfraArchitectureTest` 的规则如下：

- 允许 `infra`、`domain`、`sharedkernel` 和 `app.port.out`；
- 要求 `app.port.out` 只能包含接口，避免 Infra 实例化 App 类型；
- 允许必要的 JDK、Spring、MyBatis 类型；
- 禁止 `app.internal`、`app.service` 或 `app.port.in`；
- `new`、字段、方法参数、静态调用和继承等依赖形式都会被检查。

因此不建议使用 `forbidden-apis` 实现该规则。`forbidden-apis` 适合固定类或方法黑名单，不能表达“App 只有输出端口可被 Infra 依赖”的包级白名单。应用服务实现默认放在 `app.internal` 且保持 package-private，形成 Java 可见性和 ArchUnit 的双重约束。

## 验证

```bash
./gradlew check
./gradlew :bootstrap:bootRun
```

MySQL 选项会额外生成 MyBatis-Plus PO/Mapper；消息选项会加入对应 Wuli3 消息 starter，未选择的基础设施不会生成。
消息中间件的连接和认证配置不会由生成器猜测，请按对应 starter 文档补充 `application.yml`。
