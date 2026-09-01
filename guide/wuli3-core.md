# wuli3-core 使用指南

## 1. 模块定位

`wuli3-core` 是不依赖 Spring 的基础能力模块，面向业务工程、starter 和基础设施模块复用。当前能力包括：

- 按职责拆分的错误码模型与错误策略：`com.kjs.wuli3.core.error.*`
- 断言工具：`com.kjs.wuli3.core.assertion`
- ID 生成抽象：`com.kjs.wuli3.core.id`
- 时间工具与半开区间：`com.kjs.wuli3.core.time`
- Stream 与 `BigDecimal` 聚合工具：`com.kjs.wuli3.core.stream`

使用原则：

- 公共错误应使用 `ErrorCode` 和 `ErrorCodeException` 表达，避免直接向上抛散乱的运行时异常。
- 时间区间统一使用半开区间 `[startInclusive, endExclusive)`，避免边界重复计算。
- 时间类型优先使用 `java.time`，不要引入 `java.util.Date`、`Calendar` 或 `java.sql.Date/Time/Timestamp`。
- 模块已使用 JSpecify 表达空安全语义，调用方应按 `@Nullable` 和 `@NullMarked` 约束处理空值。

## 2. 引入方式

Gradle 多模块工程中直接依赖：

```kotlin
dependencies {
    implementation(project(":wuli3-core"))
}
```

外部项目通常先通过 `wuli3-dependencies` BOM 统一版本，再声明 `wuli3-core` 依赖。发布本地 BOM：

```bash
./gradlew :wuli3-dependencies:publishToMavenLocal
```

Maven 依赖示例：

```xml
<dependency>
  <groupId>com.kjs.wuli3</groupId>
  <artifactId>wuli3-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 3. 错误模型

### 3.1 包职责

核心错误模型位于 `com.kjs.wuli3.core.error` 根包；内置错误码、编解码和解析器按功能划分到子包。

| 包 | 主要类型 | 职责 |
| --- | --- | --- |
| `error` | `ErrorCode`、`ErrorCodeException`、`ErrorMetadata`、`ErrorModule`、`ErrorOrigin`、`ErrorSeverity`、`ErrorVisibility`、`ErrorCodeCarrier` | 稳定的核心错误模型和公共语义。 |
| `error.builtin` | `CommonErrors`、`SystemErrors`、`ErrorFrameworkErrors` | 提供框架内置错误码。 |
| `error.codec` | `ErrorCodeCarrierCodec`、`DefaultErrorCodeCarrierCodec`、`ErrorCodePropagator` | 在本地异常、传播协议和传输字段之间编解码。 |
| `error.resolver` | `ErrorCodeResolver`、`DefaultErrorCodeResolver`、`ErrorMetadataResolver` | 解析稳定错误码字符串及声明式元数据。 |

本次拆包会改变原有导入路径。升级时按下表替换：

| 原路径中的类型 | 新包 |
| --- | --- |
| `ErrorCode`、`ErrorCodeException`、`ErrorMetadata`、`ErrorModule`、`ErrorOrigin`、`ErrorSeverity`、`ErrorVisibility`、`ErrorCodeCarrier` | `com.kjs.wuli3.core.error` |
| `CommonErrors`、`SystemErrors`、`ErrorFrameworkErrors` | `com.kjs.wuli3.core.error.builtin` |
| `ErrorCodeCarrierCodec`、`DefaultErrorCodeCarrierCodec`、`ErrorCodePropagator` | `com.kjs.wuli3.core.error.codec` |
| `ErrorCodeResolver`、`DefaultErrorCodeResolver`、`ErrorMetadataResolver` | `com.kjs.wuli3.core.error.resolver` |

错误来源和严重程度属于错误码固有元数据，通过 `@ErrorMetadata` 声明；可见性属于边界输出策略，通过 `withVisibility(...)` 或 `ErrorCodeCarrierCodec` 参数指定。结构化响应明细应在具体协议边界定义。

原 `SystemErrors.ILLEGAL_ARGUMENT`、`SystemErrors.ILLEGAL_STATE` 和 `SystemErrors.UNSUPPORTED_OPERATION` 已迁移为 `CommonErrors` 中的同名常量；`SystemErrors` 现在只保留系统级错误。

核心内置错误按责任来源区分：

- `CommonErrors`：非法参数、非法状态、不支持的操作等调用方可以修正的错误，默认来源为 `CALLER`。
- `SystemErrors`：内部错误、运行配置缺失、未实现功能等需要服务自身修复的错误，默认来源为 `SERVER`。
- 数据不存在通常属于业务领域语义，应定义为 `OrderErrors.ORDER_NOT_FOUND`、`UserErrors.USER_NOT_FOUND` 等业务错误码，不使用无语义的通用错误码。

### 3.2 定义业务错误码

业务错误码应定义为枚举，并实现 `ErrorCode`。错误码枚举必须标注 `@ErrorModule`，否则解析错误元数据时会抛出框架错误。

```java
import com.kjs.wuli3.core.error.ErrorCode;
import com.kjs.wuli3.core.error.ErrorModule;
import com.kjs.wuli3.core.error.ErrorMetadata;
import com.kjs.wuli3.core.error.ErrorOrigin;
import com.kjs.wuli3.core.error.ErrorSeverity;
import com.kjs.wuli3.core.error.ErrorVisibility;

