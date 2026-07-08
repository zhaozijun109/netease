package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSONArray;

import java.util.Date;

public class ReaderMySQLDO {
  private Long id;

  private JSONArray dataSources;

  private String conditions;

  private String taskId;

  private String conf;

  private Date createTime;

  private Date modifyTime;

  private String splitKey;

  public ReaderMySQLDO() {
  }

  public ReaderMySQLDO(JSONArray dataSources, String conditions, String conf) {
    this.dataSources = dataSources;
    this.conditions = conditions;
    this.conf = conf;
    this.createTime = new Date();
    this.modifyTime = new Date();
  }

  public ReaderMySQLDO(Long id, JSONArray dataSources, String conditions, String conf) {
    this.id = id;
    this.dataSources = dataSources;
    this.conditions = conditions;
    this.conf = conf;
    this.modifyTime = new Date();
  }

  public String getSplitKey() {
    return splitKey;
  }

  public void setSplitKey(String splitKey) {
    this.splitKey = splitKey;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public JSONArray getDataSources() {
    return dataSources;
  }

  public void setDataSources(JSONArray dataSources) {
    this.dataSources = dataSources;
  }

  public String getConditions() {
    return conditions;
  }

  public void setConditions(String conditions) {
    this.conditions = conditions == null ? null : conditions.trim();
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

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId == null ? null : taskId.trim();
  }

  public String getConf() {
    return conf;
  }

  public void setConf(String conf) {
    this.conf = conf == null ? null : conf.trim();
  }
}