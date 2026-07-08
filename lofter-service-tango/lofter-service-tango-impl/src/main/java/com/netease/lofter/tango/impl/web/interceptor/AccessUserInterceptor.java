package com.netease.lofter.tango.impl.web.interceptor;

import com.netease.lofter.acl.sdk.context.UserInfoHolder;
import com.netease.lofter.acl.sdk.meta.UserInfo;
import com.netease.lofter.tango.impl.delegate.TangoAccessUsersDelegate;
import com.netease.lofter.tango.impl.entity.TangoAccessUsers;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AccessUserInterceptor extends AbstractInterceptor {

    @Autowired
    private TangoAccessUsersDelegate tangoAccessUsersDelegate;

    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = UserInfoHolder.getUserInfo();
        if (null == userInfo) {
            return true;
        }
        //todo 缓存
        if (!tangoAccessUsersDelegate.exists(userInfo.getEmail())) {
            this.saveAccessUser(userInfo.getEmail());
        }
        return true;
    }

    private void saveAccessUser(String email) {
        TangoAccessUsers tangoAccessUsers = new TangoAccessUsers();
        tangoAccessUsers.setEmail(email);
        tangoAccessUsers.setCreateTime(System.currentTimeMillis());
        tangoAccessUsers.setUpdateTime(tangoAccessUsers.getCreateTime());
        tangoAccessUsersDelegate.insertValue(tangoAccessUsers);
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
