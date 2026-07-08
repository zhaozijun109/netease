package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 兑换券元数据
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    private Long id;
    /**
    * 券名称
    */
    private String name;
    /**
    * 券使用范围 枚举
    */
    private String scope;
    /**
    * 券配图
    */
    private String icon;
    /**
    * 状态 0-正常；-1-下线
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
}