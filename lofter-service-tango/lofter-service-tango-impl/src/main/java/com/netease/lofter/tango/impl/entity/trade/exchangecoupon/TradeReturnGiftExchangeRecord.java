package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.SqlIgnore;
import java.math.BigDecimal;

/**
* 解锁券解锁记录
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ReturnGiftExchangeRecord")
public class TradeReturnGiftExchangeRecord implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 用户Id
    */
    @Column(name="userId")
    @SqlIgnore(selectIgnore = false, insertIgnore = false)
    private Long userId;
    /**
    * 兑换券id
    */
    @Column(name="couponId")
    private Long couponId;
    /**
    * 文章id
    */
    @Column(name="postId")
    private Long postId;
    /**
    * 状态 0-初始状态；1-已解锁
    */
    @Column(name="status")
    private Integer status;
    /**
    * 兑换时间
    */
    @Column(name="exchangeTime")
    private Long exchangeTime;
    /**
    * 窗口start
    */
    @Column(name="startIndex")
    private Long startIndex;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
    /**
    * 扩展信息
    */
    @Column(name="ext")
    private String ext;
    /**
    * 文章博客id
    */
    @Column(name="blogId")
    private Long blogId;
    /**
    * 回礼id
    */
    @Column(name="planId")
    private Long planId;
    /**
    * 关联id,如兑换券Trade_UserExchangeCoupon表id
    */
    @Column(name="relatedId")
    private Long relatedId;
    /**
    * 抵用礼物id
    */
    @Column(name="exchangeGiftId")
    private Long exchangeGiftId;
    /**
    * 类型 0-早期兑换券；1-兑换文章；2-抽奖
    */
    @Column(name="type")
    private Integer type;
    /**
    * 父订单id
    */
    @Column(name="parentOrderId")
    private Long parentOrderId;
    /**
    * 0-无父定单
    */
    @Column(name="parentOrderType")
    private Integer parentOrderType;
    /**
    * 来源，0：普通；1:新用户，2：合集聚合支付
    */
    @Column(name="scene")
    private Integer scene;
    /**
    * 是否结算，0：结算；-1：不结算
    */
    @Column(name="needSettle")
    private Integer needSettle;
    /**
    * 收礼内容类型 0-UGC 1-PGC
    */
    @Column(name="signType")
    private Integer signType;
    /**
    * 单价（单位统一为元）
    */
    @Column(name="unitAmount")
    private BigDecimal unitAmount;
}