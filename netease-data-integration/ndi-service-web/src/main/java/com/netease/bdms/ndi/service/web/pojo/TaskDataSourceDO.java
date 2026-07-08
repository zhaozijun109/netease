package com.netease.bdms.ndi.service.web.pojo;

import java.util.Date;

public class TaskDataSourceDO {
    private Long id;

    private Long dataSourceId;

    private String taskId;

    private String product;

    private String cluster;

    private Date createTime;

    private Date modifyTime;

    public TaskDataSourceDO() {
    }

    public TaskDataSourceDO(Long dataSourceId, String taskId, String product, String cluster) {
        this.dataSourceId = dataSourceId;
        this.taskId = taskId;
        this.product = product;
        this.cluster = cluster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product == null ? null : product.trim();
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster == null ? null : cluster.trim();
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