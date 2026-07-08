package com.netease.lofter.tango.impl.delegate;

import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoAppDO;
import com.netease.lofter.tango.impl.entity.TangoAppDO.Fields;
import com.netease.lofter.tango.impl.entity.TangoConfigDO;
import com.netease.lofter.tango.impl.mapper.TangoAppMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TangoAppDelegate implements CommonDeleteDelegate<TangoAppMapper, TangoAppDO> {

    public List<TangoAppDO> listAll() {
        return selectListNoLimit(new SqlSelect());
    }

    public TangoAppDO add(TangoAppDO appDO) {
        appDO.setCreateTime(System.currentTimeMillis());
        appDO.setUpdateTime(appDO.getCreateTime());
        insertValue(appDO);
        return appDO;
    }

    public PageDO<TangoAppDO> listByQuery(String appId, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        if (StringUtils.isNotBlank(appId)) {
            sqlSelect.andEquals(TangoAppDO.Fields.appId, appId);
        }
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(TangoConfigDO.Fields.createTime).offset(offset).limit(limit);
        List<TangoAppDO> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    public TangoAppDO selectByAppId(String appId) {
        return selectOne(new SqlSelect().andEquals(Fields.appId, appId));
    }
}
