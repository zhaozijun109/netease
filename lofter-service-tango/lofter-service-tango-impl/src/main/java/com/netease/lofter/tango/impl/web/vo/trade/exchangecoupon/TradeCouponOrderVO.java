package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeCouponOrderVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 对应trade_order表时的id
    */
    private Long tradeId;
    /**
    * 
    */
    private Long userId;
    /**
    * 0购买失败，1购买成功
    */
    private Integer status;
    /**
    * 1安卓，2苹果
    */
    private Integer platform;
    /**
    * 支付渠道，1支付宝，2苹果支付
    */
    private Integer payType;
    /**
    * 交易总金额，未减去渠道分层（channelDivision）和手续费（fee)
    */
    private BigDecimal amount;
    /**
    * 手续费，这里应该没有
    */
    private BigDecimal fee;
    /**
    * 苹果渠道分成30%，只是记录一下
    */
    private BigDecimal channelDivision;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long finishTime;
    /**
    * 商品id
    */
    private Long productId;
    /**
    * 第三方支付流水号
    */
    private String bankOrderSn;
    /**
    * 第三方支付时间
    */
    private Long bankOrderTime;

    private Integer scene;
}