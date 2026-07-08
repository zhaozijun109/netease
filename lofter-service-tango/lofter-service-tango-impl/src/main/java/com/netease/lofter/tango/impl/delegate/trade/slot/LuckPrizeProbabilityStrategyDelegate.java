
package com.netease.lofter.tango.impl.delegate.trade.slot;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrizeProbabilityStrategy;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrizeProbabilityStrategy.Fields;
import com.netease.lofter.tango.impl.mapper.trade.slot.LuckPrizeProbabilityStrategyMapper;
import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.stereotype.Service;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

 /**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: jetbi</p>
 * <p>@Create Time: 2024-7-23 10:20:37</p>
 */
@Service
public class LuckPrizeProbabilityStrategyDelegate implements CommonDeleteDelegate<LuckPrizeProbabilityStrategyMapper, LuckPrizeProbabilityStrategy> {

    public PageDO<LuckPrizeProbabilityStrategy> listByQuery(Long createTimeBegin, Long createTimeEnd,
        Long id, String appKey, String activityId, Long startTime, Long endTime, String probabilityStrategy,  int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, appKey, activityId, startTime, endTime, probabilityStrategy,  createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<LuckPrizeProbabilityStrategy> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appKey,
                            String activityId,
                            Long startTime,
                            Long endTime,
                            String probabilityStrategy,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(appKey)) {
            sqlSelect.andEquals(Fields.appKey, appKey);
        }
        if (StringUtils.isNotBlank(activityId)) {
            sqlSelect.andEquals(Fields.activityId, activityId);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
        }
        if (StringUtils.isNotBlank(probabilityStrategy)) {
            sqlSelect.andEquals(Fields.probabilityStrategy, probabilityStrategy);
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







