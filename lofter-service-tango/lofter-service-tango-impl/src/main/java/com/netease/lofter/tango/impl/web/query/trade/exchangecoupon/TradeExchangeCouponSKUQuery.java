package com.netease.lofter.tango.impl.web.query.trade.exchangecoupon;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
* 兑换券SKU
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponSKUQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 商品code
    */
    private String productId;
    /**
    * SPU类型 0-券包；1-暑期卡
    */
    private Integer type;
    /**
    * 商品名
    */
    private String name;
    /**
    * 商品配图
    */
    private String img;
    /**
    * 原价
    */
    private BigDecimal marketPrice;
    /**
    * 折扣价
    */
    private BigDecimal discountPrice;
    /**
    * 折扣文案
    */
    private String discountText;
    /**
    * 平台
    */
    private Integer platform;
    /**
    * 状态 0-正常；-1-下架
    */
    private Integer status;
    /**
    * 子商品
    */
    private String subProducts;
    /**
    * 创建时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;
    private Long activityId;
}