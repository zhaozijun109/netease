package com.netease.easyml.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by linjiuning on 2018/12/31.
 */
public class MiscUtil {

    public static <K, V> Cache<K, V> getGuavaCache() {
        return CacheBuilder.newBuilder()
                .build();
    }

    public static <K, V> Cache<K, V> getGuavaCache(long maximumSize) {
        return CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .build();
    }

    public static <K, V> Cache<K, V> getGuavaCache(long maximumSize, long duration, TimeUnit unit) {
        return CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(duration, unit)
                .build();
    }
}
