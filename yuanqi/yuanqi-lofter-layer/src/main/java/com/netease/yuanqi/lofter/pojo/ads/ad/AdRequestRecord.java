package com.netease.yuanqi.lofter.pojo.ads.ad;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AdRequestRecord {
    private String action;

    @JsonProperty("req_uid")
    private String reqUid;

    private Long time;

    @JsonProperty("req_id")
    private String reqId;

    private Long positionId;
    private String positionName;
    private String slotId;
    private String slotType;
    private String dspId;
    private Long userId;

    private Double bidPrice;
    private Double bidFactor;
    private Double ecpm;
    private int serverWin;
    private String labels;
    private String os;
    private String appVersion;

    @JsonProperty("ext_info")
    private Map<String, String> extInfo;

    public Map<String, String> getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public int getServerWin() {
        return serverWin;
    }

    public void setServerWin(int serverWin) {
        this.serverWin = serverWin;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
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

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public String getDspId() {
        return dspId;
    }

    public void setDspId(String dspId) {
        this.dspId = dspId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
