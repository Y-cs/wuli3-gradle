package com.kjs.wuli3.core.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * 半开时间点区间：{@code [startInclusive, endExclusive)}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record TimeRange(Instant startInclusive, Instant endExclusive) {

    /** 创建半开时间点区间；结束时间不能早于开始时间。 */
    public TimeRange(final Instant startInclusive, final Instant endExclusive) {
        this.startInclusive = Objects.requireNonNull(startInclusive, "startInclusive");
        this.endExclusive = Objects.requireNonNull(endExclusive, "endExclusive");
        if (this.endExclusive.isBefore(this.startInclusive)) {
            throw new IllegalArgumentException("endExclusive must not be before startInclusive");
        }
    }

    /** 判断区间是否为空。 */
    public boolean isEmpty() {
        return this.startInclusive.equals(this.endExclusive);
    }

    /** 返回半开区间的持续时间。 */
    public Duration duration() {
        return Duration.between(this.startInclusive, this.endExclusive);
    }

    /** 判断指定时间点是否位于区间内。 */
    public boolean contains(final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return !instant.isBefore(this.startInclusive) && instant.isBefore(this.endExclusive);
    }

    /** 判断两个非空区间是否存在重叠。 */
    public boolean overlaps(final TimeRange other) {
        Objects.requireNonNull(other, "other");
        return !this.isEmpty()
                && !other.isEmpty()
                && this.startInclusive.isBefore(other.endExclusive)
                && other.startInclusive.isBefore(this.endExclusive);
    }

    /** 返回两个区间的交集；没有交集时返回空。 */
    public Optional<TimeRange> intersection(final TimeRange other) {
        Objects.requireNonNull(other, "other");
        if (!this.overlaps(other)) {
            return Optional.empty();
        }
        final Instant start =
                this.startInclusive.isAfter(other.startInclusive) ? this.startInclusive : other.startInclusive;
        final Instant end = this.endExclusive.isBefore(other.endExclusive) ? this.endExclusive : other.endExclusive;
        return Optional.of(new TimeRange(start, end));
    }
}
