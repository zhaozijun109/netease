package com.netease.yuanqi.lofter.pojo.ads.ad;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdActionRecord {
    private String action;

    @JsonProperty("req_uid")
    private String reqUid;

    private Long time;
    private Double bidPrice;
    private Double bidFactor;
    private Double ecpm;
    private String os;
    private String dspId;
    private String slotId;
    private String appVersion;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDspId() {
        return dspId;
    }

    public void setDspId(String dspId) {
        this.dspId = dspId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReqUid() {
        return reqUid;
    }

    public void setReqUid(String reqUid) {
        this.reqUid = reqUid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Double getBidFactor() {
        return bidFactor;
    }

    public void setBidFactor(Double bidFactor) {
        this.bidFactor = bidFactor;
    }

    public Double getEcpm() {
        return ecpm;
    }

    public void setEcpm(Double ecpm) {
        this.ecpm = ecpm;
    }
}
