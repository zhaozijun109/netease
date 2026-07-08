package com.netease.bdms.ndi.service.web.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.pojo.DatasourceAzkabanConnectionDO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DatasourceAzkabanConnectionDOMapperTest extends DataIntegrationWebTests {

  @Autowired
  private DatasourceAzkabanConnectionDOMapper connectionDOMapper;

  @Test
  public void updateCheckingStatus() {
    int result = connectionDOMapper.updateCheckingStatus(1L, 1, Lists.newArrayList("1", "2", "3"));
    System.out.println(result);
  }

  @Test
  public void updateFinishedStatus() {
  }

  @Test
  public void updateResult() {
    JSONObject jsonObject = JSONObject.parseObject("{\n" +
        "\t\"db\": \"yr_dbtest_6\",\n" +
        "\t\"table\":\"hw_test_split\",\n" +
        "\t\"subjectIds\":[\n" +
        "\t\t\"12\", \"22\"\n" +
        "\t\t]\n" +
        "}");
    int result = connectionDOMapper.updateResult(1, jsonObject, 1L, 1, "3");
    System.out.println(result);
  }

  @Test
  public void selectByDatasourceIdsAndProduct() {
    List<DatasourceAzkabanConnectionDO> connectionDOList =  connectionDOMapper.selectByDatasourceIdsAndProduct(Lists.newArrayList(1177L), 25);
    System.out.println(connectionDOList);
  }

  @Test
  public void deleteByDataSourceIdsAndProduct() {
    int result = connectionDOMapper.deleteByDataSourceAndProductId(1155L, 25);
    System.out.println(result);
  }
}