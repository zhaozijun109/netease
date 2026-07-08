package com.netease.lofter.tango.impl.web.query.trade.exchangecoupon;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 兑换券使用资源
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponScopeResourceQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    private Long id;
    /**
    * 创建时间
    */
    private Long couponId;
    /**
    * 物料id
    */
    private String resourceId;
    /**
    * 状态 0-正常；-1-下线
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;
}