@Getter
@RequiredArgsConstructor
@ErrorModule(value = "ORDER", metadata = @ErrorMetadata(origin = ErrorOrigin.CALLER, severity = ErrorSeverity.NORMAL))
public enum OrderErrors implements ErrorCode {
    ORDER_NOT_FOUND("订单不存在"),

    @ErrorMetadata(severity = ErrorSeverity.WARNING)
    ORDER_STATUS_INVALID("订单状态不允许当前操作"),

    @ErrorMetadata(severity = ErrorSeverity.CRITICAL, origin = ErrorOrigin.SERVER)
    INVENTORY_SERVICE_UNAVAILABLE("库存服务不可用");

    private final String message;
}
```

约定：

- `@ErrorModule.value` 表示错误所属模块，应使用稳定、可读的模块名。
- `@ErrorModule.metadata` 是模块默认元数据；枚举常量上的 `@ErrorMetadata` 会覆盖对应字段。
- 本地枚举的常量名和声明类型只由元数据解析器在解析枚举时读取；传播错误直接携带完整字符串错误码。

### 3.3 抛出错误

使用 `ErrorCodeException` 携带错误码和消息。

```java
import com.kjs.wuli3.core.error.ErrorCodeException;

throw new ErrorCodeException(OrderErrors.ORDER_NOT_FOUND);
```

自定义消息：

```java
throw new ErrorCodeException(OrderErrors.ORDER_STATUS_INVALID, "订单已完成，不能取消");
```

仅当同一错误码在个别抛出点需要不同输出策略时，才使用运行时覆盖：

```java
throw new ErrorCodeException(OrderErrors.ORDER_STATUS_INVALID)
        .withVisibility(ErrorVisibility.MESSAGE_ONLY);
