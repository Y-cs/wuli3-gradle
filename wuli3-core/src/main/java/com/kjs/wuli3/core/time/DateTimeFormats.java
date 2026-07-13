package com.kjs.wuli3.core.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Stable date and time formats shared by APIs and infrastructure code.
 */
public final class DateTimeFormats {

    /** Default time zone for Wuli applications. */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static final String DATE_PATTERN = "uuuu-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String DATE_TIME_PATTERN = "uuuu-MM-dd HH:mm:ss";
    public static final String COMPACT_DATE_PATTERN = "uuuuMMdd";

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern(DateTimeFormats.DATE_PATTERN, Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern(DateTimeFormats.TIME_PATTERN, Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern(
                    DateTimeFormats.DATE_TIME_PATTERN, Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern(
                    DateTimeFormats.COMPACT_DATE_PATTERN, Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private DateTimeFormats() {}
}
