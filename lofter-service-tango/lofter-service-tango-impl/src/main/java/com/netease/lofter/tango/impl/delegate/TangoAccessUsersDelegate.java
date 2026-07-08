package com.netease.lofter.tango.impl.delegate;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.TangoAccessUsers;
import com.netease.lofter.tango.impl.entity.TangoAccessUsers.Fields;
import com.netease.lofter.tango.impl.mapper.TangoAccessUsersMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-13 18:23:26</p>
 */
@Service
public class TangoAccessUsersDelegate implements CommonDeleteDelegate<TangoAccessUsersMapper, TangoAccessUsers> {

    public List<TangoAccessUsers> listAll() {
        return selectListNoLimit(new SqlSelect());
    }

    public boolean exists(String email) {
        return count(new SqlSelect().andEquals(Fields.email, email)) > 0;
    }

    public TangoAccessUsers getAccessUser(String email) {
        return selectOne(new SqlSelect().andEquals(Fields.email, email));
    }
}







