package com.netease.bdms.ndi.service.web.util;

import java.util.Random;

/**
 * @ClassName TaskIdUtil
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class TaskIdUtil {
  public static final String get() {
    Random random = new Random();
    String id = System.currentTimeMillis() + String.format("%03d", random.nextInt(100));
    return id;
  }

  public static void main(String[] args) {
    Random random = new Random();
    System.out.println(String.format("%03d", random.nextInt(100)));
  }
}
