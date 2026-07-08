package com.netease.lofter.tango.impl.web.filter;

import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.consts.CommonProperties;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class MockFilter extends OncePerRequestFilter {

    private static final String HEADER_MOCK_EMAIL = "X-Tango-Mock-Email";
    @Autowired
    private ProfileEnv profileEnv;
    @Autowired
    private CommonProperties commonProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (profileEnv.isOnline()) {
            return true;
        }
        return super.shouldNotFilter(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String mockEmail = request.getHeader(HEADER_MOCK_EMAIL);
        if (StringUtils.isNotBlank(mockEmail) || profileEnv.isDev()) {
            mockEmail = Optional.ofNullable(mockEmail).orElse(commonProperties.getLocalMockUser());
            UserInfo mockUser = new UserInfo();
            mockUser.setEmail(mockEmail);
            mockUser.setFullName(mockEmail);
            mockUser.setNickName(mockEmail);
            request.setAttribute(UserInfo.class.getName(), mockUser);
        }
        filterChain.doFilter(request, response);
        request.removeAttribute(UserInfo.class.getName());
    }
}
