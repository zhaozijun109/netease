package com.netease.bdms.ndi.service.web.controller.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@Aspect
@Component
public class LogAspect {
  private static Logger logger = LoggerFactory.getLogger(LogAspect.class);

  @Around("execution(public * com.netease.bdms.ndi.service.web.controller.*.*(..)) " +
      "&& !execution(public * com.netease.bdms.ndi.service.web.controller.CheckController.*(..))")
  public Object LogAspect(ProceedingJoinPoint point) throws Throwable {
    long start = System.currentTimeMillis();
    Object result = null;
    Throwable th = null;
    try {
      result = point.proceed();
    } catch (Throwable throwable) {
      th = throwable;
      throw th;
    } finally {
      addLog(point, result, th, (System.currentTimeMillis() - start));
    }

    return result;
  }

  private void addLog(ProceedingJoinPoint joinPoint, Object result, Throwable e, long time) {
    String args = "";
    try {
      ArrayList<Object> printedArgs = new ArrayList<Object>();
      for (Object arg : joinPoint.getArgs()) {
        if (!(arg instanceof MultipartFile ||
          arg instanceof HttpServletResponse ||
          arg instanceof HttpServletRequest)) {
          printedArgs.add(arg);
        }
      }

      args = JSONObject.toJSONString(printedArgs);
    } catch (Exception e1) {
      logger.error(e.getMessage(), e);
    }

    String resultJSON = JSONObject.toJSONString(result);
    if (resultJSON.length() > 2048) {
      resultJSON = resultJSON.substring(0, 2048);
    }
    String method = joinPoint.getSignature().getName();
    String log = String.format("### method = %s, args = %s, result = %s" + ",cost_time=%s", method, args, resultJSON, time);
    if (e == null) {
      logger.info(log);
    } else {
      logger.error(log, e);
    }
  }
}
