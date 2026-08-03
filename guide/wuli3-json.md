# wuli3-json 使用指南

`wuli3-json` 提供统一的 Jackson 配置、JSON/树操作 facade，以及资源路径转换和敏感字段脱敏扩展。模块依赖
`wuli3-core`，JSON 失败会转换为项目错误模型。

## 引入

```kotlin
dependencies {
    implementation("com.kjs.wuli3:wuli3-json")
}
```

版本建议由 `wuli3-dependencies` BOM 管理。

## JSON 读写

```java
final String json = Jsons.toJson(order);
final byte[] bytes = Jsons.toJsonBytes(order);

final Order restored = Jsons.fromJson(json, Order.class);
final List<Order> orders = Jsons.fromJson(
        json,
        new TypeReference<List<Order>>() {});
```

`Jsons` 使用模块内固定的标准 Mapper，适合不需要应用级定制的读写。需要注入自定义模块、资源解析器或脱敏策略时，使用独立 Mapper。

## JSON Tree

```java
final JsonNode root = JsonTrees.readTree(json);
final ObjectNode object = JsonTrees.createObjectNode();
object.put("id", 1L);

final JsonNode node = JsonTrees.valueToTree(order);
final Order value = JsonTrees.treeToValue(node, Order.class);
```

## 创建独立 Mapper

获取标准配置 Mapper：

```java
final JsonMapper mapper = JacksonProvider.newJsonMapper();
```

按顺序增加装配链：

```java
final JsonMapper mapper = JsonMapperFactory.standardJsonMapperFactory()
        .addAssemblyChain(new JsonMapperResourcePathAssembly(resourcePathResolver))
        .create();
```

装配顺序会影响 Jackson 模块和 serializer 的最终行为。一个字段不要同时声明多个会接管序列化的扩展。

## 资源路径转换

`@ResourcePath` 只用于 `String` 字段。序列化时把存储路径转换为对外值，反序列化时执行反向转换：

```java
record FileView(@ResourcePath(type = "image") String path) {}
```

应用实现解析器：

```java
final class AppResourcePathResolver implements ResourcePathResolver {
    @Override
    public boolean supports(final String type) {
        return "image".equals(type);
    }

    @Override
    public String serialize(final String type, final String path) {
        return "https://static.example.com/" + path;
    }

    @Override
    public String deserialize(final String type, final String value) {
        return value.replace("https://static.example.com/", "");
    }
}
```

Spring MVC 应用使用 Web starter 时，可以注册 `ResourcePathResolver` Bean；Web starter 会把它装配到 Boot 管理的 `ObjectMapper`。

## JSON 脱敏

内置类型包括手机号、邮箱、身份证号和银行卡号：

```java
record UserView(
        @Desensitized(type = DesensitizationTypes.PHONE) String phone,
        @Desensitized(type = DesensitizationTypes.EMAIL) String email) {}
```

非 Spring 场景可以显式创建脱敏 Mapper：

```java
final JsonMapper mapper = JsonMapperFactory.standardJsonMapperFactory()
        .addAssemblyChain(new JsonMapperDesensitizationAssembly(
                DesensitizationStrategyRegistry.standard(),
                DesensitizationVisibilityPolicy.alwaysMask()))
        .create();
```

应用可通过 `DesensitizationStrategy.of(type, masker)` 增加或覆盖策略。Web starter 会收集 Spring 容器中的
`DesensitizationStrategy` Bean，并默认始终脱敏；如需按权限展示原值，提供自定义 `DesensitizationVisibilityPolicy` Bean。

## 使用边界

- `Jsons` 的标准 Mapper 不读取 Spring Bean，也不包含应用自定义 resolver。
- `@ResourcePath` 和 `@Desensitized` 面向字符串字段；类型或策略不匹配会显式失败。
- 脱敏只改变 JSON 输出，不修改领域对象中的原值。
- JSON 结构属于跨系统契约时，应使用稳定 DTO，不直接序列化内部实体。

## 验证

```bash
./gradlew :wuli3-json:test
./gradlew :wuli3-json:check
```
