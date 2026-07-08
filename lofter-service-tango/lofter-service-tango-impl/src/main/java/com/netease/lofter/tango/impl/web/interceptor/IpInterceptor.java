package com.netease.lofter.tango.impl.web.interceptor;

import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IpInterceptor extends AbstractInterceptor {

    public static final String X_FROM_IP = "x-from-ip";

    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put(X_FROM_IP, request.getHeader(X_FROM_IP));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(X_FROM_IP);
    }
}
