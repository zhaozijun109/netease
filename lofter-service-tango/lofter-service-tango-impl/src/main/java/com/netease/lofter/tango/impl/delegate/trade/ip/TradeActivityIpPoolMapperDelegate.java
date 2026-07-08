package com.netease.lofter.tango.impl.delegate.trade.ip;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.ip.TradeActivityIpPool.Fields;

import com.netease.lofter.tango.impl.entity.trade.ip.TradeActivityIpPool;
import com.netease.lofter.tango.impl.mapper.trade.ip.TradeActivityIpPoolMapper;

@Primary
@Service
public class TradeActivityIpPoolMapperDelegate implements CommonService<TradeActivityIpPoolMapper, TradeActivityIpPool>, DeleteCommonService<TradeActivityIpPoolMapper, TradeActivityIpPool>{

    public PageDO<TradeActivityIpPool> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long actId,
                                                Long businessId,
                                                Integer bindType,
                                                String ips,
                                                Long startTime,
                                                Long endTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, actId,businessId,bindType ,ips, startTime, endTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.startTime).offset(offset).limit(limit);
        List<TradeActivityIpPool> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long actId,
                            Long businessId,
                            Integer bindType,
                            String ips,
                            Long startTime,
                            Long endTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (actId != null) {
            sqlSelect.andEquals(Fields.actId, actId);
        }
        if (businessId != null) {
            sqlSelect.andEquals(Fields.businessId, businessId);
        }
        if (bindType != null) {
            sqlSelect.andEquals(Fields.bindType, bindType);
        }
        if (StringUtils.isNotBlank(ips)) {
            sqlSelect.andEquals(Fields.ips, ips);
        }
        if (startTime != null) {
            sqlSelect.andEquals(Fields.startTime, startTime);
        }
        if (endTime != null) {
            sqlSelect.andEquals(Fields.endTime, endTime);
        }
    }

    public TradeActivityIpPool queryByActId(long actId){
        SqlSelect sqlSelect = new SqlSelect().whenEquals("actId", actId);
        return selectOne(sqlSelect);
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
    }
}