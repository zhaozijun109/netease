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
* 兑换券SKU
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ExchangeCouponSKU")
public class TradeExchangeCouponSKU implements Serializable {
    private static final long serialVersionUID = -1;
    /**
     * 对标 trade CouponOrderScene
     */
    public final static int SCENE_NORMAL = 0;
    public final static int SCENE_UGC = 1;
    public final static int SCENE_ACT = 3;
    public final static int SCENE_COUPON_CARD = 9;
    public final static int SCENE_IP_CARD = 13;
    public final static int SCENE_IP_CARD_GIVEAWAY = 14;
    public final static int SCENE_IP_SINGLE_CARD = 15;
    public final static int SCENE_SYSTEM = 16;
    public final static int SCENE_IP_GROUP_CARD = 18;
    public final static int SCENE_IP_SLOT = 17;
    public final static int SCENE_IP_CARD_EXCHANGE = 20;


    public final static int TYPE_COUPON_PACK = 0;


    /**
    * 
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 商品code
    */
    @Column(name="productId")
    @SqlIgnore(selectIgnore = false, insertIgnore = false)
    private String productId;
    /**
    * SPU类型 0-券包；1-活动卡
    */
    @Column(name="type")
    private Integer type;
    /**
    * 商品名
    */
    @Column(name="name")
    private String name;
    /**
    * 商品配图
    */
    @Column(name="img")
    private String img;
    /**
    * 原价
    */
    @Column(name="marketPrice")
    private BigDecimal marketPrice;
    /**
    * 折扣价
    */
    @Column(name="discountPrice")
    private BigDecimal discountPrice;
    /**
    * 折扣文案
    */
    @Column(name="discountText")
    private String discountText;
    /**
    * 平台
    */
    @Column(name="platform")
    private Integer platform;
    /**
    * 状态 0-正常；-1-下架
    */
    @Column(name="status")
    private Integer status;
    /**
    * 子商品
    */
    @Column(name="subProducts")
    private String subProducts;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;

    @Column(name="message")
    private String message;

    @Column(name="purchaseLimit")
    private int purchaseLimit;

    /**
     * 发放策略
     */
    @Column(name="grantStrategy")
    private String grantStrategy;
    /**
     * 发放策略
     */
    @Column(name="settleStrategy")
    private String settleStrategy;

    /**
     * 赠品
     */
    @Column(name="giveawayProducts")
    private String giveawayProducts;
    /**
     * 折扣价
     */
    @Column(name="giveawayAmount")
    private BigDecimal giveawayAmount;
    /**
     * 宣传文案
     */
    @Column(name="giveawayPromotion")
    private String giveawayPromotion;

    @Column(name="scene")
    private int scene;
    @Column(name="showCondition")
    private String showCondition;
}