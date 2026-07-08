package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class ReaderHiveDO {
    private Long id;

    private JSONObject dataSource;

    private Date createTime;

    private Date modifyTime;

    private String conf;

    private String conditions;

  public ReaderHiveDO() {
  }

  public ReaderHiveDO(JSONObject dataSource, String conditions, String conf) {
    this.dataSource = dataSource;
    this.conditions = conditions;
    this.conf = conf;
    this.createTime = new Date();
    this.modifyTime = new Date();
  }

  public ReaderHiveDO(Long id, JSONObject dataSource, String conditions, String conf) {
    this.id = id;
    this.dataSource = dataSource;
    this.conditions = conditions;
    this.conf = conf;
    this.modifyTime = new Date();
  }


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

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions == null ? null : conditions.trim();
    }
}