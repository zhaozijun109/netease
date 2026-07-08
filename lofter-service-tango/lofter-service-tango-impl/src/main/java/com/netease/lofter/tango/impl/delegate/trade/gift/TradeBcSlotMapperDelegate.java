package com.netease.lofter.tango.impl.delegate.trade.gift;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.gift.TradeBcSlot.Fields;

import com.netease.lofter.tango.impl.entity.trade.gift.TradeBcSlot;
import com.netease.lofter.tango.impl.mapper.trade.gift.TradeBcSlotMapper;

@Primary
@Service
public class TradeBcSlotMapperDelegate implements CommonService<TradeBcSlotMapper, TradeBcSlot>, DeleteCommonService<TradeBcSlotMapper, TradeBcSlot>{

    public PageDO<TradeBcSlot> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String crowdIds,
                                                Integer type,
                                                Integer priority,
                                                String banner,
                                                String targetUrl,
                                                Long startTime,
                                                Long endTime,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, crowdIds, type, priority, banner, targetUrl, startTime, endTime, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeBcSlot> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String crowdIds,
                            Integer type,
                            Integer priority,
                            String banner,
                            String targetUrl,
                            Long startTime,
                            Long endTime,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(crowdIds)) {
            sqlSelect.andEquals(Fields.crowdIds, crowdIds);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (priority != null) {
            sqlSelect.andEquals(Fields.priority, priority);
        }
        if (StringUtils.isNotBlank(banner)) {
            sqlSelect.andEquals(Fields.banner, banner);
        }
        if (StringUtils.isNotBlank(targetUrl)) {
            sqlSelect.andEquals(Fields.targetUrl, targetUrl);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (createTime != null) {
            sqlSelect.andEquals(Fields.createTime, createTime);
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