package com.kjs.wuli3.core.time;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * 半开本地日期区间：{@code [startInclusive, endExclusive)}。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public record DateRange(LocalDate startInclusive, LocalDate endExclusive) {

    /** 创建半开日期区间；结束日期不能早于开始日期。 */
    public DateRange(final LocalDate startInclusive, final LocalDate endExclusive) {
        this.startInclusive = Objects.requireNonNull(startInclusive, "startInclusive");
        this.endExclusive = Objects.requireNonNull(endExclusive, "endExclusive");
        if (this.endExclusive.isBefore(this.startInclusive)) {
            throw new IllegalArgumentException("endExclusive must not be before startInclusive");
        }
    }

    /** 将闭区间转换为半开日期区间。 */
    public static DateRange closed(final LocalDate startInclusive, final LocalDate endInclusive) {
        Objects.requireNonNull(endInclusive, "endInclusive");
        if (LocalDate.MAX.equals(endInclusive)) {
            throw new IllegalArgumentException("endInclusive must be before LocalDate.MAX");
        }
        return new DateRange(startInclusive, endInclusive.plusDays(1L));
    }

    /** 判断区间是否不包含任何日期。 */
    public boolean isEmpty() {
        return this.startInclusive.equals(this.endExclusive);
    }

    /** 返回半开区间包含的天数。 */
    public long days() {
        return ChronoUnit.DAYS.between(this.startInclusive, this.endExclusive);
    }

    /** 判断指定日期是否位于区间内。 */
    public boolean contains(final LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(this.startInclusive) && date.isBefore(this.endExclusive);
    }

    /** 判断两个非空区间是否存在重叠。 */
    public boolean overlaps(final DateRange other) {
        Objects.requireNonNull(other, "other");
        return !this.isEmpty()
                && !other.isEmpty()
                && this.startInclusive.isBefore(other.endExclusive)
                && other.startInclusive.isBefore(this.endExclusive);
    }

    /** 返回两个区间的交集；没有交集时返回空。 */
    public Optional<DateRange> intersection(final DateRange other) {
        Objects.requireNonNull(other, "other");
        if (!this.overlaps(other)) {
            return Optional.empty();
        }
        final LocalDate start =
                this.startInclusive.isAfter(other.startInclusive) ? this.startInclusive : other.startInclusive;
        final LocalDate end = this.endExclusive.isBefore(other.endExclusive) ? this.endExclusive : other.endExclusive;
        return Optional.of(new DateRange(start, end));
    }
}
