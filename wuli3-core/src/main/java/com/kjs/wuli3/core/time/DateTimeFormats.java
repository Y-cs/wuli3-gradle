package com.kjs.wuli3.core.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Stable date and time formats shared by APIs and infrastructure code.
 */
public final class DateTimeFormats {

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String COMPACT_DATE_PATTERN = "yyyyMMdd";

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern(DateTimeFormats.DATE_PATTERN, Locale.ROOT);
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern(DateTimeFormats.TIME_PATTERN, Locale.ROOT);
    public static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern(DateTimeFormats.DATE_TIME_PATTERN, Locale.ROOT);
    public static final DateTimeFormatter COMPACT_DATE =
            DateTimeFormatter.ofPattern(DateTimeFormats.COMPACT_DATE_PATTERN, Locale.ROOT);

    private DateTimeFormats() {}
}
