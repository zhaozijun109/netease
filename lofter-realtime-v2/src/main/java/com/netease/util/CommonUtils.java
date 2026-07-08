package com.netease.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CommonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * Encode base64.
     *
     * @param message
     * @return
     */
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

    /**
     * Encoder MD5.
     *
     * @param value
     * @return
     */
    public static String getMD5(String value) {
        String result = "";
        try {
            result = getMD5(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Failed to get md5 of {}", value, e);
        }
        return result;
    }
}
