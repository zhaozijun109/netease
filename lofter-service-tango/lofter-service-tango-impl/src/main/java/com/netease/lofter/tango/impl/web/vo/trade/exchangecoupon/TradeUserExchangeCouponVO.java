package com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class TradeUserExchangeCouponVO implements Serializable {
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
     * 数量
     */
    private Integer count;

    /**
     * 未使用数量
     */
    private Integer balance;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 失效时间
     */
    private Long expireTime;

    /**
     * 扩展信息
     */
    private String ext;

    /**
     * 获取方式 0-充值赠送; 1-累计消费赠送; 2-抽奖获取; 3-购买
     */
    private Integer type;

    /**
     * 交易总金额，包含乐乎币购买，乐乎币购买为需要币数
     */
    private BigDecimal amount;
    /**
     * 券单价金额，包含乐乎币购买，乐乎币购买为需要币数
     */
    private BigDecimal unitAmount;

    private long tradeId;
    /** ip */
    private long scopeExt;
}
