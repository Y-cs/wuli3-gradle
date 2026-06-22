package com.kjs.wuli3.core.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TimeFormattersTest {
    @Test
    void formatsCommonDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 22, 10, 30, 5);

        assertThat(TimeFormatters.formatDateTime(dateTime)).isEqualTo("2026-06-22 10:30:05");
    }

    @Test
    void formatsCustomDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 22, 10, 30, 5);

        assertThat(TimeFormatters.formatDateTime(dateTime, "yyyy/MM/dd HH:mm")).isEqualTo("2026/06/22 10:30");
    }

    @Test
    void parsesCommonValues() {
        assertThat(TimeFormatters.parseDate("2026-06-22")).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(TimeFormatters.parseTime("10:30:05")).isEqualTo(LocalTime.of(10, 30, 5));
        assertThat(TimeFormatters.parseDateTime("2026-06-22 10:30:05"))
                .isEqualTo(LocalDateTime.of(2026, 6, 22, 10, 30, 5));
    }

    @Test
    void parsesCustomDate() {
        assertThat(TimeFormatters.parseDate("20260622", TimeFormatters.DATE_COMPACT))
                .isEqualTo(LocalDate.of(2026, 6, 22));
    }
}
