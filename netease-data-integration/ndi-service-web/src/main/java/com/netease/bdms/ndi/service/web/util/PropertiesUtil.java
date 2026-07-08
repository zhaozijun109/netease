package com.netease.bdms.ndi.service.web.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
  private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
  private static Properties properties;

  static {
    String fileName = "common.properties";
    properties = new Properties();
    try {
      properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
    } catch (IOException e) {
      log.error("Failed to load properties file", e);
    }
  }

  public static String getProperty(String key) {
    if (key != null) {
      String value = properties.getProperty(key.trim());
      if (StringUtils.isNoneBlank(value.trim())) {
        return value.trim();
      }
    }

    return null;
  }

  public static String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    if (value != null) {
      return value;
    }

    return defaultValue;
  }

  public static void main(String[] args) {
    Map<String, Integer> map = new HashMap<>();
    map.put("1", 1);
    log.info(map.toString());


    try {
      String string = null;
      System.out.println(string.getBytes());
    } catch (Exception e) {
      log.warn(e.toString(), e);
    }
    System.out.println("Test pass");
  }
}
