package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class ConfigDO {
  private Long id;

  private JSONObject prop;

  private Date creatTime;

  private Date modifyTime;

  private String name;

  private String namespace;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public JSONObject getProp() {
    return prop;
  }

  public void setProp(JSONObject prop) {
    this.prop = prop;
  }

  public Date getCreatTime() {
    return creatTime;
  }

  public void setCreatTime(Date creatTime) {
    this.creatTime = creatTime;
  }

  public Date getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(Date modifyTime) {
    this.modifyTime = modifyTime;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace == null ? null : namespace.trim();
  }
}