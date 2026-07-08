package com.netease.yuanqi.lofter.pojo.ads.ad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdBidParameter {
    private String bidFactor;
    private String bidPrice;

    public String getBidFactor() {
        return bidFactor;
    }

    public void setBidFactor(String bidFactor) {
        this.bidFactor = bidFactor;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(String bidPrice) {
        this.bidPrice = bidPrice;
    }
}
