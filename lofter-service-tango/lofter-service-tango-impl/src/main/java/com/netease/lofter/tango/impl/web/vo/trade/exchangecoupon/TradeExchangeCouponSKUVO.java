package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
* 兑换券SKU
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeExchangeCouponSKUVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键ID
    */
    private Long id;
    /**
    * 商品code
    */
    private String productId;
    /**
    * SPU类型 0-券包；1-暑期卡
    */
    private Integer type;
    /**
    * 商品名
    */
    private String name;
    /**
    * 商品配图
    */
    private String img;
    /**
    * 原价
    */
    private BigDecimal marketPrice;
    /**
    * 折扣价
    */
    private BigDecimal discountPrice;
    /**
    * 折扣文案
    */
    private String discountText;
    /**
    * 平台
    */
    private Integer platform;
    /**
    * 状态 0-正常；-1-下架
    */
    private Integer status;
    /**
    * 子商品
    */
    private String subProducts;
    /**
    * 赠品
    */
    private String giveawayProducts;
    /**
     * 折扣价
     */
    private BigDecimal giveawayAmount;
    /**
     * 宣传文案
     */
    private String giveawayPromotion;
    /**
     * 场景
     */
    private int scene;

    /**
    * 创建时间
    */
    private Long createTime;

    /**
     * 人群包id
     */
    private Long crowdId;

    private String message;

    /**
     * 子商品列表
     */
    private List<ExchangeCouponVO> exchangeCoupons;

    /**
     * 赠品
     */
    private List<ExchangeCouponVO> giveawayCoupons;


    private Long activityId;

    /**
     * 购买次数限制
     */
    private int purchaseLimit;

    private String grantStrategy;
    private String settleStrategy;
    private String showCondition;

    public List<ExchangeCouponVO> getGiveawayCoupons() {
        return giveawayCoupons == null ? Lists.newArrayList() : giveawayCoupons;
    }
}