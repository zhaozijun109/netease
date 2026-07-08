package com.netease.lofter.tango.impl.delegate.trade.activity;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivitySlotBoox.Fields;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivitySlotBoox;
import com.netease.lofter.tango.impl.mapper.trade.activity.PaidContentActivitySlotBooxMapper;

@Primary
@Service
public class PaidContentActivitySlotBooxMapperDelegate implements CommonService<PaidContentActivitySlotBooxMapper, PaidContentActivitySlotBoox>, DeleteCommonService<PaidContentActivitySlotBooxMapper, PaidContentActivitySlotBoox>{

    public long getActIdByLootboxCode(String lootboxCode) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals(Fields.lootboxCode, lootboxCode);
        PaidContentActivitySlotBoox slotBox =  selectOne(sqlSelect);
        return slotBox == null ? 0L : slotBox.getActId();
    }

    public PageDO<PaidContentActivitySlotBoox> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String name,
                                                Long actId,
                                                String lootboxCode,
                                                Long startTime,
                                                Long endTime,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, name, actId, lootboxCode, startTime, endTime, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<PaidContentActivitySlotBoox> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String name,
                            Long actId,
                            String lootboxCode,
                            Long startTime,
                            Long endTime,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (actId != null) {
            sqlSelect.andEquals(Fields.actId, actId);
        }
        if (StringUtils.isNotBlank(lootboxCode)) {
            sqlSelect.andEquals(Fields.lootboxCode, lootboxCode);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
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