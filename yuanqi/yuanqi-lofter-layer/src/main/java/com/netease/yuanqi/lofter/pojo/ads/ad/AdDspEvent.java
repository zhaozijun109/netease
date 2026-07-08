package com.netease.yuanqi.lofter.pojo.ads.ad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdDspEvent {
    private String appId;
    private String dspId;
    private String os;
    private String positionId;
    private String positionName;
    private int success;
    private long requestTime;
    private long responseTime;
    private int winFlag;
    private String uuid;
    private String reqid;
    private String slotId;
    private double price;
    private long blogId;
    private double bidFactor;
    private Map<String, String> ext;
    private String version;

    public Map<String, String> getExt() {
        return ext;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDspId() {
        return dspId;
    }

    public void setDspId(String dspId) {
        this.dspId = dspId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public int getWinFlag() {
        return winFlag;
    }

    public void setWinFlag(int winFlag) {
        this.winFlag = winFlag;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReqid() {
        return reqid;
    }

    public void setReqid(String reqid) {
        this.reqid = reqid;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getBlogId() {
        return blogId;
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public double getBidFactor() {
        return bidFactor;
    }

    public void setBidFactor(double bidFactor) {
        this.bidFactor = bidFactor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "AdDspEvent{"
                + "appId='"
                + appId
                + '\''
                + ", dspId='"
                + dspId
                + '\''
                + ", os='"
                + os
                + '\''
                + ", positionId='"
                + positionId
                + '\''
                + ", positionName='"
                + positionName
                + '\''
                + ", success="
                + success
                + ", requestTime="
                + requestTime
                + ", responseTime="
                + responseTime
                + ", winFlag="
                + winFlag
                + ", uuid='"
                + uuid
                + '\''
                + ", reqid='"
                + reqid
                + '\''
                + ", slotId='"
                + slotId
                + '\''
                + ", price="
                + price
                + ", blogId="
                + blogId
                + ", bidFactor="
                + bidFactor
                + ", ext="
                + ext
                + ", version='"
                + version
                + '\''
                + '}';
    }
}
