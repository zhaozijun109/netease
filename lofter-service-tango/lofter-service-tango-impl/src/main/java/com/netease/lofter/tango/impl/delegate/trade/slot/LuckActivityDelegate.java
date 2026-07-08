
package com.netease.lofter.tango.impl.delegate.trade.slot;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckActivity;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckActivity.Fields;
import com.netease.lofter.tango.impl.mapper.trade.slot.LuckActivityMapper;
import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: shiliang</p>
 * <p>@Create Time: 2024-7-23 10:14:10</p>
 */
@Service
public class LuckActivityDelegate implements CommonDeleteDelegate<LuckActivityMapper, LuckActivity> {

    public List<LuckActivity> getByActivityIds(List<String> activityIds) {
        if (CollectionUtils.isEmpty(activityIds)) {
            return Collections.emptyList();
        }
        SqlSelect sqlSelect = new SqlSelect().whenIn("activityId", activityIds);
        return selectListNoLimit(sqlSelect);
    }

    public LuckActivity getByActivityId(String activityId) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals("activityId", activityId);
        return selectOne(sqlSelect);
    }

    public PageDO<LuckActivity> listByQuery(Long createTimeBegin, Long createTimeEnd,
                                            Long id, String appKey, String activityId, String name, Long startTime, Long endTime, Integer dailyInitChanceCount, Integer dailySlotLimit, Integer userBingoLimit, Integer bingoCount, Integer fakeBingoCount, Integer joinCount, Integer fakeJoinCount, Integer fakeJoinCountRateMin, Integer fakeJoinCountRateMax, Integer defaultInitChanceCount, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, appKey, activityId, name, startTime, endTime, dailyInitChanceCount, dailySlotLimit, userBingoLimit, bingoCount, fakeBingoCount, joinCount, fakeJoinCount, fakeJoinCountRateMin, fakeJoinCountRateMax, defaultInitChanceCount, createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<LuckActivity> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appKey,
                            String activityId,
                            String name,
                            Long startTime,
                            Long endTime,
                            Integer dailyInitChanceCount,
                            Integer dailySlotLimit,
                            Integer userBingoLimit,
                            Integer bingoCount,
                            Integer fakeBingoCount,
                            Integer joinCount,
                            Integer fakeJoinCount,
                            Integer fakeJoinCountRateMin,
                            Integer fakeJoinCountRateMax,
                            Integer defaultInitChanceCount,
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
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
        }
        if (dailyInitChanceCount != null) {
            sqlSelect.andEquals(Fields.dailyInitChanceCount, dailyInitChanceCount);
        }
        if (dailySlotLimit != null) {
            sqlSelect.andEquals(Fields.dailySlotLimit, dailySlotLimit);
        }
        if (userBingoLimit != null) {
            sqlSelect.andEquals(Fields.userBingoLimit, userBingoLimit);
        }
        if (bingoCount != null) {
            sqlSelect.andEquals(Fields.bingoCount, bingoCount);
        }
        if (fakeBingoCount != null) {
            sqlSelect.andEquals(Fields.fakeBingoCount, fakeBingoCount);
        }
        if (joinCount != null) {
            sqlSelect.andEquals(Fields.joinCount, joinCount);
        }
        if (fakeJoinCount != null) {
            sqlSelect.andEquals(Fields.fakeJoinCount, fakeJoinCount);
        }
        if (fakeJoinCountRateMin != null) {
            sqlSelect.andEquals(Fields.fakeJoinCountRateMin, fakeJoinCountRateMin);
        }
        if (fakeJoinCountRateMax != null) {
            sqlSelect.andEquals(Fields.fakeJoinCountRateMax, fakeJoinCountRateMax);
        }
        if (defaultInitChanceCount != null) {
            sqlSelect.andEquals(Fields.defaultInitChanceCount, defaultInitChanceCount);
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







