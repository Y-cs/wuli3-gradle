# wuli3-core 使用指南

## 1. 模块定位

`wuli3-core` 是不依赖 Spring 的基础能力模块，面向业务工程、starter 和基础设施模块复用。当前能力包括：

- 错误码模型与错误策略：`com.kjs.wuli3.core.error`
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

### 3.1 定义业务错误码

业务错误码应定义为枚举，并实现 `ErrorCode`。错误码枚举必须标注 `@ErrorModule`，否则解析错误元数据时会抛出框架错误。

```java
@Getter
@RequiredArgsConstructor
@ErrorModule(value = "ORDER", policy = @ErrorPolicy(visibility = ErrorVisibility.PUBLIC))
public enum OrderErrors implements ErrorCode {
    ORDER_NOT_FOUND("订单不存在"),

    @ErrorPolicy(severity = ErrorSeverity.WARNING, visibility = ErrorVisibility.MESSAGE_ONLY)
    ORDER_STATUS_INVALID("订单状态不允许当前操作");

    private final String message;
}
```

约定：

- `@ErrorModule.value` 表示错误所属模块，应使用稳定、可读的模块名。
- `@ErrorModule.policy` 是模块默认策略。
- 枚举常量上的 `@ErrorPolicy` 会覆盖模块默认策略。
- `ErrorCode.getName()` 默认返回枚举常量名。
- `ErrorCode.getErrorType()` 默认返回枚举声明类型。

### 3.2 抛出错误

使用 `ErrorCodeException` 携带错误码、消息、策略和可选明细。

```java
throw new ErrorCodeException(OrderErrors.ORDER_NOT_FOUND);
```

自定义消息：

```java
throw new ErrorCodeException(OrderErrors.ORDER_STATUS_INVALID, "订单已完成，不能取消");
```

附加策略覆盖：

```java
throw new ErrorCodeException(OrderErrors.ORDER_STATUS_INVALID)
        .severity(ErrorSeverity.WARNING)
        .visibility(ErrorVisibility.MESSAGE_ONLY);
```

附加明细对象：

```java
throw new ErrorCodeException(OrderErrors.ORDER_NOT_FOUND)
        .detail(Map.of("orderId", orderId));
```

### 3.3 错误可见性与严重程度

`ErrorVisibility` 用于控制错误对外输出边界：

- `PUBLIC`：错误信息可以对外输出。
- `CODE_ONLY`：只输出错误码。
- `MESSAGE_ONLY`：只输出消息。
- `INTERNAL`：内部错误，不应直接对外输出。

`ErrorSeverity` 用于表达错误严重程度：

- `NORMAL`：请求参数、业务规则、资源不存在等常规错误。
- `WARNING`：需要关注，但影响局限在当前用户或当前请求。
- `CRITICAL`：系统功能受影响，例如数据库、缓存、消息中间件不可用。
- `FATAL`：系统不可恢复或继续运行会导致严重后果。

### 3.4 解析错误元数据

`ErrorMetadataParser` 会缓存错误模块和错误策略：

```java
final ResolvedErrorPolicy policy =
        ErrorMetadataParser.instance().getErrorPolicy(OrderErrors.ORDER_STATUS_INVALID);
```

正常业务代码通常不需要直接调用解析器，适配层或响应转换层可以使用它读取策略。

## 4. 断言工具

`Asserts` 使用延迟抛异常的链式风格。断言方法返回 `AssertException`，当条件不满足时调用 `throwException(...)` 抛出 `ErrorCodeException`。

```java
Asserts.notBlank(name).throwException(SystemErrors.ILLEGAL_ARGUMENT, "name 不能为空");
Asserts.notNull(order).throwException(OrderErrors.ORDER_NOT_FOUND);
Asserts.isTrue(amount.signum() > 0).throwException(SystemErrors.ILLEGAL_ARGUMENT, "amount 必须大于 0");
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
Asserts.notBlank(name).throwException(SystemErrors.ILLEGAL_ARGUMENT);
Asserts.notBlank(name).throwException(SystemErrors.ILLEGAL_ARGUMENT, "name 不能为空");
Asserts.notBlank(name).throwIllegalArgumentException("name 不能为空");
```

反转断言：

```java
Asserts.isTrue(deleted).reversed().throwException(SystemErrors.ILLEGAL_STATE, "记录已删除");
```

注意：

- `Asserts` 当前不返回被校验对象，只负责延迟抛异常。
- `throwIllegalArgumentException(String message)` 会使用 `SystemErrors.ILLEGAL_ARGUMENT`。
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

默认遇到重复 key 会抛出 `ErrorCodeException(SystemErrors.ILLEGAL_STATE)`。如需要自定义合并策略：

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

`check` 会执行编译、测试、Checkstyle、Forbidden APIs、Spotless、SpotBugs、JaCoCo 等质量检查。提交前如涉及跨模块行为，建议运行：

```bash
./gradlew clean check
```

## 9. 常见问题

### 9.1 为什么区间使用半开区间？

半开区间 `[startInclusive, endExclusive)` 可以避免相邻区间边界重复。例如 `[2026-07-01, 2026-07-03)` 与 `[2026-07-03, 2026-07-05)` 不重叠，适合按天、按小时分片统计。

### 9.2 什么时候使用 `SystemErrors`？

`SystemErrors` 用于通用系统级错误，例如非法参数、非法状态、未实现、不支持操作。业务语义明确的错误应定义自己的 `ErrorCode` 枚举，避免所有业务错误都落到系统错误中。

### 9.3 `ErrorCodeException.detail(...)` 会对外输出吗？

`detail(...)` 只是在异常对象上附加明细，是否对外输出由适配层结合 `ResolvedErrorPolicy` 决定。业务代码不应假设 detail 一定会进入 API 响应。

### 9.4 `Asserts` 为什么不直接返回被校验对象？

当前 `Asserts` 的设计是构造断言条件并在最后选择错误模型。调用方可以根据同一个条件选择错误码、自定义消息、非法参数错误或反转条件。需要返回值时，应在断言通过后继续使用原变量。
