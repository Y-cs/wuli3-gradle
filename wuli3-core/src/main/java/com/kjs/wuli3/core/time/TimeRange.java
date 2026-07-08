package com.kjs.wuli3.core.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Half-open instant range: {@code [startInclusive, endExclusive)}.
 */
public record TimeRange(Instant startInclusive, Instant endExclusive) {

    public TimeRange(final Instant startInclusive, final Instant endExclusive) {
        this.startInclusive = Objects.requireNonNull(startInclusive, "startInclusive");
        this.endExclusive = Objects.requireNonNull(endExclusive, "endExclusive");
        if (this.endExclusive.isBefore(this.startInclusive)) {
            throw new IllegalArgumentException("endExclusive must not be before startInclusive");
        }
    }

    public boolean isEmpty() {
        return this.startInclusive.equals(this.endExclusive);
    }

    public Duration duration() {
        return Duration.between(this.startInclusive, this.endExclusive);
    }

    public boolean contains(final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return !instant.isBefore(this.startInclusive) && instant.isBefore(this.endExclusive);
    }

    public boolean overlaps(final TimeRange other) {
        Objects.requireNonNull(other, "other");
        return this.startInclusive.isBefore(other.endExclusive) && other.startInclusive.isBefore(this.endExclusive);
    }

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
