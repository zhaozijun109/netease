package com.netease.bdms.ndi.service.web.config;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @ClassName NdiContext
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class NdiContext {

  private static final ThreadLocal<Map<String, Object>> MAP_HOLDER = ThreadLocal.withInitial(Maps::newHashMap);

  public static void put(String key, Object value) {
    MAP_HOLDER.get().put(key, value);
  }

  public static <T> T get(String key) {
    return (T) MAP_HOLDER.get().get(key);
  }

  public static <T> T remove(String key) {
    return (T) MAP_HOLDER.get().remove(key);
  }

  public static void clear() {
    MAP_HOLDER.remove();
  }
}
