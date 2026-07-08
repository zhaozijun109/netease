package com.netease.bdms.ndi.service.web.controller.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.CookieUtil;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName SessionHandler
 * @Description session处理
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class SessionHandler {
  @Autowired
  private RedisUtil redisUtil;

  public void addSession(HttpServletResponse response, String sessionId, Object object, int expire) {
    CookieUtil.add(response, CommonConstants.REDIS_SESSION_ID, sessionId);
    redisUtil.setEx(sessionId, JSONObject.toJSONString(object), expire);
  }

  /**
   * 添加session
   * 外部去设置cookie
   *
   * @param sessionId
   * @param object
   * @param expire
   */
  public void addSession(String sessionId, Object object, int expire) {
    redisUtil.setEx(sessionId, JSONObject.toJSONString(object), expire);
  }

  public void updateSession(HttpServletRequest request, Object object, int expire) {
    String sessionId = CookieUtil.get(request, CommonConstants.REDIS_SESSION_ID);
    redisUtil.setEx(sessionId, JSONObject.toJSONString(object), expire);
  }

  public void updateSession(String sessionId, Object object, int expire) {
    redisUtil.setEx(sessionId, JSONObject.toJSONString(object), expire);
  }

  public void deleteSession(String sessionId) {
    redisUtil.del(sessionId);
  }

  public Object getSession(String sessionId) {
    Object o = redisUtil.get(sessionId);
    return o;
  }

  public String generateSessionId(HttpServletRequest request){
    String sessionId = request.getSession().getId();
    return sessionId;
  }
}
