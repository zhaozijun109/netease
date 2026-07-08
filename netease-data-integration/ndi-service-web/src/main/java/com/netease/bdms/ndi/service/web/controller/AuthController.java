package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.facade.AuthFacade;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.CookieUtil;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 认证控制器
 *
 * @author
 * @create 2019-10-28 5:50 下午
 */
@Slf4j
@Controller
@RequestMapping("/api/v1/auth")
public class AuthController {

  @Autowired
  private AuthFacade authFacade;
  @Autowired
  private RedisUtil redisUtil;

  /**
   * 用户请求登录
   *
   * 重定向向到AAC进行验证，验证完成后会回调
   */
  @GetMapping("/login")
  public void login(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String referer = request.getHeader("referer");
    String serverName = request.getServerName();
    if ((referer != null && referer.startsWith("http://localhost")) || StringUtils.equals(serverName, "localhost")) {
      loginfake(request, response);
      response.sendRedirect(referer);
      return;
    }
    String indexUrl = request.getHeader("Referer");
    response.sendRedirect(authFacade.login(indexUrl));
  }

  /**
   * 用户请求登出
   */
  @GetMapping("/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String token = CookieUtil.get(request, CommonConstants.Token.NAME);
    setCookie(response, null, 0);
    response.sendRedirect(authFacade.logout(token));
  }

  /**
   * AAC登录后回调
   */
  @RequestMapping("/verify")
  public void verify(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String localToken = request.getParameter("localToken");
    log.info("aac verify, localToken={}", localToken);
    if (null == localToken) {
      response.sendRedirect("/");
      return;
    }
    try {
      authFacade.verify(localToken);
      String indexUrl = URLDecoder.decode(request.getParameter("indexUrl"), "UTF-8");
      try {
        new URL(indexUrl);
      } catch (Exception e) {
        indexUrl = "/";
      }
      setCookie(response, localToken, CommonConstants.Token.VALID_S);
      response.sendRedirect(indexUrl);
    } catch (Exception e) {
      log.error("aac verify error, localToken={}", localToken, e);
      response.sendRedirect("/error/no-product");
    }
  }

  /**
   * 清理token
   *
   * @param localToken token
   */
  @ResponseBody
  @GetMapping("/token/clear")
  public void clearToken(@RequestParam(name = "localToken", required = false) String localToken) {
    log.info("clear token from aac, localToken={}", localToken);
    authFacade.clearToken(localToken);
  }

  /**
   * 设置cookie
   *
   * @param token token
   * @param maxAge cookie过期时间
   */
  private void setCookie(HttpServletResponse response, String token, int maxAge) {
    Cookie cookie = new Cookie(CommonConstants.Token.NAME, token);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  public void loginfake(HttpServletRequest request, HttpServletResponse response){
    Worker worker = new Worker("赵敏","zhaomin3@corp.netease.com");
    worker.setProduct("chensuan_test");
    worker.setCluster("滨江");
    String sessionId = request.getSession().getId();
    CookieUtil.add(response, CommonConstants.REDIS_SESSION_ID, sessionId);
    redisUtil.set(sessionId, JSONObject.toJSONString(worker));
    CookieUtil.setCookies(response, worker.getUsername(), worker.getEmail(), worker.getProduct(), worker.getCluster());
  }
}

