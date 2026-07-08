package com.netease.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Time formatter util. */
public class DateTimeFormatterUtils {
    // private static final ZoneId zoneId = ZoneId.of("Etc/UTC");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_HOUR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

    public String dateFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_FORMATTER.format(localDateTime);
    }

    public String dateTimeFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    public String dateTimeHourFormat(long timestampMillis) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
        return DATE_TIME_HOUR_FORMATTER.format(localDateTime);
    }

    public Long dateTransformTime(Timestamp dateTime) {
        return dateTime.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
