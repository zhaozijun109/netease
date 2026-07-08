package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.SqlIgnore;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* 券包订单
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_Coupon_Order")
public class TradeCouponOrder implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    @Key
    @Column(name="id")
    private Long id;
    /**
    * 对应trade_order表时的id
    */
    @Column(name="tradeId")
    private Long tradeId;
    /**
    * 
    */
    @Column(name="userId")
    @SqlIgnore(selectIgnore = false, insertIgnore = false)
    private Long userId;
    /**
    * 0购买失败，1购买成功
    */
    @Column(name="status")
    private Integer status;
    /**
    * 1安卓，2苹果
    */
    @Column(name="platform")
    private Integer platform;
    /**
    * 支付渠道，1支付宝，2苹果支付
    */
    @Column(name="payType")
    private Integer payType;
    /**
    * 交易总金额，未减去渠道分层（channelDivision）和手续费（fee)
    */
    @Column(name="amount")
    private BigDecimal amount;
    /**
    * 手续费，这里应该没有
    */
    @Column(name="fee")
    private BigDecimal fee;
    /**
    * 苹果渠道分成30%，只是记录一下
    */
    @Column(name="channelDivision")
    private BigDecimal channelDivision;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
    /**
    * 修改时间
    */
    @Column(name="finishTime")
    private Long finishTime;
    /**
    * 商品id
    */
    @Column(name="productId")
    private Long productId;
    /**
    * 第三方支付流水号
    */
    @Column(name="bankOrderSn")
    private String bankOrderSn;
    /**
    * 第三方支付时间
    */
    @Column(name="bankOrderTime")
    private Long bankOrderTime;
    @Column(name="scene")
    private Integer scene;
}