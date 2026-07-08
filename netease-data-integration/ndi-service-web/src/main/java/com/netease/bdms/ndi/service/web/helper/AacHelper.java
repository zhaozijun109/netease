package com.netease.bdms.ndi.service.web.helper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants.AacService;
import com.netease.bdms.ndi.service.web.util.HttpUtil;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import com.netease.bdms.ndi.service.web.util.ProjectConfigUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * AAC服务帮助类
 *
 * @author ginger
 * @create 2019-10-29 2:06 下午
 */
@Slf4j
@Component
public class AacHelper {

  @Autowired
  private ProjectConfigUtil configUtil;

  private static final String VERIFY = "/api/v1/auth/verify?indexUrl=";

  private static final String TOKEN_CLEAR = "/api/v1/auth/token/clear";

  private static final String PATH_LOGIN = "/aac/api/auth/v1/login";

  private static final String PATH_LOGOUT = "/aac/api/auth/v1/logout";

  private static final String PATH_CLEAR_TOKEN = "/aac/api/auth/v1/token/clear";

  private static final String PATH_GET_USER = "/aac/api/auth/v1/user/get";


  /**
   * 构建登录地址
   *
   * @param indexUrl 登录后的首页地址（已编码）
   * @return 登录地址
   */
  public String buildLoginUrl(String indexUrl) throws UnsupportedEncodingException {
    if (StringUtils.isBlank(indexUrl)) {
      indexUrl = URLEncoder.encode(configUtil.get(AacService.APP_INDEX_URI), "UTF-8");
    }

    String loginUrl = configUtil.get(AacService.SERVER_URI) + configUtil.get(AacService.PATH_LOGIN);
    String callbackUrl = configUtil.get(AacService.APP_INDEX_URI) + VERIFY + indexUrl;
    String clearTokenUrl = configUtil.get(AacService.APP_CLEAR_URI) + TOKEN_CLEAR;
    return new StringBuilder()
        .append(loginUrl)
        .append("?redirectUrl=").append(URLEncoder.encode(callbackUrl, "UTF-8"))
        .append("&clearTokenUrl=").append(URLEncoder.encode(clearTokenUrl, "UTF-8"))
        .toString();
  }

  /**
   * 构建登出地址
   *
   * @return 登出地址
   */
  public String buildLogoutUrl() throws UnsupportedEncodingException {
    return new StringBuilder()
        .append(configUtil.get(AacService.SERVER_URI)).append(configUtil.get(AacService.PATH_LOGOUT))
        .append("?redirectUrl=").append(URLEncoder.encode(configUtil.get(AacService.APP_INDEX_URI), "UTF-8"))
        .toString();
  }

  /**
   * 获取用户信息
   *
   * @param token AAC分配的token
   * @return 获取到的用户信息
   */
  public Worker getUser(String token) {
    String getUserUrl = configUtil.get(AacService.SERVER_URI) + configUtil.get(AacService.PATH_GET_USER)
        + "?localToken=" + token;
    JSONObject response = HttpUtil.doGet(getUserUrl);
    log.info("get user from aac, token={}, rsp={}", token, response);

    if (null == response.get("code") || 0 != response.getInteger("code")) {
      throw new NdiException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
          "invalid aac rsp code");
    }

    JSONObject result = response.getJSONObject("result");
    String email = result.getString("email");
    String fullName = result.getString("fullName");
    if (Strings.isNullOrEmpty(fullName) || Strings.isNullOrEmpty(email)) {
      throw new NdiException(ProcessStatusEnum.HTTP_ENDPOINT_RESPONSE_ERROR.getCode(),
          "invalid aac rsp email or fullName");
    }
    log.info("aac auth successfully, token={}, email={}", token, email);
    return new Worker(fullName, email);
  }

  /**
   * 清理token
   *
   * @param token AAC分配的token
   */
  public void clearToken(String token) {
    if (StringUtils.isBlank(token)) {
      return;
    }
    try {
      String clearTokenUrl = configUtil.get(AacService.SERVER_URI) + configUtil.get(AacService.PATH_CLEAR_TOKEN);
      Map<String, Object> params = Maps.newHashMap();
      params.put("localToken", token);
      String rsp = HttpUtil.doPostWithPathParams(clearTokenUrl, params);
      JSONObject response = JSONObject.parseObject(rsp);
      log.info("clear token in aac, token={}, rsp={}", token, rsp);
      Integer code = response.getInteger("code");
      if (code != 0) {
        log.warn("清除aac token失败");
      }
    } catch (Exception e) {
      log.warn("aac clear token error, token={}", token, e);
    }
  }
}

