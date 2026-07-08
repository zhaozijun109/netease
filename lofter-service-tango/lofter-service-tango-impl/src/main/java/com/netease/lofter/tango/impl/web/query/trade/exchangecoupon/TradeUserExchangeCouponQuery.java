package com.netease.lofter.tango.impl.web.query.trade.exchangecoupon;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* 购买订单
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeUserExchangeCouponQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
     * 用户Id
     */
    private Long userId;
    private Integer scene;
    private Integer type;
    private Long tradeId;
}