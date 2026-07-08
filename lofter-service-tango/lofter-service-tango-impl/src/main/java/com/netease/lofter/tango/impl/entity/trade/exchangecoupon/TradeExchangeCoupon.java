package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 兑换券元数据
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ExchangeCoupon")
public class TradeExchangeCoupon implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 券名称
    */
    @Column(name="name")
    private String name;
    /**
    * 券使用范围 枚举
    */
    @Column(name="scope")
    private String scope;
    /**
    * 券配图
    */
    @Column(name="icon")
    private String icon;
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