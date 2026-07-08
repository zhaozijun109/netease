package com.netease.yuanqi.unified.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DspEvent {
    private String adId;
    private String appId;
    private String dspId;
    private String os;
    private String positionId;
    private String positionName;
    private int success;
    private long requestTime;
    private long responseTime;
    private String msg;
    private String externalAdId;
    private String ip;
    private int wakeupBoot;
    private int winFlag;
    private String la;
    private String lo;
    private String version;
    private String uuid;
    private String reqid;
    private String banwords;
    private String industryId;
    private String slotId;

    @JsonProperty("advertiser_type")
    private String advertiserType;

    private double price;
    private long blogId;
    private double bidFactor;
    private Map<String, String> ext;

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
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

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getExternalAdId() {
        return externalAdId;
    }

    public void setExternalAdId(String externalAdId) {
        this.externalAdId = externalAdId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getWakeupBoot() {
        return wakeupBoot;
    }

    public void setWakeupBoot(int wakeupBoot) {
        this.wakeupBoot = wakeupBoot;
    }

    public int getWinFlag() {
        return winFlag;
    }

    public void setWinFlag(int winFlag) {
        this.winFlag = winFlag;
    }

    public String getLa() {
        return la;
    }

    public void setLa(String la) {
        this.la = la;
    }

    public String getLo() {
        return lo;
    }

    public void setLo(String lo) {
        this.lo = lo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getBanwords() {
        return banwords;
    }

    public void setBanwords(String banwords) {
        this.banwords = banwords;
    }

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getAdvertiserType() {
        return advertiserType;
    }

    public void setAdvertiserType(String advertiserType) {
        this.advertiserType = advertiserType;
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

    public Map<String, String> getExt() {
        return ext;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }
}
