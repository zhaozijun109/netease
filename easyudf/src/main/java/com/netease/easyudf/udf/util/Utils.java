package com.netease.easyudf.udf.util;

public class Utils {

    public static Integer toInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return Integer.parseInt(value.toString());
    }

    public static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return Long.parseLong(value.toString());
    }

    public static Float toFloat(Object value) {
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        return Float.parseFloat(value.toString());
    }

    public static Double toDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return Double.parseDouble(value.toString());
    }

    public static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
}
