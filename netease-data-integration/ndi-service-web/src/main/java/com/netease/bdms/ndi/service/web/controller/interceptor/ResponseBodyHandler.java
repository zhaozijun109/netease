package com.netease.bdms.ndi.service.web.controller.interceptor;

import javax.servlet.http.HttpServletRequest;

import com.netease.bdms.ndi.service.web.util.ResponseResult;
import com.netease.bdms.ndi.service.web.util.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应结果处理器
 */
@ControllerAdvice
public class ResponseBodyHandler implements ResponseBodyAdvice {

  @Override
  public boolean supports(MethodParameter methodParameter, Class aClass) {
    // 对所有方法都支持
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType,
                                Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
    if (!(o instanceof ResponseResult)) {
      return o;
    }
    HttpServletRequest httpRequest = ((ServletServerHttpRequest) serverHttpRequest)
        .getServletRequest();
    ResponseResult response = (ResponseResult) o;

    String reqId = (String) httpRequest.getAttribute(LogConstant.REQ_ID);
    Long startTime = (Long) httpRequest.getAttribute(LogConstant.REQ_START_TIME);
    if (StringUtils.isNotBlank(reqId) && startTime != null) {
      long cost = System.currentTimeMillis() - startTime;
      response.setReqId(reqId);
      response.setCost(cost);
    }
    return response;
  }
}
