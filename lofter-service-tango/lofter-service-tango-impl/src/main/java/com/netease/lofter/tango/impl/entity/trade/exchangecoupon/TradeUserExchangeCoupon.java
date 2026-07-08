package com.netease.lofter.tango.impl.entity.trade.exchangecoupon;

import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.SqlIgnore;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 解锁券获取记录
 * generate by yaolu mybatis generator
 */
@Table("Trade_UserExchangeCoupon")
public class TradeUserExchangeCoupon implements Serializable {
    private static final long serialVersionUID = -1;

    /**
     * 主键ID
     */
    @Key
    @Column(name="id")
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
     * 数量
     */
    @Column(name="count")
    private Integer count;

    /**
     * 未使用数量
     */
    @Column(name="balance")
    private Integer balance;

    /**
     * 创建时间
     */
    @Column(name="createTime")
    private Long createTime;

    /**
     * 失效时间
     */
    @Column(name="expireTime")
    private Long expireTime;

    /**
     * 扩展信息
     */
    @Column(name="ext")
    private String ext;

    /**
     * 获取方式 0-充值赠送; 1-累计消费赠送; 2-抽奖获取; 3-购买
     */
    @Column(name="type")
    private Integer type;

    /**
     * 交易总金额，包含乐乎币购买，乐乎币购买为需要币数
     */
    @Column(name="amount")
    private BigDecimal amount;
    /**
     * 券单价金额，包含乐乎币购买，乐乎币购买为需要币数
     */
    @Column(name="unitAmount")
    private BigDecimal unitAmount;

    /**
     * 来源，0：普通；1:新用户，2：合集聚合支付
     */
    @Column(name="scene")
    private int scene;

    @Column(name="tradeId")
    private long tradeId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }
}