package com.netease.bdms.ndi.service.web.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.util.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 日志拦截器
 *
 * @author
 * @create 2019-09-20 16:50
 */
public class LogInterceptor implements HandlerInterceptor {

  private final static Logger log = LoggerFactory.getLogger("NDI-TRACE");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String reqId = request.getParameter(LogConstant.REQ_ID);
    if (StringUtils.isBlank(reqId)) {
      reqId = UUID.randomUUID().toString().replace("-", "");
    }
    request.setAttribute(LogConstant.REQ_ID, reqId);
    request.setAttribute(LogConstant.REQ_START_TIME, System.currentTimeMillis());
    MDC.put(LogConstant.REQ_ID, reqId);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                         ModelAndView modelAndView) throws Exception {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) throws Exception {
    try {
      long startTime = (long) request.getAttribute(LogConstant.REQ_START_TIME);
      long endTime = System.currentTimeMillis();
      long cost = endTime - startTime;
      String reqId = MDC.get(LogConstant.REQ_ID);
      MDC.remove(LogConstant.REQ_ID);
      String email = NdiContext.get(ContextConstant.EMAIL);
      Map<String, Object> map = Maps.newHashMap();
      map.put("currentTime", endTime);
      map.put("reqId", reqId);
      map.put("requestUri", URLDecoder.decode(request.getRequestURI(), "UTF-8"));
      if (null != request.getQueryString()) {
        map.put("queryString", URLDecoder.decode(request.getQueryString(), "UTF-8"));
      }
      map.put("cost", cost);
      if (StringUtils.isNotBlank(email)) {
        map.put("email", email);
      }
      log.info(JSONObject.toJSONString(map));
    } catch (Exception ignored) { }
  }
}