```

责任来源和严重程度应优先声明在 `@ErrorMetadata` 上；`ErrorVisibility` 由适配层按边界决定。

### 3.4 责任来源、严重程度与可见性

`ErrorOrigin` 用于表达由谁修正错误，是 Web 适配层确定默认 HTTP 状态的唯一业务语义：

- `CALLER`：调用方通过修正请求、状态或所选能力可以解决，默认映射为 4xx。
- `SERVER`：服务自身或其依赖需要修复，默认映射为 5xx。

`ErrorSeverity` 用于表达告警和处置优先级，不再决定 HTTP 状态：

- `NORMAL`：常规业务或输入错误。
- `WARNING`：影响局限于当前请求或局部业务，但需要关注。
- `CRITICAL`：服务功能受影响，需要及时处理。
- `FATAL`：服务不可恢复或可能造成严重后果，需要立即处理。

`ErrorVisibility` 用于控制错误对外输出边界：

- `PUBLIC`：错误信息可以对外输出。
- `CODE_ONLY`：只输出错误码。
- `MESSAGE_ONLY`：只输出消息。
- `INTERNAL`：内部错误，不应直接对外输出。

例如，JSON 序列化失败、消息发送失败等基础设施错误应声明为 `SERVER`；订单不存在、参数不合法等调用方可修正的业务错误保持默认的 `CALLER` 即可。

### 3.5 解析错误元数据

`ErrorMetadataResolver` 会缓存错误模块和固有元数据：

```java
import com.kjs.wuli3.core.error.resolver.ErrorMetadataResolver;
final ErrorOrigin origin = ErrorMetadataResolver.instance().getOrigin(OrderErrors.ORDER_STATUS_INVALID);
final ErrorSeverity severity = ErrorMetadataResolver.instance().getSeverity(OrderErrors.ORDER_STATUS_INVALID);
```

正常业务代码通常不需要直接调用解析器，适配层或响应转换层可以使用它读取策略。

### 3.6 本地声明与错误传播

错误模型明确区分本地声明和跨边界传播态：

- `ErrorCode` 是统一错误标识契约。本地错误通常由带 `@ErrorModule` 的业务枚举实现，远程错误由 `ErrorCodeCarrier` 实现。
- `ErrorCodeException` 是项目唯一错误异常，可持有本地枚举或远程传播值。
- `ErrorCodeCarrier` 是可跨进程传播的 `ErrorCode` 实现，包含稳定字符串错误码、消息、来源、严重程度和来源服务。

协议适配层使用 `DefaultErrorCodeCarrierCodec` 将 `ErrorCodeException` 序列化为 `ErrorCodeCarrier`：

| 输入 | 映射结果 |
| --- | --- |
| `ErrorCodeException`（本地枚举） | 保留完整字符串错误码、消息、来源和严重程度。 |
| `ErrorCodeException`（远程传播值） | 保留已有完整码和最初来源，支持多跳调用。 |
| 其他异常 | 由具体协议边界先包装为 `SystemErrors.INTERNAL_ERROR`，再按 `INTERNAL` 序列化，不暴露原始类型和消息。 |

`ErrorCodePropagator` 使用字段读写函数在传播协议和字符串字段之间转换：

```java
final ErrorPropagationEncoder encoder = new ErrorPropagationEncoder();
encoder.writeTo(protocol, fieldWriter);
final Optional<ErrorPropagationProtocol> decoded = encoder.readFrom(fieldReader);
```

HTTP、Dubbo 等协议统一使用 `X-Wuli3-Error-Code`、`X-Wuli3-Error-Message`、
`X-Wuli3-Error-Origin`、`X-Wuli3-Error-Severity` 和 `X-Wuli3-Error-Source-Service`。
适配层只提供 `BiConsumer<String, String>` 写入函数和允许返回空值的字段读取函数，不重复实现字段校验和枚举解析。

该兜底规则只应由 Dubbo、HTTP、消息消费等外部协议边界使用，不是全局异常转换规则。启动配置校验、纯 Java API 契约和编程错误仍应保留合适的 JDK 异常；可预期的业务失败应显式抛出 `ErrorCodeException`；数据库、缓存、消息 SDK 等基础设施异常应在对应适配器中包装为模块自己的系统错误。

这种拆分保证服务间不需要共享所有业务错误枚举。例如提供方的 `GroupErrors.PERMISSION_DENIED` 可以映射为 `GROUP.PERMISSION_DENIED` 后传播，消费方将其作为 `ErrorCodeException` 携带的 `ErrorCodeCarrier` 接收，不需要把 `GroupErrors` 放进自己的 classpath。

## 4. 断言工具

`Asserts` 使用延迟抛异常的链式风格。断言方法返回 `AssertCondition`，当条件不满足时调用 `throwException(...)` 抛出 `ErrorCodeException`。

```java
Asserts.notBlank(name).throwException(CommonErrors.ILLEGAL_ARGUMENT, "name 不能为空");
Asserts.notNull(order).throwException(OrderErrors.ORDER_NOT_FOUND);
Asserts.isTrue(amount.signum() > 0).throwException(CommonErrors.ILLEGAL_ARGUMENT, "amount 必须大于 0");
```

当前可用断言：

| 方法 | 通过条件 | 失败条件 |
| --- | --- | --- |
| `isTrue(boolean condition)` | `condition == true` | `condition == false` |
| `notNull(Object obj)` | `obj != null` | `obj == null` |
| `notBlank(String value)` | 非 `null` 且 `!value.isBlank()` | `null` 或空白字符串 |
| `notEmpty(String value)` | 非 `null` 且 `!value.isEmpty()` | `null` 或空字符串 |
| `isEmptyCollection(Collection<?> collection)` | `collection == null` 或集合为空 | 集合非空 |
| `isNotEmptyCollection(Collection<?> collection)` | 集合非 `null` 且非空 | `null` 或集合为空 |
| `isEmptyMap(Map<?, ?> map)` | `map == null` 或 Map 为空 | Map 非空 |
| `isNotEmptyMap(Map<?, ?> map)` | Map 非 `null` 且非空 | `null` 或 Map 为空 |

抛错方式：

```java
Asserts.notBlank(name).throwException(CommonErrors.ILLEGAL_ARGUMENT);
Asserts.notBlank(name).throwException(CommonErrors.ILLEGAL_ARGUMENT, "name 不能为空");
Asserts.notBlank(name).throwIllegalArgumentException("name 不能为空");
```

反转断言：

```java
Asserts.isTrue(deleted).reversed().throwException(CommonErrors.ILLEGAL_STATE, "记录已删除");
```

注意：

- `Asserts` 当前不返回被校验对象，只负责延迟抛异常。
- `throwIllegalArgumentException(String message)` 会使用 `CommonErrors.ILLEGAL_ARGUMENT`。
- 对集合和 Map 的空判断中，`isEmptyCollection(null)` 与 `isEmptyMap(null)` 视为通过。

## 5. ID 生成

`IdGenerator<T>` 是 ID 生成抽象：

```java
public interface IdGenerator<T> {
    T nextId();
}
```

默认提供 UUID 字符串生成器：

```java
final String id = UuidStringIdGenerator.INSTANCE.nextId();
```

业务需要雪花 ID、数据库号段或外部 ID 服务时，应实现 `IdGenerator<T>`，不要把具体算法散落在业务代码中。

```java
public final class OrderIdGenerator implements IdGenerator<String> {

