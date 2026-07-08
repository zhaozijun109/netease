package com.netease.lofter.tango.impl.web.query.trade.exchangecoupon;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 兑换券人群包
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponCrowdQuery extends BaseQuery implements Serializable {
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
    private Long createTimeBegin;
    private Long createTimeEnd;
}