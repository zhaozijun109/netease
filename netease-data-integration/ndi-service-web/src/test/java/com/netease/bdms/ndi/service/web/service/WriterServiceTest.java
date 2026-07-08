package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WriterServiceTest extends DataIntegrationWebTests {

  @Autowired
  private WriterService writerService;

  @Test
  public void insertHiveWriter() {
    JSONObject hiveWriter = new JSONObject();
    writerService.insertHiveWriter(hiveWriter);
  }

  @Test
  public void updateHiveWriter() {
  }

  @Test
  public void insertDdbDbiWriter() {
  }

  @Test
  public void updateDdbDbiWriter() {
  }

  @Test
  public void insertDdbQsWriter() {
  }

  @Test
  public void updateDdbQsWriter() {
  }

  @Test
  public void insertMySQLWriter() {
  }

  @Test
  public void updateMySQLWriter() {
  }
}