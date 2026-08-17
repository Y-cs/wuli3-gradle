package com.kjs.wuli3.core.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * API 和基础设施共享的稳定日期与时间格式。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DateTimeFormats {

    /** Wuli 应用的默认时区。 */
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
