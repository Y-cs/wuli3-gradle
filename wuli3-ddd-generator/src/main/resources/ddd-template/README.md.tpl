# {{service}}

由 `wuli3-ddd-generator` 模板 `{{templateVersion}}` 生成的单体多模块 DDD 服务。

根工程统一应用 `com.kjs.wuli3.java-conventions`，各模块无需重复配置 Java、测试和质量门禁；
`bootstrap` 额外应用 Spring Boot 插件生成可执行应用。

## 模块边界

```text
wuli3-core <- shared-kernel <- domain / api <- app <- infra
                                      app <- adapter
                              infra / adapter <- bootstrap
```

- `shared-kernel` 的生产依赖只有 `wuli3-core`，用于共享稳定的领域类型和状态约定。
- `domain` 保存领域模型，`api` 保存对外契约，两者只依赖 `shared-kernel`。
- `app` 保存用例、应用服务和输入/输出端口。
- `infra` 只允许依赖 `app.port.out`，并实现其中的输出端口。
- `adapter` 负责 HTTP 等入站协议，`bootstrap` 只负责最终组装和启动。

## 验证和启动

```bash
./gradlew check
./gradlew :bootstrap:bootRun
```

`InfraArchitectureTest` 会拒绝 Infra 对 App 实现类的依赖，包括构造、字段、参数和静态调用。
