package com.netease.bdms.ndi.service.web.dao;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.pojo.WriterHiveDO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class WriterHiveDOMapperTest extends DataIntegrationWebTests {
    @Autowired
    private WriterHiveDOMapper writerHiveDOMapper;

    @Test
    public void deleteByPrimaryKey() {
    }

    @Test
    public void deleteByPrimaryKeys() {

    }

    @Test
    public void insert() {
      WriterHiveDO writerHiveDO = new WriterHiveDO();
      writerHiveDO.setDataSource(new JSONObject());
      writerHiveDO.setInsertType(1);
      writerHiveDO.setConf(JSONObject.toJSONString(""));
      writerHiveDO.setCreateTime(new Date());
      writerHiveDO.setModifyTime(new Date());
      int result = writerHiveDOMapper.insert(writerHiveDO);
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
      WriterHiveDO writerHiveDO = new WriterHiveDO();
      writerHiveDO.setId(165L);
      writerHiveDO.setDataSource(new JSONObject());
      writerHiveDO.setInsertType(1);
      writerHiveDO.setConf(JSONObject.toJSONString(""));
      writerHiveDO.setCreateTime(new Date());
      writerHiveDO.setModifyTime(new Date());
      int result = writerHiveDOMapper.updateByPrimaryKeySelective(writerHiveDO);
      System.out.println(result);
    }

    @Test
    public void updateByPrimaryKey() {
    }
}