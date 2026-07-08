package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponScopeResource.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponScopeResource;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeExchangeCouponScopeResourceMapper;

@Primary
@Service
public class TradeExchangeCouponScopeResourceMapperDelegate implements CommonService<TradeExchangeCouponScopeResourceMapper, TradeExchangeCouponScopeResource>, DeleteCommonService<TradeExchangeCouponScopeResourceMapper, TradeExchangeCouponScopeResource>{

    public PageDO<TradeExchangeCouponScopeResource> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long couponId,
                                                String resourceId,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, couponId, resourceId, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeExchangeCouponScopeResource> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long couponId,
                            String resourceId,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (couponId != null) {
            sqlSelect.andEquals(Fields.couponId, couponId);
        }
        if (StringUtils.isNotBlank(resourceId)) {
            sqlSelect.andEquals(Fields.resourceId, resourceId);
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