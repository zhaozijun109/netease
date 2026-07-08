package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlDelete;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;

import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponCrowd.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponCrowd;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeExchangeCouponCrowdMapper;

@Primary
@Service
public class TradeExchangeCouponCrowdMapperDelegate implements CommonService<TradeExchangeCouponCrowdMapper, TradeExchangeCouponCrowd>, DeleteCommonService<TradeExchangeCouponCrowdMapper, TradeExchangeCouponCrowd>{

    public List<TradeExchangeCouponCrowd> getBySkuIds(List<Long> skuIds) {
        SqlSelect sqlSelect = new SqlSelect().whenIn("skuId", skuIds);
        return selectListNoLimit(sqlSelect);
    }
    public int deleteBySkuId(long skuId) {
        SqlDelete sqlSelect = new SqlDelete().whenEquals("skuId", skuId);
        return delete(sqlSelect);
    }

    public PageDO<TradeExchangeCouponCrowd> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long skuId,
                                                Long crowdId,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, skuId, crowdId, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeExchangeCouponCrowd> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long skuId,
                            Long crowdId,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (skuId != null) {
            sqlSelect.andEquals(Fields.skuId, skuId);
        }
        if (crowdId != null) {
            sqlSelect.andEquals(Fields.crowdId, crowdId);
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