package com.netease.wm.hubble.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * sign helper for openai 163yun
 *
 * https://openai.nos-jd.163yun.com/%E6%8E%A5%E5%8F%A3%E9%89%B4%E6%9D%83.pdf
 */
public class SignHelper {
    static String DEFAULT_CHARSET = "UTF-8";

    /**
     * 1.请求参数对按key进行字典升序排序 * * @param params
     */
    public static Map keysort(Map params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        Map sortMap = new TreeMap<>(Comparator.naturalOrder());
        sortMap.putAll(params);
        return sortMap;
    }

    /**
     * 2. 将列表中的参数对按URL键值对的格式拼接成字符串，value部分需要URL编码，并且拼接appkey得到字符串key1=value1&key2=value2&appkey=密钥  *  * @param params  * @return
     */
    public static String contactEncode(Map params, String appKey) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        params.forEach((key, value) -> {
            try {
                if (value instanceof ArrayList) {
                    ArrayList objects = (ArrayList) value;
                    for (Object obj : objects) {
                        String str = String.valueOf(obj);
                        sb.append(key).append("=").append(URLEncoder.encode(str, DEFAULT_CHARSET)).append("&");
                    }
                    return;
                }
                if (value != null && value != "") {
                    sb.append(key).append("=").append(URLEncoder.encode(String.valueOf(value), DEFAULT_CHARSET)).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        sb.append("appkey").append("=").append(appKey);
        return sb.toString();
    }

    /**
     * 3.字符串进行MD5运算，并转成大写 * * @param str * @return
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] b = md.digest();
            int temp;
            StringBuffer sb = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                temp = b[offset];
                if (temp < 0) {
                    temp += 256;
                }
                if (temp < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(temp));
            }
            str = sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 计算请求签名 * * @param params 参数列表，包含业务参数以及header参数 * @param appKey 应用密钥 * @return
     */
    public static String getSign(Map params, String appKey) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        return md5(contactEncode(keysort(params), appKey));
    }
}
