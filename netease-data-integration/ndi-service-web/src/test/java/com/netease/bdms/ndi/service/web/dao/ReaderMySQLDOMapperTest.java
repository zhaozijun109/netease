package com.netease.bdms.ndi.service.web.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.pojo.ReaderMySQLDO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class ReaderMySQLDOMapperTest {
    @Autowired
    private ReaderMySQLDOMapper readerMySQLDOMapper;

    @Test
    public void deleteByPrimaryKey() {

    }

    @Test
    public void insert() {
      ReaderMySQLDO readerMySQLDO = new ReaderMySQLDO();
      readerMySQLDO.setDataSources(new JSONArray());
      readerMySQLDO.setConditions(JSONObject.toJSONString(""));
      readerMySQLDO.setConf(JSONObject.toJSONString(""));
      readerMySQLDO.setCreateTime(new Date());
      readerMySQLDO.setModifyTime(new Date());
      int result = readerMySQLDOMapper.insert(readerMySQLDO);
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
    }

    @Test
    public void te(){

    }
}