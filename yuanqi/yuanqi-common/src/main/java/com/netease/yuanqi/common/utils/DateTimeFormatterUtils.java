package com.netease.yuanqi.common.utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Time formatter util. */
public class DateTimeFormatterUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    // private static final ZoneId zoneId = ZoneId.of("Etc/UTC");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_HOUR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
    private static final DateTimeFormatter DATE_TIME_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][X]");

    public static String dateFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_FORMATTER.format(localDateTime);
    }

    public static String dateTimeFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static String dateTimeHourFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_TIME_HOUR_FORMATTER.format(localDateTime);
    }

    public static Long dateTimeFormatTransformTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_ZONE_FORMATTER)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /** 将 ISO-8601 带时区的时间字符串（例如 2026-06-08T09:17:27Z）转换为毫秒时间戳. */
    public static Long isoZonedDateTimeFormatTransformTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_ZONE_FORMATTER)
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli();
    }

    public static Long dateTransformTime(Timestamp dateTime) {
        return dateTime.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
