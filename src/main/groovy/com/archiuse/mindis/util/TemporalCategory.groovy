package com.archiuse.mindis.util

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

class TemporalCategory {
    private static final Pattern PATTERN_INSTANT =
            ~/[-+]?\d{4,}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?Z/
    private static final Pattern PATTERN_OFFSET_DATE_TIME =
            ~/[-+]?\d{4,}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?[-+]\d{2}:\d{2}/
    private static final Pattern PATTERN_ZONED_DATE_TIME =
            ~/[-+]?\d{4,}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?[-+]\d{2}:\d{2}\[[^\[\]]+\]/

    static def toTemporal(String str) {
        toLocalDateTime(str)
                ?: toLocalTime(str)
                ?: toLocalDate(str)
                ?: toInstant(str)
                ?: toDuration(str)
                ?: toZonedDateTime(str)
                ?: toOffsetDateTime(str)
                ?: toOffsetTime(str)
                ?: toYearMonth(str)
                ?: toYear(str)
                ?: toMonth(str)
                ?: toMonthDay(str)
                ?: toDayOfWeek(str)
    }

    static boolean isTemporalStr(String str) {
        toTemporal(str) as boolean
    }

    static LocalTime toLocalTime(String str) {
        parseTemporalOrNull(str, LocalTime)
    }

    static boolean isLocalTime(String str) {
        toLocalTime(str) as boolean
    }

    static LocalDate toLocalDate(String str) {
        parseTemporalOrNull(str, LocalDate)
    }

    static boolean isLocalDate(String str) {
        toLocalDate(str) as boolean
    }

    static LocalDateTime toLocalDateTime(String str) {
        parseTemporalOrNull(str, LocalDateTime)
    }

    static boolean isLocalDateTime(String str) {
        toLocalDateTime(str) as boolean
    }

    static Instant toInstant(String str) {
        (str =~ PATTERN_INSTANT).matches() ? parseTemporalOrNull(str, Instant) : null
    }

    static boolean isInstant(String str) {
        toInstant(str) as boolean
    }

    static Year toYear(String str) {
        parseTemporalOrNull(str, Year)
    }

    static boolean isYear(String str) {
        toYear(str) as boolean
    }

    static YearMonth toYearMonth(String str) {
        parseTemporalOrNull(str, YearMonth)
    }

    static boolean isYearMonth(String str) {
        toYearMonth(str) as boolean
    }

    static OffsetTime toOffsetTime(String str) {
        parseTemporalOrNull(str, OffsetTime)
    }

    static boolean isOffsetTime(String str) {
        toOffsetTime(str) as boolean
    }

    static OffsetDateTime toOffsetDateTime(String str) {
        (str =~ PATTERN_OFFSET_DATE_TIME).matches() ? parseTemporalOrNull(str, OffsetDateTime) : null
    }

    static boolean isOffsetDateTime(String str) {
        toOffsetDateTime(str) as boolean
    }

    static ZonedDateTime toZonedDateTime(String str) {
        (str =~ PATTERN_ZONED_DATE_TIME).matches() ? parseTemporalOrNull(str, ZonedDateTime) : null
    }

    static boolean isZonedDateTime(String str) {
        toZonedDateTime(str) as boolean
    }

    static Duration toDuration(String str) {
        parseTemporalOrNull(str, Duration)
    }

    static boolean isDuration(String str) {
        toDuration(str) as boolean
    }

    static Month toMonth(String str) {
        parseEnumOrNull(str, Month)
    }

    static boolean isMonth(String str) {
        toMonth(str) as boolean
    }

    static MonthDay toMonthDay(String str) {
        parseTemporalOrNull(str, MonthDay)
    }

    static boolean isMonthDay(String str) {
        toMonthDay(str) as boolean
    }

    static DayOfWeek toDayOfWeek(String str) {
        parseEnumOrNull(str, DayOfWeek)
    }

    static boolean isDayOfWeek(String str) {
        toDayOfWeek(str) as boolean
    }

    private static <T> T parseTemporalOrNull(String str, Class<T> type) {
        try {
            type.parse(str)
        } catch (DateTimeParseException ignored) {
            null
        }
    }

    private static <T extends Enum<T>> T parseEnumOrNull(String str, Class<T> enumType) {
        try {
            enumType.valueOf(str)
        } catch (IllegalArgumentException ignored) {
            null
        }
    }
}
