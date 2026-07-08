package com.netease.yuanqi.lofter.pojo.ads.pve;

public class PveDialogueHourStatistics {
    private String dt;
    private Integer hour;
    private Long roleId;
    private Integer roleType;
    private Long dialoguePv;
    private Long dialogueUv;
    private Integer messageType;

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Long getDialoguePv() {
        return dialoguePv;
    }

    public void setDialoguePv(Long dialoguePv) {
        this.dialoguePv = dialoguePv;
    }

    public Long getDialogueUv() {
        return dialogueUv;
    }

    public void setDialogueUv(Long dialogueUv) {
        this.dialogueUv = dialogueUv;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }
}
