package com.netease.bdms.ndi.service.web.pojo;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class DatasourceAzkabanConnectionDO {
    private Long id;

    private Long datasourceId;

    private Long productId;

    private String clusterId;

    private Integer execStatus;

    private Integer execResult;

    private JSONObject execMessage;

    private Date createTime;

    private Date modifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public Integer getExecStatus() {
        return execStatus;
    }

    public void setExecStatus(Integer execStatus) {
        this.execStatus = execStatus;
    }

    public Integer getExecResult() {
        return execResult;
    }

    public void setExecResult(Integer execResult) {
        this.execResult = execResult;
    }

    public JSONObject getExecMessage() {
        return execMessage;
    }

    public void setExecMessage(JSONObject execMessage) {
        this.execMessage = execMessage;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}