package com.kjs.wuli3.core.stream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * BigDecimal 流式汇总结果，包含数量、总和、最小值和最大值。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@NullMarked
public final class BigDecimalSummary {

    private final long count;
    private final BigDecimal sum;
    private final @Nullable BigDecimal min;
    private final @Nullable BigDecimal max;

    BigDecimalSummary(
            final long count, final BigDecimal sum, final @Nullable BigDecimal min, final @Nullable BigDecimal max) {
        this.count = count;
        this.sum = sum;
        this.min = min;
        this.max = max;
    }

    /** 返回参与汇总的非空值数量。 */
    public long count() {
        return this.count;
    }

    /** 返回参与汇总的非空值总和。 */
    public BigDecimal sum() {
        return this.sum;
    }

    /** 返回最小值；没有非空值时返回空。 */
    public Optional<BigDecimal> min() {
        return Optional.ofNullable(this.min);
    }

    /** 返回最大值；没有非空值时返回空。 */
    public Optional<BigDecimal> max() {
        return Optional.ofNullable(this.max);
    }

    /**
     * 按指定精度和舍入模式计算平均值。
     *
     * @param scale 小数位数
     * @param roundingMode 舍入模式
     * @return 平均值；没有非空值时返回空
     */
    public Optional<BigDecimal> average(final int scale, final RoundingMode roundingMode) {
        Objects.requireNonNull(roundingMode, "roundingMode");
        if (this.count == 0L) {
            return Optional.empty();
        }
        final BigDecimal divisor = BigDecimal.valueOf(this.count);
        return Optional.of(this.sum.divide(divisor, scale, roundingMode));
    }
}