    @Override
    public String nextId() {
        return "ORDER-" + UuidStringIdGenerator.INSTANCE.nextId();
    }
}
```

## 6. 时间工具

### 6.1 ClockProvider

`ClockProvider` 用于注入应用时钟，避免业务代码硬编码系统时间。

生产环境：

```java
final ClockProvider clockProvider = ClockProvider.system(ZoneId.of("Asia/Shanghai"));
final Instant now = clockProvider.instant();
```

测试环境：

```java
final ClockProvider fixedClock = ClockProvider.fixed(
        Instant.parse("2026-07-08T00:00:00Z"),
        ZoneOffset.UTC);
```

### 6.2 DateRange

`DateRange` 表示本地日期半开区间 `[startInclusive, endExclusive)`。

```java
final DateRange range = new DateRange(
        LocalDate.of(2026, 7, 1),
        LocalDate.of(2026, 7, 3));

range.contains(LocalDate.of(2026, 7, 1)); // true
range.contains(LocalDate.of(2026, 7, 3)); // false
range.days(); // 2
```

如业务输入是闭区间，可以使用工厂方法：

```java
final DateRange closed = DateRange.closed(
        LocalDate.of(2026, 7, 1),
        LocalDate.of(2026, 7, 3));

closed.endExclusive(); // 2026-07-04
```

区间能力：

```java
range.isEmpty();
range.overlaps(otherRange);
range.intersection(otherRange);
```

### 6.3 TimeRange

`TimeRange` 表示 `Instant` 半开区间 `[startInclusive, endExclusive)`。

```java
final TimeRange range = new TimeRange(
        Instant.parse("2026-07-08T00:00:00Z"),
        Instant.parse("2026-07-08T01:00:00Z"));

range.contains(Instant.parse("2026-07-08T00:30:00Z")); // true
range.contains(Instant.parse("2026-07-08T01:00:00Z")); // false
range.duration(); // PT1H
```

### 6.4 DateTimeFormats

统一格式常量：

| 常量 | Pattern |
| --- | --- |
| `DateTimeFormats.DATE` | `yyyy-MM-dd` |
| `DateTimeFormats.TIME` | `HH:mm:ss` |
| `DateTimeFormats.DATE_TIME` | `yyyy-MM-dd HH:mm:ss` |
| `DateTimeFormats.COMPACT_DATE` | `yyyyMMdd` |

示例：

```java
final String value = LocalDateTime.now().format(DateTimeFormats.DATE_TIME);
```

## 7. Stream 工具

### 7.1 BigDecimalCollectors

求和：

```java
final BigDecimal total = orders.stream()
        .collect(BigDecimalCollectors.summing(Order::amount));
