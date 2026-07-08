package com.netease.bdms.ndi.service.web.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbDbiDO;
import org.apache.http.impl.conn.LoggingSessionOutputBuffer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class ReaderDdbDbiDOMapperTest extends DataIntegrationWebTests {

  @Autowired
  private ReaderDdbDbiDOMapper readerDdbDbiDOMapper;

  @Test
  public void deleteByPrimaryKey() {

  }

  @Test
  public void insert() {
    ReaderDdbDbiDO readerDdbDbiDO = new ReaderDdbDbiDO();
    readerDdbDbiDO.setModifyTime(new Date());
    readerDdbDbiDO.setCreateTime(new Date());
    JSONArray jsonArray = new JSONArray(Lists.newArrayList());
    readerDdbDbiDO.setDataSources(jsonArray);
    readerDdbDbiDO.setConditions(JSONObject.toJSONString(""));
    readerDdbDbiDO.setConf(JSONObject.toJSONString(""));
    int result = readerDdbDbiDOMapper.insert(readerDdbDbiDO);
    System.out.println(result);
  }

  @Test
  public void insertSelective() {
  }

  @Test
  public void selectByPrimaryKey() {
  }

  @Test
  public void updateByPrimaryKeySelective() {
  }

  @Test
  public void updateByPrimaryKey() {
    ReaderDdbDbiDO readerDdbDbiDO = new ReaderDdbDbiDO();
    readerDdbDbiDO.setId(1L);
    readerDdbDbiDO.setModifyTime(new Date());
    readerDdbDbiDO.setCreateTime(new Date());
    JSONArray jsonArray = new JSONArray(Lists.newArrayList());
    readerDdbDbiDO.setDataSources(jsonArray);
    readerDdbDbiDO.setConditions(JSONObject.toJSONString("conditions"));
    readerDdbDbiDO.setConf(JSONObject.toJSONString("conf"));
    int result = readerDdbDbiDOMapper.updateByPrimaryKeySelective(readerDdbDbiDO);
    System.out.println(result);
  }
}