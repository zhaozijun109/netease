package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CookieUtil {

  public static void setCookies(HttpServletResponse response, String username, String email, String product, String cluster) {
    Cookie c = new Cookie(CommonConstants.COOKIE_USER_NAME, username);
    c.setPath("/");
    response.addCookie(c);

    c = new Cookie(CommonConstants.COOKIE_EMAIL, email);
    c.setPath("/");
    response.addCookie(c);

    c = new Cookie(CommonConstants.COOKIE_PRODUCT, product);
    c.setPath("/");
    response.addCookie(c);

    c = new Cookie(CommonConstants.COOKIE_CLUSTER, cluster);
    c.setPath("/");
    response.addCookie(c);
  }

  public static void add(HttpServletResponse response, String name, String value) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }

  public static void add(HttpServletResponse response, String name, String value, int age) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(age);
    response.addCookie(cookie);
  }

  public static String get(HttpServletRequest request, String name) {
    String value;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (StringUtils.equals(name, cookie.getName())) {
          value = cookie.getValue();
          return value;
        }
      }
    }
    return null;
  }

  public static void remove(HttpServletRequest request, HttpServletResponse response, String... names) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null && cookies.length > 0) {
      for (Cookie cookie : cookies) {
        for (String name : names) {
          if (StringUtils.equals(cookie.getName(), name)) {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
          }
        }
      }
    }
  }
}
