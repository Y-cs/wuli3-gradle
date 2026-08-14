# wuli3-aliyun-spring-boot-starter 使用指南

该模块集成 AliYun OSS Java SDK V2，并根据 Spring Boot 配置创建多套命名 `OSSClient`。`OssClientManager` 负责客户端的创建、查找和关闭；`OssTemplate` 负责单个客户端的操作执行和异常转换。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-aliyun-spring-boot-starter")
}
```

模块通过 BOM 管理 `com.aliyun:alibabacloud-oss-v2:0.5.1`。

## 访问凭据

客户端统一使用 SDK 的 `EnvironmentVariableCredentialsProvider`。请在运行环境中设置凭据，不要将 AccessKey 写入配置文件或代码：

```bash
export OSS_ACCESS_KEY_ID="yourAccessKeyId"
export OSS_ACCESS_KEY_SECRET="yourAccessKeySecret"
# 使用 STS 临时凭据时再设置：export OSS_SESSION_TOKEN="yourSecurityToken"
```

生产环境应使用按最小权限授权的 RAM 用户，不应使用主账号 AccessKey。

## 多套配置

```yaml
wuli3:
  aliyun:
    default-profile: default
    profiles:
      default:
        access-key:
          id: ${OSS_ACCESS_KEY_ID}
          secret: ${OSS_ACCESS_KEY_SECRET}
        oss:
          region: cn-hangzhou
          bucket: example-default
      archive:
        access-key:
          id: ${OSS_ARCHIVE_ACCESS_KEY_ID}
          secret: ${OSS_ARCHIVE_ACCESS_KEY_SECRET}
        oss:
          region: cn-shanghai
          bucket: example-archive
          endpoint: https://oss-cn-shanghai-internal.aliyuncs.com
```

Java SDK V2 会根据 Region 使用默认公网 Endpoint，因此普通公网访问无需配置 `endpoint`。内网、传输加速或自定义域名场景可以为对应 profile 显式配置。

## 使用客户端

```java
final OssClientManager.Client storage = ossClientManager.get("archive");
new OssTemplate(storage.client()).execute(client -> client.putObject(PutObjectRequest.newBuilder()
        .bucket(storage.bucket())
        .key(objectKey)
        .body(BinaryData.fromString(content))
        .build()));
```

配置了 `default-profile` 时可以使用 `ossClientManager.getDefault()`。未配置默认项时，使用方必须显式指定 profile 名称。Spring 容器关闭时，管理器会关闭其创建的全部 `OSSClient`。

## 配置约束

- 每套 profile 必须配置 `region` 和 `bucket`。
- `default-profile` 如果配置，必须引用已存在的 profile。
- `access-key` 与 `oss` 是隔离的配置对象；示例使用环境变量注入凭据，避免将密钥写入仓库。
- 模块只负责客户端配置和生命周期，不封装上传、下载或删除等业务操作。

## 验证

```bash
./gradlew :wuli3-aliyun-spring-boot-starter:check
```
