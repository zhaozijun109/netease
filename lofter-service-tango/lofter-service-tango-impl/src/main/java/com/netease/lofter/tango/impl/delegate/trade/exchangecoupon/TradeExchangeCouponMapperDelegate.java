package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCoupon.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCoupon;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeExchangeCouponMapper;

@Primary
@Service
public class TradeExchangeCouponMapperDelegate implements CommonService<TradeExchangeCouponMapper, TradeExchangeCoupon>, DeleteCommonService<TradeExchangeCouponMapper, TradeExchangeCoupon>{

    public PageDO<TradeExchangeCoupon> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String name,
                                                String scope,
                                                String icon,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, name, scope, icon, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeExchangeCoupon> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String name,
                            String scope,
                            String icon,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (StringUtils.isNotBlank(scope)) {
            sqlSelect.andEquals(Fields.scope, scope);
        }
        if (StringUtils.isNotBlank(icon)) {
            sqlSelect.andEquals(Fields.icon, icon);
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