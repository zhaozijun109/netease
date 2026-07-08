package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class WriterHiveDO {
  private Long id;

  private JSONObject dataSource;

  private Integer insertType;

  private Date createTime;

  private Date modifyTime;

  private String conf;

  private String partitionList;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public JSONObject getDataSource() {
    return dataSource;
  }

  public void setDataSource(JSONObject dataSource) {
    this.dataSource = dataSource;
  }

  public Integer getInsertType() {
    return insertType;
  }

  public void setInsertType(Integer insertType) {
    this.insertType = insertType;
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

  public String getConf() {
    return conf;
  }

  public void setConf(String conf) {
    this.conf = conf == null ? null : conf.trim();
  }

  public String getPartitionList() {
    return partitionList;
  }

  public void setPartitionList(String partitionList) {
    this.partitionList = partitionList == null ? null : partitionList.trim();
  }
}
