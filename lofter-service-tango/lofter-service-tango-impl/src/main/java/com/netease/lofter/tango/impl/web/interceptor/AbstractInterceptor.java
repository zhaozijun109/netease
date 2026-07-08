package com.netease.lofter.tango.impl.web.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        DispatcherType dispatcherType = request.getDispatcherType();
        if (DispatcherType.ASYNC.equals(dispatcherType) || DispatcherType.ERROR.equals(dispatcherType)) {
            return true;
        }
        return this.doPreHandle(request, response, handler);
    }

    protected abstract boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;


}
