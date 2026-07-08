package com.netease.lofter.tango.impl.web.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsInterceptor extends AbstractInterceptor {


    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.isNotBlank(origin)) {
            UriComponents originUrl = UriComponentsBuilder.fromOriginHeader(origin).build();
            String serverName = originUrl.getHost();
            if (serverName == null) {
                return false;
            }
            boolean allow = serverName.endsWith("lofter.com")
                    || serverName.endsWith("hz.netease.com")
                    || serverName.equals("127.0.0.1")
                    || serverName.equals("localhost");
            if (!allow) {
                response.sendError(403);
                return false;
            }
        }
        return true;
    }
}
