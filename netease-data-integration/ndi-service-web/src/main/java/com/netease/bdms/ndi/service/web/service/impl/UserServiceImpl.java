package com.netease.bdms.ndi.service.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.controller.interceptor.SessionHandler;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.dto.AccountDto;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import com.netease.bdms.ndi.service.web.dto.ProductClusterDto;
import com.netease.bdms.ndi.service.web.dto.user.UserDto;
import com.netease.bdms.ndi.service.web.exception.MammutException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName UserServiceImpl
 * @Description 用户服务实现类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class UserServiceImpl implements UserService {
  private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
  @Autowired
  private MammutMetaService mammutMetaService;

  @Autowired
  private RedisUtil redisUtil;

  @Autowired
  private TaskExecutor taskExecutor;

  @Autowired
  private SessionHandler sessionHandler;

  @Override
  public List<ProductClusterDto> listProductAndCluster(String email) {
    List<ProductClusterDto> productClusterDtoList = Lists.newArrayList();
    JSONObject getAccountResponse = mammutMetaService.getAccountByEmail(email);
    JSONArray results = getAccountResponse.getJSONArray("result");
    if (results == null || results.size() == 0) {
      return productClusterDtoList;
    }
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray accounts = result.getJSONArray("accounts");
      if (accounts == null || accounts.size() == 0) {
        return productClusterDtoList;
      }
      for (int j = 0; j < accounts.size(); j ++) {
        ProductClusterDto productClusterDto = new ProductClusterDto();
        JSONObject account = accounts.getJSONObject(j);
        String product = account.getString("name");
        productClusterDto.setProduct(product);
        JSONArray clusterJSONArray = account.getJSONArray("clusters");
        ArrayList<String> clusters = Lists.newArrayList();
        if (clusterJSONArray == null || clusterJSONArray.size() == 0) {
          productClusterDto.setClusters(clusters);
          continue;
        }
        for (int k =0; k < clusterJSONArray.size(); k++) {
          JSONObject clusterJSON = clusterJSONArray.getJSONObject(k);
          clusters.add(clusterJSON.getString("name"));
        }
        productClusterDto.setClusters(clusters);
        productClusterDtoList.add(productClusterDto);
      }
    }
    return productClusterDtoList;
  }

  @Override
  public void storeProductUsers(String product) {
    JSONObject response = mammutMetaService.getProductUsers(product);
    JSONArray result = response.getJSONArray("result");
    if (result != null && result.size() > 0) {
      for (int i = 0; i < result.size(); i++) {
        JSONObject item = result.getJSONObject(i);
        String email = item.getString("email");
        String name = item.getString("name");
        redisUtil.hsetWithExpire(redisUtil.keyBuilder(CommonConstants.REDIS_PRODUCT_USER, product), email, name, CommonConstants.RedisExpire.EXPIRE_1_MONTH);
      }
    }
  }

  @Override
  public String getProductUsername(String product, String email) {
    String username = null;
    try {
      username = redisUtil.hget(redisUtil.keyBuilder(CommonConstants.REDIS_PRODUCT_USER, product), email);
      if (username != null) {
        redisUtil.expire(redisUtil.keyBuilder(CommonConstants.REDIS_PRODUCT_USER, product), CommonConstants.RedisExpire.EXPIRE_1_MONTH);
        return username;
      } else {
        taskExecutor.execute(() -> {
          storeProductUsers(product);
        });
      }
    } catch (Exception e) {
      log.warn("获取用户名字失败", e);
    }
    return getUserName(email);
  }

  @Override
  public Integer getProductId(String email, String product) {
    JSONObject listProduct = mammutMetaService.listProduct(email);
    JSONArray listProductResult = listProduct.getJSONArray("result");
    if (listProductResult != null && listProductResult.size() > 0) {
      for (int i = 0; i < listProductResult.size(); i++) {
        JSONObject item = listProductResult.getJSONObject(i);
        if (StringUtils.equals(item.getString("product"), product)) {
          Integer productId = item.getInteger("productId");
          return productId;
        }
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "User don't join any product");
  }

  @Override
  public List<UserDto> gerProductUsers(String product) {
    ParamUtil.nonNull(product, "Product can't be null");
    List<UserDto> userDtoList = new ArrayList<>();

    JSONObject response = mammutMetaService.getProductUsers(product);
    JSONArray result = response.getJSONArray("result");
    if (result != null && result.size() > 0) {
      for (int i = 0; i < result.size(); i++) {
        JSONObject item = result.getJSONObject(i);
        String email = item.getString("email");
        String name = item.getString("name");
        UserDto userDTO = new UserDto(email, name);
        userDtoList.add(userDTO);
      }
    }
    return userDtoList;
  }

  @Override
  public String getClusterId(String email, String product, String cluster) {
    JSONObject getClusters = mammutMetaService.getClusters(product);
    JSONArray getClustersResult = getClusters.getJSONArray("result");
    if (getClustersResult != null && getClustersResult.size() > 0) {
      for (int i = 0; i < getClustersResult.size(); i++) {
        JSONObject item = getClustersResult.getJSONObject(i);
        if (StringUtils.equals(item.getString("name"), cluster)) {
          return item.getString("id");
        }
      }
    }
    throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Product don't have the cluster");
  }

  @Override
  public String getUserName(String email) {
    JSONObject jsonObject = null;
    try {
      jsonObject = mammutMetaService.getUserName(email);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new MammutException(ProcessStatusEnum.MAMMUT_ERROR.getCode(), ProcessStatusEnum.MAMMUT_ERROR.getMessage());
    }
    JSONObject result = jsonObject.getJSONObject("result");
    String name = result.getString("name");
    return name;
  }

  @Override
  public List<ClusterDto> listCurrentClusters(String product) {
    List<ClusterDto> clusterDtoList = Lists.newArrayList();
    JSONObject response = mammutMetaService.getClusters(product);
    JSONArray result = response.getJSONArray("result");
    for (int i = 0; i < result.size(); i++) {
      JSONObject cluster = result.getJSONObject(i);
      String clusterName = cluster.getString("name");
      String clusterId = cluster.getString("id");
      ClusterDto clusterDto = new ClusterDto(clusterId, clusterName);
      clusterDtoList.add(clusterDto);
    }
    return clusterDtoList;
  }

  @Override
  public Worker switchProductAndCluster(String sessionId, String email, String product, String cluster) {
    String username = getUserName(email);
    JSONObject accounts = mammutMetaService.getAccount(email);
    List<String> products = accounts.getJSONArray("result").toJavaList(String.class);
    if (products == null || !products.contains(product)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "User don't join the product");
    }

    List<String> clusterNames = mammutMetaService.getClusterName(product);
    if (clusterNames == null || !clusterNames.contains(cluster)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "Product don't have the cluster");
    }

    redisUtil.hsetWithExpire(redisUtil.keyBuilder(CommonConstants.REDIS_USER_INFO, email), CommonConstants.REDIS_USER_PRODUCT,
        product, CommonConstants.RedisExpire.EXPIRE_1_WEEK);
    redisUtil.hsetWithExpire(redisUtil.keyBuilder(CommonConstants.REDIS_USER_INFO, email), CommonConstants.REDIS_USER_CLUSTER,
        cluster, CommonConstants.RedisExpire.EXPIRE_1_WEEK);

    // 添加项目和集群信息到上下文，变更集群项目组集群要更新时上下文
    Integer productId = getProductId(email, product);
    String clusterId = getClusterId(email, product, cluster);

    Worker worker = new Worker();
    worker.setUsername(username);
    worker.setEmail(email);
    worker.setProduct(product);
    worker.setCluster(cluster);
    worker.setProductId(productId);
    worker.setClusterId(clusterId);
    sessionHandler.updateSession(sessionId, worker, CommonConstants.REDIS_SESSION_EXPIRE);
    return worker;
  }

  @Override
  public AccountDto getAccountDtoByEmail(String email) {
    List<AccountDto.ProductDto> productDtoList = Lists.newArrayList();
    JSONObject getAccountResponse = mammutMetaService.getAccountByEmail(email);
    JSONArray results = getAccountResponse.getJSONArray("result");
    if (results == null || results.size() == 0) {
      return null;
    }
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray accounts = result.getJSONArray("accounts");
      if (accounts == null || accounts.size() == 0) {
        return null;
      }
      for (int j = 0; j < accounts.size(); j ++) {
        JSONObject account = accounts.getJSONObject(j);
        Integer productId = account.getInteger("id");
        String product = account.getString("name");

        JSONArray clusterJSONArray = account.getJSONArray("clusters");
        List<AccountDto.ClusterDto> clusterDtoList = Lists.newArrayList();
        if (clusterJSONArray == null || clusterJSONArray.size() == 0) {
          continue;
        }
        for (int k =0; k < clusterJSONArray.size(); k++) {
          JSONObject clusterJSON = clusterJSONArray.getJSONObject(k);
          String clusterId = clusterJSON.getString("id");
          String clusterName = clusterJSON.getString("name");
          AccountDto.ClusterDto clusterDto = new AccountDto.ClusterDto(clusterId, clusterName);
          clusterDtoList.add(clusterDto);
        }
        AccountDto.ProductDto productDto = new AccountDto.ProductDto(productId, product, clusterDtoList);
        productDtoList.add(productDto);
      }
    }
    return new AccountDto(productDtoList);
  }

  @Override
  public List<ProductClusterDto> listUserProduct(String email) {
    List<ProductClusterDto> clusterDtoList = Lists.newArrayList();
    JSONObject response = mammutMetaService.listProduct(email);
    JSONArray result = response.getJSONArray("result");
    if (result.size() == 0) {
      return clusterDtoList;
    }

    Map<String, String> clusterMap = getClusterMap(null);
    for (int i = 0; i < result.size(); i++) {
      JSONObject item = result.getJSONObject(i);
      ProductClusterDto productClusterDto = new ProductClusterDto();
      String product = item.getString("product");
      productClusterDto.setProduct(product);
      List<String> clusterIds = item.getJSONArray("clusterIds").toJavaList(String.class);
      List<String> clusterNameList = clusterIds.stream()
          .filter(Objects::nonNull)
          .map(clusterId -> clusterMap.get(clusterId))
          .collect(Collectors.toList());
      productClusterDto.setClusters(clusterNameList);
      clusterDtoList.add(productClusterDto);
    }
    return clusterDtoList;
  }

  private Map<String, String> getClusterMap(String clusterId) {
    Map<String, String> clusterMap = new HashedMap();
    JSONObject response = mammutMetaService.listCluster(clusterId);
    Integer code = response.getInteger("code");
    if (code != 200) {
      return clusterMap;
    }
    JSONArray result = response.getJSONArray("result");
    if (CollectionUtils.isEmpty(result)) {
      return clusterMap;
    }

    for (int i =0; i< result.size();i++) {
      JSONObject clusterObject = result.getJSONObject(i);
      String clusterIdKey = clusterObject.getString("id");
      String clusterName = clusterObject.getString("name");
      clusterMap.put(clusterIdKey, clusterName);
    }
    return clusterMap;
  }
}
