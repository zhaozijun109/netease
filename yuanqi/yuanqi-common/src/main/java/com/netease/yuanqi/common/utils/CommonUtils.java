package com.netease.yuanqi.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    private static final Pattern VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+).*");

    /** Encode base64. */
    public static String getBase64(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes());
    }

    public static String getMD5(byte[] bytes) {
        char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        char[] str = new char[16 * 2];
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte[] tmp = md.digest();
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
        } catch (Exception e) {
            LOG.error("failed to calculate md5 ,param byte[]", e);
        }
        return new String(str);
    }

    /** Encoder MD5. */
    public static String getMD5(String value) {
        String result = "";
        try {
            result = getMD5(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Failed to get md5 of {}", value, e);
        }
        return result;
    }

    public static Integer getAppVersionNumber(String appVersion) {
        if (appVersion == null) {
            return 0;
        }
        Matcher m = VERSION_REGEX.matcher(appVersion);
        if (!m.matches()) {
            return 0;
        }
        return Integer.parseInt(m.group(1)) * 10000
                + Integer.parseInt(m.group(2)) * 100
                + Integer.parseInt(m.group(3));
    }
}
