package com.netease.bdms.ndi.service.web.controller.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.helper.AacHelper;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SessionInterceptor
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class SessionInterceptor extends HandlerInterceptorAdapter {
  private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

  @Autowired
  private ProjectConfigUtil configUtil;
  @Autowired
  private SessionHandler sessionHandler;
  @Autowired
  private UserService userService;
  @Autowired
  private AacHelper aacHelper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String sessionId = CookieUtil.get(request, CommonConstants.Token.NAME);
    //更新session超时时间
    if (StringUtils.isNotBlank(sessionId)) {
      String workerStr = (String) sessionHandler.getSession(sessionId);
      if (StringUtils.isNotBlank(workerStr)) {
        Worker worker = JSONObject.parseObject(workerStr, Worker.class);
        if (worker != null) {
          Integer productId = userService.getProductId(worker.getEmail(), worker.getProduct());
          String clusterId = userService.getClusterId(worker.getEmail(), worker.getProduct(), worker.getCluster());
          worker.setProductId(productId);
          worker.setClusterId(clusterId);
          sessionHandler.addSession(sessionId, worker, CommonConstants.REDIS_SESSION_EXPIRE);
          NdiContext.put(ContextConstant.EMAIL, worker.getEmail());
          NdiContext.put(ContextConstant.PRODUCT_ID, worker.getProductId());
          NdiContext.put(ContextConstant.CLUSTER_ID, worker.getClusterId());
          NdiContext.put(ContextConstant.PRODUCT, worker.getProduct());
          NdiContext.put(ContextConstant.CLUSTER, worker.getCluster());
          NdiContext.put(ContextConstant.WORKER, worker);
        } else {
          return false;
        }
        log.info("User info:" + worker.toString());
        return true;
      }
      else {
        aacHelper.clearToken(sessionId);
      }
    }
    String realRemoteAddr = getClientAddr(request);
    //接口白名单和IP白名单
    if (isExcludedURL(request.getServletPath()) || isExcluededIP(realRemoteAddr)) {
      return true;
    }
    log.info("User session don't exist");
    response.setCharacterEncoding("utf-8");
    response.setContentType("application/json");
    Map<String, Object> rtn = new HashMap<>();
    rtn.put("code", ProcessStatusEnum.NO_LOGIN.getCode());
    rtn.put("message", ProcessStatusEnum.NO_LOGIN.getMessage());
    byte[] buf = JSONObject.toJSONBytes(rtn);
    OutputStream os = null;
    try {
      os = response.getOutputStream();
      os.write(buf);
    } catch (IOException e) {
      log.error("", e);
    } finally {
      try {
        if (os != null){
          os.close();
        }
      } catch (IOException ignored) {
      }
    }
    return false;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    try {
      NdiContext.clear();
    } catch (Exception ignored) {
    }
  }

  private boolean isExcludedURL(String url) {
    List<String> apis = WhiteListUtil.str2List(configUtil.get(CommonConstants.WhiteList.API));
    if (apis != null && apis.size() > 0) {
      for (String api : apis) {
        if (url.startsWith(api)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isExcluededIP(String ip) {
    List<String> ips = WhiteListUtil.str2List(configUtil.get(CommonConstants.WhiteList.IP));
    if (ips.contains(ip)) {
      return true;
    }
    return false;
  }

  /**
   * 获取客户端的真实IP地址
   * @param request
   * @return
   */
  private String getClientAddr(HttpServletRequest request){
    String remoteAddr = request.getHeader("X-From-IP");
    String forward = request.getHeader("X-Forwarded-For");
    String host = request.getHeader("Host");
    log.debug("=== nginx ===");
    log.debug("remoteAddr: " + remoteAddr);
    log.debug("forward: " + forward);
    log.debug("host: " + host);
    log.debug("*** nginx ***");
    return remoteAddr;
  }
}
