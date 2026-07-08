package com.netease.pojo.rec;

import java.util.Map;

/** Recommend request data. */
public class RecRequest {
    private String recId;
    private String userId;
    private String sceneName;
    private String itemId;
    private String itemType;
    private Map<String, String> ab;
    private Map<String, String> reqExt;
    private String flowName;
    private String recReasonType;
    private Long recTime;

    public String getRecId() {
        return recId;
    }

    public void setRecId(String recId) {
        this.recId = recId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Map<String, String> getAb() {
        return ab;
    }

    public void setAb(Map<String, String> ab) {
        this.ab = ab;
    }

    public Map<String, String> getReqExt() {
        return reqExt;
    }

    public void setReqExt(Map<String, String> reqExt) {
        this.reqExt = reqExt;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getRecReasonType() {
        return recReasonType;
    }

    public void setRecReasonType(String recReasonType) {
        this.recReasonType = recReasonType;
    }

    public Long getRecTime() {
        return recTime;
    }

    public void setRecTime(Long recTime) {
        this.recTime = recTime;
    }
}
