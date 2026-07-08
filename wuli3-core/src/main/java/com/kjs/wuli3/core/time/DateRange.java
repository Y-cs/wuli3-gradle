package com.kjs.wuli3.core.time;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * Half-open local date range: {@code [startInclusive, endExclusive)}.
 */
public record DateRange(LocalDate startInclusive, LocalDate endExclusive) {

    public DateRange(final LocalDate startInclusive, final LocalDate endExclusive) {
        this.startInclusive = Objects.requireNonNull(startInclusive, "startInclusive");
        this.endExclusive = Objects.requireNonNull(endExclusive, "endExclusive");
        if (this.endExclusive.isBefore(this.startInclusive)) {
            throw new IllegalArgumentException("endExclusive must not be before startInclusive");
        }
    }

    public static DateRange closed(final LocalDate startInclusive, final LocalDate endInclusive) {
        Objects.requireNonNull(endInclusive, "endInclusive");
        return new DateRange(startInclusive, endInclusive.plusDays(1L));
    }

    public boolean isEmpty() {
        return this.startInclusive.equals(this.endExclusive);
    }

    public long days() {
        return ChronoUnit.DAYS.between(this.startInclusive, this.endExclusive);
    }

    public boolean contains(final LocalDate date) {
        Objects.requireNonNull(date, "date");
        return !date.isBefore(this.startInclusive) && date.isBefore(this.endExclusive);
    }

    public boolean overlaps(final DateRange other) {
        Objects.requireNonNull(other, "other");
        return this.startInclusive.isBefore(other.endExclusive) && other.startInclusive.isBefore(this.endExclusive);
    }

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