```

统计：

```java
final BigDecimalSummary summary = orders.stream()
        .collect(BigDecimalCollectors.summarizing(Order::amount));

summary.count();
summary.sum();
summary.min();
summary.max();
summary.average(2, RoundingMode.HALF_UP);
```

规则：

- `mapper` 返回 `null` 时会被忽略。
- 空流的 `sum()` 为 `BigDecimal.ZERO`。
- 空流的 `min()`、`max()`、`average(...)` 返回 `Optional.empty()`。

### 7.2 MoreCollectors 与 MapMerger

按插入顺序收集为 `LinkedHashMap`：

```java
final Map<String, Order> orderMap = orders.stream()
        .collect(MoreCollectors.toLinkedMap(Order::id, Function.identity()));
```

默认遇到重复 key 会抛出 `ErrorCodeException(CommonErrors.ILLEGAL_STATE)`。如需要自定义合并策略：

```java
final Map<String, Order> orderMap = orders.stream()
        .collect(MoreCollectors.toLinkedMap(
                Order::id,
                Function.identity(),
                MapMerger::keepLastValue));
```

可用合并策略：

- `MapMerger.keepFirstValue(left, right)`：保留旧值。
- `MapMerger.keepLastValue(left, right)`：使用新值。
- `MapMerger.keepFirstNonNullValue(left, right)`：优先保留第一个非空值。
- `MapMerger.throwDuplicate(left, right)`：重复时抛错。

### 7.3 StreamUtils

过滤空值：

```java
final List<String> values = StreamUtils.filterNotNull(source.stream())
        .toList();
```

按 key 去重：

```java
final List<Order> distinctOrders = orders.stream()
        .filter(StreamUtils.distinctBy(Order::id))
        .toList();
```

注意：

- `distinctBy(...)` 内部维护并发 Set，适合单次流处理；不要复用同一个 Predicate 处理多个独立业务批次。
- `distinctBy(...)` 支持 `null` key，并将第一个 `null` key 视为有效唯一值。

## 8. 测试与质量门禁

开发 `wuli3-core` 后建议执行：

```bash
./gradlew :wuli3-core:test
./gradlew :wuli3-core:check
```

`check` 会执行编译、测试、Checkstyle、Spotless、Forbidden APIs、Error Prone、NullAway、SpotBugs 和 JaCoCo 等质量检查。提交前如涉及跨模块行为，建议运行：

```bash
./gradlew clean check
```

## 9. 常见问题

### 9.1 为什么区间使用半开区间？

半开区间 `[startInclusive, endExclusive)` 可以避免相邻区间边界重复。例如 `[2026-07-01, 2026-07-03)` 与 `[2026-07-03, 2026-07-05)` 不重叠，适合按天、按小时分片统计。

### 9.2 什么时候使用 `CommonErrors` 和 `SystemErrors`？

`CommonErrors` 用于跨模块复用的调用方错误，例如非法参数、非法状态和不支持的操作；`SystemErrors` 用于内部错误、运行配置缺失和未实现功能等系统错误。数据不存在应优先使用带领域语义的错误码，例如 `OrderErrors.ORDER_NOT_FOUND`。业务语义明确的错误都应定义自己的 `ErrorCode` 枚举，避免所有业务错误都落到通用错误中。

### 9.3 如何返回结构化错误明细？

`ErrorCodeException` 不承载任意明细对象。参数校验等由 Web starter 已知的框架异常会在 `ApiResponse.data` 中返回受控的 `ValidationErrorDetails`。业务需要返回其他结构化明细时，应在应用自己的 Web 边界定义响应 DTO 和异常映射规则，并明确其可见性与脱敏策略。

### 9.4 `Asserts` 为什么不直接返回被校验对象？

当前 `Asserts` 的设计是构造断言条件并在最后选择错误模型。调用方可以根据同一个条件选择错误码、自定义消息、非法参数错误或反转条件。需要返回值时，应在断言通过后继续使用原变量。
