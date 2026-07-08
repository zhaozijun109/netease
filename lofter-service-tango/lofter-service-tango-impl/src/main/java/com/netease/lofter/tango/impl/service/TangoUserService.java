package com.netease.lofter.tango.impl.service;

import com.netease.lofter.acl.sdk.context.UserInfoHolder;
import com.netease.lofter.tango.impl.delegate.TangoAccessUsersDelegate;
import com.netease.lofter.tango.impl.entity.TangoAccessUsers;
import com.netease.lofter.tango.impl.web.vo.SelectVO;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TangoUserService {

    @Autowired
    private TangoAccessUsersDelegate tangoAccessUsersDelegate;

    public boolean isAdmin() {
        Set<String> roles = UserInfoHolder.getUserRoles();
        return roles != null && (roles.contains("admin") || roles.contains("ADMIN"));
    }

    public List<String> listAll() {
        return CollectionUtils3.mapper(tangoAccessUsersDelegate.listAll(), TangoAccessUsers::getEmail);
    }

    public TangoAccessUsers getByEmail(String email) {
        return tangoAccessUsersDelegate.getAccessUser(email);
    }

    public List<SelectVO> listSelector() {
        return CollectionUtils3.mapper(tangoAccessUsersDelegate.listAll(), item -> {
            String email = item.getEmail();
//            String nickname = email.substring(0, email.indexOf("@"));
            return new SelectVO(email, email);
        });
    }
}
