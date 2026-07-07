package com.kjs.wuli3.core.stream;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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

    public long count() {
        return this.count;
    }

    public BigDecimal sum() {
        return this.sum;
    }

    public Optional<BigDecimal> min() {
        return Optional.ofNullable(this.min);
    }

    public Optional<BigDecimal> max() {
        return Optional.ofNullable(this.max);
    }

    public Optional<BigDecimal> average(final int scale, final RoundingMode roundingMode) {
        Objects.requireNonNull(roundingMode, "roundingMode");
        if (this.count == 0L) {
            return Optional.empty();
        }
        final BigDecimal divisor = BigDecimal.valueOf(this.count);
        return Optional.of(this.sum.divide(divisor, scale, roundingMode));
    }
}
