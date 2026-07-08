package com.netease.bdms.ndi.service.web.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.pojo.ReaderDdbQsDO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class ReaderDdbQsDOMapperTest extends DataIntegrationWebTests {

  @Autowired
  private ReaderDdbQsDOMapper readerDdbQsDOMapper;

  @Test
  public void deleteByPrimaryKey() {
  }

  @Test
  public void insert() {
    ReaderDdbQsDO readerDdbQsDO = new ReaderDdbQsDO();
    readerDdbQsDO.setDataSources(new JSONArray());
    readerDdbQsDO.setConditions(JSONObject.toJSONString(""));
    readerDdbQsDO.setConf(JSONObject.toJSONString(""));
    readerDdbQsDO.setCreateTime(new Date());
    readerDdbQsDO.setModifyTime(new Date());
    int result = readerDdbQsDOMapper.insert(readerDdbQsDO);
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
    ReaderDdbQsDO readerDdbQsDO = new ReaderDdbQsDO();
    readerDdbQsDO.setId(1L);
    readerDdbQsDO.setDataSources(new JSONArray());
    readerDdbQsDO.setConditions(JSONObject.toJSONString("conditions"));
    readerDdbQsDO.setConf(JSONObject.toJSONString("conf"));
    readerDdbQsDO.setCreateTime(new Date());
    readerDdbQsDO.setModifyTime(new Date());
    int result = readerDdbQsDOMapper.updateByPrimaryKey(readerDdbQsDO);
    System.out.println(result);
  }
}