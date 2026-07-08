package com.netease.lofter.tango.impl.delegate;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoConfigOpHistory;
import com.netease.lofter.tango.impl.entity.TangoConfigOpHistory.Fields;
import com.netease.lofter.tango.impl.mapper.TangoConfigOpHistoryMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-13 18:10:40</p>
 */
@Service
public class TangoConfigOpHistoryDelegate implements CommonDeleteDelegate<TangoConfigOpHistoryMapper, TangoConfigOpHistory> {

    public PageDO<TangoConfigOpHistory> listByQuery(Long createTimeBegin, Long createTimeEnd,
                                                    Long id, String appId, String configKey, String operator, String opType, String newValue, String oldValue, String envTags, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals(Fields.envTags, envTags);
        this.buildQuery(sqlSelect, id, appId, configKey, operator, opType, newValue, oldValue, createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TangoConfigOpHistory> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appId,
                            String configKey,
                            String operator,
                            String opType,
                            String newValue,
                            String oldValue,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(appId)) {
            sqlSelect.andEquals(Fields.appId, appId);
        }
        if (StringUtils.isNotBlank(configKey)) {
            sqlSelect.andEquals(Fields.configKey, configKey);
        }
        if (StringUtils.isNotBlank(operator)) {
            sqlSelect.andEquals(Fields.operator, operator);
        }
        if (StringUtils.isNotBlank(opType)) {
            sqlSelect.andEquals(Fields.opType, opType);
        }
        if (StringUtils.isNotBlank(newValue)) {
            sqlSelect.andEquals(Fields.newValue, newValue);
        }
        if (StringUtils.isNotBlank(oldValue)) {
            sqlSelect.andEquals(Fields.oldValue, oldValue);
        }
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
        if (createTimeBegin != null && createTimeEnd != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeEnd != null) {
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeBegin != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
        }
    }

}







