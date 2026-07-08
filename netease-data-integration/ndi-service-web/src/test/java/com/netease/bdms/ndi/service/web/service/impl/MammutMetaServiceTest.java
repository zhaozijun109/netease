package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MammutMetaServiceTest extends DataIntegrationWebTests {

  @Autowired
  private MammutMetaService mammutMetaService;

  @Test
  public void getAllUsers() throws Exception {
    JSONObject result = mammutMetaService.getAllUsers();
    System.out.println(result);
  }

  @Test
  public void getUserName() throws Exception {
    JSONObject result = mammutMetaService.getUserName("zhaomin3@corp.netease.com");
    System.out.println(result);
  }

  @Test
  public void getAccount() {
    JSONObject jsonObject = null;
    HashMap<String, List<String>> productCluster = new HashMap<>();
    try {
      jsonObject = mammutMetaService.getAccount("zhaomin3@corp.netease.com");

      JSONArray products = jsonObject.getJSONArray("result");
      if (products != null && products.size() > 0) {
        for (int i = 0; i < products.size(); i++) {
          String product = products.getString(i);
          JSONObject response = mammutMetaService.getClusters(product);
          JSONArray result = response.getJSONArray("result");
          ArrayList<String> clusters = new ArrayList<>();
          for (int j = 0; j < result.size(); j++) {
            JSONObject cluster = result.getJSONObject(j);
            String clusterName = cluster.getString("name");
            clusters.add(clusterName);
          }
          productCluster.put(product, clusters);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(jsonObject);
    System.out.println(productCluster);
  }

  @Test
  public void getUserKey() {
  }

  @Test
  public void getXmlByHiveid() {
  }

  @Test
  public void getClusters() throws Exception {
    List<ClusterDto> clusterDtoList = Lists.newArrayList();
    JSONObject response = mammutMetaService.getClusters("intern");
    JSONArray result = response.getJSONArray("result");
    for (int i = 0; i < result.size(); i++) {
      JSONObject cluster = result.getJSONObject(i);
      String clusterName = cluster.getString("name");
      String clusterId = cluster.getString("id");
      ClusterDto clusterDto = new ClusterDto(clusterId, clusterName);
      clusterDtoList.add(clusterDto);
    }
    System.out.println(result);

    System.out.println(mammutMetaService.getClusterName("intern"));
  }

  @Test
  public void getProductUsers() throws Exception {
    JSONObject result = mammutMetaService.getProductUsers("intern");

    System.out.println(result);
  }

  public void storeProductUsers(String product) throws Exception {

  }

  @Test
  public void getAccountByEmail() {
    JSONObject result = mammutMetaService.getAccountByEmail("hzzhouhao1@corp.netease.com");
    System.out.println(result);
  }


  @Test
  public void isAdmin() throws Exception {
    List<String> names = mammutMetaService.getClusterName("yqdec");
    System.out.println(names);
  }

  @Test
  public void isUserBounded() {
    JSONObject response = mammutMetaService.listProduct("zhaomin3@corp.netease.com");
    JSONArray result = response.getJSONArray("result");
    List<String> productList = Lists.newArrayList();
    if (result.size() == 0) {

    }
    for (int i = 0; i < result.size(); i++) {
      JSONObject item = result.getJSONObject(i);
      productList.add(item.getString("product"));
    }
    System.out.println(productList);
  }

  @Test
  public void listProduct() throws Exception {
    JSONObject response = mammutMetaService.listProduct("zhaomin3@corp.netease.com");
    JSONArray result = response.getJSONArray("result");
    List<String> productList = Lists.newArrayList();
    System.out.println(result.size());
    for (int i = 0; i < result.size(); i++) {
      JSONObject item = result.getJSONObject(i);
      productList.add(item.getString("product"));
      if (item.getString("product").equals("intern")) {
        System.out.println(item.getString("businessLine"));
        System.out.println(item.getString("productId"));
        System.out.println(item.getInteger("productId"));
      }
    }
    System.out.println(result);
  }

  @Test
  public void listCluster() {
    JSONObject result = mammutMetaService.listCluster(null);
    System.out.println(result);
  }
}