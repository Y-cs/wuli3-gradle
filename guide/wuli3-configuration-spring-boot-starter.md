# wuli3-configuration-spring-boot-starter 使用指南

该模块引入 `jasypt-spring-boot-starter`，为 Spring Boot 应用提供配置属性加密与解密能力。模块不替换 Jasypt 自动配置，应用可直接使用 `jasypt.encryptor.*` 配置项。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-configuration-spring-boot-starter")
}
```

当前通过 BOM 管理 `com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5`。

## 使用加密属性

将密文以 `ENC(...)` 包裹：

```yaml
spring:
  datasource:
    username: app
    password: ENC(cipher-text)

jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
```

Jasypt 主密码必须通过环境变量、容器 Secret 或其他外部安全注入方式提供，不要提交到配置文件、源码或镜像层。应用启动时，Jasypt 会自动解密 Spring Environment 中的 `ENC(...)` 属性。

## 常用配置

```yaml
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
    algorithm: PBEWITHHMACSHA512ANDAES_256
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
    salt-generator-classname: org.jasypt.salt.RandomSaltGenerator
    string-output-type: base64
```

默认算法和参数由 Jasypt starter 管理；只有在已有密文格式和兼容性要求明确时才覆盖它们。修改算法、盐或输出格式会使既有密文无法解密。

## 使用边界

- 模块只聚合 Jasypt starter，不保存或管理主密码。
- 不提供运行时密钥轮换和业务密文生成 API。
- 不应在日志、异常消息或 Actuator 环境端点中输出解密后的敏感配置。

## 验证

```bash
./gradlew :wuli3-configuration-spring-boot-starter:check
```
