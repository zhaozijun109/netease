package com.netease.lofter.tango.impl.delegate.trade.exchangecoupon;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponSKU.Fields;

import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponSKU;
import com.netease.lofter.tango.impl.mapper.trade.exchangecoupon.TradeExchangeCouponSKUMapper;
import java.math.BigDecimal;

@Primary
@Service
public class TradeExchangeCouponSKUMapperDelegate implements CommonService<TradeExchangeCouponSKUMapper, TradeExchangeCouponSKU>, DeleteCommonService<TradeExchangeCouponSKUMapper, TradeExchangeCouponSKU>{

    public PageDO<TradeExchangeCouponSKU> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String productId,
                                                Integer type,
                                                String name,
                                                String img,
                                                BigDecimal marketPrice,
                                                BigDecimal discountPrice,
                                                String discountText,
                                                Integer platform,
                                                Integer status,
                                                String subProducts,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, productId, type, name, img, marketPrice, discountPrice, discountText, platform, status, subProducts, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeExchangeCouponSKU> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String productId,
                            Integer type,
                            String name,
                            String img,
                            BigDecimal marketPrice,
                            BigDecimal discountPrice,
                            String discountText,
                            Integer platform,
                            Integer status,
                            String subProducts,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(productId)) {
            sqlSelect.andEquals(Fields.productId, productId);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (StringUtils.isNotBlank(img)) {
            sqlSelect.andEquals(Fields.img, img);
        }
        if (marketPrice != null) {
            sqlSelect.andEquals(Fields.marketPrice, marketPrice);
        }
        if (discountPrice != null) {
            sqlSelect.andEquals(Fields.discountPrice, discountPrice);
        }
        if (discountText != null) {
            sqlSelect.andEquals(Fields.discountText, discountText);
        }
        if (platform != null) {
            sqlSelect.andEquals(Fields.platform, platform);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (StringUtils.isNotBlank(subProducts)) {
            sqlSelect.andEquals(Fields.subProducts, subProducts);
        }
        if (createTime != null) {
            sqlSelect.andEquals(Fields.createTime, createTime);
        }
        sqlSelect.andEquals(Fields.scene, TradeExchangeCouponSKU.SCENE_UGC);
        sqlSelect.andEquals(Fields.type, TradeExchangeCouponSKU.TYPE_COUPON_PACK);
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