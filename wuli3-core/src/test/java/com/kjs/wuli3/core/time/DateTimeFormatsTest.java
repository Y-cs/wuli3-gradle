package com.kjs.wuli3.core.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class DateTimeFormatsTest {

    @Test
    void formatsStableApiValues() {
        final LocalDate date = LocalDate.of(2026, 7, 8);
        final LocalTime time = LocalTime.of(10, 30, 5);
        final LocalDateTime dateTime = LocalDateTime.of(date, time);

        assertThat(date.format(DateTimeFormats.DATE)).isEqualTo("2026-07-08");
        assertThat(time.format(DateTimeFormats.TIME)).isEqualTo("10:30:05");
        assertThat(dateTime.format(DateTimeFormats.DATE_TIME)).isEqualTo("2026-07-08 10:30:05");
        assertThat(date.format(DateTimeFormats.COMPACT_DATE)).isEqualTo("20260708");
    }
}
