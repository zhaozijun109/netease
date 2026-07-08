package com.netease.lofter.tango.integration.util;

public class AssertUtils {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String value, String message) {
        isTrue(value != null && !value.trim().isEmpty(), message);
    }
}
