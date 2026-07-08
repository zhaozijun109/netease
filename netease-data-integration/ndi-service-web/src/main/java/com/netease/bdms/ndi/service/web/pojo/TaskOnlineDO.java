package com.netease.bdms.ndi.service.web.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class TaskOnlineDO {
  private Long id;

  private String owner;

  private String product;

  private String cluster;

  private String creator;

  private String modifier;

  private String executor;

  private String taskId;

  private String taskName;

  private String taskDescription;

  private Byte migrationType;

  private Date createTime;

  private Date modifyTime;

  private Date executeTime;

  private Integer version;

  private Byte status;

  private JSONObject properties;

  private String handlers;

  private Long readerId;

  private Byte readerType;

  private String readerTableName;

  private String readerUrl;

  private String writerUrl;

  private Long writerId;

  private Byte writerType;

  private String writerTableName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner == null ? null : owner.trim();
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
    this.cluster = cluster;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator == null ? null : creator.trim();
  }

  public String getModifier() {
    return modifier;
  }

  public void setModifier(String modifier) {
    this.modifier = modifier == null ? null : modifier.trim();
  }

  public String getExecutor() {
    return executor;
  }

  public void setExecutor(String executor) {
    this.executor = executor == null ? null : executor.trim();
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId == null ? null : taskId.trim();
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName == null ? null : taskName.trim();
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription == null ? null : taskDescription.trim();
  }

  public Byte getMigrationType() {
    return migrationType;
  }

  public void setMigrationType(Byte migrationType) {
    this.migrationType = migrationType;
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

  public Date getExecuteTime() {
    return executeTime;
  }

  public void setExecuteTime(Date executeTime) {
    this.executeTime = executeTime;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Byte getStatus() {
    return status;
  }

  public void setStatus(Byte status) {
    this.status = status;
  }

  public JSONObject getProperties() {
    return properties;
  }

  public void setProperties(JSONObject properties) {
    this.properties = properties;
  }

  public String getHandlers() {
    return handlers;
  }

  public void setHandlers(String handlers) {
    this.handlers = handlers == null ? null : handlers.trim();
  }

  public Long getReaderId() {
    return readerId;
  }

  public void setReaderId(Long readerId) {
    this.readerId = readerId;
  }

  public Byte getReaderType() {
    return readerType;
  }

  public void setReaderType(Byte readerType) {
    this.readerType = readerType;
  }

  public String getReaderTableName() {
    return readerTableName;
  }

  public void setReaderTableName(String readerTableName) {
    this.readerTableName = readerTableName == null ? null : readerTableName.trim();
  }

  public Long getWriterId() {
    return writerId;
  }

  public void setWriterId(Long writerId) {
    this.writerId = writerId;
  }

  public Byte getWriterType() {
    return writerType;
  }

  public String getReaderUrl() {
    return readerUrl;
  }

  public void setReaderUrl(String readerUrl) {
    this.readerUrl = readerUrl;
  }

  public String getWriterUrl() {
    return writerUrl;
  }

  public void setWriterUrl(String writerUrl) {
    this.writerUrl = writerUrl;
  }

  public void setWriterType(Byte writerType) {
    this.writerType = writerType;
  }

  public String getWriterTableName() {
    return writerTableName;
  }

  public void setWriterTableName(String writerTableName) {
    this.writerTableName = writerTableName == null ? null : writerTableName.trim();
  }
}