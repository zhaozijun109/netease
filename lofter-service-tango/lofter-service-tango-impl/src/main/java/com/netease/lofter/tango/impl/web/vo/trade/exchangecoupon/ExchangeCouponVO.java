package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ExchangeCouponVO implements Serializable {
    /**
     * 券配图
     */
    private String icon;
    /**
     * 券id
     */
    private long id;
    /**
     * 数量
     */
    private int count;
    /**
     * 0-累加时间；1-自然日
     */
    private int expireType;
    /**
     * 有效期
     */
    private int day;
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    /**
     * 0-糖果券
     */
    private int type;

    private String name;

    private int grantDays;

    private String promotion;
    private int grantCount;
    private int showCount;

    private String activityCode;
    private String itemCode;
}
