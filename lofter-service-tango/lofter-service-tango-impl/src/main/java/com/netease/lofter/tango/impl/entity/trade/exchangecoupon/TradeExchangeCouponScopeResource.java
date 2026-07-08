package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 兑换券使用资源
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ExchangeCouponScopeResource")
public class TradeExchangeCouponScopeResource implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 券id
    */
    @Column(name="couponId")
    private Long couponId;
    /**
    * 物料id
    */
    @Column(name="resourceId")
    private String resourceId;
    /**
    * 状态 0-正常；-1-下线
    */
    @Column(name="status")
    private Integer status;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
}