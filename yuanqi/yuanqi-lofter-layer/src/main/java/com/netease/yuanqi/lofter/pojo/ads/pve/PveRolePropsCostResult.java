package com.netease.yuanqi.lofter.pojo.ads.pve;

public class PveRolePropsCostResult {
    private String dt;
    private Integer hour;
    private Long roleId;
    private Integer roleType;
    private Long propsId;
    private Long costStamina;
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

    public Long getPropsId() {
        return propsId;
    }

    public void setPropsId(Long propsId) {
        this.propsId = propsId;
    }

    public Long getCostStamina() {
        return costStamina;
    }

    public void setCostStamina(Long costStamina) {
        this.costStamina = costStamina;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }
}
