
package com.netease.lofter.tango.impl.delegate.trade.activity;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivity;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivity.Fields;
import com.netease.lofter.tango.impl.mapper.trade.activity.PaidContentActivityMapper;
import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

 /**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-7-12 19:28:00</p>
 */
@Service
public class PaidContentActivityDelegate implements CommonDeleteDelegate<PaidContentActivityMapper, PaidContentActivity> {

    public PageDO<PaidContentActivity> listByQuery(Long createTimeBegin, Long createTimeEnd,
        Long id, String name, String activityCode, Long parentActId, String img, Integer type, Long startTime, Long endTime, Date dbCreateTime,  int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, name, activityCode, parentActId, img, type, startTime, endTime, dbCreateTime,  createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.dbCreateTime).offset(offset).limit(limit);
        List<PaidContentActivity> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String name,
                            String activityCode,
                            Long parentActId,
                            String img,
                            Integer type,
                            Long startTime,
                            Long endTime,
                            Date dbCreateTime,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (StringUtils.isNotBlank(activityCode)) {
            sqlSelect.andEquals(Fields.activityCode, activityCode);
        }
        if (parentActId != null) {
            sqlSelect.andEquals(Fields.parentActId, parentActId);
        }
        if (StringUtils.isNotBlank(img)) {
            sqlSelect.andEquals(Fields.img, img);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
        }
        if (dbCreateTime != null) {
            sqlSelect.andEquals(Fields.dbCreateTime, dbCreateTime);
        }
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
        if (createTimeBegin != null && createTimeEnd != null) {
            sqlSelect.andGreatThanEqual(Fields.dbCreateTime, createTimeBegin);
            sqlSelect.andLessThanEqual(Fields.dbCreateTime, createTimeEnd);
        } else if (createTimeEnd != null) {
            sqlSelect.andLessThanEqual(Fields.dbCreateTime, createTimeEnd);
        } else if (createTimeBegin != null) {
            sqlSelect.andGreatThanEqual(Fields.dbCreateTime, createTimeBegin);
        }
    }

}







