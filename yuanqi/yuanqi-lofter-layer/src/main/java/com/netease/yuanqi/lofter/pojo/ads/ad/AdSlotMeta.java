package com.netease.yuanqi.lofter.pojo.ads.ad;

public class AdSlotMeta {
    private String slotId;
    private String slotType;
    private Double ecpm;
    private Long updateTime;

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

    public Double getEcpm() {
        return ecpm;
    }

    public void setEcpm(Double ecpm) {
        this.ecpm = ecpm;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public AdSlotMeta(String slotId, String slotType, Double ecpm, Long updateTime) {
        this.slotId = slotId;
        this.slotType = slotType;
        this.ecpm = ecpm;
        this.updateTime = updateTime;
    }
}
