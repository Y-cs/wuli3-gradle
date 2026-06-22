package com.kjs.wuli3.core.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TimeFormatters {
    public static final String DATE = "yyyy-MM-dd";
    public static final String DATE_COMPACT = "yyyyMMdd";
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_TIME_COMPACT = "yyyyMMddHHmmss";
    public static final String TIME = "HH:mm:ss";
    public static final String TIME_MILLIS = "HH:mm:ss.SSS";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE, Locale.CHINA);
    public static final DateTimeFormatter DATE_COMPACT_FORMATTER = DateTimeFormatter.ofPattern(DATE_COMPACT, Locale.CHINA);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME, Locale.CHINA);
    public static final DateTimeFormatter DATE_TIME_MILLIS_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_MILLIS, Locale.CHINA);
    public static final DateTimeFormatter DATE_TIME_COMPACT_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_COMPACT, Locale.CHINA);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME, Locale.CHINA);
    public static final DateTimeFormatter TIME_MILLIS_FORMATTER = DateTimeFormatter.ofPattern(TIME_MILLIS, Locale.CHINA);

    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    private TimeFormatters() {
    }

    public static DateTimeFormatter ofPattern(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, value -> DateTimeFormatter.ofPattern(value, Locale.CHINA));
    }

    public static String formatDate(LocalDate date) {
        return format(date, DATE_FORMATTER);
    }

    public static String formatDate(LocalDate date, String pattern) {
        return format(date, ofPattern(pattern));
    }

    public static String formatTime(LocalTime time) {
        return format(time, TIME_FORMATTER);
    }

    public static String formatTime(LocalTime time, String pattern) {
        return format(time, ofPattern(pattern));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return format(dateTime, DATE_TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return format(dateTime, ofPattern(pattern));
    }

    public static String formatDateTime(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static LocalDate parseDate(String value) {
        return parseDate(value, DATE_FORMATTER);
    }

    public static LocalDate parseDate(String value, String pattern) {
        return parseDate(value, ofPattern(pattern));
    }

    public static LocalDate parseDate(String value, DateTimeFormatter formatter) {
        return LocalDate.parse(value, formatter);
    }

    public static LocalTime parseTime(String value) {
        return parseTime(value, TIME_FORMATTER);
    }

    public static LocalTime parseTime(String value, String pattern) {
        return parseTime(value, ofPattern(pattern));
    }

    public static LocalTime parseTime(String value, DateTimeFormatter formatter) {
        return LocalTime.parse(value, formatter);
    }

    public static LocalDateTime parseDateTime(String value) {
        return parseDateTime(value, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String value, String pattern) {
        return parseDateTime(value, ofPattern(pattern));
    }

    public static LocalDateTime parseDateTime(String value, DateTimeFormatter formatter) {
        return LocalDateTime.parse(value, formatter);
    }

    private static String format(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    private static String format(LocalTime time, DateTimeFormatter formatter) {
        return time.format(formatter);
    }

    private static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }
}
