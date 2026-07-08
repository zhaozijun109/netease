package com.netease.easyudf.udf.util;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DayOfWeekUDF extends UDF {

    public String evaluate(String today, String dayOfWeek) {
        return evaluate(today, dayOfWeek, 0);
    }

    public String evaluate(String today, String dayOfWeek, long offset) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate td = LocalDate.parse(today, formatter);
        DayOfWeek dow = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate prevMonday = td.with(dow).minusDays(7 * offset);
        return prevMonday.toString();
    }
}