package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 兑换券人群包
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponCrowdVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 商品SKUID
    */
    private Long skuId;
    /**
    * 人群包id
    */
    private Long crowdId;
    /**
    * 创建时间
    */
    private Long createTime;
}