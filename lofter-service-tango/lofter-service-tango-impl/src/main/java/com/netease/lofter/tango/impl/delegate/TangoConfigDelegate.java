package com.netease.lofter.tango.impl.delegate;

import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoConfigDO;
import com.netease.lofter.tango.impl.entity.TangoConfigDO.Fields;
import com.netease.lofter.tango.impl.mapper.TangoConfigMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 *
 */
@Service
public class TangoConfigDelegate implements CommonDeleteDelegate<TangoConfigMapper, TangoConfigDO> {


    public PageDO<TangoConfigDO> listByQuery(String appId, String configKey, String env, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals(Fields.envTags, env);
        if (StringUtils.isNotBlank(appId)) {
            sqlSelect.andEquals(Fields.appId, appId);
        }
        if (StringUtils.isNotBlank(configKey)) {
            sqlSelect.andLike(Fields.configKey, "%" + configKey + "%");
        }
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TangoConfigDO> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    public TangoConfigDO selectByAppIdAndKey(String appId, String configKey, String env) {
        return selectOne(new SqlSelect().andEquals(Fields.appId, appId).andEquals(Fields.configKey, configKey).andEquals(Fields.envTags, env));
    }

    public List<TangoConfigDO> listByAppId(String appId, String env) {
        return selectListNoLimit(new SqlSelect().andEquals(Fields.appId, appId).andEquals(Fields.envTags, env));
    }

    public List<TangoConfigDO> listByAppIdAndKey(String appId, String envTag, Collection<String> keys) {
        return selectListNoLimit(new SqlSelect().andEquals(Fields.appId, appId).andEquals(Fields.envTags, envTag).andIn(Fields.configKey, keys));
    }

}
