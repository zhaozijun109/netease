package com.netease.pojo.pve;

public class PveUserPropsLog {
    private String tableName;
    private Long id;
    private Long userId;
    private Long propsId;
    private Long roleId;
    private Long costStamina;
    private Long dialogueNo;
    private Long dialogueId;
    private Integer status;
    private Long createTime;
    private Long updateTime;
    private Long dbUpdateTime;
    private Long userDupId;
    private String propsDesc;
    private Integer propsType;
    private Integer type;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPropsId() {
        return propsId;
    }

    public void setPropsId(Long propsId) {
        this.propsId = propsId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getCostStamina() {
        return costStamina;
    }

    public void setCostStamina(Long costStamina) {
        this.costStamina = costStamina;
    }

    public Long getDialogueNo() {
        return dialogueNo;
    }

    public void setDialogueNo(Long dialogueNo) {
        this.dialogueNo = dialogueNo;
    }

    public Long getDialogueId() {
        return dialogueId;
    }

    public void setDialogueId(Long dialogueId) {
        this.dialogueId = dialogueId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDbUpdateTime() {
        return dbUpdateTime;
    }

    public void setDbUpdateTime(Long dbUpdateTime) {
        this.dbUpdateTime = dbUpdateTime;
    }

    public Long getUserDupId() {
        return userDupId;
    }

    public void setUserDupId(Long userDupId) {
        this.userDupId = userDupId;
    }

    public String getPropsDesc() {
        return propsDesc;
    }

    public void setPropsDesc(String propsDesc) {
        this.propsDesc = propsDesc;
    }

    public Integer getPropsType() {
        return propsType;
    }

    public void setPropsType(Integer propsType) {
        this.propsType = propsType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
