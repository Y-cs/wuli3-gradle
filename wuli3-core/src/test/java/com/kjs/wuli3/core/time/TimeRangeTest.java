package com.kjs.wuli3.core.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TimeRangeTest {

    @Test
    void usesHalfOpenBounds() {
        final Instant start = Instant.parse("2026-07-08T00:00:00Z");
        final Instant end = Instant.parse("2026-07-08T01:00:00Z");
        final TimeRange range = new TimeRange(start, end);

        assertThat(range.contains(start)).isTrue();
        assertThat(range.contains(end.minusSeconds(1L))).isTrue();
        assertThat(range.contains(end)).isFalse();
        assertThat(range.duration()).isEqualTo(Duration.ofHours(1L));
    }

    @Test
    void intersectsOverlappingRanges() {
        final TimeRange left =
                new TimeRange(Instant.parse("2026-07-08T00:00:00Z"), Instant.parse("2026-07-08T02:00:00Z"));
        final TimeRange right =
                new TimeRange(Instant.parse("2026-07-08T01:00:00Z"), Instant.parse("2026-07-08T03:00:00Z"));

        assertThat(left.intersection(right))
                .isEqualTo(Optional.of(
                        new TimeRange(Instant.parse("2026-07-08T01:00:00Z"), Instant.parse("2026-07-08T02:00:00Z"))));
    }

    @Test
    void rejectsReversedBounds() {
        assertThatThrownBy(() ->
                        new TimeRange(Instant.parse("2026-07-08T01:00:00Z"), Instant.parse("2026-07-08T00:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clockProviderCanBeFixed() {
        final Instant instant = Instant.parse("2026-07-08T00:00:00Z");
        final ClockProvider provider = ClockProvider.fixed(instant, ZoneOffset.UTC);

        assertThat(provider.instant()).isEqualTo(instant);
        assertThat(provider.zone()).isEqualTo(ZoneOffset.UTC);
    }
}
