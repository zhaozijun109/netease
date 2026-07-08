package com.netease.easyml.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linjiuning on 2020/7/14.
 */

public class DateUtil {
    public static String replaceDynamicDate(String str, long timeMills) {
        if (str == null) return null;
        // Pattern pattern = Pattern.compile("\\$\\{([^\\d]+?)(,\\s?(-?\\d+?)([yMdHhmsE]))?\\}");
        Pattern pattern = Pattern.compile("\\$\\{([YyMmDdHFhSsE]+)?((-?\\d+))?\\}");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timeMills);
            String numStr = matcher.group(2);
            if (numStr != null) {
                char unit = matcher.group(1).charAt(matcher.group(1).length() - 1);
                int num = Integer.parseInt(numStr);
                switch (unit) {
                    case 'Y':
                    case 'y':
                        c.add(Calendar.YEAR, num);
                        break;
                    case 'M':
                        c.add(Calendar.MONTH, num);
                        break;
                    case 'D':
                    case 'd':
                        c.add(Calendar.DATE, num);
                        break;
                    case 'H':
                        c.add(Calendar.HOUR, num);
                        break;
                    case 'h':
                        c.add(Calendar.HOUR, num);
                        break;
                    case 'F':
                        c.add(Calendar.MINUTE, num);
                        break;
                    case 's':
                        c.add(Calendar.SECOND, num);
                        break;
                    case 'E':
                        c.add(Calendar.DATE, num * 7);
                }
            }
            String p2 = matcher.group(1);
            p2 = p2.replace('Y', 'y').replace('D', 'd').replace('F', 'm');
            SimpleDateFormat sdf = new SimpleDateFormat(p2);
            String date = sdf.format(c.getTime());
            matcher.appendReplacement(sb, date);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> getBetweenDays(String startTime, String endTime) {
        return getBetweenDays(startTime, endTime, "yyyyMMdd");
    }

    public static List<String> getBetweenDays(String startTime, String endTime, String format) {
        List<String> days = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, 1);
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    public static long getTimeMills(String date) throws Exception {
        return getTimeMills(date, "yyyyMMdd");
    }

    public static long getTimeMills(String date, String format) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = sdf.parse(date);
        return d.getTime();
    }

    public enum Season {
        SPRING, SUMMER, FALL, WINTER;

        public static Season getSeason(int month) {
            assert (month >= 1 && month <= 12) : "Month must in range [1, 12]";
            switch (month) {
                case 11:
                case 12:
                case 1:
                case 2:
                    return WINTER;
                case 3:
                case 4:
                    return SPRING;
                case 5:
                case 6:
                case 7:
                case 8:
                    return SUMMER;
                default:
                    return FALL;
            }
        }
    }

    public static String getYear() {
        return new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime());
    }

    public static String getMonth() {
        return new SimpleDateFormat("MM").format(Calendar.getInstance().getTime());
    }

    public static String getDay() {
        return new SimpleDateFormat("dd").format(Calendar.getInstance().getTime());
    }

    public static Season getSeason() {
        return Season.getSeason(Integer.parseInt(getMonth()));
    }

    public static String getToday() {
        return getToday("yyyyMMdd");
    }

    public static String getToday(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public static String dateOffset(String beginDate, int offset) throws ParseException {
        return dateOffset(beginDate, offset, "yyyyMMdd");
    }

    public static String dateOffset(String beginDate, int offset, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFormat.parse(beginDate));
        cal.add(Calendar.DATE, offset);
        return dateFormat.format(cal.getTime());
    }

    public static String monthOffset(String beginDate, int offset) throws ParseException {
        return monthOffset(beginDate, offset, "yyyyMM");
    }

    public static String monthOffset(String beginDate, int offset, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateFormat.parse(beginDate));
        cal.add(Calendar.MONTH, offset);
        return dateFormat.format(cal.getTime());
    }

    public static long interval(String date) {
        return interval(date, "yyyyMMdd");
    }

    public static long interval(String date, String format) {
        LocalDate today = LocalDate.now();
        return Duration.between(LocalDate.parse(date, DateTimeFormatter.ofPattern(format)).atStartOfDay(), today.atStartOfDay()).toDays();
    }

    public static String getFormatTime(long time, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(time);
    }
}