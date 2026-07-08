package com.netease.pojo.revenue;

/** According to orderType to window aggregate. */
public class OrderTypeAgg {
    private String orderType;
    private Long orderPv;
    private Long orderUv;
    private Integer orderAmount;
    private Long payTime;
    private String payOrderDate;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getOrderPv() {
        return orderPv;
    }

    public void setOrderPv(Long orderPv) {
        this.orderPv = orderPv;
    }

    public Long getOrderUv() {
        return orderUv;
    }

    public void setOrderUv(Long orderUv) {
        this.orderUv = orderUv;
    }

    public Integer getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Integer orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
    }

    public String getPayOrderDate() {
        return payOrderDate;
    }

    public void setPayOrderDate(String payOrderDate) {
        this.payOrderDate = payOrderDate;
    }
}
