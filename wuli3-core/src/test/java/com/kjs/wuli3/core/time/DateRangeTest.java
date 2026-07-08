package com.kjs.wuli3.core.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    void usesHalfOpenBounds() {
        final DateRange range = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3));

        assertThat(range.contains(LocalDate.of(2026, 7, 1))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 7, 2))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 7, 3))).isFalse();
        assertThat(range.days()).isEqualTo(2L);
    }

    @Test
    void supportsClosedFactory() {
        final DateRange range = DateRange.closed(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3));

        assertThat(range.endExclusive()).isEqualTo(LocalDate.of(2026, 7, 4));
    }

    @Test
    void intersectsOverlappingRanges() {
        final DateRange left = new DateRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));
        final DateRange right = new DateRange(LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));

        assertThat(left.intersection(right))
                .isEqualTo(Optional.of(new DateRange(LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 5))));
    }

    @Test
    void rejectsReversedBounds() {
        assertThatThrownBy(() -> new DateRange(LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
