package com.netease.bdms.ndi.service.web.pojo;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

public class WriterOracleDO {
    private Long id;

    private Date createTime;

    private Date modifyTime;

    private JSONObject dataSource;

    private Integer insertType;

    private String preSql;

    private String postSql;

    private String conf;

    public WriterOracleDO() {
    }

    public WriterOracleDO(Long id, JSONObject dataSource, Integer insertType, String preSql, String postSql, String conf) {
        this.id = id;
        this.dataSource = dataSource;
        this.insertType = insertType;
        this.preSql = preSql;
        this.postSql = postSql;
        this.conf = conf;
    }

    public WriterOracleDO(JSONObject dataSource, Integer insertType, String preSql, String postSql, String conf) {
        this.dataSource = dataSource;
        this.insertType = insertType;
        this.preSql = preSql;
        this.postSql = postSql;
        this.conf = conf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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