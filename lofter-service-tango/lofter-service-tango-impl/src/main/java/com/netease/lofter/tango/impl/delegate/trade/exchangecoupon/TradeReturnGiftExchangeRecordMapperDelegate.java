package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeReturnGiftExchangeRecord.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeReturnGiftExchangeRecord;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeReturnGiftExchangeRecordMapper;
import java.math.BigDecimal;

@Primary
@Service
public class TradeReturnGiftExchangeRecordMapperDelegate implements CommonService<TradeReturnGiftExchangeRecordMapper, TradeReturnGiftExchangeRecord>, DeleteCommonService<TradeReturnGiftExchangeRecordMapper, TradeReturnGiftExchangeRecord>{

    public PageDO<TradeReturnGiftExchangeRecord> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long userId,
                                                Long couponId,
                                                Long postId,
                                                Integer status,
                                                Long exchangeTime,
                                                Long startIndex,
                                                Long createTime,
                                                String ext,
                                                Long blogId,
                                                Long planId,
                                                Long relatedId,
                                                Long exchangeGiftId,
                                                Integer type,
                                                Long parentOrderId,
                                                Integer parentOrderType,
                                                Integer scene,
                                                Integer needSettle,
                                                Integer signType,
                                                BigDecimal unitAmount,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, userId, couponId, postId, status, exchangeTime, startIndex, createTime, ext, blogId, planId, relatedId, exchangeGiftId, type, parentOrderId, parentOrderType, scene, needSettle, signType, unitAmount,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeReturnGiftExchangeRecord> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long userId,
                            Long couponId,
                            Long postId,
                            Integer status,
                            Long exchangeTime,
                            Long startIndex,
                            Long createTime,
                            String ext,
                            Long blogId,
                            Long planId,
                            Long relatedId,
                            Long exchangeGiftId,
                            Integer type,
                            Long parentOrderId,
                            Integer parentOrderType,
                            Integer scene,
                            Integer needSettle,
                            Integer signType,
                            BigDecimal unitAmount,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (userId != null) {
            sqlSelect.andEquals(Fields.userId, userId);
        }
        if (couponId != null) {
            sqlSelect.andEquals(Fields.couponId, couponId);
        }
        if (postId != null) {
            sqlSelect.andEquals(Fields.postId, postId);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (exchangeTime != null) {
            sqlSelect.andEquals(Fields.exchangeTime, exchangeTime);
        }
        if (startIndex != null) {
            sqlSelect.andEquals(Fields.startIndex, startIndex);
        }
        if (createTime != null) {
            sqlSelect.andEquals(Fields.createTime, createTime);
        }
        if (StringUtils.isNotBlank(ext)) {
            sqlSelect.andEquals(Fields.ext, ext);
        }
        if (blogId != null) {
            sqlSelect.andEquals(Fields.blogId, blogId);
        }
        if (planId != null) {
            sqlSelect.andEquals(Fields.planId, planId);
        }
        if (relatedId != null) {
            sqlSelect.andEquals(Fields.relatedId, relatedId);
        }
        if (exchangeGiftId != null) {
            sqlSelect.andEquals(Fields.exchangeGiftId, exchangeGiftId);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (parentOrderId != null) {
            sqlSelect.andEquals(Fields.parentOrderId, parentOrderId);
        }
        if (parentOrderType != null) {
            sqlSelect.andEquals(Fields.parentOrderType, parentOrderType);
        }
        if (scene != null) {
            sqlSelect.andEquals(Fields.scene, scene);
        }
        if (needSettle != null) {
            sqlSelect.andEquals(Fields.needSettle, needSettle);
        }
        if (signType != null) {
            sqlSelect.andEquals(Fields.signType, signType);
        }
        if (unitAmount != null) {
            sqlSelect.andEquals(Fields.unitAmount, unitAmount);
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