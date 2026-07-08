package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class WriterDdbDbiDO {
  private Long id;

  private JSONObject dataSource;

  private Integer insertType;

  private Date createTime;

  private Date modifyTime;

  private String preSql;

  private String postSql;

  private String conf;

  public WriterDdbDbiDO() {
  }

  public WriterDdbDbiDO(JSONObject dataSource, Integer insertType, String preSql, String postSql, String conf) {
    this.dataSource = dataSource;
    this.insertType = insertType;
    this.preSql = preSql;
    this.postSql = postSql;
    this.conf = conf;
    this.createTime = new Date();
    this.modifyTime = new Date();
  }

  public WriterDdbDbiDO(Long id, JSONObject dataSource, Integer insertType, String preSql, String postSql, String conf) {
    this.id = id;
    this.dataSource = dataSource;
    this.insertType = insertType;
    this.preSql = preSql;
    this.postSql = postSql;
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

  public String getPreSql() {
    return preSql;
  }

  public void setPreSql(String preSql) {
    this.preSql = preSql == null ? null : preSql.trim();
  }

  public String getPostSql() {
    return postSql;
  }

  public void setPostSql(String postSql) {
    this.postSql = postSql == null ? null : postSql.trim();
  }

  public String getConf() {
    return conf;
  }

  public void setConf(String conf) {
    this.conf = conf == null ? null : conf.trim();
  }
}