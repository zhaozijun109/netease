package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.SqlIgnore;

/**
* 兑换券人群包
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ExchangeCouponCrowd")
public class TradeExchangeCouponCrowd implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 商品SKUID
    */
    @Column(name="skuId")
    @SqlIgnore(selectIgnore = false, insertIgnore = false)
    private Long skuId;
    /**
    * 人群包id
    */
    @Column(name="crowdId")
    private Long crowdId;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
}