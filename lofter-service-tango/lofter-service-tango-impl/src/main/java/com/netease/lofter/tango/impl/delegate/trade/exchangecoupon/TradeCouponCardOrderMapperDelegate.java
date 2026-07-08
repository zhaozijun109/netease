package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeCouponCardOrder.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeCouponCardOrder;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeCouponCardOrderMapper;
import java.math.BigDecimal;

@Primary
@Service
public class TradeCouponCardOrderMapperDelegate implements CommonService<TradeCouponCardOrderMapper, TradeCouponCardOrder>, DeleteCommonService<TradeCouponCardOrderMapper, TradeCouponCardOrder>{

    public PageDO<TradeCouponCardOrder> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long tradeId,
                                                Long userId,
                                                Integer status,
                                                Integer platform,
                                                Integer payType,
                                                BigDecimal amount,
                                                BigDecimal fee,
                                                BigDecimal channelDivision,
                                                Long createTime,
                                                Long finishTime,
                                                Long productId,
                                                String bankOrderSn,
                                                Long bankOrderTime,
                                                int offset,
                                                int limit) {
        if(userId == null || userId <= 0) {
            return PageDO.empty();
        }
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, tradeId, userId, status, platform, payType, amount, fee, channelDivision, createTime, finishTime, productId, bankOrderSn, bankOrderTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeCouponCardOrder> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long tradeId,
                            Long userId,
                            Integer status,
                            Integer platform,
                            Integer payType,
                            BigDecimal amount,
                            BigDecimal fee,
                            BigDecimal channelDivision,
                            Long createTime,
                            Long finishTime,
                            Long productId,
                            String bankOrderSn,
                            Long bankOrderTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (tradeId != null) {
            sqlSelect.andEquals(Fields.tradeId, tradeId);
        }
        if (userId != null) {
            sqlSelect.andEquals(Fields.userId, userId);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (platform != null) {
            sqlSelect.andEquals(Fields.platform, platform);
        }
        if (payType != null) {
            sqlSelect.andEquals(Fields.payType, payType);
        }
        if (amount != null) {
            sqlSelect.andEquals(Fields.amount, amount);
        }
        if (fee != null) {
            sqlSelect.andEquals(Fields.fee, fee);
        }
        if (channelDivision != null) {
            sqlSelect.andEquals(Fields.channelDivision, channelDivision);
        }
        if (createTime != null) {
            sqlSelect.andEquals(Fields.createTime, createTime);
        }
        if (finishTime != null) {
            sqlSelect.andEquals(Fields.finishTime, finishTime);
        }
        if (productId != null) {
            sqlSelect.andEquals(Fields.productId, productId);
        }
        if (StringUtils.isNotBlank(bankOrderSn)) {
            sqlSelect.andEquals(Fields.bankOrderSn, bankOrderSn);
        }
        if (bankOrderTime != null) {
            sqlSelect.andEquals(Fields.bankOrderTime, bankOrderTime);
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