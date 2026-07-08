package com.netease.easyml.common.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by linjiuning on 2020/2/22.
 */
public class ResourceManager {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);
    private static final Map<String, Object> INSTANCES = new HashMap<>();

    public synchronized static <T> T getOrCreate(String key, Supplier<T> supplier) {
        if (INSTANCES.containsKey(key)) {
            return (T) INSTANCES.get(key);
        } else {
            T instance = supplier.get();
            INSTANCES.put(key, instance);
            return instance;
        }
    }

    public synchronized static void remove(String key) {
        if (containsKey(key)) {
            Object obj = INSTANCES.remove(key);
            try {
                if (obj instanceof AutoCloseable) {
                    ((AutoCloseable) obj).close();
                }
            } catch (Exception e) {
                log.error("Exception: " + e.getMessage());
            }
        }
    }

    public synchronized static boolean containsKey(String key) {
        return INSTANCES.containsKey(key);
    }
}
