package com.kjs.wuli3.core.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeFormatters {
    public static final String DATE = "yyyy-MM-dd";
    public static final String TIME = "HH:mm:ss";
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_COMPACT = "yyyyMMdd";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE, Locale.ROOT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME, Locale.ROOT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME, Locale.ROOT);

    private TimeFormatters() {
    }

    public static String formatDate(LocalDate value) {
        return value.format(DATE_FORMATTER);
    }

    public static String formatDate(LocalDate value, String pattern) {
        return value.format(formatter(pattern));
    }

    public static String formatTime(LocalTime value) {
        return value.format(TIME_FORMATTER);
    }

    public static String formatTime(LocalTime value, String pattern) {
        return value.format(formatter(pattern));
    }

    public static String formatDateTime(LocalDateTime value) {
        return value.format(DATE_TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime value, String pattern) {
        return value.format(formatter(pattern));
    }

    public static LocalDate parseDate(String value) {
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    public static LocalDate parseDate(String value, String pattern) {
        return LocalDate.parse(value, formatter(pattern));
    }

    public static LocalTime parseTime(String value) {
        return LocalTime.parse(value, TIME_FORMATTER);
    }

    public static LocalTime parseTime(String value, String pattern) {
        return LocalTime.parse(value, formatter(pattern));
    }

    public static LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String value, String pattern) {
        return LocalDateTime.parse(value, formatter(pattern));
    }

    private static DateTimeFormatter formatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }
}
