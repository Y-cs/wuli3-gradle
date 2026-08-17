package com.kjs.wuli3.core.stream;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 提供忽略空值的 BigDecimal 求和与汇总收集器。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@NullMarked
public final class BigDecimalCollectors {

    private BigDecimalCollectors() {}

    /** 返回忽略空值的 BigDecimal 求和收集器。 */
    public static Collector<BigDecimal, ?, BigDecimal> summing() {
        return Collectors.collectingAndThen(BigDecimalCollectors.summarizing(), BigDecimalSummary::sum);
    }

    /**
     * 返回按映射结果求和的收集器；映射结果为空时忽略该元素。
     *
     * @param mapper 元素到 BigDecimal 的映射函数
     * @param <T> 输入元素类型
     * @return BigDecimal 求和收集器
     */
    public static <T> Collector<T, ?, BigDecimal> summing(
            final Function<? super T, ? extends @Nullable BigDecimal> mapper) {
        return Collectors.collectingAndThen(BigDecimalCollectors.summarizing(mapper), BigDecimalSummary::sum);
    }

    /** 返回忽略空值的 BigDecimal 汇总收集器。 */
    public static Collector<BigDecimal, ?, BigDecimalSummary> summarizing() {
        return BigDecimalCollectors.summarizing(value -> value);
    }

    /**
     * 返回按映射结果汇总的收集器；汇总包含数量、总和、最小值和最大值。
     *
     * @param mapper 元素到 BigDecimal 的映射函数
     * @param <T> 输入元素类型
     * @return BigDecimal 汇总收集器
     */
    public static <T> Collector<T, ?, BigDecimalSummary> summarizing(
            final Function<? super T, ? extends @Nullable BigDecimal> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return Collector.of(
                Accumulator::new,
                (final Accumulator accumulator, final T value) -> accumulator.accept(mapper.apply(value)),
                Accumulator::combine,
                Accumulator::toSummary,
                Collector.Characteristics.UNORDERED);
    }

    private static final class Accumulator {

        private long count;
        private BigDecimal sum = BigDecimal.ZERO;
        private @Nullable BigDecimal min;
        private @Nullable BigDecimal max;

        private void accept(final @Nullable BigDecimal value) {
            if (value == null) {
                return;
            }
            this.sum = this.sum.add(value);
            if (this.count == 0L) {
                this.count = 1L;
                this.min = value;
                this.max = value;
                return;
            }
            this.count++;
            if (this.min == null || value.compareTo(this.min) < 0) {
                this.min = value;
            }
            if (this.max == null || value.compareTo(this.max) > 0) {
                this.max = value;
            }
        }

        private Accumulator combine(final Accumulator other) {
            if (other.count == 0L) {
                return this;
            }
            if (this.count == 0L) {
                return other;
            }
            this.count += other.count;
            this.sum = this.sum.add(other.sum);
            if (this.min == null || (other.min != null && other.min.compareTo(this.min) < 0)) {
                this.min = other.min;
            }
            if (this.max == null || (other.max != null && other.max.compareTo(this.max) > 0)) {
                this.max = other.max;
            }
            return this;
        }

        private BigDecimalSummary toSummary() {
            return new BigDecimalSummary(this.count, this.sum, this.min, this.max);
        }
    }
}
