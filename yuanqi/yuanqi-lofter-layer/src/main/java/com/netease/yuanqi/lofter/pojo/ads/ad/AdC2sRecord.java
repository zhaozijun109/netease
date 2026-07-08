package com.netease.yuanqi.lofter.pojo.ads.ad;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdC2sRecord {
    private String action;

    @JsonProperty("step_code")
    private String stepCode;

    @JsonProperty("result_code")
    private String resultCode;

    private String url;

    @JsonProperty("req_uid")
    private String reqUid;

    @JsonProperty("message_type")
    private String messageType;

    @JsonProperty("error_description")
    private String errorDescription;

    private String c2sNum;
    private String ip;
    private String userid;
    private String logtime;
    private String appver;
    private String os;
    private String osver;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStepCode() {
        return stepCode;
    }

    public void setStepCode(String stepCode) {
        this.stepCode = stepCode;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReqUid() {
        return reqUid;
    }

    public void setReqUid(String reqUid) {
        this.reqUid = reqUid;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getC2sNum() {
        return c2sNum;
    }

    public void setC2sNum(String c2sNum) {
        this.c2sNum = c2sNum;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLogtime() {
        return logtime;
    }

    public void setLogtime(String logtime) {
        this.logtime = logtime;
    }

    public String getAppver() {
        return appver;
    }

    public void setAppver(String appver) {
        this.appver = appver;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsver() {
        return osver;
    }

    public void setOsver(String osver) {
        this.osver = osver;
    }
}
