package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetahubServiceImplTest extends DataIntegrationWebTests {

  @Autowired
  private MetahubService metahubService;
  @Autowired
  private UserService userService;
  private static final Integer accountId = 25;
  private static final String catalog = "hz1-hive-catalog";

  @Test
  public void getCatalog() {
    String jsonObject = metahubService.getCatalog(1201L, 678);
    System.out.println(jsonObject);

  }

  @Test
  public void addCatalog() {
    JSONObject addCatalogParam = new JSONObject();
    addCatalogParam.put("accountId", 38);
    addCatalogParam.put("name", "test_catalog");
    addCatalogParam.put("creator", "zhaomin3@corp.netease.com");
    addCatalogParam.put("modifier", "zhaomin3@corp.netease.com");
    addCatalogParam.put("description", "test");
    addCatalogParam.put("type","mysql1");
    JSONArray jsonArray = new JSONArray();
    Map<String, Object> ds = new HashMap<>();
    ds.put("dataSourceId", 942);
    ds.put("isDefault", 1);
    jsonArray.add(ds);
    addCatalogParam.put("dataSourceList", jsonArray);
    JSONObject addCatalogResponse = metahubService.addCatalog(addCatalogParam);
    System.out.println(addCatalogResponse);
  }

  @Test
  public void listDatabases() {
    JSONObject param = new JSONObject();
    param.put("accountId", 38);
    param.put("catalog", "ddb_测试新建dbi_catalog");
    param.put("offset", 0);
    param.put("limit", 25);
    JSONArray response = metahubService.listDatabases(accountId, catalog, 0, Integer.MAX_VALUE, null, null);
    System.out.println(response);
  }

  @Test
  public void listTableNames() {
    JSONObject param = new JSONObject();
    param.put("catalog", "ddb_测试新建dbi_catalog");
    param.put("db", "ndi");
    param.put("offset", 0);
    param.put("limit", 25);
    param.put("accountId", 25);
    JSONArray response = metahubService.listTableNames(catalog, "linna", 0,
        Integer.MAX_VALUE, accountId, null, null);
    System.out.println(response);
  }

  @Test
  public void getTable() {
    String catalog = "hz1-hive-catalog";
    String db = "linna";
    String table = "delim_serde_normal";
    Integer accountId = 38;
    JSONObject response = metahubService.getTable(catalog, accountId, db, table);
    System.out.println(response);
  }

  @Test
  public void addDataSource() {
    JSONObject info = new JSONObject();
    info.put("url", "jdbc:mysql://10.122.144.140:3331/ndi");
    info.put("user", "work");
    info.put("userName", "work");
    info.put("password", "a7078796123A!");

    Integer accountId = 38;
    String name = "赵敏的测试数据源";
    String type = "mysql";
    String env = "read";
    String creator = "zhaomin3@corp.netease.com";
    JSONObject result = metahubService.addDataSource(accountId, name, type, env, info, creator);
    System.out.println(result);
  }

  @Test
  public void deleteDataSource() {
    Integer accountId = 25;
    Long dataSourceId = 1022L;
    JSONObject result = metahubService.deleteDataSource(accountId, dataSourceId, "zhaomin3");
    System.out.println(result);
  }

  @Test
  public void modifyDataSource() {
    JSONObject params = new JSONObject();
    JSONObject info = new JSONObject();
    info.put("url", "jdbc:mysql://10.122.144.140:3331/ndi");
//    info.put("user", "work");
//    info.put("userName", "work");
//    info.put("password", "a7078796123A!");
    params.put("id", 1029);
    params.put("accountId", 38);

    params.put("name", "赵敏的测试数据源111");
    params.put("type", "mysql");
    params.put("env", "read");
    params.put("info", info);
    params.put("modifier", "zhaomin3");

    JSONObject result = metahubService.modifyDataSource(params);
    System.out.println(result);
  }

  @Test
  public void getDataSource() {
    Integer accountId = 25;
    Long dataSourceId = 1045L;
    JSONObject result = metahubService.getDataSource(accountId, dataSourceId);
    System.out.println(result);
  }

  @Test
  public void listDataSources() {
    String email = "zhaomin3@corp.netease.com";
    String product = "intern";
    Integer productId = userService.getProductId(email, product);
    System.out.println(productId);
    JSONObject param = new JSONObject();
    param.put("offset", 0);
    param.put("limit", Integer.MAX_VALUE);
    param.put("accountId", productId);
    param.put("sortBy", "db_update_time");
    param.put("order", "DESC");
    String result = metahubService.listAccountDatasource(25, "db_update_time", "DESC",
        0, Integer.MAX_VALUE, null, "mysql");
    System.out.println(result);

  }

  @Test
  public void dataSourceConnCheck() {
    Integer accountId = 38;
    String catalog = "mysql_chensuan_catalog";
    String result = metahubService.dataSourceConnCheck(catalog, accountId);
    System.out.println(result);
  }

  @Test
  public void batchConnectionStatus() {
    Integer accountId = 25;
    List<Long> ids = Lists.newArrayList(787L);
    JSONArray result = metahubService.batchConnectionStatus(accountId, ids);
    System.out.println(result);
  }

  @Test
  public void batchConnectionResult() {
    Integer accountId = 25;
    List<Long> ids = Lists.newArrayList();
    JSONArray result = metahubService.batchConnectionResult(accountId, ids);
    System.out.println(result);
  }

  @Test
  public void listCatalogWithDataSources(){
    JSONObject param = new JSONObject();
    //mammut: 38; intern: 25;
    String email = "zhaomin3@corp.netease.com";
    String product = "mammut";
    Integer productId = userService.getProductId(email, product);

    Integer accountId = 25;
    Integer offset = 0;
    Integer limit = Integer.MAX_VALUE;
    String type = "";
    String clusterId = "";
    JSONObject response = metahubService.listCatalogWithDataSources(accountId, null, null, "mysql", null);
    System.out.println(response);
  }

  @Test
  public void storeCatalogName() {

  }

  @Test
  public void getCatalogName() {

  }

  @Test
  public void listTableByRegex(){

  }

}