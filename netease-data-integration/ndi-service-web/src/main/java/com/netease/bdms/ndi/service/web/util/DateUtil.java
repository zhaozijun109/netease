package com.netease.bdms.ndi.service.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName DateUtil
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class DateUtil {

  public static final String format(Date date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String dateString = simpleDateFormat.format(date);
    return dateString;
  }

  public static void main(String[] args) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    System.out.println(simpleDateFormat.format(new Date(0)));
  }
}
