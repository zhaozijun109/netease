package com.netease.lofter.tango.impl.aspect;

import com.google.common.collect.Sets;
import com.netease.lofter.acl.sdk.context.UserInfoHolder;
import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.service.OperatorLogService;
import com.netease.lofter.tango.impl.web.vo.OperatorLogVO;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Aspect
public class TangoLogAspect {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private OperatorLogService operatorLogService;
    private Set<String> supportPathSuffix = Sets.newHashSet("add", "save", "update", "delete");

    @Around("execution(public com.netease.lofter.tango.impl.web.vo.Result+ com.netease.lofter.tango.impl.web.controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Map<String, Object> params = retrieveParams(pjp);
        Map<String, Object> response = new HashMap<>();
        try {
            Result result = (Result) pjp.proceed();
            response.put("code", result.getCode());
            response.put("message", result.getMessage());
            return result;
        } finally {
            if (shouldLog()) {
                OperatorLogVO logVO = new OperatorLogVO();
                logVO.setController(pjp.getTarget().getClass().getSimpleName());
                logVO.setMethod(logVO.getController() + "." + pjp.getSignature().getName());
                String param = JsonUtils.toJsonString(params);
                logVO.setParams(param.substring(0, Math.min(param.length(), 4096)));
                logVO.setOperator(Optional.ofNullable(UserInfoHolder.getUserInfo()).map(UserInfo::getEmail).orElse("anony"));
                logVO.setUrl(urlPathHelper.getLookupPathForRequest(httpServletRequest));
                logVO.setResponse(JsonUtils.toJsonString(response));
                operatorLogService.add(logVO);
            }
        }
    }

    private Map<String, Object> retrieveParams(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Map<String, Object> params = new HashMap<>();
        int len = signature.getParameterNames().length;
        for (int i = 0; i < len; i++) {
            Object arg = pjp.getArgs()[i];
            if (arg instanceof UserInfo) {
                continue;
            }
            params.put(signature.getParameterNames()[i], arg);
        }
        return params;
    }

    private boolean shouldLog() {
        String method = httpServletRequest.getMethod();
        if (!HttpMethod.POST.name().equalsIgnoreCase(method)) {
            return false;
        }
        String path = urlPathHelper.getLookupPathForRequest(httpServletRequest);
        if (!path.startsWith("/tango")) {
            return false;
        }
        if (path.startsWith("/tango/config")) {
            return false;
        }
        boolean present = supportPathSuffix.stream().anyMatch(path::endsWith);
        if (!present) {
            return false;
        }
        return true;
    }
}
