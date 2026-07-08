package com.netease.bdms.ndi.service.web.dao;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.pojo.ReaderHiveDO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class ReaderHiveDOMapperTest extends DataIntegrationWebTests {

  @Autowired
  private ReaderHiveDOMapper readerHiveDOMapper;

  @Test
  public void deleteByPrimaryKey() {
  }

  @Test
  public void insert() {
    ReaderHiveDO readerHiveDO = new ReaderHiveDO();
    readerHiveDO.setDataSource(new JSONObject());
    readerHiveDO.setConditions(JSONObject.toJSONString(""));
    readerHiveDO.setConf(JSONObject.toJSONString(""));
    readerHiveDO.setCreateTime(new Date());
    readerHiveDO.setModifyTime(new Date());
    readerHiveDOMapper.insert(readerHiveDO);
    System.out.println(readerHiveDO.getId());
  }

  @Test
  public void insertSelective() {
  }

  @Test
  public void selectByPrimaryKey() {
  }

  @Test
  public void updateByPrimaryKeySelective() {

    ReaderHiveDO readerHiveDO = new ReaderHiveDO();
    readerHiveDO.setId(27L);
    readerHiveDO.setDataSource(new JSONObject());
    readerHiveDO.setConditions(JSONObject.toJSONString(""));
    readerHiveDO.setConf(JSONObject.toJSONString(""));
    readerHiveDO.setCreateTime(new Date());
    readerHiveDO.setModifyTime(new Date());
    int result = readerHiveDOMapper.updateByPrimaryKeySelective(readerHiveDO);
    System.out.println(result);
  }

  @Test
  public void updateByPrimaryKey() {
  }
}