package com.netease.bdms.ndi.service.web.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName WhiteListUtil
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class WhiteListUtil {
  public static List<String> str2List(String string) {
    List<String> list = new ArrayList<>();
    String[] strs = string.split(",");
    if (strs != null && strs.length > 0) {
      for (String str : strs) {
        list.add(str);
      }
    }
    return list;
  }
}
