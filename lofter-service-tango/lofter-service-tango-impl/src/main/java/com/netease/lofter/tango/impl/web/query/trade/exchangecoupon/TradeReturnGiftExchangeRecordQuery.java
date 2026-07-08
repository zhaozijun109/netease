package com.netease.lofter.tango.impl.web.query.trade.exchangecoupon;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
* 解锁券解锁记录
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeReturnGiftExchangeRecordQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    private Long id;
    /**
    * 用户Id
    */
    private Long userId;
    /**
    * 兑换券id
    */
    private Long couponId;
    /**
    * 文章id
    */
    private Long postId;
    /**
    * 状态 0-初始状态；1-已解锁
    */
    private Integer status;
    /**
    * 兑换时间
    */
    private Long exchangeTime;
    /**
    * 窗口start
    */
    private Long startIndex;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 扩展信息
    */
    private String ext;
    /**
    * 文章博客id
    */
    private Long blogId;
    /**
    * 回礼id
    */
    private Long planId;
    /**
    * 关联id,如兑换券Trade_UserExchangeCoupon表id
    */
    private Long relatedId;
    /**
    * 抵用礼物id
    */
    private Long exchangeGiftId;
    /**
    * 类型 0-早期兑换券；1-兑换文章；2-抽奖
    */
    private Integer type;
    /**
    * 父订单id
    */
    private Long parentOrderId;
    /**
    * 0-无父定单
    */
    private Integer parentOrderType;
    /**
    * 来源，0：普通；1:新用户，2：合集聚合支付
    */
    private Integer scene;
    /**
    * 是否结算，0：结算；-1：不结算
    */
    private Integer needSettle;
    /**
    * 收礼内容类型 0-UGC 1-PGC
    */
    private Integer signType;
    /**
    * 单价（单位统一为元）
    */
    private BigDecimal unitAmount;
    private Long createTimeBegin;
    private Long createTimeEnd;
}