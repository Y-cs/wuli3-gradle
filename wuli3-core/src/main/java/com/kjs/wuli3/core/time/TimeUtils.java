package com.kjs.wuli3.core.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtils {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private TimeUtils() {
    }

    public static ZoneId defaultZone() {
        return DEFAULT_ZONE;
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    public static Clock defaultClock() {
        return Clock.system(DEFAULT_ZONE);
    }
}
