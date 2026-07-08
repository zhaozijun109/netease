package com.netease.bdms.ndi.service.web.service.impl;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.DataIntegrationWebTests;
import com.netease.bdms.ndi.service.web.dto.AccountDto;
import com.netease.bdms.ndi.service.web.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class UserServiceImplTest extends DataIntegrationWebTests {
  @Autowired
  private UserService userService;

  @Test
  public void listProductAndCluster() {
  }

  @Test
  public void storeProductUsers() {
  }

  @Test
  public void getProductUsername() {
  }

  @Test
  public void getProductId() {
    String email = "zhaomin3@corp.netease.com";
    String product = "data_transform";
    Integer productId = userService.getProductId(email, product);
    System.out.println(productId);
  }

  @Test
  public void gerProductUsers() {
  }

  @Test
  public void getClusterId() {
  }

  @Test
  public void getUserName() {

  }

  @Test
  public void getAccountDtoByEmail() {
    AccountDto accountDto = userService.getAccountDtoByEmail("zhaomin3@corp.netease.com");
    System.out.println(accountDto);
  }
}