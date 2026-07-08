package com.netease.bdms.ndi.service.web.service;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ReaderServiceTest extends DataIntegrationWebTests {

  @Autowired
  private ReaderService readerService;

  @Test
  public void insertHiveReader() {

  }

  @Test
  public void updateHiveReader() {
  }

  @Test
  public void insertDdbDbiReader() {
    JSONObject reader = new JSONObject();

    readerService.insertDdbDbiReader(reader);
  }

  @Test
  public void updateDdbDbiReader() {
  }

  @Test
  public void insertDdbQsReader() {
  }

  @Test
  public void updateDdbQsReader() {
  }

  @Test
  public void insertMySQLReader() {
  }

  @Test
  public void updateMySQLReader() {
  }
}